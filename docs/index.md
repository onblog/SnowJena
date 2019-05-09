# CurrentLimiting

## What

基于令牌桶算法和漏桶算法实现的纳秒级分布式无锁限流插件，完美嵌入SpringBoot、SpringCloud应用，支持接口限流、方法限流、系统限流、IP限流、用户限流等规则，支持熔断降级，支持设置系统启动保护时间（保护时间内不允许访问），提供快速失败与CAS阻塞两种限流方案，支持可视化QPS监控，开箱即用。[入门示例](https://github.com/yueshutong/spring-boot-starter-current-limiting/wiki/%E5%A6%82%E4%BD%95%E5%AF%B9CurrentLimiting%E8%BF%9B%E8%A1%8C%E9%99%90%E6%B5%81%E6%B5%8B%E8%AF%95%EF%BC%9F)

Based on token bucket algorithm and bucket algorithm implementation of nanosecond distributed unlocked current-limiting plug-in, perfect embedded SpringBoot, SpringCloud application, current limiting support interface, method, current limiting, IP system current limit, current limiting, user current limiting rules, such as support for fusing downgrade, support system startup protection set time (protection time is not allowed to access), provides a quick failure and CAS block two current-limiting methods, support visualization QPS monitoring, out of the box. Introduction to [example](https://github.com/yueshutong/spring-boot-starter-current-limiting/wiki/%E5%A6%82%E4%BD%95%E5%AF%B9CurrentLimiting%E8%BF%9B%E8%A1%8C%E9%99%90%E6%B5%81%E6%B5%8B%E8%AF%95%EF%BC%9F)

**Document: [中文](https://yueshutong.github.io/CurrentLimiting/README_CN)|[English](https://yueshutong.github.io/CurrentLimiting/README_EN)**

## Maven

Spring Boot
```xml
<dependency>
  <groupId>cn.yueshutong</groupId>
  <artifactId>spring-boot-starter-current-limiting</artifactId>
  <version>0.1.1.RELEASE</version>
</dependency>
```

## About

Blog：[http://www.yueshutong.cn](http://www.yueshutong.cn/)

Email：[yster@foxmail.com](mailto:yster@foxmail.com)

Github：<https://github.com/yueshutong/CurrentLimiting>

Gitee：<https://gitee.com/zyzpp/CurrentLimiting>

交流QQ群：781927207

如果帮助到你了，请不吝赞赏！如果贵司或团队使用了本限流插件，欢迎在issues留言，我会在底部链接贵司的主页，谢谢！

If help to you, please do not hesitate to praise! If your company or team used this issue, please feel free to comment on issues, and I will link to your company's homepage at the bottom, thank you!

<img src="https://gitee.com/zyzpp/Doctor/raw/master/picture/%E8%B5%9E%E8%B5%8F%E7%A0%81.png" width="300px">
