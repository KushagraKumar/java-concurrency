package dev.roadmap.l2_safety;

import dev.roadmap.l2_safety.counters.VolatileCounter;
import org.junit.jupiter.api.Test;

public class TestVolatileCounter extends AbstractCounterTest {

    @Test
    public void testVolatileCounter() throws InterruptedException {
        VolatileCounter counter = new VolatileCounter();

        assertThreadSafe(counter);
    }
}
