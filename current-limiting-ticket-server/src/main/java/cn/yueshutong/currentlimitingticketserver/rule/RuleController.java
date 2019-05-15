package cn.yueshutong.currentlimitingticketserver.rule;

import cn.yueshutong.commoon.entity.LimiterRule;
import cn.yueshutong.currentlimitingticketserver.rule.service.RuleService;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RuleController {
    @Autowired
    private RuleService ruleService;

    @RequestMapping(value = "/rule",method = RequestMethod.POST)
    public String rule(@RequestParam("data") String rule){
        LimiterRule limiterRule = JSON.parseObject(rule, LimiterRule.class);
        return JSON.toJSONString(ruleService.check(limiterRule));
    }

}
