package cn.yueshutong.currentlimitingticketserver.rule;

import cn.yueshutong.commoon.entity.LimiterRule;
import cn.yueshutong.currentlimitingticketserver.rule.service.RuleService;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RuleController {
    @Autowired
    private RuleService ruleService;

    @RequestMapping(value = "/heart")
    public String heartbeat(@RequestParam("data") String rule){
        LimiterRule limiterRule = JSON.parseObject(rule, LimiterRule.class);
        return JSON.toJSONString(ruleService.heartbeat(limiterRule));
    }

    @RequestMapping(value = "/rule",method = RequestMethod.GET)
    public List<LimiterRule> get(String app,String id,String name){
        return ruleService.getAll(app,id,name);
    }

    @RequestMapping(value = "/rule",method = RequestMethod.PUT)
    public boolean update(@RequestParam("data") String rule){
        LimiterRule limiterRule = JSON.parseObject(rule, LimiterRule.class);
        return ruleService.update(limiterRule);
    }

}
