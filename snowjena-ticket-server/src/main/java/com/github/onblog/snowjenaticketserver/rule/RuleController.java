package com.github.onblog.snowjenaticketserver.rule;

import com.github.onblog.commoon.entity.RateLimiterRule;
import com.github.onblog.commoon.entity.RateLimiterRuleBuilder;
import com.github.onblog.snowjenaticketserver.exception.ResultEnum;
import com.github.onblog.snowjenaticketserver.exception.ResultException;
import com.github.onblog.snowjenaticketserver.rule.entity.Result;
import com.github.onblog.snowjenaticketserver.rule.service.RuleService;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RuleController {
    @Autowired
    private RuleService ruleService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 心跳包
     * @param rule 规则字符串
     * @return 最新规则字符串
     */
    @RequestMapping(value = "/heart", method = RequestMethod.POST)
    public String heartbeat(@RequestParam("data") String rule) {
        logger.debug("heart:" + rule);
        RateLimiterRule rateLimiterRule = JSON.parseObject(rule, RateLimiterRule.class);
        return JSON.toJSONString(ruleService.heartbeat(rateLimiterRule));
    }

    /**
     * @return 所有规则
     */
    @RequestMapping(value = "/rule", method = RequestMethod.GET)
    @ResultException
    public Result<RateLimiterRule> getAllRule(String app, String id, int page, int limit) {
        return ruleService.getAllRule(app, id, page, limit);
    }

    /**
     * 修改限流规则
     * @return true/false
     */
    @RequestMapping(value = "/rule", method = RequestMethod.PUT)
    @ResultException
    public Result update(@RequestParam("data") String rule) {
        RateLimiterRule rateLimiterRule = JSON.parseObject(rule, RateLimiterRule.class);
        RateLimiterRuleBuilder.check(rateLimiterRule);
        ruleService.update(rateLimiterRule);
        return new Result(ResultEnum.SUCCESS);
    }

}
