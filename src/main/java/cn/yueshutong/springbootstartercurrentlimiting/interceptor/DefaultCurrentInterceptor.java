package cn.yueshutong.springbootstartercurrentlimiting.interceptor;

import cn.yueshutong.springbootstartercurrentlimiting.common.SpringContextUtil;
import cn.yueshutong.springbootstartercurrentlimiting.core.RateLimiter;
import cn.yueshutong.springbootstartercurrentlimiting.core.RateLimiterCloud;
import cn.yueshutong.springbootstartercurrentlimiting.core.RateLimiterSingle;
import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentInterceptorHandler;
import cn.yueshutong.springbootstartercurrentlimiting.properties.CurrentProperties;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Create by yster@foxmail.com 2019/4/21 0021 11:19
 */
public class DefaultCurrentInterceptor implements HandlerInterceptor {
    private RateLimiter rateLimiter;
    private boolean failFast;
    private CurrentInterceptorHandler interceptorHandler;

    DefaultCurrentInterceptor(CurrentProperties properties, CurrentInterceptorHandler handler) {
        this.failFast = properties.isFailFast();
        this.interceptorHandler = handler;
        if (properties.isCloudEnabled()){
            rateLimiter = RateLimiterCloud.of(properties.getQps(), properties.getInitialDelay(),SpringContextUtil.getApplicationName());
        }else {
            rateLimiter = RateLimiterSingle.of(properties.getQps(), properties.getInitialDelay());
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (failFast) { //执行快速失败
            return tryAcquireFailed(request, response, handler);
        }else { //执行阻塞策略
            return rateLimiter.tryAcquire();
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