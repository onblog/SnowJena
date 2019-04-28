package cn.yueshutong.springbootstartercurrentlimiting.interceptor;

import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentInterceptorHandler;
import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentRuleHandler;
import cn.yueshutong.springbootstartercurrentlimiting.properties.CurrentProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * spring boot 2.0 拦截器
 */
@Configuration
@ConditionalOnProperty(prefix = "current.limiting", name = "enabled", havingValue = "true")
public class CurrentInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private CurrentProperties properties;

    @Autowired(required = false)
    private CurrentInterceptorHandler handler;

    @Autowired(required = false)
    private CurrentRuleHandler rule;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (rule==null) { //是否自定义规则
            registry.addInterceptor(new DefaultCurrentInterceptor(properties, handler)).addPathPatterns("/**");
        }else {
            registry.addInterceptor(new CustomCurrentInterceptor(properties, handler,rule)).addPathPatterns("/**");
        }
    }

}
