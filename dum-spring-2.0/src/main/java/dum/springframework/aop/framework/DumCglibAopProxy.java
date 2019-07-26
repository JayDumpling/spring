package dum.springframework.aop.framework;

/**
 * @Auther : Dumpling
 * @Description
 **/
public class DumCglibAopProxy implements DumAopProxy {
    DumAdvisedSupport advised;

    public DumCglibAopProxy(DumAdvisedSupport advised) {
        this.advised = advised;
    }

    @Override
    public Object getProxy() {
        return null;
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        return null;
    }
}
