package cn.yueshutong.commoon.entity;

import cn.yueshutong.commoon.enums.AcquireModel;
import cn.yueshutong.commoon.enums.LimiterModel;
import cn.yueshutong.commoon.enums.RuleAuthority;

import java.util.concurrent.TimeUnit;

/**
 * 限流规则
 */
public class LimiterRule implements Comparable<LimiterRule>{
    // APP
    /**
     * app name
     */
    private String app;
    /**
     * 限流规则名称
     */
    private String id;
    /**
     * 相同的限流规则，不同的实例标识(不需要用户配置)
     */
    private String name;

    //QPS
    /**
     * 每个时间段对应的令牌数
     */
    private long limit;
    /**
     * 时间段的长度
     */
    private long period;
    /**
     * 第一次放入令牌的延迟时间
     */
    private long initialDelay;
    /**
     * 时间段以及延迟时间的单位
     */
    private TimeUnit unit;

    //get bucket
    /**
     * 每批次取多少个令牌 (0,limit)
     */
    private long batch = 1;
    /**
     * 现有令牌数/批次令牌数<=? [0,1]
     */
    private double remaining = 1;

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
    private String[] limitApp;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getApp() {
        assert app != null;
        return app;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public long getMonitor() {
        return monitor;
    }

    public void setMonitor(long monitor) {
        this.monitor = monitor;
    }

    public long getPeriod() {
        assert period!=0;
        return period;
    }
    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }
    public void setPeriod(long period) {
        this.period = period;
    }

    public TimeUnit getUnit() {
        assert unit!=null;
        return unit;
    }

    public void setUnit(TimeUnit unit) {
        this.unit = unit;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String[] getLimitApp() {
        return limitApp;
    }

    public void setLimitApp(String[] limitApp) {
        this.limitApp = limitApp;
    }

    public RuleAuthority getRuleAuthority() {
        return ruleAuthority;
    }

    public void setRuleAuthority(RuleAuthority ruleAuthority) {
        this.ruleAuthority = ruleAuthority;
    }


    public String getId() {
        assert id != null;
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
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


    public long getBatch() {
        return batch;
    }

    public void setBatch(long batch) {
        assert batch>0&&batch<=limit;
        this.batch = batch;
    }

    public double getRemaining() {
        return remaining;
    }

    public void setRemaining(double remaining) {
        assert remaining>=0&&remaining<=100;
        this.remaining = remaining;
    }

    @Override
    public int compareTo(LimiterRule o) {
        if (this.version<o.getVersion()){
            return -1;
        }else if (this.version==o.getVersion()){
            return 0;
        }
        return 1;
    }
}
