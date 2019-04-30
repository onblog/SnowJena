package cn.yueshutong.springbootstartercurrentlimiting.handler;

import cn.yueshutong.springbootstartercurrentlimiting.property.CurrentProperty;

import javax.servlet.http.HttpServletRequest;

public interface CurrentRuleHandler {
    /**
     * Rules for interceptors
     */
    CurrentProperty rule(HttpServletRequest request);
}
