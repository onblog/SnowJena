package cn.yueshutong.currentlimitingticketserver.controller;

import cn.yueshutong.commoon.entity.LimiterRule;
import com.alibaba.fastjson.JSON;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RuleController {

    @RequestMapping(value = "/rule",method = RequestMethod.POST)
    public String rule(@RequestParam("data") String rule){
        System.out.println(JSON.parseObject(rule, LimiterRule.class));
        return rule;
    }

}
