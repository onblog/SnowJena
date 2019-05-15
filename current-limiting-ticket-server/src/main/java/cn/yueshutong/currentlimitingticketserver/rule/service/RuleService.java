package cn.yueshutong.currentlimitingticketserver.rule.service;

import cn.yueshutong.commoon.entity.LimiterRule;

public interface RuleService {
    LimiterRule check(LimiterRule limiterRule);
}
