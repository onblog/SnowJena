package com.example.springbootdemo;

import cn.yueshutong.annotation.aspect.RateLimiterAspect;
import cn.yueshutong.annotation.entity.Limiter;
import cn.yueshutong.commoon.entity.RateLimiterRule;
import cn.yueshutong.commoon.entity.RateLimiterRuleBuilder;
import cn.yueshutong.core.limiter.RateLimiter;
import cn.yueshutong.core.limiter.RateLimiterFactory;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@RestController
public class TestController {
    @Value("${server.port}")
    private String port;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 注入RateLimiterAspect
     */
    @Bean
    public RateLimiterAspect rateLimiterAspect(){
        return new RateLimiterAspect();
    }

    /**
     * 使用注解之前需要生产限流器
     */
    @PostConstruct
    public void register() {
        RateLimiterRule rateLimiterRule = new RateLimiterRuleBuilder()
                .setId("limiter") //ID很重要，对应注解@Limiter中的value
                .setLimit(1)
                .setPeriod(1000)
                .setUnit(TimeUnit.MILLISECONDS)
                .setInitialDelay(100)
                .build();
        RateLimiter rateLimiter = RateLimiterFactory.of(rateLimiterRule);
    }

    /**
     * Value:rateLimiter.getId()
     * fallback:回调函数名称
     */
    @RequestMapping("/hello")
    @Limiter(value = "limiter", fallback = "sayFallback")
    public String say() {
        logger.info("hello");
        return "hello";
    }

    /**
     * fallback函数的参数要与原函数一致
     */
    public String sayFallback() {
        return "fallback_hello";
    }

    @Scheduled(fixedDelay = 100, initialDelay = 100)
    public void task() throws IOException {
        Jsoup.connect("http://localhost:" + port + "/hello")
                .get();
    }

}
