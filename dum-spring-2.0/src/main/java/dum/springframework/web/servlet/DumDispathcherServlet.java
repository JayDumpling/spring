package dum.springframework.web.servlet;

import dum.springframework.annotation.DumController;
import dum.springframework.annotation.DumRequestMapping;
import dum.springframework.context.DumApplicationContext;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auther : Dumpling
 * @Description
 **/
@Slf4j
public class DumDispathcherServlet extends HttpServlet {
    private final String LOCATION = "contextConfigLocation";
    private DumApplicationContext context;
    private List<DumHandlerMapping> handlerMappings = new ArrayList<>();
    private List<DumViewResolver> viewResolvers = new ArrayList<>();
    private Map<DumHandlerMapping, DumHandlerAdapter> handlerAdapterMap = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        DumHandlerMapping handler = getHandler(req);
        if (handler == null) {
            processDispatchResult(req,resp,new DumModelAndView("404"));
            return;
        }
        DumHandlerAdapter ha = getHandlerAdapter(handler);

        DumModelAndView mv = ha.handle(req, resp, handler);

        processDispatchResult(req,resp,mv);

    }

    private DumHandlerAdapter getHandlerAdapter(DumHandlerMapping handler) {
        if (handlerAdapterMap.isEmpty())
            return null;
        DumHandlerAdapter ha = this.handlerAdapterMap.get(handler);
        if (ha.supports(handler))
            return ha;
        return null;
    }

    private DumHandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty())
            return null;
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url.replace(contextPath, "").replaceAll("/+", "/");
        for (DumHandlerMapping handler : this.handlerMappings) {
            Matcher matcher = handler.getPattern().matcher(url);
            if (!matcher.matches())
                continue;
            return handler;
        }
        return null;
    }

    private void processDispatchResult(HttpServletRequest request, HttpServletResponse response, DumModelAndView mv) throws Exception {
        //把给我的ModleAndView变成一个HTML、OuputStream、json、freemark、veolcity
        //ContextType
        if(null == mv){return;}

        //如果ModelAndView不为null，怎么办？
        if(this.viewResolvers.isEmpty()){return;}

        for (DumViewResolver viewResolver : this.viewResolvers) {
            DumView view = viewResolver.resolveViewName(mv.getViewName(),null);
            view.render(mv.getModel(),request,response);
            return;
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        context = new DumApplicationContext(config.getInitParameter(LOCATION));
        initStrategies(context);
    }

    private void initStrategies(DumApplicationContext context) {
        //多文件上传组件
        initMultipartResolver(context);
        //初始化本地语言环境
        initLocaleResolver(context);
        //初始化模板处理器
        initThemeResolver(context);
        //初始化handlerMapping,需要实现
        initHandlerMappings(context);
        //初始化参数适配器,需要实现
        initHandlerAdapters(context);
        //初始化异常拦截器
        initHandlerExceptionResolvers(context);
        //初始化视图预处理器
        initRequestToViewNameTranslator(context);
        //初始化视图转化器,需要实现
        initViewResolvers(context);
        //初始化缓存组件
        initFlashMapManager(context);
    }

    private void initHandlerMappings(DumApplicationContext context) {
        String[] beanNames = context.getBeanDefinitionNames();
        try {
            for (String beanName : beanNames) {
                Object controller = context.getBean(beanName);
                Class<?> clazz = controller.getClass();
                if (!clazz.isAnnotationPresent(DumController.class))
                    continue;
                String baseUrl = "";
                if (clazz.isAnnotationPresent(DumRequestMapping.class)) {
                    DumRequestMapping requestMapping = clazz.getAnnotation(DumRequestMapping.class);
                    baseUrl = requestMapping.value();
                }
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(DumRequestMapping.class))
                        continue;
                    DumRequestMapping requestMapping = method.getAnnotation(DumRequestMapping.class);
                    String regex = ("/" + baseUrl + (requestMapping.value()).replaceAll("\\*", ".*")).replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(regex);
                    this.handlerMappings.add(new DumHandlerMapping(controller, method, pattern));
                    log.info("Mapped " + regex + "," + method);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initHandlerAdapters(DumApplicationContext context) {
        for (DumHandlerMapping handlerMapping : this.handlerMappings) {
            handlerAdapterMap.put(handlerMapping, new DumHandlerAdapter());
        }
    }

    private void initViewResolvers(DumApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templatePath = this.getClass().getClassLoader().getResource(templateRoot).getPath();
        File templateDir = new File(templatePath);
        for (File file : templateDir.listFiles()) {
            this.viewResolvers.add(new DumViewResolver(templateRoot));
        }
    }

    private void initFlashMapManager(DumApplicationContext context) {
    }

    private void initRequestToViewNameTranslator(DumApplicationContext context) {
    }

    private void initHandlerExceptionResolvers(DumApplicationContext context) {
    }

    private void initThemeResolver(DumApplicationContext context) {
    }

    private void initLocaleResolver(DumApplicationContext context) {
    }

    private void initMultipartResolver(DumApplicationContext context) {
    }
}
