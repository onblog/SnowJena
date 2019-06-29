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

    /**
     * 是否关闭限流功能
     */
    private boolean enable;

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
     * 每批次取多少个令牌
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
     * 部署方式（本地/分布式）
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

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * Build设计模式
     */
    public static class LimiterRuleBuilder {
        private LimiterRule limiterRule;

        public LimiterRuleBuilder() {
            this.limiterRule = new LimiterRule();
        }

        public LimiterRuleBuilder setApp(String app) {
            this.limiterRule.app = app;
            return this;
        }
        /**
         * 限流规则名称
         */
        public LimiterRuleBuilder setId(String id) {
            this.limiterRule.id = id;
            return this;
        }
        /**
         * 时间单位
         */
        public LimiterRuleBuilder setUnit(TimeUnit unit) {
            this.limiterRule.unit = unit;
            return this;
        }
        /**
         * 单位时间大小
         */
        public LimiterRuleBuilder setPeriod(long period) {
            this.limiterRule.period = period;
            return this;
        }
        /**
         * 单位时间放入的令牌数
         */
        public LimiterRuleBuilder setLimit(long limit) {
            this.limiterRule.limit = limit;
            return this;
        }
        /**
         * 第一次放入令牌的延迟时间
         */
        public LimiterRuleBuilder setInitialDelay(long initialDelay) {
            this.limiterRule.initialDelay = initialDelay;
            return this;
        }
        /**
         * 每批次取多少个令牌
         */
        public LimiterRuleBuilder setBatch(long batch) {
            this.limiterRule.batch = batch;
            return this;
        }
        /**
         * 现有令牌数/批次令牌数<=? [0,1]
         */
        public LimiterRuleBuilder setRemaining(double remaining) {
            this.limiterRule.remaining = remaining;
            return this;
        }
        /**
         * 监控时长，秒，0为关闭
         */
        public LimiterRuleBuilder setMonitor(long monitor) {
            this.limiterRule.monitor = monitor;
            return this;
        }
        /**
         * 黑白名单列表
         */
        public LimiterRuleBuilder setLimitUser(String[] limitUser) {
            this.limiterRule.limitUser = limitUser;
            return this;
        }
        /**
         * 黑名单/白名单/无
         */
        public LimiterRuleBuilder setRuleAuthority(RuleAuthority ruleAuthority) {
            this.limiterRule.ruleAuthority = ruleAuthority;
            return this;
        }
        /**
         * 控制行为：快速失败/阻塞
         */
        public LimiterRuleBuilder setAcquireModel(AcquireModel acquireModel) {
            this.limiterRule.acquireModel = acquireModel;
            return this;
        }
        /**
         * 部署方式（本地/分布式）
         */
        public LimiterRuleBuilder setLimiterModel(LimiterModel limiterModel) {
            this.limiterRule.limiterModel = limiterModel;
            return this;
        }

        /**
         * 构建限流规则对象
         */
        public LimiterRule build() {
            LimiterRuleBuilder.check(this.limiterRule);
            return this.limiterRule;
        }

        public static void check(LimiterRule limiterRule) {
            assert limiterRule.batch > 0;
            assert limiterRule.remaining >= 0 && limiterRule.remaining <= 1;
            assert limiterRule.period >= 0;
            assert limiterRule.initialDelay >= 0;
            assert limiterRule.monitor >= 0;
        }
    }
}
