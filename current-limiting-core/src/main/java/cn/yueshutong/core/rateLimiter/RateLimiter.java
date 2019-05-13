package cn.yueshutong.core.rateLimiter;

import cn.yueshutong.commoon.entity.LimiterRule;

public interface RateLimiter {

    void init(LimiterRule rule);

    boolean tryAcquire();

    boolean tryAcquire(String o);

    String getId();

    LimiterRule getLimiterRule();
}
