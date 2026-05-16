# Level 7: System Design

## Focus
Applying concurrency primitives to common high-level system patterns.

## Problems
- **P7.1 — Token Bucket Rate Limiter**
    - Implementing rate limiting with controlled bursts.
    - Using lazy evaluation (clock-based replenishment) to avoid timer threads.
- **P7.2 — Producer-Consumer Pipeline**
    - Multi-stage processing with bounded-queue backpressure.
    - Graceful shutdown and poison pills.
- **P7.3 — Publish-Subscribe Event Bus**
    - Implementing safe `unsubscribe` with happens-before lifecycle guarantees.
    - Efficient dispatch using `CopyOnWriteArrayList`.

## Key Concepts
- Rate limiting vs. Concurrency limiting
- Monotonic vs. Wall clocks
- Pipeline bottleneck analysis (Amdahl's Law)
- Async event dispatch and lifecycle management
