package dum.springframework.beans.factory.config;

import lombok.Data;

/**
 * @Auther : Dumpling
 * @Description //用来存储配置文件中的信息
 *              //相当于保存在内存中的配置
 **/
@Data
public class DumBeanDefinition {
    private String beanClassName;
    private boolean lazyInit = false;
    private String factoryBeanName;
}
