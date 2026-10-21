# FAQ

## Is this a distributed rate limiter?

No. One limiter is one counter in one JVM, so with three instances "100 per second" means 100 per
second per instance. That is the right behaviour for protecting a process's own resources, and it is
the reason the library has no Redis client, no network call and no configuration server.

For a cluster-wide limit, combine this limiter with a shared counter: the local limiter absorbs bursts
with no latency, and a Redis `INCR` + `PEXPIRE` (or a gateway limit) provides the global ceiling.
Version 4.x shipped a Redis-backed rule server for exactly that; it was removed in 5.0.0 because it
tripled the dependency footprint for a small minority of users.

## How is this different from Guava's `RateLimiter`?

| | java-limiter | Guava `RateLimiter` |
| --- | --- | --- |
| Dependencies | none | Guava |
| Locking | lock-free CAS | `synchronized` on an internal mutex |
| Permits argument | `int` | `int` (stored permit), `double` (permits per second) |
| Rolling-window limit | yes (`SlidingWindowLimiter`) | no |
| Alignment / fixed window | yes | no |
| Introspection | `availablePermits()` | none |

Guava's is a smooth rate limiter; here that role belongs to `LeakyBucketLimiter`, and the token bucket,
sliding window and fixed window variants are additions. If Guava is already on your classpath and you
only need smooth pacing, Guava is a reasonable choice. If you want no dependency, no mutex, and a
window-based limit, this library covers it.

## Why is there no logging and no metrics integration?

Both would need a dependency (`slf4j-api`, `micrometer-core`) and a configuration surface. Instead every
limiter carries a name, and you wire metrics in one line:

```java
metrics.gauge("limiter.available", Tags.of("limiter", limiter.name()), limiter, RateLimiter::availablePermits);
```

## Why no `@RateLimit` annotation or AOP support?

Annotations would require Spring or AspectJ on the classpath, and AOP proxies add indirection to a call
that is supposed to cost one atomic operation. An annotation-based facade over a `Map<String, RateLimiter>`
is a dozen lines of your own code if you want it; see
[docs/en/guide → Per-key limiting](/en/guide#per-key-limiting).

## Do I need to close a limiter? Is anything scheduled?

Nothing to close and nothing scheduled. There is no thread, no timer, no buffer and no connection. A
limiter holds a handful of `long`s and disappears with the class loader.

## What happens if the system clock changes?

Nothing. All implementations read `System.nanoTime()`, which is monotonic, so an NTP jump or a manual
clock change cannot make a limiter grant a burst or stall.

## What is the memory footprint?

| Limiter | Heap |
| --- | --- |
| `TokenBucketLimiter`, `LeakyBucketLimiter` | one `AtomicLong` |
| `FixedWindowLimiter` | one `AtomicLong` |
| `SlidingWindowLimiter` | `AtomicLongArray(slotCount)`, 10 longs by default |

The jar is about 12 KB and the whole addressable API is 7 classes.

## Does it work on Java 17, 21 and 25?

Yes. The artifact is Java 8 bytecode (class file version 52) with
`Automatic-Module-Name: io.github.onblog.limiter`, so it works on the classpath and the module path from
Java 8 through the current release, virtual threads included.

## Is the limit exact?

For `TokenBucketLimiter`, `LeakyBucketLimiter` and `FixedWindowLimiter` the admitted count is exact: the
check and the update happen in one compare-and-set. `SlidingWindowLimiter` fails closed under
contention, so it can reject a request during a microsecond of racing but never exceeds the limit.

The *rate* is as accurate as the operating system's timer. Above a few hundred permits per second that
is exact in practice; for very low rates (a few per hour) a permit may arrive one scheduling tick late.

## Why was the white/black list removed?

`tryAcquire(String user)` mixed two concerns: rate limiting and access control. It was also part of the
cloud-rule feature that was dropped. Do access control in your own code — it is a `Set.contains` call —
and keep the limiter doing one thing.

## Is 5.0.0 a breaking change?

Yes: new coordinates (`io.github.onblog:java-limiter`), new package (`io.github.onblog.limiter`), no
cloud rules, no Redis, no FastJSON. The old artifacts (`com.github.onblog:snowjena-core` 4.0.0.RELEASE)
remain on Maven Central permanently, so nothing breaks for projects that stay on 4.x. The
[migration table](https://github.com/onblog/java-limiter#migrating-from-snowjena) maps the old types to
the new ones; the core call `tryAcquire()` means the same thing it always did.

## Can I use one limiter for several endpoints?

You can, but do not: a shared limiter means traffic to one endpoint consumes the budget of the others,
and the effective limit of each endpoint drops as neighbours get busy. One limiter per protected
resource (and, with [per-key limiting](/en/guide#per-key-limiting), one map of limiters per protected
dimension).

## Found a bug or need another algorithm?

Open an issue at <https://github.com/onblog/java-limiter/issues> — Chinese or English, both are fine.
