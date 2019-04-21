package cn.yueshutong.springbootstartercurrentlimiting.handler;

import cn.yueshutong.springbootstartercurrentlimiting.annotation.CurrentLimiter;
import org.aspectj.lang.ProceedingJoinPoint;
/**
 * Create by yster@foxmail.com
 */
public interface CurrentAspectHandler {
    /**
     * 自定义对CurrentLimiter注解的拦截处理
     * @param pjp
     * @param rateLimiter
     * @return
     * @throws Throwable
     */
    Object around(ProceedingJoinPoint pjp, CurrentLimiter rateLimiter)throws Throwable;
}
