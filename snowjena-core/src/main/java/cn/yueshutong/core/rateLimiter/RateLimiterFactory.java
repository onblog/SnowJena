package cn.yueshutong.core.rateLimiter;

import cn.yueshutong.commoon.entity.LimiterRule;
import cn.yueshutong.core.config.RateLimiterConfig;
import cn.yueshutong.core.exception.RuleNotParameter;
import cn.yueshutong.core.observer.RateLimiterObserver;

/**
 * 简单工厂模式
 */
public class RateLimiterFactory {

    public static RateLimiter of(LimiterRule rule) {
        return of(rule, RateLimiterConfig.getInstance());
    }

    public static RateLimiter of(LimiterRule rule, RateLimiterConfig config) {
        switch (rule.getLimiterModel()) {
            case POINT: //单点限流
                return new RateLimiterDefault(rule, config);
            case CLOUD: //集群限流
                RateLimiter limiterDefault = new RateLimiterDefault(rule, config);
                rule.setAllQps(rule.getQps());
                rule.setName(rule.getName() == null ? String.valueOf(limiterDefault.hashCode()) : rule.getName());
                RateLimiterObserver.registered(limiterDefault,config);
                return limiterDefault;
            default:
                throw new RuleNotParameter("CurrentModel Parameter not set");
        }
    }

}
