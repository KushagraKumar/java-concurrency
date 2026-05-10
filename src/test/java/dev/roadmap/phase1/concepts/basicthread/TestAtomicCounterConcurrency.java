package dev.roadmap.phase1.concepts.basicthread;

import dev.roadmap.phase1.problems.counter.AtomicCounter;
import org.junit.jupiter.api.Test;

public class TestAtomicCounterConcurrency extends AbstractCounterTest {

    @Test
    public void testConcurrentIncrements() throws InterruptedException {
        AtomicCounter counter = new AtomicCounter();
        assertThreadSafe(counter);
    }
}
