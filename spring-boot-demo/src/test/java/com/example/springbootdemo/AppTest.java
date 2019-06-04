package com.example.springbootdemo;

import cn.yueshutong.commoon.entity.LimiterRule;
import cn.yueshutong.commoon.enums.LimiterModel;
import cn.yueshutong.commoon.enums.RuleAuthority;
import cn.yueshutong.core.config.RateLimiterConfig;
import cn.yueshutong.core.rateLimiter.RateLimiter;
import cn.yueshutong.core.rateLimiter.RateLimiterFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Create by yster@foxmail.com 2019/6/4 0004 15:06
 */
public class AppTest {
    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 单点限流
     */
    @Test
    public void test1() {
        // 1.配置规则
        LimiterRule limiterRule = new LimiterRule();
        limiterRule.setQps(1);
        // 2.工厂模式生产限流器
        RateLimiter limiter = RateLimiterFactory.of(limiterRule);
        // 3.使用
        while (true) {
            if (limiter.tryAcquire()) {
                logger.info("ok");
            }
        }
    }

    /**
     * 黑名单
     */
    @Test
    public void test2() {
        // 1.配置规则
        LimiterRule limiterRule = new LimiterRule();
        limiterRule.setQps(1);
        limiterRule.setRuleAuthority(RuleAuthority.AUTHORITY_BLACK);
        limiterRule.setLimitApp(new String[]{"user1", "user2"});
        // 2.工厂模式生产限流器
        RateLimiter limiter = RateLimiterFactory.of(limiterRule);
        // 3.使用
        while (true) {
            if (limiter.tryAcquire("user1")) {
                logger.info("user1");
            }
            if (limiter.tryAcquire("user2")) {
                logger.info("user2");
            }
            if (limiter.tryAcquire("user3")) {
                logger.info("user3");
            }
        }
    }

    /**
     * 白名单
     */
    @Test
    public void test3() {
        // 1.配置规则
        LimiterRule limiterRule = new LimiterRule();
        limiterRule.setQps(1);
        limiterRule.setRuleAuthority(RuleAuthority.AUTHORITY_WHITE);
        limiterRule.setLimitApp(new String[]{"user1", "user2"});
        // 2.工厂模式生产限流器
        RateLimiter limiter = RateLimiterFactory.of(limiterRule);
        // 3.使用
        while (true) {
            if (limiter.tryAcquire("user1")) {
                logger.info("user1");
            }
            if (limiter.tryAcquire("user2")) {
                logger.info("user2");
            }
            if (limiter.tryAcquire("user3")) {
                logger.info("user3");
            }
        }
    }

    /**
     * 分布式限流
     */
    @Test
    public void test4() throws InterruptedException {
        // 1.限流配置
        LimiterRule limiterRule = new LimiterRule();
        limiterRule.setApp("Application"); //应用名
        limiterRule.setId("myId"); //限流器ID
        limiterRule.setQps(1);
        limiterRule.setLimiterModel(LimiterModel.CLOUD); //分布式限流,需启动TicketServer控制台
        // 2.配置TicketServer地址（支持集群、加权重）
        Map<String,Integer> map = new HashMap<>();
        map.put("127.0.0.1:8521",1);
        // 3.全局配置
        RateLimiterConfig config = RateLimiterConfig.getInstance();
        config.setTicketServer(map);
        // 4.工厂模式生产限流器
        RateLimiter limiter = RateLimiterFactory.of(limiterRule, config);
        // 5.使用
        while (true) {
            if (limiter.tryAcquire()) {
                logger.info("ok");
            }
            Thread.sleep(100);
        }
    }

}
