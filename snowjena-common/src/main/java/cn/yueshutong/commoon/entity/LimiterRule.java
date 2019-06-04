package cn.yueshutong.commoon.entity;

import cn.yueshutong.commoon.enums.Algorithm;
import cn.yueshutong.commoon.enums.LimiterModel;
import cn.yueshutong.commoon.enums.AcquireModel;
import cn.yueshutong.commoon.enums.RuleAuthority;

/**
 * 限流规则
 */
public class LimiterRule implements Comparable<LimiterRule>{
    private String app; //app name
    private String id; //限流规则名称
    private String name; //相同的限流规则，不同的实例标识
    private long monitor = 10; //监控时长，0为关闭
    private int number; //APP-ID实例数
    private double qps; //实际值，每秒并发量：等于0默认禁止访问//由程序修改，不可手动修改
    private long initialDelay; //初次允许访问的延迟时间：毫秒
    private AcquireModel acquireModel; //控制行为：快速失败/阻塞
    private Algorithm algorithm; //算法：令牌桶与漏桶的切换
    private LimiterModel limiterModel; //部署方式（单点/集群）
    private RuleAuthority ruleAuthority; //黑名单/白名单/无
    private String[] limitApp; //黑白名单列表
    private long version; //版本号
    private double allQps; //理论值，原值，集群，手动修改

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

    @Override
    public int compareTo(LimiterRule o) {
        if (this.version<o.getVersion()){
            return -1;
        }else if (this.version==o.getVersion()){
            return 0;
        }
        return 1;
    }

    public class Rule {
        private long size; //size：令牌桶容量
        private long period; //间隔：纳秒

        public long getPeriod() {
            if (getQps() != 0) {
                return (long) (1000 * 1000 * 1000 / getQps());
            }
            return 0;
        }

        public long getSize() {
            switch (getAlgorithm()) {
                case TOKENBUCKET:
                    return getQps() < 1 ? 1 : (long) getQps();
                case LEAKBUCKET:
                    return 1;
                default:
                    return 0;
            }
        }
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public double getAllQps() {
        return allQps;
    }

    public void setAllQps(double allQps) {
        this.allQps = allQps;
    }

    public String[] getLimitApp() {
        return limitApp;
    }

    public void setLimitApp(String[] limitApp) {
        this.limitApp = limitApp;
    }

    public RuleAuthority getRuleAuthority() {
        return ruleAuthority == null ? RuleAuthority.NULL : ruleAuthority;
    }

    public void setRuleAuthority(RuleAuthority ruleAuthority) {
        this.ruleAuthority = ruleAuthority;
    }

    public LimiterRule() {

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

    public double getQps() {
        return qps;
    }

    public void setQps(double qps) {
        this.qps = qps;
    }


    public Algorithm getAlgorithm() {
        return algorithm == null ? Algorithm.TOKENBUCKET : algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public AcquireModel getAcquireModel() {
        return acquireModel == null ? AcquireModel.FAILFAST : acquireModel;
    }

    public void setAcquireModel(AcquireModel acquireModel) {
        this.acquireModel = acquireModel;
    }

    public LimiterModel getLimiterModel() {
        return limiterModel == null ? LimiterModel.POINT : limiterModel;
    }

    public void setLimiterModel(LimiterModel limiterModel) {
        this.limiterModel = limiterModel;
    }

}
