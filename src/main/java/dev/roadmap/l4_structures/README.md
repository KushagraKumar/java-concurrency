# Level 4: Concurrent Data Structures

## Focus
Building complex thread-safe structures and understanding the trade-offs of library implementations.

## Problems
- **P3.4 — Bounded Blocking Queue**
    - Building a canonical producer-consumer queue using `Object.wait()`.
    - Comparison with `ArrayBlockingQueue` internals.
- **P4.1 — Read-Heavy Leaderboard**
    - Implementing read-write separation with `ReentrantReadWriteLock`.
    - Comparing with `StampedLock` optimistic reads.
- **P4.2 — Concurrent LRU Cache**
    - Managing state mutation in a cache.
    - Trade-offs between `LinkedHashMap + synchronized` and deferred bookkeeping (Caffeine).

## Key Concepts
- Bounded buffers & two-condition wait
- Read-Write separation and locking granularity
- Optimistic concurrency control
- Scalability vs. Simplicity in cache design
