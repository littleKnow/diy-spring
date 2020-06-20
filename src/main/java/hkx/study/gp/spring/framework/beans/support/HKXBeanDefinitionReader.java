package hkx.study.gp.spring.framework.beans.support;

import hkx.study.gp.spring.framework.beans.config.HKXBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 读取配置文件
 */
public class HKXBeanDefinitionReader {

    /**
     * 保存扫描的结果
     */
    private List<String> registryBeanClasses = new ArrayList<>();

    /**
     * 保存读取到的配置文件内容
     */
    private Properties properties = new Properties();

    public HKXBeanDefinitionReader(String... configLocations) {
        for (String configLocation : configLocations) {
            doLoadConfig(configLocation);
        }
        doScanner(this.properties.getProperty("scanPackage"));
    }

    /**
     * 扫描包下的所有class，并保存
     *
     * @param scanPackage
     */
    private void doScanner(String scanPackage) {
        URL resource = this.getClass().getClassLoader().getResource("/" + scanPackage.replace(".", File.separator));
        File file = new File(resource.getFile());

        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                doScanner(scanPackage + "." + f.getName());
            } else {
                if (!f.getName().endsWith(".class")) {
                    continue;
                }
                String beanClasse = scanPackage + "." + f.getName().replace(".class", "");
                registryBeanClasses.add(beanClasse);
            }
        }
    }

    /**
     * 创建BeanDefinition
     *
     * @return
     */
    public List<HKXBeanDefinition> loadBeanDefinition() {
        List<HKXBeanDefinition> beanDefinitionList = new ArrayList<>();
        try {
            for (String registryBeanClass : registryBeanClasses) {
                // 反射类
                Class<?> aClass = Class.forName(registryBeanClass);
                if (aClass.isAnnotation() || aClass.isInterface() || aClass.isEnum()) {
                    continue;
                }

                // 保存类的全类名和bean名称
                String classFullName = aClass.getName();
                String beanName = toLowerFirstCase(aClass.getSimpleName());
                beanDefinitionList.add(doCreateBeanDefinition(beanName, classFullName));

                // 自定义

                // 接口注入
                for (Class<?> interfaceClass : aClass.getInterfaces()) {
                    beanDefinitionList.add(doCreateBeanDefinition(interfaceClass.getName(), classFullName));
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return beanDefinitionList;
    }

    private HKXBeanDefinition doCreateBeanDefinition(String beanName, String classFullName) {
        return new HKXBeanDefinition(beanName, classFullName);
    }

    private String toLowerFirstCase(String name) {
        char[] chars = name.toCharArray();
        chars[0] = (chars[0] + "").toLowerCase().charAt(0);
        return new String(chars);
    }

    /**
     * 读取配置文件，并保存
     *
     * @param configLocation
     */
    private void doLoadConfig(String configLocation) {
        try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(
                configLocation.replace("classpath:", ""))) {
            this.properties.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
