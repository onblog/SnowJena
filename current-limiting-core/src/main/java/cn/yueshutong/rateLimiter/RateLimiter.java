package cn.yueshutong.rateLimiter;

import cn.yueshutong.propertry.LimiterRule;

public interface RateLimiter {

    void init(LimiterRule rule);

    boolean tryAcquire();

    boolean tryAcquire(String o);

    String getId();

    LimiterRule getLimiterRule();
}
