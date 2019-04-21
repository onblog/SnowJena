package cn.yueshutong.springbootstartercurrentlimiting.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * Create by yster@foxmail.com
 */
public interface CurrentInterceptorHandler {
    /**
     * 自定义全局的拦截处理
     * true:表示放行，false:表示拦截
     */
    void preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;
}
