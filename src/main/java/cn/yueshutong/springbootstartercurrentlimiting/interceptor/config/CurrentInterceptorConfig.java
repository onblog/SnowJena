package cn.yueshutong.springbootstartercurrentlimiting.interceptor.config;

import cn.yueshutong.springbootstartercurrentlimiting.core.RateLimiter;
import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentInterceptorHandler;
import cn.yueshutong.springbootstartercurrentlimiting.interceptor.CurrentInterceptor;
import cn.yueshutong.springbootstartercurrentlimiting.interceptor.properties.CurrentProperties;
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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new CurrentInterceptor(RateLimiter.of(properties.getQps(),properties.getInitialDelay()),properties.isFailFast(),handler)).addPathPatterns("/**");
    }

}
