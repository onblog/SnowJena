package cn.yueshutong.rateLimiter;

import cn.yueshutong.config.AppManager;
import cn.yueshutong.config.ThreadManager;
import cn.yueshutong.exception.RuleNotParameter;
import cn.yueshutong.observer.RateLimiterObserver;
import cn.yueshutong.propertry.LimiterRule;

/**
 * 简单工厂模式
 */
public class RateLimiterFactory {

    public static RateLimiter of(LimiterRule rule) {
        switch (rule.getCurrentModel()) {
            case POINT:
                RateLimiterDefault limiterPoint = new RateLimiterDefault(rule);
                return limiterPoint;
            case CLOUD:
                rule.setId(AppManager.getApp()+rule.getId());
                rule.setAllqps(rule.getQps());
                RateLimiterDefault limiterCloud = new RateLimiterDefault(rule);
                RateLimiterObserver.registered(limiterCloud);
                return limiterCloud;
            default:
                throw new RuleNotParameter("CurrentModel Parameter not set");
        }
    }

}
