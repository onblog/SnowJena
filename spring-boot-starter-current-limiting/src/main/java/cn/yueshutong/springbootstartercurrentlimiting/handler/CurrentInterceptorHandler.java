package cn.yueshutong.springbootstartercurrentlimiting.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * Create by yster@foxmail.com
 */
public interface CurrentInterceptorHandler {
    /**
     * After being intercepted by the interceptor..
     */
    void preHandle(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
