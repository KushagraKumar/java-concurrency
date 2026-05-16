package dev.roadmap.l2_safety;

import dev.roadmap.l2_safety.counters.Counter;
import dev.roadmap.utils.ConcurrentTestHarness;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class AbstractCounterTest {

    protected void assertThreadSafe(Counter counter) throws InterruptedException {
        int threads = 10;
        int iterations = 100_000;
        int expectedValue = threads * iterations;

        ConcurrentTestHarness.runConcurrently(threads, iterations, (i) -> counter.increment());

        assertEquals(expectedValue, counter.get(),
                "Race condition detected in " + counter.getClass().getSimpleName() + 
                "! Final count was less than " + expectedValue);
    }
}
