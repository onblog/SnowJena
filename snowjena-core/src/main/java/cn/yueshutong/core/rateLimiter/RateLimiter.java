package cn.yueshutong.core.rateLimiter;

import cn.yueshutong.commoon.entity.LimiterRule;
import cn.yueshutong.monitor.client.MonitorService;

public interface RateLimiter {

    MonitorService getMonitorService();

    void init(LimiterRule rule);

    boolean tryAcquire();

    boolean tryAcquire(String s);

    String getId();

    LimiterRule getRule();
}
