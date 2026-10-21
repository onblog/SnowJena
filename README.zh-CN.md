<div align="center">

<img src="assets/logo.svg" width="104" alt="java-limiter 疾限 logo" />

# java-limiter · 疾限

**Java 8+ 无锁限流库：六种算法压进一个 18 KB 的 jar，零运行时依赖，没有后台线程。**

<a href="https://limiter.wordtk.com"><img alt="在线文档" src="https://img.shields.io/badge/%E2%96%B6_%E5%9C%A8%E7%BA%BF%E6%96%87%E6%A1%A3-limiter.wordtk.com-2563eb?style=for-the-badge"></a>

<br/>

<img alt="Maven Central" src="https://img.shields.io/maven-central/v/io.github.onblog/java-limiter?style=flat-square">
<img alt="许可：Apache 2.0" src="https://img.shields.io/github/license/onblog/java-limiter?style=flat-square">
<img alt="Java 8+" src="https://img.shields.io/badge/Java-8%2B-f89820?style=flat-square">
<img alt="运行时依赖：0" src="https://img.shields.io/badge/runtime_deps-0-2f7d52?style=flat-square">
<img alt="单元测试：58" src="https://img.shields.io/badge/tests-58-3f7d3e?style=flat-square">

<br/><br/>

[🇺🇸 English](README.md) &nbsp;·&nbsp; 🇨🇳 **简体中文**

<br/>

<img src="assets/algorithms.svg" width="880" alt="同一配置下六种限流器的放行差异">

</div>

> **同样是每秒 4 个许可，六种完全不同的放行形态。** 令牌桶先花掉突发再回归稳态；漏桶严格匀速；滑动窗口不给你跨越边界的机会；固定窗口在整秒处白送一倍速率；预热限流刚启动时是冷的；并发数限流根本无视时钟。一个接口、一个 jar，classpath 上不多任何东西。

---

## 目录

| | |
| --- | --- |
| [它在做什么](#它在做什么) | [引入](#引入) |
| [三行上手](#三行上手) | [六种算法怎么选](#六种算法怎么选) |
| [常用姿势](#常用姿势) | [文档](#文档) |
| [本地构建](#本地构建) | [许可](#许可) |

---

## 它在做什么

- **零运行时依赖**：只用 JDK。没有框架、没有日志实现绑定、没有 JSON 库、没有 Redis 客户端。不需要从构建里排除任何传递依赖，也不会引发版本冲突。
- **无锁**：每次请求一次原子读、最多一次 CAS。快路径上没有互斥锁、不阻塞、没有锁竞争。
- **没有后台线程**：速率在请求到来时由 `System.nanoTime()` 现算，空闲的限流器不占 CPU、不启动任何东西、内存也不增长。
- **每次调用零分配**：无论流量多大，一个限流器只持有固定的几个 `long`。
- **Java 8 字节码**：class 文件版本 52，带 `Automatic-Module-Name: io.github.onblog.limiter`；同一个产物从 Java 8 跑到 25，classpath 与 module path 都能用。
- **六种算法共用一个接口**：把令牌桶换成滚动窗口，调用处一行都不用改；而且没有任何一种算法是另一种改个名字。

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

## 三行上手

```java
import io.github.onblog.limiter.RateLimiter;
import io.github.onblog.limiter.RateLimiters;

import java.time.Duration;

// 平均每秒 100 次，允许最多 200 次的突发
RateLimiter limiter = RateLimiters.tokenBucket("payments", 200, 100, Duration.ofSeconds(1));

if (limiter.tryAcquire()) {
    handleRequest();
} else {
    response.setStatus(429);          // 触发限流
}
```

被限流不是异常：`tryAcquire` 返回 `false`，既不抛异常也不打日志。

## 六种算法怎么选

| 限流器 | 行为 | 适用场景 |
| --- | --- | --- |
| `TokenBucketLimiter` | 平均速率 + 空闲时累积的突发额度 | 对外 API、保护能容忍突发的下游 |
| `LeakyBucketLimiter` | 严格匀速放行，永不累积突发 | 调用外部接口、MQ 消费等要求平滑的场景 |
| `SlidingWindowLimiter` | 任意滑动窗口内不超过 N 次 | 登录尝试、按分钟/小时计配额且窗口边界不可被利用 |
| `FixedWindowLimiter` | 固定窗口内不超过 N 次，一个 `AtomicLong` | 流量极高、精度要求宽松 |
| `WarmUpTokenBucketLimiter` | 令牌桶，但冷启动时只有 1/3 速率，之后按预热时长线性爬升 | 刚启动就承载流量的服务：缓存是空的、代码没 JIT、Pod 刚拉起 |
| `ConcurrencyLimiter` | 限的是**同时在飞**的许可数，不是每秒次数 | 保护线程池、连接池、响应慢的下游 |

`TokenBucketLimiter` 与 `LeakyBucketLimiter` 共用同一套虚拟调度内核（GCRA），区别只在突发容忍度——不是同一份代码改个名字，详见[算法原理](https://limiter.wordtk.com/zh-CN/algorithms)。

## 常用姿势

**一次申请多个许可，要么全给要么全不给**：

```java
if (limiter.tryAcquire(5)) { ... }
```

**等待而不是拒绝**（线程会被 park，不会空转）：

```java
if (limiter.tryAcquire(1, 200, TimeUnit.MILLISECONDS)) {
    callUpstream();                   // 用背压替代 429
}
```

**限制同时处理的数量**——并发限流数的是"槽位"，用掉必须归还，try-with-resources 会自动还（任务抛异常也照样还）：

```java
ConcurrencyLimiter pool = RateLimiters.concurrency("db-pool", 20);

try (ConcurrencyLimiter.Permit slot = pool.tryEnter()) {
    if (slot == null) {
        response.setStatus(429);      // 槽位已满
        return;
    }
    queryDatabase();
}                                     // 这里自动归还
```

**启动后慢慢爬升**——t=0 时只有 1/3 速率，预热时长后到满速：

```java
RateLimiter search = RateLimiters.warmUpTokenBucket("search", 600, Duration.ofMinutes(2));
```

**Spring Boot 中注册成 Bean**：

```java
@Configuration
public class LimiterConfig {

    @Bean
    public RateLimiter apiLimiter() {
        return RateLimiters.slidingWindow("api", 600, Duration.ofMinutes(1));
    }
}
```

**Servlet 过滤器里提前拦截**：

```java
public class RateLimitFilter implements Filter {

    private final RateLimiter limiter = RateLimiters.tokenBucket("http", 500);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (limiter.tryAcquire()) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).setStatus(429);
        }
    }
}
```

更多内容见[使用指南](https://limiter.wordtk.com/zh-CN/guide)。

## 文档

| | |
| --- | --- |
| 🇨🇳 中文 | <https://limiter.wordtk.com/zh-CN/> |
| 🇺🇸 English | <https://limiter.wordtk.com/en/> |
| API 参考 | [中文](https://limiter.wordtk.com/zh-CN/api) · [en](https://limiter.wordtk.com/en/api) |
| 算法原理 | [中文](https://limiter.wordtk.com/zh-CN/algorithms) · [en](https://limiter.wordtk.com/en/algorithms) |

## 本地构建

```bash
mvn clean verify          # 编译 + 58 个单测 + 打包
mvn -P release package    # 额外生成 sources/javadoc 并签名
mvn -P release deploy     # 上传签名后的 bundle 到 Maven 中央仓库
```

## 许可

[Apache License 2.0](LICENSE) —— 可自由商用，包括闭源产品。

你可以使用、修改、嵌入、再分发本库，包括放进闭源软件里，**不需要公开你自己的源码**。Apache-2.0 是宽松许可而非 copyleft：基于它构建的东西不会继承任何开源义务。

如果你再分发（源码、jar，或打包进你的产品），需要做到：

- 保留版权声明与许可证副本；
- 如果你修改了代码，说明改了什么；
- 随附原有的 `NOTICE` 文件（本仓库没有 NOTICE，所以无需额外打包）。

该许可不授予商标权，软件不提供任何担保。

不要求签署贡献者许可协议（CLA）。提交 PR 即表示你同意以同样的 Apache-2.0 条款授权你的贡献。
