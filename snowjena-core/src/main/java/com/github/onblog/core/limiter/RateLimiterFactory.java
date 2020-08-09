package com.github.onblog.core.limiter;

import com.github.onblog.commoon.entity.RateLimiterRule;
import com.github.onblog.core.config.RateLimiterConfig;
import com.github.onblog.core.exception.SnowJeanException;
import com.github.onblog.core.observer.RateLimiterObserver;

/**
 * 简单工厂模式
 */
public class RateLimiterFactory {

    public static RateLimiter of(RateLimiterRule rule) {
        return of(rule, RateLimiterConfig.getInstance());
    }

    public static RateLimiter of(RateLimiterRule rule, RateLimiterConfig config) {
        switch (rule.getLimiterModel()) {
            case POINT: //本地限流
                RateLimiter limiterDefault = new RateLimiterDefault(rule, config);
                RateLimiterObserver.registered(limiterDefault,config);
                return limiterDefault;
            case CLOUD: //集群限流
                limiterDefault = new RateLimiterDefault(rule, config);
                rule.setName(rule.getName() == null ? String.valueOf(limiterDefault.hashCode()) : rule.getName());
                RateLimiterObserver.registered(limiterDefault,config);
                return limiterDefault;
            default:
                throw new SnowJeanException("CurrentModel Parameter not set");
        }
    }

}
