package cn.yueshutong.springbootstartercurrentlimiting.core;

public interface RateLimiter {
    String message = "<pre>The specified service is not currently available.</pre>";

    boolean tryAcquire();

    boolean tryAcquireFailed();
}
