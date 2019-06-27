package cn.yueshutong.commoon.entity;

import cn.yueshutong.commoon.enums.AcquireModel;
import cn.yueshutong.commoon.enums.LimiterModel;
import cn.yueshutong.commoon.enums.RuleAuthority;

import java.util.concurrent.TimeUnit;

/**
 * 限流规则
 */
public class LimiterRule implements Comparable<LimiterRule> {
    // APP
    /**
     * app name
     */
    private String app = "Application";
    /**
     * 限流规则名称
     */
    private String id = "id";
    /**
     * 相同的限流规则，不同的实例标识(不需要用户配置)
     */
    private String name;

    //QPS
    /**
     * 单位时间存放的令牌数
     */
    private long limit;
    /**
     * 单位时间大小
     */
    private long period = 1;
    /**
     * 第一次放入令牌的延迟时间
     */
    private long initialDelay = 0;
    /**
     * 时间单位
     */
    private TimeUnit unit = TimeUnit.SECONDS;

    //get bucket
    /**
     * 每批次取多少个令牌 (0,limit]
     */
    private long batch = 1;
    /**
     * 现有令牌数/批次令牌数<=? [0,1]
     */
    private double remaining = 0.5;

    //Monitor
    /**
     * 监控时长，秒，0为关闭
     */
    private long monitor = 10;

    //Select
    /**
     * 控制行为：快速失败/阻塞
     */
    private AcquireModel acquireModel = AcquireModel.FAILFAST;
    /**
     * 部署方式（单点/分布式）
     */
    private LimiterModel limiterModel = LimiterModel.POINT;
    /**
     * 黑名单/白名单/无
     */
    private RuleAuthority ruleAuthority = RuleAuthority.NULL;
    /**
     * 黑白名单列表
     */
    private String[] limitUser;

    //System
    /**
     * APP-ID实例数(不需要用户配置)
     */
    private int number;
    /**
     * 版本号(不需要用户配置)
     */
    private long version;
//    private double qps; //实际值，每秒并发量：等于0默认禁止访问//由程序修改，不可手动修改
//    private double allQps; //理论值，原值，集群，手动修改(不需要用户配置)


    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    public long getBatch() {
        return batch;
    }

    public void setBatch(long batch) {
        this.batch = batch;
    }

    public double getRemaining() {
        return remaining;
    }

    public void setRemaining(double remaining) {
        this.remaining = remaining;
    }

    public long getMonitor() {
        return monitor;
    }

    public void setMonitor(long monitor) {
        this.monitor = monitor;
    }

    public AcquireModel getAcquireModel() {
        return acquireModel;
    }

    public void setAcquireModel(AcquireModel acquireModel) {
        this.acquireModel = acquireModel;
    }

    public LimiterModel getLimiterModel() {
        return limiterModel;
    }

    public void setLimiterModel(LimiterModel limiterModel) {
        this.limiterModel = limiterModel;
    }

    public RuleAuthority getRuleAuthority() {
        return ruleAuthority;
    }

    public void setRuleAuthority(RuleAuthority ruleAuthority) {
        this.ruleAuthority = ruleAuthority;
    }

    public String[] getLimitUser() {
        return limitUser;
    }

    public void setLimitUser(String[] limitUser) {
        this.limitUser = limitUser;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public int compareTo(LimiterRule o) {
        if (this.version < o.getVersion()) {
            return -1;
        } else if (this.version == o.getVersion()) {
            return 0;
        }
        return 1;
    }

    /**
     * Build设计模式
     */
    public static class LimiterRuleBuilder{
        private LimiterRule limiterRule;

        public LimiterRuleBuilder(){
            this.limiterRule = new LimiterRule();
        }

        public LimiterRuleBuilder setApp(String app) {
            this.limiterRule.app = app;
            return this;
        }

        public LimiterRuleBuilder setId(String id) {
            this.limiterRule.id = id;
            return this;
        }

        public LimiterRuleBuilder setMonitor(long monitor) {
            this.limiterRule.monitor = monitor;
            return this;
        }

        public LimiterRuleBuilder setUnit(TimeUnit unit) {
            this.limiterRule.unit = unit;
            return this;
        }

        public LimiterRuleBuilder setPeriod(long period) {
            this.limiterRule.period = period;
            return this;
        }

        public LimiterRuleBuilder setLimit(long limit) {
            this.limiterRule.limit = limit;
            return this;
        }

        public LimiterRuleBuilder setInitialDelay(long initialDelay) {
            this.limiterRule.initialDelay = initialDelay;
            return this;
        }

        public LimiterRuleBuilder setBatch(long batch) {
            this.limiterRule.batch = batch;
            return this;
        }

        public LimiterRuleBuilder setRemaining(double remaining) {
            this.limiterRule.remaining = remaining;
            return this;
        }

        public LimiterRuleBuilder setLimitUser(String[] limitUser) {
            this.limiterRule.limitUser = limitUser;
            return this;
        }

        public LimiterRuleBuilder setRuleAuthority(RuleAuthority ruleAuthority) {
            this.limiterRule.ruleAuthority = ruleAuthority;
            return this;
        }

        public LimiterRuleBuilder setAcquireModel(AcquireModel acquireModel) {
            this.limiterRule.acquireModel = acquireModel;
            return this;
        }

        public LimiterRuleBuilder setLimiterModel(LimiterModel limiterModel) {
            this.limiterRule.limiterModel = limiterModel;
            return this;
        }

        public LimiterRule build(){
            assert this.limiterRule.batch > 0 && this.limiterRule.batch <= this.limiterRule.limit;
            assert this.limiterRule.remaining >= 0 && this.limiterRule.remaining <= 1;
            return this.limiterRule;
        }
    }
}
