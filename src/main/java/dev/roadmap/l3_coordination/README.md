# Level 3: Coordination Primitives

## Focus
Learning how threads communicate and coordinate work using signaling and barriers.

## Problems
- **P3.1 — Single-Slot Mailbox**
    - Implementing a thread-safe handoff using `wait()` and `notifyAll()`.
    - Handling spurious wakeups and `InterruptedException`.
- **P3.2 — Semaphore-Based Connection Pool**
    - Managing a limited resource pool with permits.
    - Understanding fairness and timed acquisition.
- **P3.3 — Multi-Phase Coordination**
    - Using `CyclicBarrier` for fixed-participant phases.
    - Using `Phaser` for dynamic participant arrival/departure.

## Key Concepts
- Monitor wait/notify semantics
- Predicate loops (`while` vs `if`)
- Condition queues
- Resource counting with `Semaphore`
- Barrier and Phase coordination
