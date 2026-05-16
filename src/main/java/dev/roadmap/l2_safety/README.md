# Level 2: Thread Safety & JMM

## Focus
Understanding visibility, atomicity, and ordering at the Java Memory Model (JMM) level.

## Problems
- **P2.0 — JMM Happens-Before Reasoning**
    - Analyzing code fragments for race conditions using JMM rules.
    - Verifying visibility failures using **JCStress**.
- **P2.1 — Thread-Safe Counter**
    - Comparing `BasicCounter`, `VolatileCounter`, `SynchronizedCounter`, and `AtomicCounter`.
    - Proving why `volatile` is insufficient for compound operations (`value++`).
- **P2.2 — Safe Publication**
    - Demonstrating how objects can be partially constructed without safe publication.
    - Using `volatile` and `final` (freeze guarantee) for safe sharing.
- **P2.3 — LongAdder vs AtomicLong**
    - Benchmarking high-contention throughput with **JMH**.
    - Understanding striped counters and false sharing mitigation.

## Key Concepts
- JMM Happens-Before Rules (Program Order, Monitor, Volatile, etc.)
- Visibility vs. Atomicity
- Safe Publication & Constructor Escapes
- CAS (Compare-And-Swap) contention
- Performance striping (`LongAdder`)
