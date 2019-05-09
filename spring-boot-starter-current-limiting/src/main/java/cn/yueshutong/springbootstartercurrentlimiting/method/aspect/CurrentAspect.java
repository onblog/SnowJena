package cn.yueshutong.springbootstartercurrentlimiting.method.aspect;

import cn.yueshutong.springbootstartercurrentlimiting.method.annotation.CurrentLimiter;
import cn.yueshutong.springbootstartercurrentlimiting.common.SpringContextUtil;
import cn.yueshutong.springbootstartercurrentlimiting.rateLimiter.RateLimiter;
import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentAspectHandler;
import cn.yueshutong.springbootstartercurrentlimiting.properties.CurrentProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Aspect
@ConditionalOnProperty(prefix = "current.limiting", name = "part-enabled", havingValue = "true", matchIfMissing = true)
public class CurrentAspect {
    //One method, one limiter
    private Map<String, RateLimiter> map = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private CurrentAspectHandler handler;

    @Autowired
    private CurrentProperties properties;

    //The statement point of tangency
    @Pointcut("@annotation(cn.yueshutong.springbootstartercurrentlimiting.method.annotation.CurrentLimiter)")
    public void pointcut() {
    }

    @Around("pointcut() && @annotation(currentLimiter)")
    public Object around(ProceedingJoinPoint pjp, CurrentLimiter currentLimiter) throws Throwable {
        RateLimiter rateLimiter = initCurrentLimiting(pjp,currentLimiter);
        if (currentLimiter.failFast()){ //Fast execution fails
            return tryAcquireFailed(pjp, currentLimiter, rateLimiter);
        }else { //Execute blocking strategy
            rateLimiter.tryAcquire();
            return pjp.proceed();
        }
    }

    private Object tryAcquireFailed(ProceedingJoinPoint pjp, CurrentLimiter currentLimiter, RateLimiter rateLimiter) throws Throwable {
        if (rateLimiter.tryAcquireFailed()) { //To get the token
            return pjp.proceed();
        }else { //No token was taken
            return handler==null? RateLimiter.message :handler.around(pjp,currentLimiter);
        }
    }

    /**
     * Initialize the current limiter
     * 为了提高性能，不加同步锁，所以存在初始的误差。
     */
    private RateLimiter initCurrentLimiting(ProceedingJoinPoint pjp, CurrentLimiter currentLimiter) {
        String key = pjp.getSignature().toLongString();
        if (!map.containsKey(key)) {
            map.put(key,RateLimiter.of(currentLimiter.QPS(),currentLimiter.initialDelay(), SpringContextUtil.getApplicationName()+key,currentLimiter.overflow()));
        }
        return map.get(key);
    }

}