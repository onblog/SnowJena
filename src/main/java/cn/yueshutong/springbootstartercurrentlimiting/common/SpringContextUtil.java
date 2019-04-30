package cn.yueshutong.springbootstartercurrentlimiting.common;

import cn.yueshutong.springbootstartercurrentlimiting.properties.CurrentProperties;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext; // Spring应用上下文环境

    private static String applicationName;
    private static String port;
    private static int corePoolSize;
    private static boolean cloudEnabled;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
        SpringContextUtil.applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");
        SpringContextUtil.applicationName = SpringContextUtil.applicationName == null ? "application" : SpringContextUtil.applicationName;
        SpringContextUtil.port = applicationContext.getEnvironment().getProperty("server.port");
        SpringContextUtil.port = SpringContextUtil.port == null ? "8080" : SpringContextUtil.port;
        SpringContextUtil.corePoolSize = applicationContext.getBean(CurrentProperties.class).getCorePoolSize();
        SpringContextUtil.cloudEnabled = applicationContext.getBean(CurrentProperties.class).isCloudEnabled();
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

    public static boolean isCloudEnabled() {
        return cloudEnabled;
    }

    public static Object getBean(String name) throws BeansException {
        return applicationContext.getBean(name);
    }

    public static int getCorePoolSize() {
        return corePoolSize;
    }

    public static <T> T getBean(Class<T> clz) throws BeansException {
        return applicationContext.getBean(clz);
    }
}
