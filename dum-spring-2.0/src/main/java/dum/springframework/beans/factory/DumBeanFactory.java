package dum.springframework.beans.factory;

/**
 * @Auther : Dumpling
 * @Description 单例工厂的顶层设计
 **/
public interface DumBeanFactory {
    /**
     * 根据 beanName 从 IOC 容器中获得一个实例 Bean
     * @param beanName
     * @return
     * @throws Exception
     */
    Object getBean(String beanName) throws Exception;

    <T> T getBean(Class<T> requiredType) throws Exception;
}
