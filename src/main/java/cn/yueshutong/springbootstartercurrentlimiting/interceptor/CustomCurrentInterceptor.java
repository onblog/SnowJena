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
import java.util.HashMap;
import java.util.Map;

public class CustomCurrentInterceptor implements HandlerInterceptor {
    private CurrentInterceptorHandler interceptorHandler;
    private CurrentRuleHandler limiterRule;
    private CurrentProperties properties;
    private RateLimiter rateLimiter;
    private Map<String,RateLimiter> map = new HashMap<>();

    CustomCurrentInterceptor(CurrentProperties properties, CurrentInterceptorHandler handler, CurrentRuleHandler limiterRule) {
        this.limiterRule = limiterRule;
        this.interceptorHandler = handler;
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //读取实现的规则
        CurrentProperty rule = limiterRule.rule(request);
        //初始化限流器
        initRateLimiter(request,rule);
        if (rule.isFailFast()){ //执行快速失败
            return tryAcquireFailed(request,response,handler);
        }else { //执行阻塞策略
            return rateLimiter.tryAcquire();
        }
    }

    private void initRateLimiter(HttpServletRequest request, CurrentProperty rule) {
        //获取限流器
        if (map.containsKey(rule.getId())){
            rateLimiter = map.get(rule.getId());
        }else {
            //判断是否是集群
            if (properties.isCloudEnabled()) {
                rateLimiter = RateLimiterCloud.of(rule.getQps(),rule.getInitialDelay(), SpringContextUtil.getApplicationName()+rule.getId(),rule.isOverflow());
            } else {
                rateLimiter = RateLimiterSingle.of(rule.getQps(), rule.getInitialDelay(),rule.isOverflow());
            }
        }
    }

    private boolean tryAcquireFailed(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
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

