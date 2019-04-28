package cn.yueshutong.springbootstartercurrentlimiting.interceptor;

import cn.yueshutong.springbootstartercurrentlimiting.common.SpringContextUtil;
import cn.yueshutong.springbootstartercurrentlimiting.core.RateLimiter;
import cn.yueshutong.springbootstartercurrentlimiting.core.RateLimiterCloud;
import cn.yueshutong.springbootstartercurrentlimiting.core.RateLimiterSingle;
import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentInterceptorHandler;
import cn.yueshutong.springbootstartercurrentlimiting.properties.CurrentProperties;
import cn.yueshutong.springbootstartercurrentlimiting.properties.CurrentRuleProperties;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 系统默认限流规则：通过配置文件指定参数
 * Create by yster@foxmail.com 2019/4/21 0021 11:19
 */
public class DefaultCurrentInterceptor implements HandlerInterceptor {
    private RateLimiter rateLimiter;
    private CurrentRuleProperties limiterRule;
    private CurrentInterceptorHandler interceptorHandler;

    DefaultCurrentInterceptor(CurrentProperties properties, CurrentInterceptorHandler handler, CurrentRuleProperties limiterRule) {
        this.limiterRule = limiterRule;
        this.interceptorHandler = handler;
        initRateLimiter(properties, limiterRule);
    }

    private void initRateLimiter(CurrentProperties properties, CurrentRuleProperties limiterRule) {
        if (properties.isCloudEnabled()){
            this.rateLimiter = RateLimiterCloud.of(limiterRule.getQps(), limiterRule.getInitialDelay(), SpringContextUtil.getApplicationName(),limiterRule.isOverflow());
        }else {
            this.rateLimiter = RateLimiterSingle.of(limiterRule.getQps(), limiterRule.getInitialDelay(),limiterRule.isOverflow());
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (limiterRule.isFailFast()) { //执行快速失败
            return tryAcquireFailed(request, response);
        }else { //执行阻塞策略
            return rateLimiter.tryAcquire();
        }
    }

    private boolean tryAcquireFailed(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (rateLimiter.tryAcquireFailed()){ //取到令牌
            return true;
        }else { //没取到令牌
            if (interceptorHandler == null) {
                response.getWriter().print(RateLimiter.message);
            } else {
                interceptorHandler.preHandle(request, response);
            }
            return false;
        }
    }
}