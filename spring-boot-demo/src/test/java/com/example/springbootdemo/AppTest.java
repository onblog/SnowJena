package com.example.springbootdemo;

import com.github.onblog.commoon.entity.RateLimiterRule;
import com.github.onblog.commoon.entity.RateLimiterRuleBuilder;
import com.github.onblog.commoon.enums.LimiterModel;
import com.github.onblog.commoon.enums.RuleAuthority;
import com.github.onblog.core.config.RateLimiterConfig;
import com.github.onblog.core.limiter.RateLimiter;
import com.github.onblog.core.limiter.RateLimiterFactory;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Create by yster@foxmail.com 2019/6/4 0004 15:06
 */
public class AppTest {
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 本地限流
     */
    @Test
    public void test1() {
        // 1.配置规则
        RateLimiterRule rateLimiterRule = new RateLimiterRuleBuilder()
                .setLimit(1)
                .setPeriod(1)
                .setUnit(TimeUnit.SECONDS) //每秒令牌数为1
                .build();
        // 2.工厂模式生产限流器
        RateLimiter limiter = RateLimiterFactory.of(rateLimiterRule);
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
        RateLimiterRule rateLimiterRule = new RateLimiterRuleBuilder()
                .setLimit(1)
                .setRuleAuthority(RuleAuthority.AUTHORITY_BLACK)
                .setLimitUser(new String[]{"user1", "user2"})
                .build();
        // 2.工厂模式生产限流器
        RateLimiter limiter = RateLimiterFactory.of(rateLimiterRule);
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
        RateLimiterRule rateLimiterRule = new RateLimiterRuleBuilder()
                .setLimit(1)
                .setRuleAuthority(RuleAuthority.AUTHORITY_WHITE)
                .setLimitUser(new String[]{"user1", "user2"})
                .build();
        // 2.工厂模式生产限流器
        RateLimiter limiter = RateLimiterFactory.of(rateLimiterRule);
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
        RateLimiterRule rateLimiterRule = new RateLimiterRuleBuilder()
                .setApp("Application")
                .setId("myId")
                .setLimit(1)
                .setBatch(1)
                .setLimiterModel(LimiterModel.CLOUD) //分布式限流,需启动TicketServer控制台
                .build();
        // 2.配置TicketServer地址（支持集群、加权重）
        Map<String, Integer> map = new HashMap<>();
        map.put("127.0.0.1:8521", 1);
        // 3.全局配置
        RateLimiterConfig config = RateLimiterConfig.getInstance();
        config.setTicketServer(map);
        // 4.工厂模式生产限流器
        RateLimiter limiter = RateLimiterFactory.of(rateLimiterRule, config);
        // 5.使用
        while (true) {
            if (limiter.tryAcquire()) {
                logger.info("ok");
            }
            Thread.sleep(100);
        }
    }

}
