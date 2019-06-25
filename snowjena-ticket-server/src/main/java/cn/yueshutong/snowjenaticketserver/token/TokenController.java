package cn.yueshutong.snowjenaticketserver.token;

import cn.yueshutong.commoon.entity.LimiterRule;
import cn.yueshutong.snowjenaticketserver.token.service.TokenService;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenController {
    @Autowired
    private TokenService tokenService;

    @RequestMapping(value = "/token",method = RequestMethod.POST)
    public String token(@RequestParam("data") String rule){
        LimiterRule limiterRule = JSON.parseObject(rule, LimiterRule.class);
        return JSON.toJSONString(tokenService.token(limiterRule));
    }

}
