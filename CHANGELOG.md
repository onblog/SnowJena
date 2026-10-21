# Changelog

All notable changes to this project are documented in this file.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and the project uses
[Semantic Versioning](https://semver.org/).

## [5.0.0] — 2026-10-21

The repository was renamed from **SnowJena** to **java-limiter** and the library was rewritten as a
single-module, zero-dependency jar. Git history and stars are preserved; the old Maven coordinates stay
on Maven Central and keep working.

### Added

- `RateLimiter` interface with `tryAcquire()`, `tryAcquire(int)`, non-blocking
  `tryAcquire(int, long, TimeUnit)`, `availablePermits()` and `name()`.
- `TokenBucketLimiter` — average rate with a configurable burst, implemented with lock-free virtual
  scheduling (GCRA).
- `LeakyBucketLimiter` — strictly even pacing with no accumulated burst.
- `SlidingWindowLimiter` — at most N permits in any rolling window, 10 slots by default, fail-closed
  under contention.
- `FixedWindowLimiter` — cheapest implementation, whole state in one `AtomicLong`.
- `RateLimiters` factory with per-second convenience overloads.
- 37 JUnit 5 tests, including concurrency tests that assert a limit is never exceeded under contention.
- Bilingual documentation site (`docs/en`, `docs/zh-CN`) published at <https://limiter.wordtk.com>.

### Removed

- Multi-module layout: `snowjena-common`, `snowjena-core`, `snowjena-monitor`,
  `snowjean-spring-boot-starter`, `snowjena-ticket-server`, `spring-boot-demo`.
- The cloud control plane: Redis-backed rule distribution, heartbeat and monitor reporting, and the
  ticket server they depended on.
- Runtime dependencies: FastJSON 1.2.73, Logback, SLF4J and the HTTP client. The jar now depends on
  nothing but the JDK.
- The background `ScheduledExecutorService` that refilled tokens; the rate is now derived from
  `System.nanoTime()` on demand.
- The white/black list feature (`tryAcquire(String user)`) — access control belongs in application code.
- The `Algorithm` enum, which advertised a leaky bucket that was never implemented.

### Changed

- Coordinates: `com.github.onblog:snowjena-*` → `io.github.onblog:java-limiter`.
- Package: `com.github.onblog.*` → `io.github.onblog.limiter`.
- Bytecode target stays Java 8; `Automatic-Module-Name: io.github.onblog.limiter` added for the module
  path. Runs unchanged on Java 8 through 25.
- Fixed the misspelled package `commoon` and class name `SnowJeanException` by removing both.

## [4.0.0.RELEASE] — 2020-08-11

Last release under the SnowJena name: multi-module framework with token bucket limiting, Redis rule
distribution and a monitor module.
