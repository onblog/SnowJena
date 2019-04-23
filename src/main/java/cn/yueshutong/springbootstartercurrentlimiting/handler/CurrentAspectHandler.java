package cn.yueshutong.springbootstartercurrentlimiting.handler;

import cn.yueshutong.springbootstartercurrentlimiting.annotation.CurrentLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
/**
 * Create by yster@foxmail.com
 */
@Deprecated
public interface CurrentAspectHandler {
    /**
     * CurrentLimiter注解拦截后的反馈
     */
    Object around(ProceedingJoinPoint pjp, CurrentLimiter rateLimiter)throws Throwable;
}
