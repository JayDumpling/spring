package dum.springframework.aop.framework;

import dum.springframework.aop.aspect.DumAfterReturningAdviceInterceptor;
import dum.springframework.aop.aspect.DumAfterThrowingAdviceInterceptor;
import dum.springframework.aop.aspect.DumMethodBeforeAdviceInterceptor;
import dum.springframework.aop.config.DumAopConfig;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auther : Dumpling
 * @Description
 **/
public class DumAdvisedSupport {

    private Class<?> targetClass;

    private Object target;

    private DumAopConfig config;

    private Pattern pointCutClassPattern;

    private transient Map<Method, List<Object>> methodCache;// 目标类方法与切面类方法



    public DumAdvisedSupport(DumAopConfig aopConfig) {
        this.config = aopConfig;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    private void parse() {
        String pointCut = config.getPointCut()
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*", ".*")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");
        //pointCut=public .* com.dum.spring.demo.service..*Service..*(.*)
        //玩正则
        String pointCutForClassRegex = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegex.substring(
                pointCutForClassRegex.lastIndexOf(" ") + 1));
        try {
            methodCache = new HashMap<Method, List<Object>>();
            Pattern pattern = Pattern.compile(pointCut);

            Class aspectClass = Class.forName(config.getAspectClass());
            Map<String,Method> aspectMethods = new HashMap<>();
            for (Method m : aspectClass.getMethods()) {
                aspectMethods.put(m.getName(),m);
            }


            for (Method m : this.targetClass.getMethods()) {
                String methodString = m.toString();
                if (methodString.contains("throws")) {
                    methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
                }
                Matcher matcher = pattern.matcher(methodString);
                if (matcher.matches()) {
                    List<Object> advises = new LinkedList<>();
                    if (config.getAspectBefore() != null && config.getAspectBefore() != "") {
                        advises.add(new DumMethodBeforeAdviceInterceptor(aspectMethods.get(config.getAspectBefore()),aspectClass.newInstance()));
                    }
                    if (config.getAspectAfter() != null && config.getAspectBefore() != "")
                    {
                        advises.add(new DumAfterReturningAdviceInterceptor(aspectMethods.get(config.getAspectAfter()),aspectClass.newInstance()));
                    }
                    if (config.getAspectAfterThrow() != null && config.getAspectAfterThrow() != "")
                    {
                        DumAfterThrowingAdviceInterceptor throwAdvice =
                                new DumAfterThrowingAdviceInterceptor(aspectMethods.get(config.getAspectAfterThrow()),
                                        aspectClass.newInstance());
                        throwAdvice.setThrowName(config.getAspectAfterThrowingName());
                        advises.add(throwAdvice);
                    }
                    methodCache.put(m,advises);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) throws Exception {
        List<Object> cached = methodCache.get(method);
        if(cached == null){
            Method m = targetClass.getMethod(method.getName(),method.getParameterTypes());
            cached = methodCache.get(m);
            //底层逻辑，对代理方法进行一个兼容处理
            this.methodCache.put(m,cached);
        }
        return cached;
    }
}
