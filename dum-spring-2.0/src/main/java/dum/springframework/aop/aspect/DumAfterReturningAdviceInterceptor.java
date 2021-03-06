package dum.springframework.aop.aspect;

import dum.springframework.aop.intercept.DumMethodInterceptor;
import dum.springframework.aop.intercept.DumMethodInvocation;

import java.lang.reflect.Method;

/**
 * @Auther : Dumpling
 * @Description
 **/
public class DumAfterReturningAdviceInterceptor extends DumAbstractAspectAdvice implements DumMethodInterceptor{
    DumJoinPoint joinPoint;

    public DumAfterReturningAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(DumMethodInvocation im) throws Throwable {
        //从被织入的代码中才能拿到，JoinPoint
        this.joinPoint = im;
        after(im.getMethod(), im.getArguments(), im.getThis());
        return im.proceed();
    }

    private void after(Method method, Object[] arguments, Object aThis) throws Throwable {
        super.invokeAdviceMethod(this.joinPoint,null,null);
    }
}
