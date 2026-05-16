# Level 5: Failure Modes

## Focus
Reproducing, identifying, and fixing the four canonical concurrency bugs.

## Problems
- **P5.1 — Deadlock**
    - Reproducing circular wait in bank transfers.
    - Using `jstack` and `ThreadMXBean` for detection.
    - Fixing with total lock ordering.
- **P5.2 — Liveness (Livelock & Starvation)**
    - Demonstrating "polite diner" livelock and fixing with randomized backoff.
    - Observing starvation in unfair locks and fixing with fairness.
- **P5.3 — False Sharing**
    - Demonstrating CPU cache invalidation overhead with adjacent fields.
    - Using padding and `@Contended` to increase throughput.
- **P5.4 — Lock-Free Stack (Treiber Stack)**
    - Implementing a stack using only `AtomicReference` and CAS.
    - Understanding the ABA problem and GC-based mitigation.

## Key Concepts
- Coffman Conditions for deadlock
- Livelock (RUNNABLE but no progress)
- Starvation and Lock Fairness (Barging)
- Cache Lines and MESI Protocol
- Lock-Free algorithms and Retries
