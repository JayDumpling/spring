package dum.springframework.beans.factory.support;

import dum.springframework.beans.factory.config.DumBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @Auther : Dumpling
 * @Description 读取配置信息 Spring本身使用装饰器模式分为XML和注解方式读取，这里简单实现
 **/
public class DumBeanDefinitionReader {

    private Properties config = new Properties();

    private final String SCAN_PACKAGE = "scanPackage";

    //className
    private List<String> registryBeanNames = new ArrayList<>();

    public DumBeanDefinitionReader(String... configLocations) {
        //这里暂时写死就一个路径
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configLocations[0].replace("classpath:", ""));
        try {
            config.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    /**
     * 扫描包下面所有的类，把类名存在registryBeanNames
     *
     * @param scanPackage
     */
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String classname = scanPackage + "." + file.getName().replaceAll(".class", "");
                registryBeanNames.add(classname);
            }
        }
    }

    public List<DumBeanDefinition> loadBeanDefinitions() {
        List<DumBeanDefinition> result = new ArrayList<>();
        try {
            for (String className : registryBeanNames) {
                Class<?> clazz = Class.forName(className);
                if (clazz.isInterface()) continue;
                result.add(doCreateBeanDefinition(toLowerFirstCase(clazz.getSimpleName()), clazz.getName()));
                result.add(doCreateBeanDefinition(clazz.getName(), clazz.getName()));
                Class<?>[] interfaces = clazz.getInterfaces();
                for (Class<?> i : interfaces) {
                    result.add(doCreateBeanDefinition(i.getName(),clazz.getName()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 把每一个配置信息解析为BeanDefinition
     *
     * @param className
     * @return
     */
    private DumBeanDefinition doCreateBeanDefinition(String factoryBeanName, String className) {
        DumBeanDefinition beanDefinition = new DumBeanDefinition();
        beanDefinition.setFactoryBeanName(factoryBeanName);
        beanDefinition.setBeanClassName(className);
        return beanDefinition;
    }

    private String toLowerFirstCase(String simpleName) {
        if (Character.isLowerCase(simpleName.charAt(0))) {
            return simpleName;
        }

        char[] chars = simpleName.toCharArray();
        if (chars.length <= 0)
            return simpleName;
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public Properties getConfig() {
        return this.config;
    }

    public String[] getBeanDefinitionNames() {
        String[] strings = new String[registryBeanNames.size()];
        registryBeanNames.toArray(strings);
        return strings;
    }
}
