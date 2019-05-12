package com.example.demo;

import cn.yueshutong.aspectj.CurrentResourceAspect;
import cn.yueshutong.aspectj.annotations.Limiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {
    private Logger logger = LoggerFactory.getLogger(MyController.class);

    @RequestMapping("/hi")
    @Limiter(QPS = 2)
    public String hi() {
        logger.warn("Hi");
        return "Hello World";
    }

    @Bean
    public CurrentResourceAspect currentResourceAspect(){
        //启用注解
        return new CurrentResourceAspect();
    }

}
