package com.dum.mvcframework.annotation;

import java.lang.annotation.*;

/**
 * @Auther : Dumpling
 * @Description
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DumController {
    String value() default "";

}
