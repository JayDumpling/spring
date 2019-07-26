package dum.springframework.aop.framework;

/**
 * @Auther : Dumpling
 * @Description
 **/
public interface DumAopProxy {

    Object getProxy();


    Object getProxy(ClassLoader classLoader);

}
