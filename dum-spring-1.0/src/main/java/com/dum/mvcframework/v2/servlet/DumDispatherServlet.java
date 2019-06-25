package com.dum.mvcframework.v2.servlet;

import com.dum.mvcframework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auther : Dumpling
 * @Description
 **/
public class DumDispatherServlet extends HttpServlet {
    //保存application.properties配置文件中的内容
    private Properties contextConfig = new Properties();

    private List<String> classNames = new ArrayList<String>();

    private Map<String,Object> ioc = new HashMap<String, Object>();

    private List<HandlerMapping> handlerMappings = new ArrayList<HandlerMapping>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //6.调用运行阶段
        try {
            doDispatch(req,resp);
        } catch (Exception e)
        {
            e.printStackTrace();
            resp.getWriter().write("500 Exception Detail:" + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {

        HandlerMapping handlerMapping = getHandleMapping(req);
        if (handlerMapping == null)
        {
            resp.getWriter().write("404 Not Found!!!");
            return;
        }

        Map<String,String[]> params = req.getParameterMap();
        Parameter[] parameters = handlerMapping.getParameters();
        Object[] paramValues = new Object[parameters.length];
        if (handlerMapping.getParamIndexMapping().containsKey(HttpServletRequest.class.getName())) {
            int index = handlerMapping.getParamIndexMapping().get(HttpServletRequest.class.getName());
            paramValues[index] = req;
        }
        if (handlerMapping.getParamIndexMapping().containsKey(HttpServletResponse.class.getName())) {
            int index = handlerMapping.getParamIndexMapping().get(HttpServletResponse.class.getName());
            paramValues[index] = resp;
        }
        for (Map.Entry<String, String[]> param : params.entrySet()) {
            if (!handlerMapping.getParamIndexMapping().containsKey(param.getKey())){continue;}
            int index = handlerMapping.getParamIndexMapping().get(param.getKey());
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]","");
            Class<?> paramType = parameters[index].getType();
            Object paramValue = covert(paramType,value);
            paramValues[index] = paramValue;
        }
        handlerMapping.getMethod().invoke(handlerMapping.getController(),paramValues);
    }

    private HandlerMapping getHandleMapping(HttpServletRequest req) {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");
        for (HandlerMapping handlerMapping : this.handlerMappings) {
            Matcher matcher = handlerMapping.getUrl().matcher(url);
            if (!matcher.matches())
                continue;
            return handlerMapping;
        }
        return null;
    }

    private Object covert(Class<?> type,String value)
    {
        if (type == Integer.class)
            return Integer.valueOf(value);
        else if (type == Double.class)
            return Double.valueOf(value);
        return value;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //2.扫描相关类
        doScanner(contextConfig.getProperty("scanPackage"));
        //3.初始化相关类，并且将它们放到IOC容器中
        doInstance();
        //4.完成依赖注入
        doAutowired();
        //5.初始化HandlerMapping
        initHandleMapping();

        System.out.println("Dum spring framework is init.");
    }

    private void initHandleMapping() {
        if (ioc.isEmpty()){return;}
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(DumController.class)){continue;}
            String baseUrl = "";
            if (clazz.isAnnotationPresent(DumRequestMapping.class))
            {
                DumRequestMapping requestMapping = clazz.getAnnotation(DumRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            for (Method method : clazz.getMethods()) {
                if (method.isAnnotationPresent(DumRequestMapping.class))
                {
                    DumRequestMapping requestMapping = method.getAnnotation(DumRequestMapping.class);
                    String url = ("/" + baseUrl + "/" +requestMapping.value()).replaceAll("/+","/");
                    Pattern pattern = Pattern.compile(url);
                    handlerMappings.add(new HandlerMapping(pattern,entry.getValue(),method));
                    System.out.println("Mapped :" + url + "," + method);

                }
            }

        }

    }

    private void doAutowired() {
        if (ioc.isEmpty()){return;}
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(DumAutowired.class)){continue;}
                DumAutowired autowired = field.getAnnotation(DumAutowired.class);
                String beanName = autowired.value();
                if ("".equals(beanName))
                {
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void doInstance() {
        //初始化，为DI做准备
        if (classNames.isEmpty()){return;}
        for (String classname:classNames) {
            try {
                Class<?> clazz = Class.forName(classname);
                //加了注解的类，才初始化，怎么判断？
                //为了简化代码逻辑，主要体会设计思想，只举例 @Controller和@Service,
                // @Componment...就一一举例了
                if (clazz.isAnnotationPresent(DumController.class))
                {
                    Object instance = clazz.newInstance();
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName,instance);
                }
                else if (clazz.isAnnotationPresent(DumService.class))
                {
                    //1.自定义beanName
                    DumService service = clazz.getAnnotation(DumService.class);
                    String beanName = service.value();
                    //2.默认首字母小写的类名
                    if ("".equals(beanName))
                    {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);
                    for (Class<?> anInterface : clazz.getInterfaces()) {
                        if (ioc.containsKey(anInterface.getName()))
                        {
                            throw new Exception("The ‘" + anInterface.getName() + "’" + "is exist!");
                        }
                        ioc.put(anInterface.getName(),instance);
                    }

                }
                else{
                    continue;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    //如果类名本身是小写字母，确实会出问题
    //但是我要说明的是：这个方法是我自己用，private的
    //传值也是自己传，类也都遵循了驼峰命名法
    //默认传入的值，存在首字母小写的情况，也不可能出现非字母的情况
    //为了简化程序逻辑，就不做其他判断了
    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        if (chars.length <= 0)
            return simpleName;
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getResource("/" + scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        for (File file:classPath.listFiles()) {
            if (file.isDirectory())
            {
                doScanner(scanPackage + "." + file.getName());
            }
            else
            {
                if (!file.getName().endsWith(".class")){continue;}
                String classname = scanPackage + "." + file.getName().replaceAll(".class","");
                classNames.add(classname);
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) {

        InputStream fis = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);

        try {
            contextConfig.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (null != fis)
            {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
