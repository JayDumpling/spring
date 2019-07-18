package dum.springframework.web.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @Auther : Dumpling
 * @Description
 **/
public class DumHandlerMapping {
    Object controller;
    Method method;
    Pattern pattern;

    public DumHandlerMapping(Object controller, Method method, Pattern pattern) {
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;
    }

    public Object getController() {
        return controller;
    }

    public Method getMethod() {
        return method;
    }

    public Pattern getPattern() {
        return pattern;
    }
}
