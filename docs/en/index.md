---
layout: home

hero:
  name: java-limiter
  text: Lock-free rate limiting for Java 8+
  tagline: One 18 KB jar, six algorithms, zero runtime dependencies, no background threads.
  image:
    src: /logo.svg
    alt: java-limiter
  actions:
    - theme: brand
      text: Get started
      link: /en/guide
    - theme: alt
      text: GitHub
      link: https://github.com/onblog/java-limiter

features:
  - title: Zero dependencies
    details: Only the JDK. No framework, no logging binding, no JSON library, no Redis client to exclude from your build.
  - title: Lock-free
    details: One atomic read and at most one compare-and-set per request. No mutex, no blocking, no allocation on the fast path.
  - title: No background threads
    details: The rate is derived from System.nanoTime() when a request arrives, so an idle limiter costs no CPU and never grows.
  - title: Six algorithms
    details: Token bucket, leaky bucket, sliding window, fixed window, warm-up and a concurrency bulkhead behind one small interface, each with its trade-off documented.
---

## Install

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

## First limiter

```java
import io.github.onblog.limiter.RateLimiter;
import io.github.onblog.limiter.RateLimiters;

import java.time.Duration;

// 100 requests per second on average, bursts of up to 200.
RateLimiter limiter = RateLimiters.tokenBucket("payments", 200, 100, Duration.ofSeconds(1));

if (limiter.tryAcquire()) {
    handleRequest();
} else {
    response.setStatus(429);
}
```

A rejected request is not an error: `tryAcquire` returns `false`, nothing is logged and nothing is thrown.

## Pick an algorithm

| Limiter | Behaviour | Use it for |
| --- | --- | --- |
| `TokenBucketLimiter` | Average rate plus a burst budget that refills while idle | Public APIs, protecting a dependency that tolerates bursts |
| `LeakyBucketLimiter` | Strictly even pacing, never accumulates a burst | Outbound calls, MQ consumption, anything that must be smooth |
| `SlidingWindowLimiter` | At most N permits in any rolling window | Login attempts, per-minute or per-hour quota with no exploitable boundary |
| `FixedWindowLimiter` | At most N permits per aligned window, one `AtomicLong` | Very high traffic with loose accuracy needs |
| `WarmUpTokenBucketLimiter` | Starts at a third of the rate, ramps up over a warm-up period | Services that are cold right after start-up |
| `ConcurrencyLimiter` | Caps permits **in flight**, not permits per second | Protecting thread pools, connection pools, slow downstreams |

Read [Algorithms](/en/algorithms) for how each one works and what it guarantees under contention.

## 中文文档

The full documentation is also available in Chinese: [简体中文](/zh-CN/).
