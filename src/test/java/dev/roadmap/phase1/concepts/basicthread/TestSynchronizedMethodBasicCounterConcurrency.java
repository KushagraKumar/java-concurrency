package dev.roadmap.phase1.concepts.basicthread;

import dev.roadmap.utils.ConcurrentTestHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSynchronizedMethodBasicCounterConcurrency {

    @Test
    public void testCorrectBasicCounterConcurrency() throws InterruptedException {
        SynchronizedMethodBasicCounter counter = new SynchronizedMethodBasicCounter();
        int threads = 10;
        int iterations = 100_000;
        int expectedValue = threads * iterations;

        ConcurrentTestHarness.runConcurrently(threads, iterations, i -> counter.increment());

        assertEquals(counter.get(), expectedValue);

    }
}
