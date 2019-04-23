package cn.yueshutong.springbootstartercurrentlimiting.handler;

import cn.yueshutong.springbootstartercurrentlimiting.handler.entity.CurrentLimiterProperty;

import javax.servlet.http.HttpServletRequest;

public interface CurrentRuleHandler {
    /**
     * 拦截规则
     */
    CurrentLimiterProperty rule(HttpServletRequest request);
}
