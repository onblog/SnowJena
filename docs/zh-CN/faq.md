# 常见问题

## 这是分布式限流吗？

不是。一个限流器就是**一个 JVM 里的一份计数器**，三个实例部署的话"每秒 100 次"是**每实例**每秒 100 次。对于保护进程自身资源，这正是想要的语义，也是这个库不需要 Redis、不需要网络调用、不需要配置中心的原因。

需要集群级上限时，把本库和共享计数器组合使用：本地限流器零延迟吸收突发，Redis 的 `INCR` + `PEXPIRE`（或网关层限流）给出全局天花板。

## 和 Guava 的 `RateLimiter` 有什么区别？

| | java-limiter | Guava `RateLimiter` |
| --- | --- | --- |
| 依赖 | 无 | Guava |
| 加锁 | 无锁 CAS | 内部 `synchronized` 互斥 |
| 许可参数 | `int` | `int`（存储许可）／`double`（每秒速率） |
| 滚动窗口限流 | 有（`SlidingWindowLimiter`） | 无 |
| 对齐/固定窗口 | 有 | 无 |
| 可观测 | `availablePermits()` | 无 |

Guava 那个属于"平滑限流器"，在本库里对应 `LeakyBucketLimiter`；令牌桶、滑动窗口、固定窗口是额外的能力。如果你的工程本来就有 Guava、只需要平滑限速，用 Guava 完全合理；如果你不想要额外依赖、不想要互斥锁，或者需要基于窗口的限流，就用本库。

## 为什么不打日志、不内置监控？

两者都需要依赖（`slf4j-api`、`micrometer-core`）以及配置面。这里改成了每个限流器带一个名字，接指标一行就够：

```java
metrics.gauge("limiter.available", Tags.of("limiter", limiter.name()), limiter, RateLimiter::availablePermits);
```

## 为什么没有 `@RateLimit` 注解和 AOP？

注解需要把 Spring 或 AspectJ 拉进 classpath，AOP 代理又会给本该只有一次原子操作的调用加上一层间接。如果你确实想要注解门面，在 `Map<String, RateLimiter>` 之上自己写十几行即可，见[指南 → 按 key 限流](/zh-CN/guide#按-key-限流)。

## 需要 close 吗？有定时任务吗？

不需要关闭，也没有任何调度：没有线程、没有定时器、没有缓冲、没有连接。一个限流器就持有几个 `long`，随类加载器一起消失。

## 系统时间被改了会怎样？

不会有影响。所有实现都读 `System.nanoTime()`，它是单调递增的，NTP 校时或手工改时钟既不会让限流器突然放开一波，也不会让它卡住。

## 内存占用多大？

| 限流器 | 堆占用 |
| --- | --- |
| `TokenBucketLimiter`、`LeakyBucketLimiter` | 一个 `AtomicLong` |
| `FixedWindowLimiter` | 一个 `AtomicLong` |
| `SlidingWindowLimiter` | `AtomicLongArray(slotCount)`，默认 10 个 long |
| `WarmUpTokenBucketLimiter` | 一个 `AtomicLong` 加一个预热起始时刻 |
| `ConcurrencyLimiter` | 一个 `AtomicInteger` |

jar 约 18 KB，整个对外 API 只有 9 个类。

## 能在 Java 17 / 21 / 25 上用吗？

可以。产物是 Java 8 字节码（class 文件版本 52），并声明了 `Automatic-Module-Name: io.github.onblog.limiter`，从 Java 8 到当前版本、classpath 与 module path 都能用，虚拟线程也没问题。

## 限流是精确的吗？

`TokenBucketLimiter`、`LeakyBucketLimiter`、`FixedWindowLimiter` 的放行数量是精确的：判断与更新在同一次 CAS 里完成。`SlidingWindowLimiter` 并发下失败关闭，可能在极短的竞争窗口里误拒一次，但绝不会超过上限。

至于**速率**，精度取决于操作系统计时器。每秒几百次以上在实践中是精确的；对每小时几次这种极低速率，许可可能晚一个调度刻度到达。

## 能不能几个接口共用一个限流器？

能，但不建议：共用意味着访问 A 接口的流量会吃掉 B 接口的额度，各接口的实际可用上限会随邻居的繁忙程度波动。请按"一个被保护对象一个限流器"来配；需要按用户/IP 维度限流时，用[按 key 限流](/zh-CN/guide#按-key-限流)那套 map。

## 发现 bug 或者想要别的算法？

到 <https://github.com/onblog/java-limiter/issues> 提 issue，中英文都可以。
