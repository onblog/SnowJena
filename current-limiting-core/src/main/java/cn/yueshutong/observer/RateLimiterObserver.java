package cn.yueshutong.observer;

import cn.yueshutong.config.ThreadManager;
import cn.yueshutong.exception.RuleBeReplaced;
import cn.yueshutong.propertry.LimiterRule;
import cn.yueshutong.rateLimiter.RateLimiter;
import cn.yueshutong.config.TicketServerManager;
import com.alibaba.fastjson.JSON;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 观察者模式（一对多）
 */
public class RateLimiterObserver {
    private static Map<String, RateLimiter> map = new ConcurrentHashMap<>();

    public static void registered(RateLimiter rule) {
        if (map.containsKey(rule.getId())) {
            throw new RuleBeReplaced("Repeat registration for current limiting rules:" + rule.getId());
        }
        map.put(rule.getId(), rule);
//        update(rule);
    }

    /**
     * 动态更新限流规则
     */
    private static void update(RateLimiter rule){
        ThreadManager.getScheduled().scheduleAtFixedRate(() -> {
            String rules = TicketServerManager.getTicketServer().connect("/rule?id="+JSON.toJSONString(rule.getLimiterRule()));
            LimiterRule limiterRule = JSON.parseObject(rules, LimiterRule.class);
            if (limiterRule.getVersion()>rule.getLimiterRule().getVersion()) {
                map.get(rule.getId()).init(limiterRule);
            }
        },0,1, TimeUnit.SECONDS);
    }

}
