package cn.yueshutong.springbootstartercurrentlimiting.handler;

import cn.yueshutong.springbootstartercurrentlimiting.method.annotation.CurrentLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
/**
 * Create by yster@foxmail.com
 */
public interface CurrentAspectHandler {
    /**
     * After being intercepted by annotations..
     */
    Object around(ProceedingJoinPoint pjp, CurrentLimiter rateLimiter)throws Throwable;
}
