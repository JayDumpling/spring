package dum.springframework.aop.aspect;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @Auther : Dumpling
 * @Description
 **/
public interface DumJoinPoint {
    Object getThis();

    Object[] getArguments();

    Method getMethod();

    void setUserAttribute(String key, Object value);

    Object getUserAttribute(String key);
}
