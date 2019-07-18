package dum.springframework.web.servlet;

import dum.springframework.annotation.DumRequestMapping;
import dum.springframework.annotation.DumRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther : Dumpling
 * @Description
 **/
public class DumHandlerAdapter {
    public boolean supports(Object handler) {
        return (handler instanceof DumHandlerMapping);
    }

    DumModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        DumHandlerMapping handlerMapping = (DumHandlerMapping) handler;

        //参数名和参数位置的键值
        Map<String, Integer> paramIndexMapping = new HashMap<>();
        //一个方法有多个参数，一个参数可能有多个注解，所有是个二维数组
        Annotation[][] parameterAnnotations = handlerMapping.getMethod().getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation instanceof DumRequestParam) {
                    String paramName = ((DumRequestParam) annotation).value();
                    if(!"".equals(paramName) && paramName != null)
                    {
                        paramIndexMapping.put(paramName,i);
                    }
                }
            }
        }
        Class<?>[] paramTypes = handlerMapping.getMethod().getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == HttpServletRequest.class || paramTypes[i] == HttpServletResponse.class)
                paramIndexMapping.put(paramTypes[i].getName(),i);
        }
        Map<String,String[]> parameterMap = request.getParameterMap();
        Object []paramValues = new Object[paramTypes.length];
        for (Map.Entry<String, String[]> parm : parameterMap.entrySet()) {
            String value = Arrays.toString(parm.getValue()).replaceAll("\\[|\\]","").replaceAll("\\s","");
            if (!paramIndexMapping.containsKey(parm.getKey()))
                continue;
            int index = paramIndexMapping.get(parm.getKey());
            paramValues[index] = value;
        }
        if (paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
            int index = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[index] = request;
        }
        if (paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
            int index = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[index] = response;
        }
        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(),paramValues);
        if (result == null || result instanceof Void) return null;
        if (handlerMapping.getMethod().getReturnType() == DumModelAndView.class)
            return (DumModelAndView)result;
        return null;
    }
}
