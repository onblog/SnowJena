# Changelog

All notable changes to this project are documented in this file.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses
[Semantic Versioning](https://semver.org/).

## [1.0.0] — 2026-10-21

First release: one module, one jar, zero third-party runtime dependencies.

### Added

- `RateLimiter` interface with `tryAcquire()`, atomic multi-permit `tryAcquire(int)`, parking
  `tryAcquire(int, long, TimeUnit)`, `availablePermits()` and `name()`.
- `TokenBucketLimiter` — average rate with a configurable burst, implemented with lock-free virtual
  scheduling (GCRA): one `AtomicLong` and one compare-and-set per request, no refill thread.
- `LeakyBucketLimiter` — strictly even pacing that never accumulates a burst.
- `SlidingWindowLimiter` — at most N permits in any rolling window, 10 slots by default, fail-closed
  under contention so the limit is never exceeded.
- `FixedWindowLimiter` — whole state in one `AtomicLong`, the cheapest implementation.
- `RateLimiters` factory with per-second convenience overloads.
- 37 JUnit 5 tests, including concurrency tests that assert a limit holds when many threads race for
  the last permits.
- Bilingual documentation site (`docs/en`, `docs/zh-CN`) published at <https://limiter.wordtk.com>.

### Notes

- Bytecode target is Java 8 (class file version 52) with
  `Automatic-Module-Name: io.github.onblog.limiter`, so the same artifact runs on Java 8 through 25 on
  both the classpath and the module path.
- Limiters are thread-safe, allocate nothing per call, start no threads, and keep no state beyond a
  few `long`s.
