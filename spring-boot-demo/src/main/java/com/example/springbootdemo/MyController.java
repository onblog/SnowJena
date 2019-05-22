package com.example.springbootdemo;

import cn.yueshutong.core.rateLimiter.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {
    private Logger logger = LoggerFactory.getLogger(MyController.class);
    @Autowired
    private RateLimiter rateLimiter;

    @RequestMapping("/hi")
    public String hi() {
        if (rateLimiter.tryAcquire()){
            logger.warn("Hi");
        }
//        logger.info("NO");
        return "Hello World";
    }

}
