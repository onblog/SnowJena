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
        switch (rule.getCurrentModel()) {
            case POINT:
                RateLimiterDefault limiterPoint = new RateLimiterDefault(rule, config);
                return limiterPoint;
            case CLOUD:
                RateLimiterDefault limiterDefault = new RateLimiterDefault(rule, config);
                rule.setAllqps(rule.getQps());
                rule.setName(rule.getName() == null ? String.valueOf(limiterDefault.hashCode()) : rule.getName());
                RateLimiterObserver.registered(limiterDefault);
                RateLimiterObserver.update(limiterDefault, config);
                RateLimiterObserver.monitor(limiterDefault,config);
                return limiterDefault;
            default:
                throw new RuleNotParameter("CurrentModel Parameter not set");
        }
    }

}
