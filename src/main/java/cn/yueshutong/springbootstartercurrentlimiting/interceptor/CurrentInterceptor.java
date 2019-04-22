package cn.yueshutong.springbootstartercurrentlimiting.interceptor;

import cn.yueshutong.springbootstartercurrentlimiting.core.RateLimiter;
import cn.yueshutong.springbootstartercurrentlimiting.handler.CurrentInterceptorHandler;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Create by yster@foxmail.com 2019/4/21 0021 11:19
 */
public class CurrentInterceptor implements HandlerInterceptor {
    private RateLimiter currentLimiter;
    private boolean failFast;
    private CurrentInterceptorHandler interceptorHandler;

    public CurrentInterceptor(RateLimiter currentLimiter, boolean failFast, CurrentInterceptorHandler handler) {
        this.currentLimiter = currentLimiter;
        this.failFast = failFast;
        this.interceptorHandler = handler;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (failFast) { //执行快速失败
            return tryAcquireFailed(request, response, handler);
        }else { //执行阻塞策略
            return currentLimiter.tryAcquire();
        }
    }

    private boolean tryAcquireFailed(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (currentLimiter.tryAcquireFailed()){ //取到令牌
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