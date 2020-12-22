# :gift_heart:SnowJena

<img src="https://img.shields.io/badge/Language-Java8-green.svg" referrerPolicy="no-referrer"/><img src="https://img.shields.io/badge/Maven-3-green.svg" referrerPolicy="no-referrer"/><img src="https://img.shields.io/badge/License-Apache2.0-green.svg" referrerPolicy="no-referrer"/>

## What

基于令牌桶算法实现的分布式无锁限流框架，支持动态配置规则，支持可视化监控，开箱即用。

## Document

使用文档：[中文](./CN_README.md)|[English](./EN_README.md)

## 功能概要

| 限流   | 降级   | 注解   | 监控   | 动态   |
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
  <groupId>com.github.onblog</groupId>
  <artifactId>snowjena-core</artifactId>
  <version>4.0.0.RELEASE</version>
</dependency>
```

## 本地限流

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


# About

Email：[codeblog@qq.com](mailto:codeblog@qq.com)

如果帮助到你了，请不吝赞赏！谢谢！

<img src='https://i.loli.net/2020/01/13/pPoFNwT6fKCZQ2i.png' width="400px" />
