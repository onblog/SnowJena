package com.github.onblog.snowjeanspringbootstarter;

import com.github.onblog.snowjeanspringbootstarter.annotation.aspect.RateLimiterAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ComponentScan
public class SnowJeanAutoConfiguration {
    /**
     * 注入RateLimiterAspect
     */
    @Bean
    public RateLimiterAspect rateLimiterAspect() {
        return new RateLimiterAspect();
    }
}
