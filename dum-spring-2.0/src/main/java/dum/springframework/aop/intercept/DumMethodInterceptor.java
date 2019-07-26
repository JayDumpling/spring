package dum.springframework.aop.intercept;

/**
 * @Auther : Dumpling
 * @Description
 **/
public interface DumMethodInterceptor {

    Object invoke(DumMethodInvocation invocation) throws Throwable;
}
