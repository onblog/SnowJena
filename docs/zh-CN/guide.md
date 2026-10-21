# 使用指南

## 环境要求

Java 8 及以上。库没有运行时依赖，你的框架、日志栈、JSON 库是什么版本都无所谓。

## 引入

::: code-group

```xml [Maven]
<dependency>
    <groupId>io.github.onblog</groupId>
    <artifactId>java-limiter</artifactId>
    <version>1.0.0</version>
</dependency>
```

```groovy [Gradle]
implementation 'io.github.onblog:java-limiter:1.0.0'
```

:::

## 创建限流器

**务必给限流器起名字**：名字会出现在 `toString()` 里，也是你接监控指标时要打的标签。

```java
import io.github.onblog.limiter.*;
import java.time.Duration;

// 每秒 100 次，允许 100 次的突发
RateLimiter perSecond = RateLimiters.tokenBucket("api", 100);

// 每秒 100 次，允许 250 次的突发
RateLimiter bursty = RateLimiters.tokenBucket("api", 250, 100, Duration.ofSeconds(1));

// 每 10ms 放行一个，严格平滑
RateLimiter smooth = RateLimiters.leakyBucket("upstream", 100);

// 任意滚动的一分钟内最多 600 次
RateLimiter rolling = RateLimiters.slidingWindow("login", 600, Duration.ofMinutes(1));

// 每个对齐的一分钟内最多 600 次，最省
RateLimiter aligned = RateLimiters.fixedWindow("login", 600, Duration.ofMinutes(1));
```

四种实现都实现了同一个 `RateLimiter` 接口，换算法不用改调用处。

## 一个被保护对象一个限流器

限流器实例本身就是计数器。每个被保护的东西各给一个实例——支付接口、登录接口、每个下游主机各一个——并存到应用生命周期结束。**每次请求都 new 一个限流器等于没限流**，因为计数器被重置了。

```java
public final class Limiters {

    private static final RateLimiter PAYMENTS = RateLimiters.tokenBucket("payments", 200, 100, Duration.ofSeconds(1));
    private static final RateLimiter SEARCH = RateLimiters.slidingWindow("search", 60, Duration.ofMinutes(1));

    private Limiters() {
    }
}
```

## 处理被限流

`tryAcquire()` 返回 `false` 表示这次请求不该继续。它不抛异常也不打日志——怎么处理是调用方的事：

```java
if (limiter.tryAcquire()) {
    handleRequest();
} else {
    response.setStatus(429);                                  // HTTP
    response.setHeader("Retry-After", "1");
}
```

常见的处理方式：返回 `429`、返回 `Optional.empty()`、返回降级值，或者把消息重新放回队列。

## 一次申请多个许可

一次请求消耗多个许可时（批量、重查询、批量写），应该**原子地**把整个成本一次申请完：要么全部给，要么一个都不给，绝不会出现"请求执行了一半"：

```java
int cost = items.size();
if (limiter.tryAcquire(cost)) {
    process(items);
}
```

大于限流器上限的单次请求永远不可能成功，所以限流上限至少要配到单次请求的最大成本之上。

## 等待而不是拒绝

当"背压"比"拒绝"更合适时，可以有界地等待一段时间。等待的线程会被 park（不会空转），每毫秒醒来重试一次：

```java
if (limiter.tryAcquire(1, 200, TimeUnit.MILLISECONDS)) {
    callUpstream();
} else {
    // 200ms 后仍然不允许
}
```

等待期间被中断会抛 `InterruptedException` 并清除中断标志，所以你在 `catch` 里写 `Thread.currentThread().interrupt()` 的标准写法依然有效。

## 读取余量做监控

`availablePermits()` 是"此刻还能放行多少"的快照，用于看板和自适应逻辑，**绝不能用来做限流判断**——读到值和真正动手之间，许可可能已经被别的线程拿走了：

```java
metrics.gauge("limiter.available", Tags.of("limiter", limiter.name()), limiter, l -> l.availablePermits());
```

## Spring Boot：一个限流器一个 Bean

```java
@Configuration
public class LimiterConfig {

    @Bean
    public RateLimiter paymentLimiter() {
        return RateLimiters.slidingWindow("payments", 600, Duration.ofMinutes(1));
    }
}
```

```java
@Service
public class PaymentService {

    private final RateLimiter limiter;

    public PaymentService(@Qualifier("paymentLimiter") RateLimiter limiter) {
        this.limiter = limiter;
    }

    public void pay(Payment payment) {
        if (!limiter.tryAcquire()) {
            throw new TooManyRequestsException("payments");
        }
        // ...
    }
}
```

## Spring MVC：在拦截器里拦

```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter limiter = RateLimiters.tokenBucket("http", 500);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (limiter.tryAcquire()) {
            return true;
        }
        response.setStatus(429);
        return false;
    }
}
```

Servlet 的 `Filter` 里写法一样，见 [README](https://github.com/onblog/java-limiter/blob/master/README.zh-CN.md#常用姿势)。

## 按 key 限流

这个库限的是"资源"，一个限流器只有一个计数器。要按用户、按 IP、按租户限流，就自己用 `ConcurrentHashMap` 维护"每个 key 一个限流器"，并在不再需要时回收：

```java
private final ConcurrentMap<String, RateLimiter> perUser = new ConcurrentHashMap<>();

public boolean allowed(String userId) {
    return perUser.computeIfAbsent(userId, id -> RateLimiters.slidingWindow("user:" + id, 30, Duration.ofMinutes(1)))
            .tryAcquire();
}
```

这张 map 是全库唯一会**无界增长**的地方，因为 key 集合就是你的业务数据。用你已有的手段回收空闲 key（Caffeine、定时扫描、LRU 上限）。

## 怎么测限流器

限流器只能靠真实时间去测——这里**故意不提供可注入的时钟**，免得某个测试把一个"根本不看时间"的限流器测成通过。时间配小一点：

```java
RateLimiter limiter = RateLimiters.tokenBucket("test", 3, 100, Duration.ofSeconds(1)); // 3 突发，每 10ms 一个
assertTrue(limiter.tryAcquire(3));
assertFalse(limiter.tryAcquire());
Thread.sleep(35);
assertTrue(limiter.tryAcquire());
```

窗口建议配到几百毫秒以上，断言用区间而不是精确值，因为线程什么时候真的醒来由操作系统决定。
