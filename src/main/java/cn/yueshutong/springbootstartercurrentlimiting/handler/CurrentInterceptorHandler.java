package cn.yueshutong.springbootstartercurrentlimiting.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * Create by yster@foxmail.com
 */
public interface CurrentInterceptorHandler {
    /**
     * 拦截器拦截后的反馈
     */
    void preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;
}
