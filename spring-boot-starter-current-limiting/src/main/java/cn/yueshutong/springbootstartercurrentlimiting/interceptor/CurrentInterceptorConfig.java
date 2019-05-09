package cn.yueshutong.springbootstartercurrentlimiting.interceptor;

import cn.yueshutong.springbootstartercurrentlimiting.current.flag.MyCurrentInterceptor;
import cn.yueshutong.springbootstartercurrentlimiting.monitor.MonitorInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * spring boot 1.x Interceptor
 */
@Configuration
@ConditionalOnProperty(prefix = "current.limiting", name = "enabled", havingValue = "true")
public class CurrentInterceptorConfig extends WebMvcConfigurerAdapter {
    @Autowired
    private MyCurrentInterceptor myHandlerInterceptor;

    @Autowired(required = false)
    private MonitorInterceptor monitorInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (monitorInterceptor!=null){
            registry.addInterceptor(monitorInterceptor).addPathPatterns("/**");
        }
        registry.addInterceptor(myHandlerInterceptor).addPathPatterns("/**");
    }
}
