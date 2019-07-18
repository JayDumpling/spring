package dum.springframework.beans.factory.support;

import dum.springframework.beans.factory.config.DumBeanDefinition;
import dum.springframework.context.support.DumAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther : Dumpling
 * @Description
 **/
public class DumDefaultListableBeanFactory extends DumAbstractApplicationContext {
    //存储注册信息的BeanDefinition
    protected final Map<String, DumBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
}
