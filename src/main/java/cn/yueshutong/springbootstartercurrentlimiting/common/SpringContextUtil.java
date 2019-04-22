package cn.yueshutong.springbootstartercurrentlimiting.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component()
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext; // Spring应用上下文环境

    private static String applicationName;
    private static String port;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
        SpringContextUtil.applicationName = applicationContext.getId();
        SpringContextUtil.port = applicationContext.getEnvironment().getProperty("server.port");
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static String getPort() {
        return port;
    }

    public static String getApplicationName() {
        return applicationName;
    }

    public static <T> T getBean(String name) throws BeansException {
        return (T) applicationContext.getBean(name);
    }

    public static <T> T getBean(Class<?> clz) throws BeansException {
        return (T) applicationContext.getBean(clz);
    }
}
