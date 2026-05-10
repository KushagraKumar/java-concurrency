package dev.roadmap.phase1.concepts.basicthread;

import dev.roadmap.utils.ConcurrentTestHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestIncorrectBasicCounterConcurrency {

    @Test
    public void testConcurrentIncrements() throws InterruptedException {
        IncorrectBasicCounter counter = new IncorrectBasicCounter();
        int threads = 10;
        int iterations = 100_000;
        int expectedValue = threads * iterations;

        ConcurrentTestHarness.runConcurrently(threads, iterations, (i) -> counter.increment());

        assertEquals(expectedValue, counter.get(),
                "Race condition detected! Final count was less than " + expectedValue);
    }
}
