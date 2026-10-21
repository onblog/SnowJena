# Guide

## Requirements

Java 8 or newer. The library has no runtime dependencies, so it does not matter which framework,
logging stack or JSON library your application uses.

## Install

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

## Create a limiter

Always name a limiter: the name shows up in `toString()` and is what you pass to your metrics library.

```java
import io.github.onblog.limiter.*;
import java.time.Duration;

// 100 permits per second, bursts of up to 100
RateLimiter perSecond = RateLimiters.tokenBucket("api", 100);

// 100 permits per second, bursts of up to 250
RateLimiter bursty = RateLimiters.tokenBucket("api", 250, 100, Duration.ofSeconds(1));

// one permit every 10ms, strictly smooth
RateLimiter smooth = RateLimiters.leakyBucket("upstream", 100);

// at most 600 permits in any rolling minute
RateLimiter rolling = RateLimiters.slidingWindow("login", 600, Duration.ofMinutes(1));

// at most 600 permits per aligned minute, cheapest
RateLimiter aligned = RateLimiters.fixedWindow("login", 600, Duration.ofMinutes(1));
```

All four implement the same `RateLimiter` interface, so you can swap one for another without
touching the call sites.

## One limiter per protected resource

A limiter instance is the counter. Give each protected thing its own instance — the payment endpoint,
the login endpoint, one per downstream host — and store them for the lifetime of the application.
Creating a limiter per request resets the counter and limits nothing.

```java
public final class Limiters {

    private static final RateLimiter PAYMENTS = RateLimiters.tokenBucket("payments", 200, 100, Duration.ofSeconds(1));
    private static final RateLimiter SEARCH = RateLimiters.slidingWindow("search", 60, Duration.ofMinutes(1));

    private Limiters() {
    }
}
```

## Handle a rejection

`tryAcquire()` returns `false` when the request must not proceed. It does not throw and does not log —
the decision about what to do belongs to the caller:

```java
if (limiter.tryAcquire()) {
    handleRequest();
} else {
    response.setStatus(429);                                  // HTTP
    response.setHeader("Retry-After", "1");
}
```

Typical reactions are a `429`, an `Optional.empty()`, a fallback value, or putting the message back
on the queue.

## Acquire several permits at once

A request that costs more than one permit — a batch, a heavy query, a bulk write — asks for the whole
cost atomically. Either every permit is granted or none is, so a partially admitted request never
happens:

```java
int cost = items.size();
if (limiter.tryAcquire(cost)) {
    process(items);
}
```

A request larger than the limiter's maximum can never succeed; choose a limit that is at least the
largest single request cost.

## Wait instead of rejecting

When back pressure is better than a rejection, block for a bounded time. The waiting thread is parked
(no busy loop) and is woken every millisecond to retry:

```java
if (limiter.tryAcquire(1, 200, TimeUnit.MILLISECONDS)) {
    callUpstream();
} else {
    // still not allowed after 200ms
}
```

An interrupt while waiting throws `InterruptedException` and clears the interrupt flag, so the usual
`Thread.currentThread().interrupt()` idiom in your `catch` block keeps working.

## Read the balance for metrics

`availablePermits()` is a snapshot of what could be granted right now. It is meant for dashboards and
adaptive logic, never for enforcing a limit — another thread may consume the permits between the read
and your action:

```java
metrics.gauge("limiter.available", Tags.of("limiter", limiter.name()), limiter, l -> l.availablePermits());
```

## Spring Boot: one bean per limiter

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

## Spring MVC: reject in an interceptor

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

A plain servlet `Filter` works the same way; see the [README](https://github.com/onblog/java-limiter#common-usage).

## Per-key limiting

The library limits a resource, not a key: a limiter holds one counter. For per-user, per-IP or per-tenant
limits keep one limiter per key in a `ConcurrentHashMap` and remove entries you no longer need:

```java
private final ConcurrentMap<String, RateLimiter> perUser = new ConcurrentHashMap<>();

public boolean allowed(String userId) {
    return perUser.computeIfAbsent(userId, id -> RateLimiters.slidingWindow("user:" + id, 30, Duration.ofMinutes(1)))
            .tryAcquire();
}
```

That map is the one place where growth is unbounded, because the set of keys is your data. Evict idle
keys with any policy you already use (Caffeine, a scheduled sweep, an LRU cap).

## Testing against a limiter

Waiting for real time is the only way to test a limiter — there is deliberately no injected clock, so
a test cannot accidentally accept a limiter that ignores time. Keep the durations tiny:

```java
RateLimiter limiter = RateLimiters.tokenBucket("test", 3, 100, Duration.ofSeconds(1)); // 3 burst, 1 per 10ms
assertTrue(limiter.tryAcquire(3));
assertFalse(limiter.tryAcquire());
Thread.sleep(35);
assertTrue(limiter.tryAcquire());
```

Use windows of at least a few hundred milliseconds and assert ranges rather than exact numbers, since
the operating system decides when a sleeping thread actually resumes.
