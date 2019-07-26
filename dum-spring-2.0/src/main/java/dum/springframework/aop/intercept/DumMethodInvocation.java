package dum.springframework.aop.intercept;

import dum.springframework.aop.aspect.DumJoinPoint;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Auther : Dumpling
 * @Description
 **/
public class DumMethodInvocation implements DumJoinPoint {

    private Object proxy;
    private Object target;
    Method method;
    Object[] args;
    Class<?> targetClass;
    List<Object> interceptorsAndDynamicMethodMatchers;

    private Map<String, Object> userAttributes;
    //定义一个索引，从-1开始来记录当前拦截器执行的位置
    private int currentInterceptorIndex = -1;

    public DumMethodInvocation(Object proxy, Object target, Method method, Object[] args, Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers) {
        this.proxy = proxy;
        this.target = target;
        this.method = method;
        this.args = args;
        this.targetClass = targetClass;
        this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
    }

    public Object proceed() throws Throwable {
        if (currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1)
            return this.method.invoke(target, args);

        Object interceptorOrInterceptionAdvice = this.interceptorsAndDynamicMethodMatchers.get(++currentInterceptorIndex);
        if (interceptorOrInterceptionAdvice instanceof DumMethodInterceptor) {
            DumMethodInterceptor mi = (DumMethodInterceptor) interceptorOrInterceptionAdvice;
            return mi.invoke(this);
        } else {
            proceed();
        }
        return null;
    }

    @Override
    public Object getThis() {
        return this.target;
    }

    @Override
    public Object[] getArguments() {
        return this.args;
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public void setUserAttribute(String key, Object value) {
        if (value != null) {
            if (this.userAttributes == null) {
                this.userAttributes = new HashMap<String,Object>();
            }
            this.userAttributes.put(key, value);
        }
        else {
            if (this.userAttributes != null) {
                this.userAttributes.remove(key);
            }
        }
    }

    @Override
    public Object getUserAttribute(String key) {
        return (this.userAttributes != null ? this.userAttributes.get(key) : null);
    }
}
