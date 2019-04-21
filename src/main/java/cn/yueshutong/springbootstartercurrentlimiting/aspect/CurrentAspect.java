package cn.yueshutong.springbootstartercurrentlimiting.aspect;

import cn.yueshutong.springbootstartercurrentlimiting.annotation.CurrentLimiter;
import cn.yueshutong.springbootstartercurrentlimiting.common.CurrentEnum;
import cn.yueshutong.springbootstartercurrentlimiting.core.RateLimiter;
import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentAspectHandler;
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
    //一个方法一个限流器
    private Map<String, RateLimiter> map = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private CurrentAspectHandler handler;

    //声明切点
    @Pointcut("@annotation(cn.yueshutong.springbootstartercurrentlimiting.annotation.CurrentLimiter)")
    public void pointcut() {
    }

    //环绕通知：目标方法执行前后分别执行一些代码，发生异常的时候执行另外一些代码
    @Around("pointcut() && @annotation(currentLimiter)")
    public Object around(ProceedingJoinPoint pjp, CurrentLimiter currentLimiter) throws Throwable {
        //是否初始化限流器
        String key = pjp.getSignature().toLongString();
        if (!map.containsKey(key)) {
            map.put(key, RateLimiter.of(currentLimiter.QPS(), currentLimiter.initialDelay()));
        }
        //获取限流器
        RateLimiter rateLimiter = map.get(key);
        if (currentLimiter.failFast()){ //执行快速失败
            return tryAcquireFailed(pjp, currentLimiter, rateLimiter);
        }else { //执行阻塞策略
            rateLimiter.tryAcquire();
            return pjp.proceed();
        }
    }

    private Object tryAcquireFailed(ProceedingJoinPoint pjp, CurrentLimiter currentLimiter, RateLimiter rateLimiter) throws Throwable {
        if (rateLimiter.tryAcquireFailed()) { //取到令牌
            return pjp.proceed();
        }else { //没取到令牌
            return handler==null? CurrentEnum.MESSAGE.getMessage() :handler.around(pjp,currentLimiter);
        }
    }

}