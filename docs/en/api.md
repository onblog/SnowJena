# API reference

Everything lives in one package: `io.github.onblog.limiter`. The jar sets
`Automatic-Module-Name: io.github.onblog.limiter` for the module path.

## `RateLimiter` (interface)

```java
public interface RateLimiter {
    String name();
    int availablePermits();
    default boolean tryAcquire();
    boolean tryAcquire(int permits);
    default boolean tryAcquire(int permits, long timeout, TimeUnit unit) throws InterruptedException;
}
```

| Method | Semantics |
| --- | --- |
| `name()` | The name given at construction time, for logs and metrics. |
| `availablePermits()` | Snapshot of the permits that could be granted right now, `0` to the configured maximum. Reporting only — never use it to enforce a limit. |
| `tryAcquire()` | Grants one permit, equivalent to `tryAcquire(1)`. |
| `tryAcquire(int)` | Grants all requested permits or none. Returns `false` when the request must be rejected. Throws `IllegalArgumentException` if `permits < 1`. |
| `tryAcquire(int, long, TimeUnit)` | Parks the thread until the permits are granted or the timeout elapses. Throws `InterruptedException` if interrupted (and clears the flag). |

A rejection is a normal outcome: `false` is returned without logging and without an exception.

## `RateLimiters` (factory)

```java
public final class RateLimiters {
    TokenBucketLimiter  tokenBucket(String name, int permitsPerSecond);
    TokenBucketLimiter  tokenBucket(String name, int capacity, int permitsPerWindow, Duration window);
    LeakyBucketLimiter  leakyBucket(String name, int permitsPerSecond);
    LeakyBucketLimiter  leakyBucket(String name, int permitsPerWindow, Duration window);
    SlidingWindowLimiter slidingWindow(String name, int maxPermits, Duration window);
    FixedWindowLimiter  fixedWindow(String name, int maxPermits, Duration window);
}
```

The one-argument overloads for the bucket limiters mean "per second"; the first overload of
`tokenBucket` uses `capacity == permitsPerSecond`.

## `TokenBucketLimiter`

```java
new TokenBucketLimiter(String name, int capacity, int permitsPerWindow, Duration window);
int capacity();
```

Average rate of `permitsPerWindow` per `window`, with bursts of up to `capacity` permits. `capacity`
is also the largest number of permits one request may ask for.

## `LeakyBucketLimiter`

```java
new LeakyBucketLimiter(String name, int permitsPerWindow, Duration window);
int permitsPerWindow();
```

Grants one permit every `window / permitsPerWindow`, evenly spaced, with no burst. A request for two
or more permits is always rejected, because the bucket holds a single permit.

## `SlidingWindowLimiter`

```java
public static final int DEFAULT_SLOTS = 10;

new SlidingWindowLimiter(String name, int maxPermits, Duration window);
new SlidingWindowLimiter(String name, int maxPermits, Duration window, int slotCount);
int maxPermits();
```

At most `maxPermits` in any rolling `window`. `slotCount` (default 10) is the precision/memory knob:
the window is divided into that many buckets, so precision is `window / slotCount`.

## `FixedWindowLimiter`

```java
new FixedWindowLimiter(String name, int maxPermits, Duration window);
int maxPermits();
```

At most `maxPermits` per aligned `window`, where a window starts at a multiple of its length measured
from an arbitrary fixed origin. Cheapest implementation; may allow up to twice the limit across a
window boundary.

## Exceptions

| Exception | When |
| --- | --- |
| `IllegalArgumentException` | A constructor argument is not positive, the name is empty, the window is zero/negative/null, or `tryAcquire(permits)` is called with `permits < 1`. The message names the offending argument, for example `capacity must be > 0 but was 0`. |
| `InterruptedException` | The blocking `tryAcquire(permits, timeout, unit)` was interrupted. The interrupt flag is cleared, so re-interrupt in your `catch` block if you do not propagate. |

Rejections never throw. No method logs.

## Thread safety

Every implementation is immutable in configuration and mutates only atomics, so instances are safe to
share across threads without synchronisation and safe to publish through a `static final` field or a
container bean.

A limiter is not `Serializable` and cannot be reconstructed from its state: it is a runtime object, not
configuration.

## Cost per call

| Limiter | Fast path |
| --- | --- |
| `TokenBucketLimiter`, `LeakyBucketLimiter` | 1 atomic read, 1 CAS |
| `FixedWindowLimiter` | 1 atomic read, 1 CAS |
| `SlidingWindowLimiter` | `slotCount` atomic reads, 1 CAS, plus one extra read for the rollback check |

No method allocates on the accepted or rejected path (a rejected blocking call allocates nothing
either; the blocking call parks the thread).
