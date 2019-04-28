package cn.yueshutong.springbootstartercurrentlimiting.interceptor;

import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentInterceptorHandler;
import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentRuleHandler;
import cn.yueshutong.springbootstartercurrentlimiting.properties.CurrentProperties;
import cn.yueshutong.springbootstartercurrentlimiting.properties.CurrentRuleProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * spring boot 1.x 拦截器
 */
@Configuration
@ConditionalOnProperty(prefix = "current.limiting", name = "enabled", havingValue = "true")
public class CurrentInterceptorConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private CurrentProperties properties;

    @Autowired
    private CurrentRuleProperties rules;

    @Autowired(required = false)
    private CurrentInterceptorHandler handler;

    @Autowired(required = false)
    private CurrentRuleHandler rule;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (rule==null) { //是否自定义规则
            registry.addInterceptor(new DefaultCurrentInterceptor(properties, handler, rules)).addPathPatterns("/**");
        }else {
            registry.addInterceptor(new CustomCurrentInterceptor(properties, handler,rule)).addPathPatterns("/**");
        }
    }

}
