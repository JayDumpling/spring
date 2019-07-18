package dum.springframework.beans;

/**
 * @Auther : Dumpling
 * @Description
 **/
public class DumBeanWrapper {
    Object wrappedInstance;
    Class<?> wrappedClass;

    public DumBeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    public Object getWrappedInstance() {
        return wrappedInstance;
    }

    public void setWrappedInstance(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    public Class<?> getWrappedClass() {
        return this.wrappedInstance.getClass();
    }
}
