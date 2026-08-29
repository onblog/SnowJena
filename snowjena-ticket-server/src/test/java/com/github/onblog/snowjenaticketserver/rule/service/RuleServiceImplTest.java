package com.github.onblog.snowjenaticketserver.rule.service;

import com.alibaba.fastjson.JSON;
import com.github.onblog.commoon.entity.RateLimiterRule;
import com.github.onblog.monitor.entity.MonitorBean;
import org.junit.Test;

public class RuleServiceImplTest {

    @Test
    public void json() {
        String j = "{\"app\":\"Application\",\"id\":\"myId\",\"name\":\"1355531311\",\"limit\":\"1\",\"period\":1,\"initialDelay\":0,\"unit\":\"SECONDS\",\"batch\":2,\"remaining\":0.5,\"monitor\":10,\"acquireModel\":\"FAILFAST\",\"limiterModel\":\"CLOUD\",\"ruleAuthority\":\"NULL\",\"limitUser\":null,\"number\":0,\"version\":0}";
        RateLimiterRule rateLimiterRule = JSON.parseObject(j, RateLimiterRule.class);
        System.out.println(rateLimiterRule);
    }

    @Test
    public void check() {
        double s = 0.00000031 / 4;
        System.out.println(s == 0.00000031 / 4 ? s : false);
    }

    @Test
    public void entity() {
        MonitorBean monitorBean = new MonitorBean();
        monitorBean.setName("A");
        to(monitorBean);
        System.out.println(monitorBean.getName());
    }

    private void to(MonitorBean monitorBean) {
        monitorBean = new MonitorBean();
        monitorBean.setName("B");
        ;
    }

    @Test
    public void test1() {
        test2("", "", "");
    }

    public void test2(String... name) {
        StringBuilder builder = new StringBuilder();
        for (String value : name) {
            builder.append(value);
        }
        System.out.println(builder.toString());
    }
}