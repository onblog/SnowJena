package cn.yueshutong.springbootstartercurrentlimiting.handler;

import cn.yueshutong.springbootstartercurrentlimiting.property.CurrentProperty;

import javax.servlet.http.HttpServletRequest;

public interface CurrentRuleHandler {
    /**
     * 拦截规则
     */
    CurrentProperty rule(HttpServletRequest request);
}
