package cn.yueshutong.core.observer;

import cn.yueshutong.commoon.entity.LimiterRule;
import cn.yueshutong.core.config.RateLimiterConfig;
import cn.yueshutong.core.exception.RuleBeReplaced;
import cn.yueshutong.core.rateLimiter.RateLimiter;
import cn.yueshutong.monitor.entity.MonitorBean;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 观察者模式（一对多）
 */
public class RateLimiterObserver {
    private static Map<String, RateLimiter> map = new ConcurrentHashMap<>();
    private static Logger logger = LoggerFactory.getLogger(RateLimiterObserver.class);

    public static void registered(RateLimiter rule) {
        if (map.containsKey(rule.getId())) {
            throw new RuleBeReplaced("Repeat registration for current limiting rules:" + rule.getId());
        }
        map.put(rule.getId(), rule);
    }

    /**
     * 动态更新限流规则
     */
    public static void update(RateLimiter rule, RateLimiterConfig config){
        config.getScheduled().scheduleWithFixedDelay(() -> {
            String rules = config.getTicketServer().connect("heart", JSON.toJSONString(rule.getLimiterRule()));
            LimiterRule limiterRule = JSON.parseObject(rules, LimiterRule.class);
            if (limiterRule.getVersion()>rule.getLimiterRule().getVersion()) {
                map.get(rule.getId()).init(limiterRule);
                logger.warn("rule update: "+rule.getId());
            }
        },0,1, TimeUnit.SECONDS);
    }

    /**
     * 监控数据上报
     */
    public static void monitor(RateLimiter rule, RateLimiterConfig config) {
        config.getScheduled().scheduleWithFixedDelay(() -> {
            List<MonitorBean> monitorBeans = rule.getMonitorService().getAndDelete();
            if (monitorBeans.size()<1){
                return;
            }
            String result = config.getTicketServer().connect("monitor", JSON.toJSONString(monitorBeans));
            if (result!=null) {
                logger.debug("monitor update success");
            }
        },0,3, TimeUnit.SECONDS);
    }

}
