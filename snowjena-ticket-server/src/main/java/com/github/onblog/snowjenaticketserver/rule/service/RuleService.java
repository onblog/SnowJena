package com.github.onblog.snowjenaticketserver.rule.service;

import cn.yueshutong.commoon.entity.RateLimiterRule;
import com.github.onblog.snowjenaticketserver.rule.entity.Result;

public interface RuleService {
    String INSTANCE = "$INSTANCE$"; //实例前戳
    String RULE = "$RULE$"; //规则前戳
    String LOCK = "$LOCK$"; //锁前戳
    String BUCKET = "$BUCKET$"; //令牌桶前戳
    String BUCKET_PRINCIPAL = "$BUCKET_PRINCIPAL$"; //令牌桶负责的线程

    static String getInstanceKey(RateLimiterRule rateLimiterRule) {
        return INSTANCE + rateLimiterRule.getApp() + rateLimiterRule.getId() + rateLimiterRule.getName();
    }

    static String getInstanceKeys(RateLimiterRule rateLimiterRule) {
        return INSTANCE + rateLimiterRule.getApp() + rateLimiterRule.getId() + "*";
    }

    static String getLimiterRuleKey(RateLimiterRule rateLimiterRule) {
        return RULE + rateLimiterRule.getApp() + rateLimiterRule.getId();
    }

    static String getLimiterRuleKeys(String app, String id) {
        StringBuilder builder = new StringBuilder();
        if (app==null||app.isEmpty()){
            builder.append("*");
        }else {
            builder.append(app);
            if (id!=null) {
                builder.append(id);
            }
        }
        return RULE + builder.toString();
    }

    static String getBucketKey(RateLimiterRule rateLimiterRule) {
        return BUCKET + rateLimiterRule.getApp() + rateLimiterRule.getId();
    }

    static String getBucketPrincipalKey(RateLimiterRule rateLimiterRule) {
        return BUCKET_PRINCIPAL + rateLimiterRule.getApp() + rateLimiterRule.getId();
    }

    static String getLockKey(RateLimiterRule rateLimiterRule) {
        return LOCK + rateLimiterRule.getApp() + rateLimiterRule.getId();
    }


    /**
     * 心跳 Heartbeat
     * @param rateLimiterRule 客户端
     * @return 新规则
     */
    RateLimiterRule heartbeat(RateLimiterRule rateLimiterRule);

    /**
     * 更新规则一定更新版本号
     * @param rateLimiterRule 参数
     * @return 结果
     */
    boolean update(RateLimiterRule rateLimiterRule);

    /**
     * 查看规则
     * @param app，id，name
     * @param page
     * @param limit
     * @return 规则集合
     */
    Result<RateLimiterRule> getAllRule(String app, String id, int page, int limit);

}
