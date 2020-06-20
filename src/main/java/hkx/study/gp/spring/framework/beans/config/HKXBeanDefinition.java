package hkx.study.gp.spring.framework.beans.config;

/**
 * 保存bean配置
 */
public class HKXBeanDefinition {

    private String factoryBeanName;
    private String beanClassName;

    public HKXBeanDefinition(String factoryBeanName, String beanClassName) {
        this.factoryBeanName = factoryBeanName;
        this.beanClassName = beanClassName;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }
}
