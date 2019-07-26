package dum.springframework.aop.aspect;

import dum.springframework.aop.intercept.DumMethodInterceptor;
import dum.springframework.aop.intercept.DumMethodInvocation;

import java.lang.reflect.Method;

/**
 * @Auther : Dumpling
 * @Description
 **/
public class DumAfterThrowingAdviceInterceptor extends DumAbstractAspectAdvice implements DumMethodInterceptor {
    private String throwingName;

    public DumAfterThrowingAdviceInterceptor(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }

    @Override
    public Object invoke(DumMethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        }catch (Throwable e){
            invokeAdviceMethod(mi,null,e.getCause());
            throw e;
        }
    }

    public void setThrowName(String throwName)
    {
        this.throwingName = throwName;
    }
}
