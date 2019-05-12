package cn.yueshutong.propertry;

import cn.yueshutong.enums.AcquireModel;
import cn.yueshutong.enums.Algorithm;
import cn.yueshutong.enums.LimiterModel;
import cn.yueshutong.enums.RuleAuthority;

/**
 * 限流规则
 */
public class LimiterRule {
    private String id; //限流规则名称
    private double qps; //实际值，每秒并发量：等于0默认禁止访问
    private long initialDelay; //初次允许访问的延迟时间：毫秒
    private AcquireModel acquireModel; //控制行为：快速失败/阻塞
    private Algorithm algorithm; //算法：令牌桶与漏桶的切换
    private LimiterModel currentModel; //限流器模型（单点/集群）
    private RuleAuthority ruleAuthority; //黑名单/白名单/无
    private String[] limitApp; //黑白名单列表
    private long version; //版本号
    private double allqps; //理论值，原值，集群

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

    public double getAllqps() {
        return allqps;
    }

    public void setAllqps(double allqps) {
        this.allqps = allqps;
    }

    public String[] getLimitApp() {
        return limitApp;
    }

    public void setLimitApp(String[] limitApp) {
        this.limitApp = limitApp;
    }

    public RuleAuthority getRuleAuthority() {
        return ruleAuthority==null?RuleAuthority.NULL:ruleAuthority;
    }

    public void setRuleAuthority(RuleAuthority ruleAuthority) {
        this.ruleAuthority = ruleAuthority;
    }

    public LimiterRule() {

    }

    public String getId() {
        assert this.id!=null;
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
        return algorithm == null? Algorithm.TOKENBUCKET:algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public AcquireModel getAcquireModel() {
        return acquireModel==null?AcquireModel.FAILFAST:acquireModel;
    }

    public void setAcquireModel(AcquireModel acquireModel) {
        this.acquireModel = acquireModel;
    }

    public LimiterModel getCurrentModel() {
        return currentModel ==null?LimiterModel.POINT:currentModel;
    }

    public void setCurrentModel(LimiterModel currentModel) {
        this.currentModel = currentModel;
    }

}
