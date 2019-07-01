package cn.yueshutong.core.limiter;

import cn.yueshutong.commoon.entity.RateLimiterRule;
import cn.yueshutong.monitor.client.MonitorService;

public interface RateLimiter {

    MonitorService getMonitorService();

    void init(RateLimiterRule rule);

    boolean tryAcquire();

    boolean tryAcquire(String s);

    String getId();

    RateLimiterRule getRule();
}
