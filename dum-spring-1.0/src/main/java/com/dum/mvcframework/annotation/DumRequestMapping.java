package com.dum.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @Auther : Dumpling
 * @Description
 **/
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DumRequestMapping {

    String value() default "";

}