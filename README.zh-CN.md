<p align="center">
  <img src="assets/logo.svg" width="96" height="96" alt="java-limiter logo">
</p>

<h1 align="center">java-limiter · 疾限</h1>

<p align="center"><strong>Java 8+ 无锁限流库：一个 jar、六种算法、零运行时依赖。</strong></p>

<p align="center">
  <a href="README.md">🇺🇸 English</a> ·
  <a href="README.zh-CN.md">🇨🇳 中文</a>
</p>

<p align="center">
  <code>Java 8+</code> · <code>零依赖</code> · <code>18 KB jar</code> · <code>Apache-2.0</code> · <code>58 个单测</code>
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

- 🇨🇳 中文：<https://limiter.wordtk.com/zh-CN/>
- 🇺🇸 English: <https://limiter.wordtk.com/en/>

## 本地构建

```bash
mvn clean verify          # 编译 + 58 个单测 + 打包
mvn -P release package    # 额外生成 sources/javadoc 并签名
```

## 许可

[Apache License 2.0](LICENSE)
