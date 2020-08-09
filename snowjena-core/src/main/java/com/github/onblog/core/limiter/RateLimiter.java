package com.github.onblog.core.limiter;

import com.github.onblog.commoon.entity.RateLimiterRule;
import com.github.onblog.monitor.client.MonitorService;

public interface RateLimiter {

    MonitorService getMonitorService();

    void init(RateLimiterRule rule);

    boolean tryAcquire();

    boolean tryAcquire(String s);

    String getId();

    RateLimiterRule getRule();
}
