package dum.springframework.context;

import dum.springframework.annotation.DumAutowired;
import dum.springframework.annotation.DumController;
import dum.springframework.annotation.DumService;
import dum.springframework.aop.config.DumAopConfig;
import dum.springframework.aop.framework.DumAdvisedSupport;
import dum.springframework.aop.framework.DumAopProxy;
import dum.springframework.aop.framework.DumCglibAopProxy;
import dum.springframework.aop.framework.DumJdkDynamicAopProxy;
import dum.springframework.beans.DumBeanWrapper;
import dum.springframework.beans.factory.DumBeanFactory;
import dum.springframework.beans.factory.config.DumBeanDefinition;
import dum.springframework.beans.factory.support.DumBeanDefinitionReader;
import dum.springframework.beans.factory.support.DumDefaultListableBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther : Dumpling
 * @Description
 **/
public class DumApplicationContext extends DumDefaultListableBeanFactory implements DumBeanFactory {
    private String [] configLocations;

    private DumBeanDefinitionReader reader;

    private String[] beanDefinitionNames;
    //单例的IOC容器缓存
    private Map<String,Object> factoryBeanObjectCache = new ConcurrentHashMap<String, Object>();

    //通用的IOC容器
    private Map<String,DumBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<String, DumBeanWrapper>();

    public DumApplicationContext(String... configLocations)
    {
        this.configLocations = configLocations;
        try {
            refresh();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void refresh() throws Exception {
        //1.定位，定位配置文件
        reader = new DumBeanDefinitionReader(this.configLocations);

        //2.加载配置文件，扫描相关类，封装成BeanDefinition
        List<DumBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        beanDefinitionNames = reader.getBeanDefinitionNames();
        //3.注册，把配置信息放到容器中(伪IOC容器)
        doRegisterBeanDefinition(beanDefinitions);

        //4.把不是延时加载的类，提前初始化
        doAutowired();
    }

    private void doAutowired() {
        for (Map.Entry<String, DumBeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            if (!beanDefinitionEntry.getValue().isLazyInit())
            {
                try {
                    getBean(beanName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void doRegisterBeanDefinition(List<DumBeanDefinition> beanDefinitions) throws Exception {
        for (DumBeanDefinition beanDefinition : beanDefinitions) {
            if (beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName()))
                throw new Exception("The “" + beanDefinition.getFactoryBeanName() + "” is exists!!");
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
        }

    }

    @Override
    public Object getBean(String beanName) throws Exception {
        DumBeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        //初始化Bean
        Object instance = instantiateBean(beanName,beanDefinition);//实例化bean
        //把这个对象封装到BeanWrapper中
        DumBeanWrapper beanWrapper = new DumBeanWrapper(instance);

        if (factoryBeanInstanceCache.containsKey(beanName))
        {
            return this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();
        }

        factoryBeanInstanceCache.put(beanName,beanWrapper);
        //factoryBeanInstanceCache.put(beanWrapper.getWrappedClass().getName(),beanWrapper);
        //注入
        populateBean(beanName, new DumBeanDefinition(), beanWrapper);
        return this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();
    }

    private void populateBean(String beanName, DumBeanDefinition dumBeanDefinition, DumBeanWrapper beanWrapper) {
        Object instance = beanWrapper.getWrappedInstance();
        Class<?> clazz = beanWrapper.getWrappedClass();
        if (!(clazz.isAnnotationPresent(DumController.class) || clazz.isAnnotationPresent(DumService.class)))
            return;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(DumAutowired.class))
                continue;
            String autowiredBeanName = field.getAnnotation(DumAutowired.class).value();
            if ("".equals(autowiredBeanName))
                autowiredBeanName = field.getType().getName();
            field.setAccessible(true);
            try {
                if(!factoryBeanObjectCache.containsKey(autowiredBeanName))
                {
                    if(beanDefinitionMap.containsKey(autowiredBeanName))
                    {
                        factoryBeanObjectCache.put(autowiredBeanName,instantiateBean(autowiredBeanName,beanDefinitionMap.get(autowiredBeanName)));
                    }
                    else
                    {
                        continue;
                    }
                }
                field.set(instance,factoryBeanObjectCache.get(autowiredBeanName));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

    private Object instantiateBean(String beanName, DumBeanDefinition dumBeanDefinition) {
        //类名
        String className = dumBeanDefinition.getBeanClassName();
        Object instance = null;

        try {
            ////假设默认就是单例,细节暂且不考虑，先把主线拉通
            if (factoryBeanObjectCache.containsKey(className))
                instance = factoryBeanObjectCache.get(className);
            else
            {
                Class<?> clazz = Class.forName(className);
                //实例化对象
                instance = clazz.newInstance();

                DumAdvisedSupport config = instantionAopConfig(dumBeanDefinition);
                config.setTargetClass(clazz);
                config.setTarget(instance);

                if(config.pointCutMatch()) {
                    instance = createProxy(config).getProxy();
                }

                factoryBeanObjectCache.put(className,instance);
                factoryBeanObjectCache.put(beanName,instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return instance;
    }

    private DumAopProxy createProxy(DumAdvisedSupport config) {
        if (config.getTargetClass().getInterfaces().length > 0)
        {
            return new DumJdkDynamicAopProxy(config);
        }
        return new DumCglibAopProxy(config);
    }

    private DumAdvisedSupport instantionAopConfig(DumBeanDefinition dumBeanDefinition) {
        DumAopConfig config = new DumAopConfig();
        config.setPointCut(this.reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(this.reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(this.reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(this.reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(this.reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(this.reader.getConfig().getProperty("aspectAfterThrowingName"));
        return new DumAdvisedSupport(config);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws Exception {
        return (T) getBean(requiredType.getName());
    }

    public String[] getBeanDefinitionNames()
    {
        return  beanDefinitionNames;
    }

    public Properties getConfig()
    {
        return this.reader.getConfig();
    }
}
