package cn.yueshutong.springbootstartercurrentlimiting.interceptor;

import cn.yueshutong.springbootstartercurrentlimiting.common.SpringContextUtil;
import cn.yueshutong.springbootstartercurrentlimiting.core.RateLimiter;
import cn.yueshutong.springbootstartercurrentlimiting.core.RateLimiterCloud;
import cn.yueshutong.springbootstartercurrentlimiting.core.RateLimiterSingle;
import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentInterceptorHandler;
import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentRuleHandler;
import cn.yueshutong.springbootstartercurrentlimiting.property.CurrentProperty;
import cn.yueshutong.springbootstartercurrentlimiting.properties.CurrentProperties;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomCurrentInterceptor implements HandlerInterceptor {
    private CurrentInterceptorHandler interceptorHandler;
    private CurrentRuleHandler limiterRule;
    private CurrentProperties properties;
    private Map<String,RateLimiter> map = new ConcurrentHashMap<>();

    CustomCurrentInterceptor(CurrentProperties properties, CurrentInterceptorHandler handler, CurrentRuleHandler limiterRule) {
        this.limiterRule = limiterRule;
        this.interceptorHandler = handler;
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //读取实现的规则
        CurrentProperty property = limiterRule.rule(request);
        if (property==null){ //为NULL则默认不限制
            return true;
        }
        //初始化限流器
        RateLimiter rateLimiter = initRateLimiter(property);
        if (property.isFailFast()){ //执行快速失败
            return tryAcquireFailed(request,response,handler,rateLimiter);
        }else { //执行阻塞策略
            return rateLimiter.tryAcquire();
        }
    }

    /**
     * 初始化限流器
     * 为了提高性能，不加同步锁，所以刚开始可能存在极短暂的误差。
     */
    private RateLimiter initRateLimiter(CurrentProperty property) {
        //获取限流器
        if (!map.containsKey(property.getId())){
            //判断是否是集群
            if (properties.isCloudEnabled()) {
                map.put(property.getId(),RateLimiterCloud.of(property.getQps(),property.getInitialDelay(), SpringContextUtil.getApplicationName()+property.getId(),property.isOverflow()));
            } else {
                map.put(property.getId(),RateLimiterSingle.of(property.getQps(), property.getInitialDelay(),property.isOverflow()));
            }
        }
        return map.get(property.getId());
    }

    private boolean tryAcquireFailed(HttpServletRequest request, HttpServletResponse response, Object handler,RateLimiter rateLimiter) throws Exception {
        if (rateLimiter.tryAcquireFailed()){ //取到令牌
            return true;
        }else { //没取到令牌
            if (interceptorHandler == null) {
                response.getWriter().print(RateLimiter.message);
            } else {
                interceptorHandler.preHandle(request, response, handler);
            }
            return false;
        }
    }

}

