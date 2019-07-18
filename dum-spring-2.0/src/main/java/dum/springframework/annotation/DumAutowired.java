package dum.springframework.annotation;

import java.lang.annotation.*;

/**
 * @Auther : Dumpling
 * @Description
 **/
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DumAutowired {

    String value() default "";
}
