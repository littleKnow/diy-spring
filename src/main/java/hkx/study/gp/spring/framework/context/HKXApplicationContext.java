package hkx.study.gp.spring.framework.context;

import hkx.study.gp.spring.framework.annotation.HKXAutowired;
import hkx.study.gp.spring.framework.annotation.HKXComponent;
import hkx.study.gp.spring.framework.beans.HKXBeanWrapper;
import hkx.study.gp.spring.framework.beans.config.HKXBeanDefinition;
import hkx.study.gp.spring.framework.beans.support.HKXBeanDefinitionReader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HKXApplicationContext {

    private Map<String, HKXBeanDefinition> beanDefinitionMap;

    private Map<String, HKXBeanWrapper> factoryBeanInstanceCache = new HashMap<>();
    private Map<String, Object> factoryBeanObjectCache = new HashMap<>();

    private HKXBeanDefinitionReader reader;

    public HKXApplicationContext(String... configLocations) {
        //1.加载配置文件
        reader = new HKXBeanDefinitionReader(configLocations);
        //2.解析配置文件，封装成BeanDefinition
        List<HKXBeanDefinition> beanDefinitions = reader.loadBeanDefinition();
        //3.缓存BeanDefinition
        doRegisterBeanDefinition(beanDefinitions);
        //4.依赖注入
        doAutowried();
    }

    private void doAutowried() {
        // 调用getBean触发
        for (Map.Entry<String, HKXBeanDefinition> definitionEntry : beanDefinitionMap.entrySet()) {
            getBean(definitionEntry.getKey());
        }
    }

    private void doRegisterBeanDefinition(List<HKXBeanDefinition> beanDefinitions) {
        beanDefinitionMap = new HashMap<>(beanDefinitions.size());
        for (HKXBeanDefinition beanDefinition : beanDefinitions) {
            if (beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new RuntimeException("this bean is already exists:" + beanDefinition.getFactoryBeanName());
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }
    }

    /**
     * bean的实例化，DI是从这个方法开始的
     *
     * @param beanName
     * @return
     */
    public Object getBean(String beanName) {
        if (this.factoryBeanInstanceCache.containsKey(beanName)) {
            return this.factoryBeanInstanceCache.get(beanName).getWrapperInstance();
        }
        try {
            // 从配置中拿到BeanDefinition
            HKXBeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            // 实例化
            Object instance = instanceBean(beanName, beanDefinition);
            // 创建BeanWrapper
            HKXBeanWrapper beanWrapper = new HKXBeanWrapper(instance);
            // 保存到IOC容器
            factoryBeanInstanceCache.put(beanName, beanWrapper);
            // 执行依赖注入
            populateBean(beanName, beanWrapper, beanDefinition);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return factoryBeanInstanceCache.get(beanName).getWrapperInstance();
    }

    public Object getBean(Class<?> beanClass) {
        return getBean(beanClass.getName());
    }

    private void populateBean(String beanName, HKXBeanWrapper beanWrapper, HKXBeanDefinition beanDefinition) throws IllegalAccessException {
        // 对使用了Autowired注解的成员变量赋值spring托管的bean

        // 反射当前类中的所有成员变量
        Object bean = getBean(beanName);

        Class<?> beanClass = bean.getClass();
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            // 获取到有Autowired注解的成员变量，从ioc容器中，获取bean对象并赋值
            if (field.isAnnotationPresent(HKXAutowired.class)) {
                field.setAccessible(true);

                Object bean2 = getBean(field.getType());
                field.set(bean, bean2);
            }
        }
    }

    private Object instanceBean(String beanName, HKXBeanDefinition beanDefinition) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<?> aClass = Class.forName(beanDefinition.getBeanClassName());
        if (!isComponent(aClass)) {
            return null;
        }
        Object instance = aClass.newInstance();
        this.factoryBeanObjectCache.put(beanName, instance);
        return instance;
    }

    private boolean isComponent(Class<?> aClass) {
        if (aClass.isInterface() || aClass.isAnnotation() || aClass.isEnum() || aClass.isArray()) {
            return false;
        }
        return isComponentClass(aClass);
    }

    private boolean isComponentClass(Class<?> aClass) {
        if (aClass.isAnnotationPresent(HKXComponent.class)) {
            return true;
        }
        for (Annotation annotation : aClass.getDeclaredAnnotations()) {
            if (isComponentClass(annotation.annotationType())) {
                return true;
            }
        }
        return false;
    }

    public int getBeanDefinitionCount() {
        return this.factoryBeanInstanceCache.size();
    }

    public String[] getBeanDefinitionNames() {
        return this.factoryBeanInstanceCache.keySet().toArray(new String[factoryBeanInstanceCache.size()]);
    }
}
