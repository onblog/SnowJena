package com.example.springbootdemo;

import cn.yueshutong.annotation.entity.Limiter;
import cn.yueshutong.commoon.entity.RateLimiterRule;
import cn.yueshutong.commoon.entity.RateLimiterRuleBuilder;
import cn.yueshutong.core.config.RateLimiterConfig;
import cn.yueshutong.core.limiter.RateLimiter;
import cn.yueshutong.core.limiter.RateLimiterFactory;
import cn.yueshutong.core.observer.RateLimiterObserver;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {
    @Value("${server.port}")
    private String port;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 使用注解之前需要生产限流器
     */
    @PostConstruct
    public void register() {
        RateLimiterRule rateLimiterRule = new RateLimiterRuleBuilder()
                .setId("limiter") //ID很重要，对应注解@Limiter中的value
                .setLimit(10)
                .setBatch(8)
//                .setLimiterModel(LimiterModel.CLOUD)
                .build();
        // 2.配置TicketServer地址（支持集群、加权重）
        Map<String, Integer> map = new HashMap<>();
        map.put("127.0.0.1:8521", 1);
        // 3.全局配置
        RateLimiterConfig config = RateLimiterConfig.getInstance();
        config.setTicketServer(map);
        //生产限流器
        RateLimiter rateLimiter = RateLimiterFactory.of(rateLimiterRule,config);
        //随时随地获取已生产的限流器
        RateLimiter rateLimiter1 = RateLimiterObserver.getMap().get(rateLimiter.getId());
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
        logger.warn("no");
        return "fallback_hello";
    }

    @Scheduled(fixedDelay = 10, initialDelay = 100)
    public void task() throws IOException {
        Jsoup.connect("http://localhost:" + port + "/hello")
                .get();
    }

}
