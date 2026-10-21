---
layout: home

hero:
  name: java-limiter · 疾限
  text: Java 8+ 无锁限流库
  tagline: 一个 18 KB 的 jar、六种算法、零运行时依赖、没有后台线程。
  image:
    src: /logo.svg
    alt: java-limiter
  actions:
    - theme: brand
      text: 开始使用
      link: /zh-CN/guide
    - theme: alt
      text: GitHub
      link: https://github.com/onblog/java-limiter

features:
  - title: 零依赖
    details: 只用 JDK。没有框架、没有日志实现绑定、没有 JSON 库、没有 Redis 客户端需要排除。
  - title: 无锁
    details: 每次请求一次原子读、最多一次 CAS。快路径上没有互斥锁、不阻塞、不分配对象。
  - title: 无后台线程
    details: 请求到来时用 System.nanoTime() 现算速率，空闲的限流器不占 CPU、内存也不增长。
  - title: 六种算法
    details: 令牌桶、漏桶、滑动窗口、固定窗口、预热限流、并发数限流，统一在一个小接口后面，每个的取舍都有文档说明。
---

## 引入

```xml
<dependency>
    <groupId>io.github.onblog</groupId>
    <artifactId>java-limiter</artifactId>
    <version>1.0.0</version>
</dependency>
```

```groovy
implementation 'io.github.onblog:java-limiter:1.0.0'
```

## 第一个限流器

```java
import io.github.onblog.limiter.RateLimiter;
import io.github.onblog.limiter.RateLimiters;

import java.time.Duration;

// 平均每秒 100 次，允许最多 200 次的突发
RateLimiter limiter = RateLimiters.tokenBucket("payments", 200, 100, Duration.ofSeconds(1));

if (limiter.tryAcquire()) {
    handleRequest();
} else {
    response.setStatus(429);
}
```

被限流不是异常：`tryAcquire` 返回 `false`，既不抛异常也不打日志。

## 六种算法怎么选

| 限流器 | 行为 | 适用场景 |
| --- | --- | --- |
| `TokenBucketLimiter` | 平均速率 + 空闲时累积的突发额度 | 对外 API、保护能容忍突发的下游 |
| `LeakyBucketLimiter` | 严格匀速放行，永不累积突发 | 调用外部接口、MQ 消费等要求平滑的场景 |
| `SlidingWindowLimiter` | 任意滑动窗口内不超过 N 次 | 登录尝试、按分钟/小时计配额且边界不可被利用 |
| `FixedWindowLimiter` | 固定窗口内不超过 N 次，一个 `AtomicLong` | 流量极高、精度要求宽松 |
| `WarmUpTokenBucketLimiter` | 冷启动时只有 1/3 速率，按预热时长线性爬升 | 刚启动就承载流量的服务 |
| `ConcurrencyLimiter` | 限的是**同时在飞**数，不是每秒次数 | 保护线程池、连接池、响应慢的下游 |

每种算法怎么实现、并发下保证什么，见[算法原理](/zh-CN/algorithms)。

## English documentation

The full documentation is also available in English: [English](/en/).
