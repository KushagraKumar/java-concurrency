package dev.roadmap.l2_safety;

import dev.roadmap.l2_safety.counters.ReentrantLockBasicCounter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestReentrantLockBasicCounter extends AbstractCounterTest {

    @Test
    public void testReentrantLockCounter() throws InterruptedException {
        ReentrantLockBasicCounter counter = new ReentrantLockBasicCounter();
        assertThreadSafe(counter);
    }
}
