package dev.roadmap.phase1.concepts.basicthread;

import dev.roadmap.phase1.problems.counter.VolatileCounter;
import org.junit.jupiter.api.Test;

public class TestVolatileCounter extends AbstractCounterTest {

    @Test
    public void testVolatileCounter() throws InterruptedException {
        VolatileCounter counter = new VolatileCounter();

        assertThreadSafe(counter);
    }
}
