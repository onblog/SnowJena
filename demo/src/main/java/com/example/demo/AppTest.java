package com.example.demo;

import cn.yueshutong.aspectj.CurrentResourceAspect;
import cn.yueshutong.aspectj.annotations.Limiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppTest {
    private Logger logger = LoggerFactory.getLogger(AppTest.class);

    public static void main(String[] args) {
        AppTest appTest = new AppTest();
        CurrentResourceAspect aspect = new CurrentResourceAspect();
        while (true) {
            appTest.sayHi();
        }
    }

    @Limiter(QPS = 1)
    private void sayHi() {
        logger.info("hi");
    }
}
