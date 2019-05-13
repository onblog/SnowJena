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
        return of(rule,null);
    }

    public static RateLimiter of(LimiterRule rule, RateLimiterConfig config) {
        switch (rule.getCurrentModel()) {
            case POINT:
                RateLimiterDefault limiterPoint = new RateLimiterDefault(rule,config);
                return limiterPoint;
            case CLOUD:
                rule.setId(config.getApp()+rule.getId());
                rule.setAllqps(rule.getQps());
                RateLimiterDefault limiterCloud = new RateLimiterDefault(rule,config);
                RateLimiterObserver.registered(limiterCloud);
                RateLimiterObserver.update(limiterCloud,config);
                return limiterCloud;
            default:
                throw new RuleNotParameter("CurrentModel Parameter not set");
        }
    }

}
