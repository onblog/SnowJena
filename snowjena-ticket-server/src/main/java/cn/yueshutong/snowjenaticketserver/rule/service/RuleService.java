package cn.yueshutong.snowjenaticketserver.rule.service;

import cn.yueshutong.commoon.entity.LimiterRule;
import cn.yueshutong.snowjenaticketserver.rule.entity.Result;

public interface RuleService {
    String INSTANCE = "$INSTANCE$"; //实例前戳
    String RULE = "$RULE$"; //规则前戳
    String LOCK = "$LOCK$"; //锁前戳
    String BUCKET = "$BUCKET$"; //令牌桶前戳

    static String getInstanceKey(LimiterRule limiterRule) {
        return INSTANCE + limiterRule.getApp() + limiterRule.getId() + limiterRule.getName();
    }

    static String getInstanceKeys(LimiterRule limiterRule) {
        return INSTANCE + limiterRule.getApp() + limiterRule.getId() + "*";
    }

    static String getLimiterRuleKey(LimiterRule limiterRule) {
        return RULE + limiterRule.getApp() + limiterRule.getId();
    }

    static String getBucketKey(LimiterRule limiterRule) {
        return BUCKET + limiterRule.getApp() + limiterRule.getId();
    }

    static String getLockKey(LimiterRule limiterRule) {
        return LOCK + limiterRule.getApp() + limiterRule.getId();
    }


    /**
     * 心跳 Heartbeat
     * @param limiterRule 客户端
     * @return 新规则
     */
    LimiterRule heartbeat(LimiterRule limiterRule);

    /**
     * 更新规则一定更新版本号
     * @param limiterRule 参数
     * @return 结果
     */
    boolean update(LimiterRule limiterRule);

    /**
     * 查看规则
     * @param app，id，name
     * @param page
     * @param limit
     * @return 规则集合
     */
    Result<LimiterRule> getAllRule(String app, String id,int page, int limit);

}
