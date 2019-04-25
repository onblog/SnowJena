package cn.yueshutong.springbootstartercurrentlimiting.core;

import cn.yueshutong.springbootstartercurrentlimiting.common.SpringContextUtil;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public interface RateLimiter {
    String message = "<pre>The specified service is not currently available.</pre>";

    ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(SpringContextUtil.getCorePoolSize());

    boolean tryAcquire();

    boolean tryAcquireFailed();

    LocalDateTime getExpirationTime();
}
