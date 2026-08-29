package com.github.onblog.snowjeanspringbootstarter.annotation.aspect;

import com.github.onblog.core.limiter.RateLimiter;
import com.github.onblog.core.observer.RateLimiterObserver;
import com.github.onblog.snowjeanspringbootstarter.annotation.entity.Limiter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@Aspect
public class RateLimiterAspect {

    @Pointcut("@annotation(com.github.onblog.snowjeanspringbootstarter.annotation.entity.Limiter)")
    public void pointcut() {
    }

    @Around("pointcut() && @annotation(limiter)")
    public Object around(ProceedingJoinPoint pjp, Limiter limiter) throws Throwable {
        RateLimiter rateLimiter = RateLimiterObserver.getMap().get(limiter.value());
        if (rateLimiter.tryAcquire()) {
            return pjp.proceed();
        }
        Signature sig = pjp.getSignature();
        if (!(sig instanceof MethodSignature)) {
            throw new IllegalArgumentException("This annotation can only be used in methods.");
        }
        MethodSignature msg = (MethodSignature) sig;
        Object target = pjp.getTarget();
        Method fallback = target.getClass().getMethod(limiter.fallback(), msg.getParameterTypes());
        return fallback.invoke(target, pjp.getArgs());
    }

}
