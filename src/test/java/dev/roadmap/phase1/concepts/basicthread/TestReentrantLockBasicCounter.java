package dev.roadmap.phase1.concepts.basicthread;

import dev.roadmap.utils.ConcurrentTestHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestReentrantLockBasicCounter extends AbstractCounterTest {

    @Test
    public void testReentrantLockCounter() throws InterruptedException {
        ReentrantLockBasicCounter counter = new ReentrantLockBasicCounter();
        assertThreadSafe(counter);
    }
}
