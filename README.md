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
CurrentLimiting的功能

限流

当我们设计了一个函数，准备上线，这时候这个函数会消耗一些资源，处理上限是1秒服务3000个QPS，但如果实际情况遇到高于3000的QPS该如何解决呢？CurrentLimiting提供了当QPS超出某个设定的阈值，系统可以通过直接拒绝、匀速器三种方式来应对，从而起流量控制的作用。

降级

接触过Spring Cloud、Service Mesh的同学，都知道熔断降级的概念。服务之间会有相互依赖关系，例如服务A做到了1秒上万个QPS，但这时候服务B并无法满足1秒上万个QPS，那么如何保证服务A在高频调用服务B时，服务B仍能正常工作呢？一种比较常见的情况是，服务A调用服务B时，服务B因无法满足高频调用出现响应时间过长的情况，导致服务A也出现响应过长的情况，进而产生连锁反应影响整个依赖链上的所有应用，这时候就需要熔断和降级的方法。CurrentLimiting通过响应时间对资源进行降级两种手段来对服务进行熔断或降级。

塑形

通常我们遇到的流量具有随机性、不规则、不受控的特点，但系统的处理能力往往是有限的，我们需要根据系统的处理能力对流量进行塑形，即规则化，从而根据我们的需要来处理流量。CurrentLimiting通过资源的调用关系、运行指标、控制的效果三个维度来对流量进行控制，开发者可以自行灵活组合，从而达到理想的效果。

![](./picture/monitor.jpg)

## About

Blog：[http://www.yueshutong.cn](http://www.yueshutong.cn/)

Email：[yster@foxmail.com](mailto:yster@foxmail.com)

Github：<https://github.com/yueshutong/CurrentLimiting>

Gitee：<https://gitee.com/zyzpp/CurrentLimiting>

交流QQ群：781927207

如果帮助到你了，请不吝赞赏！如果贵司或团队使用了本限流插件，欢迎在issues留言，我会在底部链接贵司的主页，谢谢！

If help to you, please do not hesitate to praise! If your company or team used this issue, please feel free to comment on issues, and I will link to your company's homepage at the bottom, thank you!

<img src="https://gitee.com/zyzpp/Doctor/raw/master/picture/%E8%B5%9E%E8%B5%8F%E7%A0%81.png" width="300px">
