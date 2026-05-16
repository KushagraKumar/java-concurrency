package dev.roadmap.l2_safety;
/**
 * PROBLEM 1: Thread-Safe Counter
 * ✅ Acceptance: Final count == 1,000,000 under 10 threads × 100,000 increments. No Thread.sleep().
 * 🎯 Staff+ Lens: Explain why volatile fails for compound ops. Discuss monitor costs vs JVM optimizations.
 */
public class L2_ThreadSafeCounter {
    // TODO: Implement thread-safe increment() and get()
    // TODO: Add JUnit test using ConcurrentTestHarness.runConcurrently(10, 100_000, ...)
}
