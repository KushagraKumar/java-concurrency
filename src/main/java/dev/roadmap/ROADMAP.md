# Java Concurrency Staff+ Roadmap

A progressive curriculum for developing staff-level proficiency in Java concurrency.
The structure moves from primitives to failure modes to system design — the same arc
a senior engineer follows when debugging a production incident.

Each problem has three components:
- **Implement**: write the code that passes the acceptance tests
- **Break**: write a test that *demonstrates the failure mode* of the naive approach
- **Explain**: articulate why the correct approach works at the JMM / hardware level

---

## Guiding Philosophy

Staff+ concurrency proficiency is not about memorising APIs. It is about:

1. **Reasoning under uncertainty** — can you prove this is correct, not just test it?
2. **Failure mode literacy** — can you reproduce, name, and explain the bug?
3. **Design tradeoff judgment** — can you defend why you chose X over Y for workload Z?
4. **Operational thinking** — can you diagnose this in production with a thread dump?
5. **Teaching clarity** — can you explain the memory model to someone who has never heard of happens-before?

---

## Phase 1 — Thread Lifecycle

> Goal: Understand how threads start, cooperate, and stop cleanly.
> `Thread.join()` is a prerequisite, not a problem — the real exercise is cancellation.

### P1.1 — Thread Lifecycle & Cooperative Cancellation `[STUB]`

**File:** `l1_basics/L1_ThreadJoinProblem.java`

**Problem (Part A — Prerequisite):** Start 3 worker threads. The main thread must
print "All workers done" only after all 3 finish. Use `Thread.join()`.

**Problem (Part B — The Exercise):** A long-running worker performs 10,000 iterations.
Implement clean shutdown: the main thread cancels it mid-run without `Thread.stop()`,
and the worker always cleans up its resources before exiting.

**Acceptance:**
- Remove the `join()` calls in Part A and verify the test fails — this confirms
  `join()` is what establishes the guarantee, not just timing
- Worker in Part B responds to cancellation within 1 iteration
- Resources (a counter of "open resources") always reach 0 on exit, even on cancellation
- Cancellation from 5 concurrent requestors is safe

**Staff+ Lens:**
- `join()` establishes a happens-before edge: all actions in the joined thread
  happen-before the `join()` returns in the joining thread. This is a JMM guarantee,
  not a scheduling coincidence.
- `Thread.stop()` is deprecated because it releases all monitors the thread holds,
  leaving shared state in a partially-updated, inconsistent condition.
- `Thread.interrupt()` vs `volatile boolean cancelled`: interrupt integrates with
  blocking calls (`wait`, `sleep`, `join`, `LockSupport.park`) — they throw
  `InterruptedException` immediately. A `volatile` flag only helps if the thread
  checks it; it cannot unblock a sleeping thread.
- `InterruptedException` must be propagated, not swallowed. If you cannot propagate
  it (e.g., you implement `Runnable`), re-interrupt the thread:
  `Thread.currentThread().interrupt()`.

**Concepts:** `Thread.start()`, `Thread.join()`, `Thread.interrupt()`, `isInterrupted()`,
cooperative cancellation, `InterruptedException` propagation

---

## Phase 2 — Thread Safety & The Java Memory Model

> Goal: Understand visibility, atomicity, and ordering at the JMM level.
> Know what `volatile` guarantees and what it does not. Be able to draw the
> happens-before partial order for a given program.

### P2.0 — JMM Happens-Before Reasoning `[NEW]`

**Problem:** For each of the five code fragments below, determine whether the read
of `x` in the second thread is guaranteed to see the write in the first thread.
Cite the specific JMM rule that justifies your answer.

```
Fragment A: plain field, no synchronisation
Fragment B: volatile write then read of same variable
Fragment C: write before thread.start(), read inside thread
Fragment D: write inside thread, read after thread.join()
Fragment E: write in synchronized block (lock L), read in synchronized block (lock M ≠ L)
```

Then write a JCStress test for Fragment A that reliably exposes the visibility failure.

**Acceptance:**
- Correct happens-before analysis for all five fragments, citing the JMM rule
  (program order, monitor lock, volatile, thread start, thread join, or none)
- JCStress test for Fragment A compiles and produces `FORBIDDEN` outcomes, proving
  the visibility hole cannot be reliably detected with plain JUnit
- Written explanation: why does JCStress find bugs that `Thread.sleep()`-based
  tests miss?

**Staff+ Lens:**
- The six happens-before rules in the JMM (JLS 17.4.5): program order, monitor
  unlock→lock, volatile write→read, thread start, thread join, and transitivity.
  Every correct concurrent program's safety argument reduces to one of these.
- JCStress runs the test with controlled scheduling perturbations and exhausts
  the observable outcomes — it is not just "run it a lot." A standard JUnit test
  that passes is evidence of absence of detected races, not proof of correctness.
- Fragment E is the classic trap: two different locks give no ordering guarantee
  between a writer holding L and a reader holding M. Programmers frequently assume
  "any `synchronized` block is enough."

**Concepts:** JMM happens-before rules, JCStress, reordering, visibility, program order

**Tooling:** Requires `org.openjdk.jcstress:jcstress-core`. See Tooling Reference section.

---

### P2.1 — Thread-Safe Counter `[STUB]`

**File:** `l2_safety/L2_ThreadSafeCounter.java`

**Problem:** Implement a counter that reaches exactly 1,000,000 under
10 threads × 100,000 increments. Provide and benchmark four variants:

| Variant | Mechanism |
|---|---|
| `BasicCounter` | Unsynchronised (baseline / broken) |
| `VolatileCounter` | `volatile int` (broken — demonstrates the trap) |
| `SynchronizedCounter` | `synchronized` method |
| `AtomicCounter` | `AtomicInteger.incrementAndGet()` |

**Acceptance:**
- `BasicCounter` and `VolatileCounter` are *expected to produce wrong results*.
  Run each 50 times using JCStress or repeated `ConcurrentTestHarness` runs;
  assert that at least one run produces a count less than 1,000,000. A single run
  is not a reliable assertion — lucky scheduling can produce the correct value.
- `SynchronizedCounter` and `AtomicCounter` must reach exactly 1,000,000
  in every run
- `get()` must also be synchronised in the `SynchronizedCounter` variant
- Written explanation: why does leaving `get()` unsynchronised break correctness
  even when `increment()` is synchronised?

**Staff+ Lens:**
- `volatile` guarantees that each individual load and each individual store is
  visible to other threads and not reordered with other `volatile` accesses.
  What it does not do is make `value++` atomic. `value++` is a non-atomic
  read-modify-write: read value, compute value+1, write back. Two threads can
  both read the same value before either writes back — the second write silently
  overwrites the first. No amount of visibility guarantee fixes this; you need
  atomicity of the entire read-modify-write.
- `synchronized` establishes a happens-before between the unlock of one thread
  and the lock acquisition of the next. This covers both visibility (no stale reads
  after locking) and atomicity (only one thread executes the critical section).
- An unsynchronised `get()` has no happens-before with any `increment()` call.
  A thread calling `get()` may see any value the hardware has cached, including
  values written by threads before the current execution began.

**Concepts:** visibility vs atomicity, happens-before, monitor semantics, `AtomicInteger`

---

### P2.2 — Safe Publication `[NEW]`

**Problem:** A configuration object `AppConfig` is constructed once in a background
thread and read by many worker threads. Demonstrate three publication strategies:

1. Plain field — broken
2. `volatile` reference — safe
3. Immutable object with `final` fields — safe

**Acceptance:**
- A JCStress test for the plain-field case produces `FORBIDDEN` outcomes (reader
  observes null fields on a non-null reference), demonstrating partial construction
- `volatile` and `final` variants: JCStress produces no `FORBIDDEN` outcomes
- The `final`-field variant must not allow `this` to escape from the constructor
  (passing `this` to another thread inside the constructor defeats the guarantee —
  demonstrate this case and explain why it breaks)
- Written explanation: what does "freeze" mean in the JMM?

**Staff+ Lens:**
- Without a safe-publication mechanism, the JMM allows the JIT or hardware to
  reorder the store to the reference with stores to its fields. A reader can
  observe a non-null reference pointing at an object whose fields still read as
  their default values (0, null, false).
- `final` fields get a special freeze guarantee: after the constructor completes,
  all threads that subsequently read the reference are guaranteed to see the
  fully-initialised `final` fields — but *only if the reference did not escape
  from the constructor before it finished*. Passing `this` to another thread or
  storing it in a static field inside the constructor voids the guarantee.
- This is why `String`, `Integer`, and Java records are safely shareable without
  additional synchronisation — all their fields are `final`, and their constructors
  do not leak `this`.

**Concepts:** safe publication, `final` field freeze, `this`-escape, `volatile` reference, JMM

---

### P2.3 — LongAdder vs AtomicLong Under Contention `[NEW]`

**Problem:** Benchmark a high-throughput counter at 64 threads × 1,000,000
increments using `AtomicLong` and `LongAdder`. Report throughput (ops/sec) for both.

**Acceptance:**
- `LongAdder.sum()` produces the correct total
- JMH benchmark (see Tooling Reference) shows measurable throughput advantage
  for `LongAdder` under high contention — report the ratio; do not assume a
  specific multiplier as the result is hardware-dependent
- Written explanation: why does striping win? Under what access pattern does
  `LongAdder` *not* win?

**Staff+ Lens:**
- `AtomicLong` uses a single CAS loop. Under high contention, threads spin
  retrying the CAS. This is a form of serialisation — effectively, threads queue
  to update one memory location.
- `LongAdder` starts with a single `base` field (used under low contention) and
  lazily allocates a dynamically-growing table of `Cell` objects when CAS contention
  is detected. It uses a thread-local probe hash to select a cell, so threads
  typically write to different cells. `sum()` adds `base` plus all cells — the
  read-time aggregation cost is the tradeoff for write-time parallelism.
  Each `Cell` uses `@Contended` padding to prevent false sharing between adjacent cells.
- `LongAdder` is the right tool when you increment far more often than you read.
  If reads and writes are equally frequent, the aggregation cost of `sum()` erases
  the write-side gain.

**Concepts:** `LongAdder`, striped counters, false sharing, `@Contended`, CAS contention

**Tooling:** Benchmark with JMH. See Tooling Reference section.

---

## Phase 3 — Coordination Primitives

> Goal: Learn the building blocks for threads that must cooperate, not just
> avoid interfering with each other.

### P3.1 — Single-Slot Mailbox `[STUB]`

**File:** `l3_coordination/L3_SingleSlotMailbox.java`

**Problem:** Implement a thread-safe mailbox that holds exactly one message.
- `put(T msg)`: blocks if full, then stores the message and wakes a waiter
- `take()`: blocks if empty, then retrieves the message and wakes a waiter

**Acceptance:**
- Correct under 10 producer threads and 10 consumer threads (no messages lost,
  no message delivered twice)
- Spurious-wakeup safe: use a predicate loop (`while`), not an `if`
- `InterruptedException` propagates correctly — never swallowed, status restored
  via `Thread.currentThread().interrupt()` if caught internally

**Staff+ Lens:**
- The predicate loop (`while (!condition) wait()`) is mandatory. The JVM spec
  explicitly permits `wait()` to return without a corresponding `notify()` —
  a spurious wakeup. An `if` check proceeds on a false condition, which causes
  data corruption (two takers both see a full mailbox, both proceed).
- `notifyAll()` is safer than `notify()` here: with one condition queue, `notify()`
  wakes exactly one thread, which may be another producer when you need a consumer.
  `notifyAll()` wakes all waiters; only the one whose predicate is true proceeds.
- `ReentrantLock` + separate `Condition` objects (`notFull`, `notEmpty`) eliminates
  the `notifyAll()` inefficiency: signal precisely the right waiters.
  This is the design `ArrayBlockingQueue` uses.

**Concepts:** `wait/notifyAll`, predicate loops, spurious wakeups, condition queues

---

### P3.2 — Semaphore-Based Connection Pool `[NEW]`

**Problem:** Implement a `ConnectionPool` that holds at most N connections.
- `acquire()`: blocks until a connection is available, then returns one
- `release(conn)`: returns the connection to the pool
- `tryAcquire(timeout, unit)`: non-blocking variant

**Acceptance:**
- Never issues more than N connections simultaneously, verified under 50 threads
- `tryAcquire` returns `false` promptly on timeout, never blocks past the deadline
- Released connections are reused, not discarded

**Staff+ Lens:**
- A `Semaphore` models a counted resource: permits represent available connections.
  It is not a mutex — the thread that acquires need not be the one that releases.
  This asymmetry is the whole point for resource pools.
- `Semaphore(N, true)` (fair) uses a FIFO queue of waiters, guaranteeing no thread
  waits indefinitely. Unfair allows barging — higher throughput, possible starvation.
- `Semaphore` vs `BlockingQueue<Connection>`: both work. `BlockingQueue` is
  preferable if you need to return a *specific* connection (e.g., for health checks
  or per-connection state). `Semaphore` is simpler if connections are fungible.

**Concepts:** `Semaphore`, fairness, `tryAcquire`, resource pools

---

### P3.3 — Multi-Phase Coordination with CyclicBarrier and Phaser `[NEW]`

**Problem:** Split a large array across N worker threads. Each worker computes
a partial sum in Phase 1. After all workers finish Phase 1, aggregate and start
Phase 2 on the result. Then add a third phase where threads can dynamically
arrive and depart.

**Acceptance:**
- Correct result for N=1, 4, 8, 16 threads
- Phase 1 and Phase 2 use `CyclicBarrier` (demonstrate reuse across phases)
- Phase 3 uses `Phaser` with `register()`/`arriveAndDeregister()` to handle
  threads joining and leaving dynamically
- Written explanation: when would you use `Phaser` over `CyclicBarrier`?

**Staff+ Lens:**
- `CountDownLatch` is single-use. `CyclicBarrier` resets automatically and can
  coordinate repeated fixed-participant phases.
- The `CyclicBarrier` barrier action runs in the last thread to arrive — not a
  separate aggregator thread. This avoids an extra context switch and handoff.
- If one worker throws an exception, `BrokenBarrierException` is delivered to all
  other waiting threads. Your error handling must account for this — a broken
  barrier cannot be reset without creating a new one.
- `Phaser` generalises both `CountDownLatch` and `CyclicBarrier`: parties can
  register and deregister dynamically, and phases advance automatically when all
  registered parties arrive. Use `Phaser` when the set of participants changes
  across phases.

**Concepts:** `CyclicBarrier`, `CountDownLatch`, `Phaser`, barrier actions, phase coordination

---

### P3.4 — Bounded Blocking Queue `[STUB]`

**File:** `l4_structures/L4_BoundedBlockingQueue.java`

**Problem:** Implement a bounded blocking queue using `Object.wait()/notifyAll()`
with capacity N.
- `put(E)`: blocks when full
- `take()`: blocks when empty
- `size()`: returns current element count

**Acceptance:**
- Correct under 10 producers and 10 consumers: no items lost, no item delivered twice
- Blocks appropriately: producer blocks on full queue, consumer blocks on empty queue
- Spurious-wakeup safe (predicate loop)
- `InterruptedException` propagates, never swallowed
- Written comparison with `ArrayBlockingQueue`: when would you use yours?

**Staff+ Lens:**
- This is the canonical two-condition problem. With a single `Object` monitor there
  is one condition queue. `notifyAll()` wakes both producers and consumers — a
  newly-woken consumer may find the queue still full and wait again. This is
  correct but not efficient.
- With `ReentrantLock` + two `Condition` objects (`notFull`, `notEmpty`) you wake
  only the relevant waiters — `ArrayBlockingQueue` does exactly this. The
  improvement matters under high contention.
- `ArrayBlockingQueue` uses a circular buffer with two index pointers rather than
  `LinkedList` for cache locality: producer and consumer pointer updates are on the
  same array, which stays L1-hot.

**Concepts:** bounded queue, two-condition wait, `ArrayBlockingQueue` internals

---

## Phase 4 — Concurrent Data Structures

> Goal: Build correct concurrent structures from primitives. Understand when
> to use library types vs rolling your own.

### P4.1 — Read-Heavy Leaderboard with ReadWriteLock `[NEW]`

**File:** `l4_structures/leaderboard/`

**Problem:** Implement a thread-safe `Leaderboard` using `ReentrantReadWriteLock`.
Multiple threads read `topKPlayers()` concurrently; `addScore()` is exclusive.
`addScore` must *accumulate* scores, not replace them.

**Acceptance:**
- Concurrent `topKPlayers()` calls do not block each other
- `addScore()` is exclusive with all reads and writes
- Correct under the existing `AbstractLeaderboardTest.assertThreadSafe()` test
  (note: the current `ThreadUnsafeLeaderboard.addScore` uses `put` which *replaces*
  the score — this is a bug; the correct implementation must accumulate)
- JMH benchmark (90% reads, 10% writes, 20 threads): report throughput versus an
  equivalent `synchronized` implementation and explain the result
- Written comparison: `ReentrantReadWriteLock` vs `StampedLock` optimistic reads —
  when does each win?

**Staff+ Lens:**
- A plain `synchronized` method serialises all access — reads block other reads
  unnecessarily in a read-heavy workload.
- `ReadWriteLock` allows N concurrent readers or 1 exclusive writer, never both.
  For a 90/10 read/write split, this can significantly increase read throughput.
- `StampedLock` goes further: `tryOptimisticRead()` acquires no lock at all,
  returning a stamp. After the read, validate the stamp — if a writer intervened,
  retry under a read lock. For short read-only operations, optimistic reads can
  eliminate lock acquisition entirely on the common path.
- `ReadWriteLock` does not always win: if writes are frequent, the overhead of
  managing the read count and the write-exclusive state can exceed the gain from
  concurrent reads. Benchmark before committing.

**Concepts:** `ReentrantReadWriteLock`, `StampedLock`, optimistic reads, read-write separation

**Tooling:** Benchmark with JMH. See Tooling Reference section.

---

### P4.2 — Concurrent LRU Cache `[NEW]`

**Problem:** Implement a thread-safe LRU cache with capacity N.
- `get(K)`: return value or null; promote entry to MRU position
- `put(K, V)`: insert or update; evict LRU entry if at capacity

**Acceptance:**
- Correct single-threaded eviction order: after inserting A, B, C (capacity=2)
  and accessing A, evict B (LRU), not A
- Stress test: 20 concurrent threads each performing 1,000 `get` and `put`
  operations with random keys. Assert: `size() <= capacity` at all times;
  no `ClassCastException` or `NullPointerException`
- Written explanation of the tradeoff between `LinkedHashMap + synchronized`
  (simple, correct, non-scalable) and `ConcurrentHashMap` + deferred LRU bookkeeping
  (Caffeine's approach) — which is appropriate when?

**Staff+ Lens:**
- `LinkedHashMap` with `accessOrder=true` gives O(1) LRU eviction via an internal
  doubly-linked list that re-links on every access. But `get()` mutates state
  (the access-order list), so the entire structure must be held under a single lock.
  This is correct but serialises all access.
- `ConcurrentHashMap` alone is insufficient: `get()` followed by a separate
  "move-to-front" update is not an atomic map operation. A writer can interleave
  between the two steps.
- Caffeine decouples the LRU bookkeeping from the hot path: reads record access
  events in a ring buffer per thread; a maintenance thread drains the buffer and
  updates ordering asynchronously. `LinkedHashMap + synchronized` is the right
  default implementation; switch to Caffeine when lock contention appears on
  a profiler.

**Concepts:** `LinkedHashMap`, lock granularity, composite operations, Caffeine architecture

---

## Phase 5 — Failure Modes

> Goal: Reproduce, identify, and fix the four canonical concurrency failure categories.
> This phase is the most operationally relevant for staff+ work.
> Producing a real artefact (thread dump, JFR recording) is part of each acceptance.

### P5.1 — Deadlock: Cause, Detect, Fix `[NEW]`

**Problem:** Write a program with two bank accounts and a `transfer(from, to, amount)`
method that acquires both account locks. Construct a scenario where two concurrent
transfers deadlock. Detect it programmatically. Then fix it.

**Acceptance:**
- Broken version: `ThreadMXBean.findDeadlockedThreads()` returns non-null within
  5 seconds of starting the transfers — use this as the test assertion, not a
  wall-clock timeout
- Capture a `jstack` output of the deadlocked JVM and commit it alongside the
  broken implementation. Run `ThreadDumpAnalyzer` on it and verify it identifies
  the cycle.
- Fixed version: lock ordering (acquire locks in canonical account-ID order)
  passes 10,000 concurrent transfer pairs without deadlock
- Written explanation: why does total ordering on lock acquisition guarantee
  freedom from deadlock?

**Staff+ Lens:**
- Deadlock requires all four Coffman conditions simultaneously: mutual exclusion,
  hold-and-wait, no preemption, circular wait. Lock ordering eliminates circular
  wait — if every thread acquires locks in the same global order, no cycle can form.
- `System.identityHashCode()` as a tiebreaker when account IDs are equal: rarely
  two different objects can have the same identity hash code. Handle this with a
  "tie-breaking lock" (a third lock used only when hashes collide) as
  `java.util.concurrent.locks.ReentrantLock` itself does internally.
- `tryLock(timeout)` as an alternative: acquire the first lock, attempt the second
  with a timeout. If it times out, release the first and retry. This avoids deadlock
  but introduces livelock risk if both threads always fail simultaneously — add
  randomised backoff.
- Production detection: `jstack <pid>`, `jcmd <pid> Thread.print`, or
  `ThreadMXBean.findDeadlockedThreads()` from a management endpoint.

**Concepts:** Coffman conditions, lock ordering, `ThreadMXBean`, `jstack`, `tryLock`

**Tooling:** `jstack`, `ThreadDumpAnalyzer`, `ThreadMXBean`. See Tooling Reference section.

---

### P5.2 — Liveness Failures: Livelock & Starvation `[NEW]`

**Part A — Livelock:**

**Problem:** Two threads each yield when they detect a conflict, but they yield
simultaneously and conflict again — forever. Then fix it with randomised backoff.

**Acceptance:**
- Livelock version: a progress counter shows no advancement after 1,000,000 loop
  iterations; threads are RUNNABLE (verify with a thread dump)
- Fixed version: completes within 1 second on 100 consecutive runs
- Written explanation: why is livelock harder to diagnose than deadlock in production?

**Staff+ Lens:**
- Livelock shows 100% CPU (threads are running), unlike deadlock (threads are
  blocked). `jstack` shows RUNNABLE, not BLOCKED/WAITING — a common source of
  misdiagnosis ("threads are running, so there's no concurrency problem").
- Randomised backoff (uniform or exponential with jitter) breaks the symmetry that
  causes the conflict to repeat. This is the same technique used in Ethernet
  CSMA/CD collision avoidance and TCP retransmit timers.

---

**Part B — Starvation:**

**Problem:** 10 threads contend for a shared `ReentrantLock(false)` (unfair) in
a tight loop — each acquires the lock, increments a counter, and releases.
Measure each thread's share of total acquisitions. Show starvation. Fix it.

**Acceptance:**
- Unfair version: after 10,000 total acquisitions, at least one thread holds
  < 5% of its fair share (i.e., < 500 acquisitions); verify by recording per-thread
  counts
- Fair version (`ReentrantLock(true)`): each thread's share is within ±30% of
  its fair 10% share
- Written explanation: what is "barging" and why does the unfair lock enable it?

**Staff+ Lens:**
- With `ReentrantLock(false)`, a thread that just released the lock can immediately
  re-acquire it — *barging* — before queued waiters are scheduled. The OS scheduler
  does not run blocked threads instantly; the releasing thread can complete many
  iterations before any waiting thread gets CPU time.
- This is not a thread-priority problem (and `Thread.setPriority()` is effectively
  a no-op on Linux for non-root JVM processes — do not use thread priority to
  demonstrate starvation; it is platform-unreliable).
- Fair `ReentrantLock` maintains a strict FIFO waiter queue. A thread must go to
  the back of the queue after releasing the lock. This eliminates starvation at
  the cost of throughput: no freshly-released thread can barge in.

**Concepts:** livelock, starvation, barging, `ReentrantLock` fairness, RUNNABLE vs BLOCKED

---

### P5.3 — False Sharing `[NEW]`

**Problem:** Two threads each increment their own counter in a tight loop. Place
both counters as adjacent fields in a single class. Measure throughput. Pad them
to separate cache lines and measure again.

```java
// Before — both longs share one 64-byte cache line
class SharedCounters {
    volatile long a;   // thread A writes this
    volatile long b;   // thread B writes this
}

// After — each long on its own cache line
class PaddedCounters {
    volatile long a, p1, p2, p3, p4, p5, p6, p7;  // a + 7 padding longs = 64 bytes
    volatile long b, q1, q2, q3, q4, q5, q6, q7;
}
```

**Acceptance:**
- JMH benchmark with 2 threads: padded version shows measurably higher throughput —
  report the ratio; expect 2–10× on x86 (hardware-dependent, do not hard-code)
- `@jdk.internal.vm.annotation.Contended` variant as an alternative to manual padding
- Written explanation: what is a cache line, and why does sharing one degrade
  performance even when threads write completely independent variables?

**Staff+ Lens:**
- A cache line is typically 64 bytes. Two `volatile long` fields declared adjacent
  in a class are typically laid out consecutively in memory and will occupy the
  same cache line.
- When thread A writes `a`, the CPU must gain exclusive ownership of the cache line
  (MESI protocol: transition to Modified state). This invalidates the line in thread
  B's L1/L2 cache, even though B is only writing `b`. B must reload the line before
  its next write — a cross-core round-trip of 50–300 ns. At tight-loop speeds, this
  dominates the runtime.
- This is purely a performance issue, not a correctness issue. The MESI invalidation
  is transparent to the programmer — no race condition, no lost update. It is
  invisible until profiled.
- `LongAdder` uses `@Contended` on its `Cell` class for exactly this reason —
  each cell must be on its own cache line to achieve parallel writes.
- Note: two separate `AtomicLong` *objects* are unlikely to share a cache line
  because each heap allocation has an object header and TLAB-based placement is
  non-deterministic. Use adjacent primitive fields or `AtomicLongArray` slots to
  reliably reproduce false sharing.

**Concepts:** cache lines, MESI protocol, `@Contended`, false sharing vs true sharing

**Tooling:** Benchmark with JMH. See Tooling Reference section.

---

### P5.4 — Lock-Free Stack (Treiber Stack) `[NEW]`

**Problem:** Implement a concurrent stack using only `AtomicReference` and CAS.
No `synchronized`, no `Lock`.
- `push(T)`: prepend a new node atomically
- `pop()`: remove and return the head atomically, return `null` if empty

**Acceptance:**
- Correct under 20 concurrent pushers and 20 concurrent poppers: no items lost,
  no item returned twice, no `null` returned for a non-empty stack
- Written explanation of the ABA problem: under what conditions is ABA benign for
  this implementation, and under what conditions would it cause corruption?
- Written explanation: why might CAS-based lock-free algorithms not outperform
  locks under very high contention?

**Staff+ Lens:**
- CAS is the foundation of lock-free algorithms: atomically swap a value only if
  it currently matches an expected value. On failure, re-read and retry. This is
  a spin loop, but the loop terminates as soon as a conflicting thread makes progress.
- ABA: thread T1 reads head A. Thread T2 pops A, pushes B, pops B, pushes A again.
  T1's CAS on head succeeds (head is still A) but now head.next points to a
  different successor than T1 observed — data structure corruption.
  For a Treiber stack with GC-allocated nodes: ABA is benign *only* because GC
  guarantees that a node referenced by any in-flight operation will not be
  garbage-collected and reused for a different node while the CAS is pending.
  With node pooling (C++, or explicit recycling in Java), ABA causes real corruption
  — use `AtomicStampedReference` to add a monotonic version counter.
- Under extreme contention, all threads retry their CAS repeatedly, burning CPU.
  This CAS stampede can make lock-free slower than a `synchronized` block.
  Exponential backoff (random sleep before retry) reduces stampede at the cost of
  latency under low contention.

**Concepts:** CAS, `AtomicReference`, lock-free algorithms, ABA problem, `AtomicStampedReference`

---

## Phase 6 — Execution Models

> Goal: Understand how work is dispatched, how modern Java concurrency models
> differ, and where each model breaks down.

### P6.1 — Custom ThreadPoolExecutor `[NEW]`

**Problem:** Configure a `ThreadPoolExecutor` with:
- Core pool: 4 threads, Max pool: 16 threads
- Queue: bounded `LinkedBlockingQueue(100)`
- Rejection policy: caller-runs with a logged warning

Demonstrate the difference in behaviour between bounded and unbounded queues
under a spike of 1,000 tasks submitted in 1 second.

**Acceptance:**
- Bounded version: backpressure kicks in, no `OutOfMemoryError`, rejection policy fires
- Unbounded version (`Executors.newFixedThreadPool`): heap grows unboundedly;
  demonstrate with `-Xmx64m` and a task that allocates 100 KB per task
- Written explanation: at what point does `ThreadPoolExecutor` create a new thread
  beyond core size? Most engineers get this wrong.

**Staff+ Lens:**
- `Executors.newFixedThreadPool(N)` uses an *unbounded* `LinkedBlockingQueue` —
  a common source of production OOM errors during traffic spikes. The default
  `Executors` factory methods hide this.
- Thread creation rule (counter-intuitive): beyond core size, a new thread is
  created only when the queue is *full*, not when it is non-empty. So with a
  core=4, max=16, queue=100 pool, you will not see more than 4 threads until
  100 tasks are queued. Only then does the pool grow toward 16.
- Rejection policies: `AbortPolicy` (throws `RejectedExecutionException`),
  `CallerRunsPolicy` (the submitting thread runs the task — implicit backpressure),
  `DiscardPolicy` (silent drop), `DiscardOldestPolicy` (drops the queue head).
  `CallerRunsPolicy` is usually the safest default for rate-limited processing.

**Concepts:** `ThreadPoolExecutor`, rejection policies, backpressure, thread creation rules

---

### P6.2 — Virtual Threads & Structured Concurrency `[NEW]`

**Problem (Part A):** Rewrite the `CompletableFuture` pipeline from P6.4 using
virtual threads. Each fetch runs in its own virtual thread via
`Executors.newVirtualThreadPerTaskExecutor()`. Compare throughput and thread count
with the platform-thread version.

**Problem (Part B):** Implement the fetch-enrich-aggregate flow using
`StructuredTaskScope.ShutdownOnFailure`. If any fetch fails, all remaining fetches
are cancelled and the error is propagated to the caller.

**Acceptance:**
- Part A: virtual thread version has equivalent or better throughput with far fewer
  OS threads (verify via `jcmd <pid> Thread.print | grep "virtual"`)
- Part A: a slow fetch that `Thread.sleep()`s for 1 second does *not* block the
  carrier thread (verify: 1,000 sleeping virtual threads fit within 10 carrier
  threads using a custom scheduler or `Thread.ofVirtual().scheduler(...)`)
- Part B: `StructuredTaskScope` correctly propagates the first failure and cancels
  remaining fetches; no tasks complete after the scope closes
- Part B: demonstrate `synchronized` block pinning: a virtual thread that enters a
  `synchronized` block cannot unmount from its carrier thread. Show the fix
  (`ReentrantLock` instead of `synchronized`).

**Staff+ Lens:**
- Virtual threads (JDK 21 LTS) are cheap JVM-managed threads that unmount from
  OS ("carrier") threads on blocking operations (`I/O`, `sleep`, `Object.wait`,
  `LockSupport.park`). Creating 100,000 virtual threads is routine; creating 100,000
  platform threads is not.
- Thread pools become unnecessary for I/O-bound work with virtual threads: the
  "one thread per request" model is viable again. Use `newVirtualThreadPerTaskExecutor()`
  and let the JVM manage scheduling.
- Pinning: virtual threads cannot unmount while inside a `synchronized` block or
  a native frame. Pinning on a blocking call wastes a carrier thread. The JDK is
  migrating `synchronized` usages in the standard library (e.g., `BufferedInputStream`)
  to `ReentrantLock`. Monitor your code: `-Djdk.tracePinnedThreads=full`.
- `StructuredTaskScope` (JDK 21 Preview, JDK 25 targeting GA) enforces a
  structured concurrency discipline: a scope's lifetime is bounded by the block
  that created it. All forked tasks must complete or be cancelled before the
  scope closes. This prevents task-lifecycle leaks that plague raw
  `CompletableFuture` pipelines.

**Concepts:** virtual threads, carrier thread pinning, `StructuredTaskScope`,
structured concurrency, `synchronized` vs `ReentrantLock` for virtual threads

---

### P6.3 — ThreadLocal & Memory Leaks `[NEW]`

**Problem:** Demonstrate a `ThreadLocal` memory leak in a thread-pool context.
A `ThreadLocal<LargeObject>` is set by a task but never removed. After 10,000
tasks, show that memory is not reclaimed. Then fix it.

**Acceptance:**
- Leak version: heap usage after 10,000 tasks is proportional to thread count ×
  `LargeObject` size; GC does not reclaim the objects
- Fixed version: task always calls `threadLocal.remove()` in a `finally` block;
  heap usage is bounded and stable
- Written explanation: why does `InheritableThreadLocal` interact badly with
  virtual threads?

**Staff+ Lens:**
- `ThreadLocal` entries are stored in a `ThreadLocalMap` on the `Thread` object
  itself. When a thread-pool thread is reused for a new task, the previous task's
  `ThreadLocal` values are still present. They will not be GC'd because the live
  thread holds a strong reference chain.
- The fix is always `threadLocal.remove()` in a `finally` block at the task boundary.
  Consider wrapping `ThreadLocal` in a utility that enforces cleanup on task completion.
- `InheritableThreadLocal` copies the parent's values into child threads at creation
  time. Virtual threads are created frequently (one per request), so each inherits
  a snapshot of the parent's `InheritableThreadLocal` state. This is both a
  performance cost (copying) and a semantic hazard (stale snapshots). Prefer
  explicit context passing or `ScopedValue` (JDK 21+ preview) over
  `InheritableThreadLocal` with virtual threads.

**Concepts:** `ThreadLocal`, thread-pool memory leaks, `InheritableThreadLocal`,
`ScopedValue`, virtual thread interaction

---

### P6.4 — CompletableFuture Pipeline `[NEW]`

**Problem:** Given a list of 100 user IDs, fetch each user's profile from a
(simulated) remote service (50 ms latency), enrich each profile with their
order count from a second service, and aggregate into a summary report.

**Acceptance:**
- Sequential version: establishes baseline latency (~5,000 ms)
- Concurrent version: fetches execute concurrently (verify by recording in-flight
  count using an `AtomicInteger`; peak count must be > 1 and close to 100)
- Do not assert wall-clock completion under a specific millisecond threshold —
  that ties the test to machine speed. Assert logical concurrency instead.
- Exception in one fetch does not fail the entire pipeline — use `exceptionally`
  or `handle` with a fallback value
- Written explanation: `thenApply` vs `thenCompose` vs `thenCombine`; and which
  thread runs `thenApply`?

**Staff+ Lens:**
- `thenApply(fn)` applies `fn` synchronously. The thread that runs `fn` is
  non-deterministic: if the future is already complete when `thenApply` is called,
  `fn` runs on the calling thread; if not, `fn` runs on whatever thread completes
  the future. This non-determinism is a common source of subtle bugs when `fn`
  is blocking or has thread-affinity requirements.
- `thenCompose(fn)` is flatMap: `fn` returns a `CompletableFuture<U>`, and
  `thenCompose` unwraps it. Use it whenever the next step is itself asynchronous.
  `thenApply` on an async step gives `CompletableFuture<CompletableFuture<U>>`.
- `thenApplyAsync(fn, executor)` pins execution to the given executor.
  Never block inside `thenApply` on the common `ForkJoinPool` — a blocking `fn`
  holds a common-pool thread, starving `parallelStream()` across the JVM.
- `CompletableFuture.allOf()` creates a barrier but does not short-circuit on
  failure. Use `exceptionally` or `handle` per stage, or compose with
  `whenComplete`.

**Concepts:** `CompletableFuture`, `thenCompose`, `allOf`, async vs sync stages, pool starvation

---

### P6.5 — ForkJoin and Work Stealing `[NEW]`

**Problem:** Implement parallel merge sort using `RecursiveTask<int[]>` and
`ForkJoinPool`. Compare with sequential merge sort and `parallelStream()`.

**Acceptance:**
- Correct sort output for random arrays of 1M elements
- JMH benchmark: ForkJoin version is faster than sequential for arrays > 100,000
  elements — report the speedup and the sequential cutoff you chose
- `parallelStream()` version produces the same result with less code — explain
  when `parallelStream()` is preferable and when it is not
- Written explanation: what is work stealing?

**Staff+ Lens:**
- Work stealing: each thread has a deque. Threads push/pop their own tasks LIFO
  from one end. When idle, a thread steals from the *other* end of another thread's
  deque (FIFO). This minimises contention on the victim's hot end and balances
  work under uneven task sizes without a central queue.
- `ForkJoinPool.commonPool()` is shared JVM-wide. Submitting blocking tasks to it
  starves `parallelStream()` on other threads. Use a dedicated `ForkJoinPool` for
  blocking or long-running work.
- Sequential cutoff: below a threshold (benchmark to find yours, typically 512–4096
  elements for sort), recursion overhead exceeds the parallelism gain. Always
  benchmark on representative data before committing to a cutoff.
- `parallelStream()` is preferable when the task is stateless, the overhead of
  `RecursiveTask` setup is not justified, and you do not need to control the pool.
  Use `ForkJoinPool` directly when you need a custom pool, custom stealing
  parallelism, or `ManagedBlocker` for blocking tasks.

**Concepts:** `ForkJoinPool`, `RecursiveTask`, work stealing, common pool pitfalls

**Tooling:** Benchmark with JMH. See Tooling Reference section.

---

## Phase 7 — System Design

> Goal: Apply concurrency primitives to recognisable production patterns.
> Each problem has an architectural tradeoff question with no single right answer.

### P7.1 — Token Bucket Rate Limiter `[NEW]`

**Problem:** Implement a `RateLimiter` that allows at most `rate` tokens per second
using the token bucket algorithm. `tryAcquire()` returns true if a token is
available, false otherwise. Bucket capacity is configurable (allows controlled
bursts).

**Acceptance:**
- Correct under 50 concurrent callers
- In any 1-second window, tokens granted do not exceed `rate + bucketCapacity`
  (token bucket *does* allow bursts up to bucket capacity — this is by design,
  not a bug; verify by recording grant timestamps and asserting the window invariant)
- Lazy token replenishment: tokens are computed from elapsed time at call time,
  not by a background thread — verify by running the test with no background threads
- Written comparison with `Semaphore`-based limiting: rate limiting vs concurrency
  limiting — what is the practical difference?

**Staff+ Lens:**
- Token bucket permits controlled bursts: tokens accumulate up to `bucketCapacity`
  when the caller is idle, then can be consumed instantly. This smooths traffic
  without punishing idle periods.
- A `Semaphore` limits the number of concurrent in-flight requests, not the rate
  of new requests. They solve different problems: a connection pool uses a semaphore;
  an API gateway uses a rate limiter.
- Lazy evaluation vs replenishment thread: lazy evaluation computes
  `tokensToAdd = min(capacity, storedTokens + rate * elapsedSeconds)` at each
  `tryAcquire` call using `System.nanoTime()`. This avoids a timer thread, is
  more accurate at short intervals, and is the approach Guava's `RateLimiter` takes.
- Clock choice: use `System.nanoTime()` (monotonic), never `System.currentTimeMillis()`
  (wall clock — can jump backwards on NTP sync).

**Concepts:** token bucket, lazy replenishment, rate vs concurrency limiting, monotonic clock

---

### P7.2 — Producer-Consumer Pipeline with Backpressure `[NEW]`

**Problem:** Build a 3-stage pipeline: Fetcher → Enricher → Writer.
Each stage runs on a fixed thread pool and communicates via bounded queues.
The pipeline must handle a slow Writer without causing the Fetcher to OOM.

**Acceptance:**
- End-to-end throughput measured and reported (ops/sec)
- Backpressure propagates: throttle the Writer and verify the Fetcher blocks
  (not accumulates) — measure Fetcher's queue wait time as the Writer slows
- Graceful shutdown: send a poison pill or use `ExecutorService.shutdown()`;
  verify all in-flight messages are processed and none dropped
- Written explanation: where is the bottleneck, and how would you identify it in
  production?

**Staff+ Lens:**
- Bounded queues are the backpressure mechanism. An unbounded queue decouples
  producers from consumers but allows unbounded memory growth — the producer
  keeps running while consumers fall behind.
- The slowest stage determines throughput. Adding threads to fast stages has no
  effect beyond a certain point (Amdahl's law at the pipeline level). Identify the
  bottleneck stage by measuring per-stage queue depths and processing latency.
- Queue size tuning: too small → frequent producer blocking (context-switch overhead
  dominates). Too large → high end-to-end latency before backpressure kicks in.
  Start with queue depth ≈ 2× the downstream stage's throughput per second;
  then benchmark.
- Reactive Streams (`java.util.concurrent.Flow`) formalises what you just built:
  publishers, subscribers, and a demand-based backpressure protocol. Compare your
  design to the `Flow` API — what does `Flow` add?

**Concepts:** pipeline concurrency, backpressure, bounded queues, bottleneck analysis,
`java.util.concurrent.Flow`

---

### P7.3 — Publish-Subscribe Event Bus `[NEW]`

**Problem:** Implement a simple in-process event bus:
- `subscribe(EventType, handler)`: registers a handler
- `publish(event)`: dispatches to all matching handlers asynchronously
- `unsubscribe(handler)`: removes the handler safely; no events are delivered to
  the handler after `unsubscribe` returns

**Acceptance:**
- Events are delivered to all subscribers registered at publish time
- Handlers run on a thread pool, not the publishing thread
- `unsubscribe` guarantee: implement with an `AtomicInteger` per handler tracking
  in-flight invocations, incremented before dispatch and decremented on completion.
  `unsubscribe` removes the handler, then waits (using `LockSupport.parkNanos` or
  a `Phaser`) until the in-flight count reaches zero. A `CountDownLatch` per handler
  does not work — it is single-use and cannot track multiple concurrent invocations.
- Stress test: 20 threads publishing; 5 threads repeatedly subscribing and
  unsubscribing. Assert no invocations observed after `unsubscribe` returns.

**Staff+ Lens:**
- The unsubscribe guarantee requires a happens-before between the last handler
  invocation completing and `unsubscribe` returning. The in-flight counter
  + wait-for-zero pattern achieves this: the final decrement happens-before
  the `unsubscribe` caller reads zero and returns.
- `CopyOnWriteArrayList` for the subscriber list allows lock-free reads during
  `publish` (no blocking readers) at the cost of O(N) array copy on every
  `subscribe`/`unsubscribe`. This is fine when publishes vastly outnumber
  (un)subscribes.
- A slow or throwing handler must not block or kill the dispatch thread. Wrap
  handler invocation in a try-catch; submit to the thread pool with
  `executor.submit(handler).exceptionally(...)` rather than calling directly.
  Alternatively, use per-handler timeout via `Future.get(timeout, unit)`.

**Concepts:** event bus, `CopyOnWriteArrayList`, in-flight tracking, `Phaser`,
happens-before for lifecycle guarantees

---

## Tooling Reference

The following tools are mandatory or strongly recommended across multiple problems.
Benchmark results without JMH and JMM assertions without JCStress are unreliable.

### JCStress
Canonical tool for testing JMM properties. Unlike JUnit, JCStress runs at the VM
level with scheduling perturbations to expose reordering and visibility bugs that
timing-dependent tests miss. A plain JUnit test that passes is evidence of
*absence of detected races*, not proof of correctness.

- **Required for:** P2.0 (happens-before), P2.1 (VolatileCounter race detection),
  P2.2 (safe publication)
- **Setup:** `org.openjdk.jcstress:jcstress-core`; annotate test classes with
  `@JCStressTest`, outcomes with `@Outcome(id = "...", expect = Expect.FORBIDDEN)`

### JMH (Java Microbenchmark Harness)
Required for all benchmark acceptance criteria. `System.nanoTime` loops without
warmup produce unreliable numbers: JIT compilation happens mid-measurement,
dead-code elimination silently removes work, and OS scheduling noise dominates
short measurements.

- **Required for:** P2.3 (LongAdder), P4.1 (RW Leaderboard), P5.3 (false sharing),
  P6.5 (ForkJoin merge sort)
- **Setup:** `org.openjdk.jmh:jmh-core` + `jmh-generator-annprocess`; annotate
  with `@Benchmark`, `@Warmup(iterations=3)`, `@Measurement(iterations=5)`,
  `@Fork(1)`, `@BenchmarkMode(Mode.Throughput)`

### jstack / ThreadMXBean
- `jstack <pid>` or `kill -3 <pid>`: capture a live thread dump
- `jcmd <pid> Thread.print`: equivalent via JMX
- `ThreadMXBean.findDeadlockedThreads()`: programmatic detection; use in P5.1
  acceptance test instead of a wall-clock timeout

### Java Flight Recorder (JFR) & async-profiler
- JFR: `jcmd <pid> JFR.start duration=30s filename=recording.jfr` — built-in,
  < 2% overhead, captures lock contention, thread parking, GC, and I/O
- async-profiler: CPU and wall-clock profiling with flame graph output; can
  attribute time to lock contention vs computation
- **Useful for:** P5.1–P5.4 (diagnosing failure modes), P6.1 (thread pool tuning),
  P7.2 (pipeline bottleneck analysis)

### jcmd Thread.print + Virtual Thread Diagnostics
- `-Djdk.tracePinnedThreads=full`: logs whenever a virtual thread pins its
  carrier thread (e.g., inside `synchronized` + blocking call)
- Required for: P6.2 (virtual thread pinning demonstration)

---

## Summary: Skill Coverage Matrix

| Phase | Problem | JMM | Atomicity | Locking | Coordination | Lock-Free | Failure Modes | Execution | Design |
|---|---|---|---|---|---|---|---|---|---|
| 1 | P1.1 Lifecycle & Cancellation | ✓ | | | ✓ | | | | |
| 2 | P2.0 JMM Happens-Before | ✓ | | | | | | | |
| 2 | P2.1 Counter | ✓ | ✓ | ✓ | | | | | |
| 2 | P2.2 Safe Publication | ✓ | | | | | | | |
| 2 | P2.3 LongAdder | ✓ | ✓ | | | ✓ | ✓ (perf) | | |
| 3 | P3.1 Mailbox | | | ✓ | ✓ | | | | |
| 3 | P3.2 Connection Pool | | | | ✓ | | | | ✓ |
| 3 | P3.3 CyclicBarrier & Phaser | | | | ✓ | | | | |
| 3 | P3.4 Blocking Queue | | ✓ | ✓ | ✓ | | | | |
| 4 | P4.1 RW Leaderboard | | | ✓ | | | | | ✓ |
| 4 | P4.2 LRU Cache | | ✓ | ✓ | | | | | ✓ |
| 5 | P5.1 Deadlock | | | ✓ | | | ✓ | | |
| 5 | P5.2 Livelock & Starvation | | | ✓ | | | ✓ | | |
| 5 | P5.3 False Sharing | ✓ | | | | | ✓ (perf) | | |
| 5 | P5.4 Treiber Stack | | ✓ | | | ✓ | | | |
| 6 | P6.1 ThreadPoolExecutor | | | | | | | ✓ | ✓ |
| 6 | P6.2 Virtual Threads | | | ✓ | | | | ✓ | ✓ |
| 6 | P6.3 ThreadLocal | ✓ | | | | | ✓ (leak) | ✓ | |
| 6 | P6.4 CompletableFuture | | | | | | | ✓ | ✓ |
| 6 | P6.5 ForkJoin | | | | ✓ | | | ✓ | |
| 7 | P7.1 Rate Limiter | | | | ✓ | | | | ✓ |
| 7 | P7.2 Pipeline | | | | ✓ | | | ✓ | ✓ |
| 7 | P7.3 Event Bus | ✓ | | | ✓ | | | ✓ | ✓ |

---

## Current Implementation Status

| Problem | File | Status |
|---|---|---|
| P1.1 Thread Lifecycle | `l1_basics/L1_ThreadJoinProblem.java` | Stub |
| P2.1 Thread-Safe Counter | `l2_safety/L2_ThreadSafeCounter.java` | Stub |
| P2.1 Counter variants | `l2_safety/counters/` | Implemented (5 variants — see known issues) |
| P3.1 Mailbox | `l3_coordination/L3_SingleSlotMailbox.java` | Stub |
| P3.4 Blocking Queue | `l4_structures/L4_BoundedBlockingQueue.java` | Stub |
| P4.1 Leaderboard (unsafe ref) | `l4_structures/leaderboard/` | Implemented (unsafe version only) |
| All others | — | Not yet created |

### Known Issues to Fix Before Implementing New Problems

**Correctness bugs in existing code:**

1. `ThreadUnsafeLeaderboard.addScore` calls `put` (replace semantics) — tests in
   `AbstractLeaderboardTest` expect accumulation semantics. Fix: use
   `playerScore.merge(playerId, score, Integer::sum)`.

2. `SynchronizedMethodBasicCounter.get()` is not `synchronized`. The unsynchronised
   read has no happens-before with any `increment()` call — concurrent readers can
   observe stale values. Either fix it (add `synchronized`) or rename the class to
   `PartiallySynchronizedCounter` to make it a pedagogical negative example.

3. `ReentrantLockBasicCounter.get()` does not acquire the lock, and `value` is not
   `volatile`. Readers can observe arbitrarily stale values. Same options: fix or
   rename.

4. `PlayerScore` model class is defined but never used — delete or integrate into a
   future thread-safe leaderboard implementation.

**Bugs in `ConcurrentTestHarness`:**

5. `doneLatch.await(10, TimeUnit.SECONDS)` — the return value is ignored. If the
   latch times out (worker threads hung), the harness silently proceeds and reports
   success if no errors were recorded. Fix: `if (!doneLatch.await(10, SECONDS)) throw new AssertionError("timed out waiting for workers")`.

6. `catch (Exception e)` in the worker lambda does not catch `AssertionError` or
   other `Throwable`s thrown by the task. An `AssertionError` inside the task will
   surface as an uncaught exception on the executor thread and the harness reports
   success. Fix: change to `catch (Throwable t)`.
