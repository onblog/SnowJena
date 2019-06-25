package com.example.springbootdemo;

import cn.yueshutong.commoon.entity.LimiterRule;
import cn.yueshutong.commoon.enums.LimiterModel;
import cn.yueshutong.commoon.enums.RuleAuthority;
import cn.yueshutong.core.config.RateLimiterConfig;
import cn.yueshutong.core.rateLimiter.RateLimiter;
import cn.yueshutong.core.rateLimiter.RateLimiterFactory;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Create by yster@foxmail.com 2019/5/26 0026 18:34
 */
@SpringBootApplication
@EnableScheduling
public class DemoApplication {

    @Value("${server.port}")
    private String port;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }


//    @Scheduled(fixedRate = 100,initialDelay = 1000)
    public void task(){
        try {
            Jsoup.connect("http://127.0.0.1:"+port+"/hi").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @Bean
    public RateLimiter rateLimiter(){
        //rule
        LimiterRule limiterRule = new LimiterRule();
        limiterRule.setApp("APP");
        limiterRule.setId("ID");

//        limiterRule.setLimiterModel(LimiterModel.POINT); //POINT限流,没有TicketServer的支持
        limiterRule.setLimiterModel(LimiterModel.CLOUD); //CLOUD限流,有TicketServer的支持(监控、动态规则)
        limiterRule.setRuleAuthority(RuleAuthority.AUTHORITY_BLACK);
        limiterRule.setLimitApp(new String[]{"user1","user2"});
        //config
        RateLimiterConfig config = RateLimiterConfig.getInstance();
        Map<String,Integer> map = new HashMap<>();
        map.put("127.0.0.1:8521",1);
        config.setTicketServer(map);
        return RateLimiterFactory.of(limiterRule,config);
    }

}