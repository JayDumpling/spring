package com.dum.mvcframework.v2.servlet;

import com.dum.mvcframework.annotation.DumRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Auther : Dumpling
 * @Description 保存一个url和一个Method的关系
 **/
public class HandlerMapping {
    private Pattern url;
    private Method method;
    private Object controller;
    private Parameter[] parameters;



    private Map<String,Integer> paramIndexMapping;

    public HandlerMapping(Pattern url,Object controller, Method method) {
        this.url = url;
        this.method = method;
        this.controller = controller;
        this.parameters = method.getParameters();
        paramIndexMapping = new HashMap<>();
        putParamIndexMapping(parameters);
    }

    private void putParamIndexMapping(Parameter[] parameters) {
        for (int i=0;i<parameters.length;i++)
        {
            Parameter parameter = parameters[i];
            Class paramType = parameter.getType();
            if (paramType == HttpServletRequest.class || paramType == HttpServletResponse.class)
                paramIndexMapping.put(parameter.getType().getName(),i);
            else {
                if (!parameter.isAnnotationPresent(DumRequestParam.class)) { continue; }
                DumRequestParam requestParam = parameter.getAnnotation(DumRequestParam.class);
                String paramName = requestParam.value();
                if (!"".equals(paramName)) {
                    paramIndexMapping.put(paramName,i);
                }
            }
        }
    }

    public Pattern getUrl() {
        return url;
    }

    public Method getMethod() {
        return method;
    }

    public Object getController() {
        return controller;
    }

    public Parameter[] getParameters() {
        return parameters;
    }

    public Map<String, Integer> getParamIndexMapping() {
        return paramIndexMapping;
    }
}
