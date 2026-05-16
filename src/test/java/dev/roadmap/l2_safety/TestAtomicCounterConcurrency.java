package dev.roadmap.l2_safety;

import dev.roadmap.l2_safety.counters.AtomicCounter;
import org.junit.jupiter.api.Test;

public class TestAtomicCounterConcurrency extends AbstractCounterTest {

    @Test
    public void testConcurrentIncrements() throws InterruptedException {
        AtomicCounter counter = new AtomicCounter();
        assertThreadSafe(counter);
    }
}
