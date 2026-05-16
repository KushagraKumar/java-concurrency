# Level 6: Execution Models

## Focus
Tuning the thread management layer and leveraging modern async/parallel patterns.

## Problems
- **P6.1 — Custom ThreadPoolExecutor**
    - Configuring core/max sizes, bounded queues, and rejection policies.
    - Observing backpressure and thread creation rules.
- **P6.2 — Virtual Threads & Structured Concurrency**
    - Migrating to high-throughput lightweight threads (JDK 21).
    - Implementing failure propagation with `StructuredTaskScope`.
- **P6.3 — ThreadLocal & Memory Leaks**
    - Reproducing leaks in thread pools and fixing with `remove()`.
    - Understanding Scoped Values vs. InheritableThreadLocal.
- **P6.4 — CompletableFuture Pipeline**
    - Composing asynchronous tasks without blocking common pools.
- **P6.5 — ForkJoin and Work Stealing**
    - Implementing parallel divide-and-conquer (Merge Sort).
    - Tuning sequential cutoffs.

## Key Concepts
- Thread pool sizing and backpressure
- Carrier thread pinning (Virtual Threads)
- Structured Concurrency discipline
- ThreadLocal lifecycle in pools
- Work-Stealing deques
