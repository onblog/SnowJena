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
    WarmUpTokenBucketLimiter warmUpTokenBucket(String name, int permitsPerSecond, Duration warmUpPeriod);
    WarmUpTokenBucketLimiter warmUpTokenBucket(String name, int capacity, int permitsPerWindow,
                                              Duration window, Duration warmUpPeriod);
    ConcurrencyLimiter  concurrency(String name, int maxConcurrency);
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

## `WarmUpTokenBucketLimiter`

```java
public static final int COLD_FACTOR = 3;

new WarmUpTokenBucketLimiter(String name, int capacity, int permitsPerWindow, Duration window,
                             Duration warmUpPeriod);
int capacity();
int permitsPerWindow();
int coldFactor();
double warmUpProgress();     // 0.0 at start-up, 1.0 once warm
```

A token bucket whose rate ramps from `1 / COLD_FACTOR` to the configured rate over `warmUpPeriod`, with
the burst budget shrinking in step. The ramp follows the age of the limiter, not its traffic: an idle
limiter still warms up. `warmUpProgress()` exposes the ramp for dashboards and tests.

## `ConcurrencyLimiter`

```java
new ConcurrencyLimiter(String name, int maxConcurrency);
int maxConcurrency();
int inFlight();
void release();
void release(int permits);
Permit tryEnter();           // null when full
Permit tryEnter(int permits); // null when not enough slots
void execute(Runnable task);
<T> T call(Callable<T> task);

interface Permit extends AutoCloseable {
    @Override void close();  // idempotent
}
```

Caps the permits held at once. Unlike the rate limiters, a permit is a slot that must be returned:
`tryAcquire()` without a matching `release()` permanently shrinks the limit, so prefer `tryEnter()` in a
try-with-resources block, or `execute()`/`call()`, which acquire and release around the task. Closing a
`Permit` twice is a no-op, so a double `close()` never inflates the limit.

`execute()` and `call()` throw `IllegalStateException` when every slot is busy; `call()` also wraps a
checked exception thrown by the task in an `IllegalStateException` with the original as its cause.

## Exceptions

| Exception | When |
| --- | --- |
| `IllegalArgumentException` | A constructor argument is not positive, the name is empty, the window is zero/negative/null, or `tryAcquire(permits)` is called with `permits < 1`. The message names the offending argument, for example `capacity must be > 0 but was 0`. |
| `InterruptedException` | The blocking `tryAcquire(permits, timeout, unit)` was interrupted. The interrupt flag is cleared, so re-interrupt in your `catch` block if you do not propagate. |
| `IllegalStateException` | `ConcurrencyLimiter.release(n)` returns more slots than are held (a double-release bug), or `execute()`/`call()` runs while every slot is busy. |

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
| `WarmUpTokenBucketLimiter` | 1 atomic read, 1 CAS (the ramp is computed from the clock) |
| `ConcurrencyLimiter` | 1 atomic read, 1 CAS |

No method allocates on the accepted or rejected path (a rejected blocking call allocates nothing
either; the blocking call parks the thread).
