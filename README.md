# :gift_heart:SnowJena

<img src="https://img.shields.io/badge/Language-Java8-green.svg" referrerPolicy="no-referrer"/><img src="https://img.shields.io/badge/Maven-3-green.svg" referrerPolicy="no-referrer"/><img src="https://img.shields.io/badge/License-Apache2.0-green.svg" referrerPolicy="no-referrer"/>

## What

基于令牌桶算法实现的分布式无锁限流框架，支持熔断降级，支持动态配置规则，支持可视化监控，开箱即用。

## Document

使用文档：[中文](./CN_README.md)|[English](./EN_README.md)

## Function

| 限流   | 熔断   | 降级   | 监控   | 注解   |
| ------ | ------ | ------ | ------ | ------ |
| 黑名单 | 白名单 | 控制台 | 分布式 | 高可用 |

## 设计模式

| 单例模式 | 观察者模式 | 工厂模式   | 建造者模式 | MVC模式 |
| -------- | ---------- | ---------- | ---------- | ------- |
| 全局配置 | 动态规则   | 生产限流器 | 限流规则   | 控制台  |


# Quick Start

## Maven

```xml
<dependency>
  <groupId>cn.yueshutong</groupId>
  <artifactId>snowjena-core</artifactId>
  <version>3.0.0.RELEASE</version>
</dependency>
```

## 本地限流

本项目提供了简单易用的API，对于本地限流，只需要通过工厂模式生产限流器，在需要限流的代码运行之前调用tryAcquire()方法即可。

```java
public class AppTest {
    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 本地限流
     */
    @Test
    public void test1() {
        // 1.配置规则
        RateLimiterRule rateLimiterRule = new RateLimiterRuleBuilder()
                .setLimit(1)
                .setPeriod(1)
                .setUnit(TimeUnit.SECONDS) //每秒令牌数为1
                .build();
        // 2.工厂模式生产限流器
        RateLimiter limiter = RateLimiterFactory.of(rateLimiterRule);
        // 3.使用
        while (true) {
            if (limiter.tryAcquire()) {
                logger.info("ok");
            }
        }
    }

}
```

查看控制台打印

```verilog
10:01:40.179 [main] INFO com.example.springbootdemo.AppTest - ok
10:01:41.178 [main] INFO com.example.springbootdemo.AppTest - ok
10:01:42.178 [main] INFO com.example.springbootdemo.AppTest - ok
10:01:43.179 [main] INFO com.example.springbootdemo.AppTest - ok
```

# About

Blog：<http://www.yueshutong.cn/>

Email：[yster@foxmail.com](mailto:yster@foxmail.com)

Github：<https://github.com/yueshutong/SnowJena>

Gitee：<https://gitee.com/zyzpp/SnowJena>

交流QQ群：781927207

如果帮助到你了，请不吝赞赏！谢谢！

<img src="http://ww3.sinaimg.cn/large/006tNc79ly1g43096t4oaj30tc0tc41y.jpg" width="300px" referrerpolicy="no-referrer">

<img src="http://ww3.sinaimg.cn/large/006tNc79ly1g4rgr77f3ij308c0au74a.jpg" width="300px" referrerPolicy="no-referrer">