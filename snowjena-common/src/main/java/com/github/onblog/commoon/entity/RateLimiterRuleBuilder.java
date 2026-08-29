package com.github.onblog.commoon.entity;

import com.github.onblog.commoon.enums.AcquireModel;
import com.github.onblog.commoon.enums.LimiterModel;
import com.github.onblog.commoon.enums.RuleAuthority;

import java.util.concurrent.TimeUnit;

/**
 * Build设计模式
 */
public class RateLimiterRuleBuilder {
    private RateLimiterRule rateLimiterRule;

    public RateLimiterRuleBuilder() {
        this.rateLimiterRule = new RateLimiterRule();
    }

    public RateLimiterRuleBuilder setApp(String app) {
        this.rateLimiterRule.setApp(app);
        return this;
    }

    /**
     * 限流规则名称
     */
    public RateLimiterRuleBuilder setId(String id) {
        this.rateLimiterRule.setId(id);
        return this;
    }

    /**
     * 单位时间放入的令牌数
     */
    public RateLimiterRuleBuilder setLimit(long limit) {
        this.rateLimiterRule.setLimit(limit);
        return this;
    }

    /**
     * 单位时间大小
     */
    public RateLimiterRuleBuilder setPeriod(long period) {
        this.rateLimiterRule.setPeriod(period);
        return this;
    }

    /**
     * 时间单位
     */
    public RateLimiterRuleBuilder setUnit(TimeUnit unit) {
        this.rateLimiterRule.setUnit(unit);
        return this;
    }

    /**
     * 第一次放入令牌的延迟时间
     */
    public RateLimiterRuleBuilder setInitialDelay(long initialDelay) {
        this.rateLimiterRule.setInitialDelay(initialDelay);
        return this;
    }

    /**
     * 每批次取多少个令牌
     */
    public RateLimiterRuleBuilder setBatch(long batch) {
        this.rateLimiterRule.setBatch(batch);
        return this;
    }

    /**
     * 现有令牌数/批次令牌数<=? [0,1]
     */
    public RateLimiterRuleBuilder setRemaining(double remaining) {
        this.rateLimiterRule.setRemaining(remaining);
        return this;
    }

    /**
     * 监控时长，秒，0为关闭
     */
    public RateLimiterRuleBuilder setMonitor(long monitor) {
        this.rateLimiterRule.setMonitor(monitor);
        return this;
    }

    /**
     * 黑白名单列表
     */
    public RateLimiterRuleBuilder setLimitUser(String[] limitUser) {
        this.rateLimiterRule.setLimitUser(limitUser);
        return this;
    }

    /**
     * 黑名单/白名单/无
     */
    public RateLimiterRuleBuilder setRuleAuthority(RuleAuthority ruleAuthority) {
        this.rateLimiterRule.setRuleAuthority(ruleAuthority);
        return this;
    }

    /**
     * 控制行为：快速失败/阻塞
     */
    public RateLimiterRuleBuilder setAcquireModel(AcquireModel acquireModel) {
        this.rateLimiterRule.setAcquireModel(acquireModel);
        return this;
    }

    /**
     * 部署方式（本地/分布式）
     */
    public RateLimiterRuleBuilder setLimiterModel(LimiterModel limiterModel) {
        this.rateLimiterRule.setLimiterModel(limiterModel);
        return this;
    }

    /**
     * 构建限流规则对象
     */
    public RateLimiterRule build() {
        RateLimiterRuleBuilder.check(this.rateLimiterRule);
        return this.rateLimiterRule;
    }

    public static void check(RateLimiterRule rateLimiterRule) {
        assert rateLimiterRule.getBatch() > 0;
        assert rateLimiterRule.getRemaining() >= 0 && rateLimiterRule.getRemaining() <= 1;
        assert rateLimiterRule.getPeriod() >= 0;
        assert rateLimiterRule.getInitialDelay() >= 0;
        assert rateLimiterRule.getMonitor() >= 0;
    }
}
