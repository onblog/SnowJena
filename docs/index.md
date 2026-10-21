---
layout: home

hero:
  name: java-limiter · 疾限
  text: Lock-free rate limiting
  tagline: Six algorithms in one 18 KB jar, Java 8+, zero dependencies, no background threads. 六种算法、单个 18 KB jar、零依赖、无后台线程。
  image:
    src: /logo.svg
    alt: java-limiter
  actions:
    - theme: brand
      text: 🇺🇸 English docs
      link: /en/
    - theme: alt
      text: 🇨🇳 中文文档
      link: /zh-CN/
    - theme: alt
      text: GitHub
      link: https://github.com/onblog/java-limiter

features:
  - title: Zero dependencies · 零依赖
    details: Only the JDK — no framework, no logging binding, no JSON library, no Redis client. 只用 JDK，没有框架、日志绑定、JSON 库或 Redis 客户端。
  - title: Lock-free · 无锁
    details: One atomic read and at most one compare-and-set per request. 每次请求一次原子读、最多一次 CAS，快路径上没有互斥锁。
  - title: No background threads · 无后台线程
    details: The rate is derived from System.nanoTime() on demand, so an idle limiter costs nothing. 请求到来时现算速率，空闲不限流器不占 CPU。
  - title: Six algorithms · 六种算法
    details: Token bucket, leaky bucket, sliding window, fixed window, warm-up and a concurrency bulkhead. 令牌桶、漏桶、滑动窗口、固定窗口、预热限流、并发数限流。
---

## Six algorithms, one interface

<img src="/algorithms.svg" width="880" alt="How each limiter admits the same configured rate">

Same configuration, six admission patterns: a burst then steady, evenly paced, a rolling window that
refuses the boundary trick, a fixed window that hands out twice the rate across a clock tick, a cold
start that ramps up, and a bulkhead that ignores the clock. 同一配置、六种放行形态。

## Two documentation languages

- 🇺🇸 [English documentation](/en/) — introduction, guide, algorithms, API reference and FAQ.
- 🇨🇳 [中文文档](/zh-CN/) — 简介、使用指南、算法原理、API 参考与常见问题。

## Install

```xml
<dependency>
    <groupId>io.github.onblog</groupId>
    <artifactId>java-limiter</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
RateLimiter limiter = RateLimiters.tokenBucket("api", 200, 100, Duration.ofSeconds(1));

if (limiter.tryAcquire()) {
    handleRequest();
} else {
    response.setStatus(429);
}
```
