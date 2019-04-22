package cn.yueshutong.springbootstartercurrentlimiting.interceptor;

import cn.yueshutong.springbootstartercurrentlimiting.common.SpringContextUtil;
import cn.yueshutong.springbootstartercurrentlimiting.core.RateLimiterCloud;
import cn.yueshutong.springbootstartercurrentlimiting.core.RateLimiterSingle;
import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentInterceptorHandler;
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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (properties.isCloudEnabled()) {
            registry.addInterceptor(new CurrentInterceptor(RateLimiterCloud.of(properties.getQps(), properties.getInitialDelay(), SpringContextUtil.getApplicationName()), properties.isFailFast(), handler)).addPathPatterns("/**");
        }else {
            registry.addInterceptor(new CurrentInterceptor(RateLimiterSingle.of(properties.getQps(), properties.getInitialDelay()), properties.isFailFast(), handler)).addPathPatterns("/**");
        }
    }

}
