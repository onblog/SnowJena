<p align="center">
  <img src="assets/logo.svg" width="96" height="96" alt="java-limiter logo">
</p>

<h1 align="center">java-limiter · 疾限</h1>

<p align="center"><strong>Java 8+ 无锁限流库：一个 jar、四种算法、零运行时依赖。</strong></p>

<p align="center">
  <a href="README.md">🇺🇸 English</a> ·
  <a href="README.zh-CN.md">🇨🇳 中文</a>
</p>

<p align="center">
  <code>Java 8+</code> · <code>零依赖</code> · <code>12 KB jar</code> · <code>Apache-2.0</code> · <code>37 个单测</code>
</p>

---

## 为什么再造一个限流器

常见的限流库会把框架、日志实现、序列化库甚至 Redis 客户端塞进你的 classpath，还有不少会专门起一个后台线程去补充令牌。java-limiter 一概不做：

- **零运行时依赖**：只用 JDK，不需要排除任何传递依赖，也不会引发版本冲突。
- **无锁**：每次请求一次原子读 + 最多一次 CAS，快路径上没有互斥锁、不会阻塞。
- **没有后台线程**：限流速率在请求到来时由 `System.nanoTime()` 现算，空闲时不占 CPU、内存也不增长。
- **每次调用零分配**：一个限流器只持有固定的几个 `long`，与流量无关。
- **Java 8 字节码**：从 Java 8 到 25 都不用改。

## 引入

```xml
<dependency>
    <groupId>io.github.onblog</groupId>
    <artifactId>java-limiter</artifactId>
    <version>5.0.0</version>
</dependency>
```

```groovy
implementation 'io.github.onblog:java-limiter:5.0.0'
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

## 四种算法怎么选

| 限流器 | 行为 | 适用场景 |
| --- | --- | --- |
| `TokenBucketLimiter` | 平均速率 + 空闲时累积的突发额度 | 对外 API、保护能容忍突发的下游 |
| `LeakyBucketLimiter` | 严格匀速放行，永不累积突发 | 调用外部接口、MQ 消费等要求平滑的场景 |
| `SlidingWindowLimiter` | 任意滑动窗口内不超过 N 次 | 登录尝试、按分钟/小时计配额且窗口边界不可被利用 |
| `FixedWindowLimiter` | 固定窗口内不超过 N 次，一个 `AtomicLong` | 流量极高、精度要求宽松 |

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

- 🇨🇳 中文：<https://limiter.wordtk.com/zh-CN/>
- 🇺🇸 English: <https://limiter.wordtk.com/en/>

## 从 SnowJena 迁移

本仓库以前叫 **SnowJena**：一个多模块限流框架，带 Redis 规则下发服务。5.0.0 是重写版本——单模块、单 jar、没有云端控制面、没有 Redis、没有 FastJSON。旧坐标会永久留在 Maven 中央仓库，继续用也不会坏。

| SnowJena 4.x | java-limiter 5.x |
| --- | --- |
| `com.github.onblog:snowjena-core` | `io.github.onblog:java-limiter` |
| `com.github.onblog.core.limiter.RateLimiterFactory` | `io.github.onblog.limiter.RateLimiters` |
| `RateLimiterRule` + builder | 构造器 / 工厂方法参数 |
| `tryAcquire()` | `tryAcquire()`（语义不变） |
| `tryAcquire(String user)`（黑白名单） | 只有 `tryAcquire()`，黑白名单请放在业务代码里 |
| 通过 `snowjena-ticket-server` + Redis 下发规则 | 已移除：单进程限流不需要服务端 |
| FastJSON、Logback、HTTP 客户端 | 无 |

黑白名单、ticket-server 与 monitor 模块是**刻意移除**的：它们构成了这个库几乎全部的依赖开销，而真正用到的用户极少。

## 本地构建

```bash
mvn clean verify          # 编译 + 37 个单测 + 打包
mvn -P release package    # 额外生成 sources/javadoc 并签名
```

## 许可

[Apache License 2.0](LICENSE)
