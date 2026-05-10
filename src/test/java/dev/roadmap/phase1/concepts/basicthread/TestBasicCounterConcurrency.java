package dev.roadmap.phase1.concepts.basicthread;

import dev.roadmap.utils.ConcurrentTestHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBasicCounterConcurrency extends AbstractCounterTest {

    @Test
    public void testConcurrentIncrements() throws InterruptedException {
        BasicCounter counter = new BasicCounter();
        // This is expected to fail because BasicCounter is not thread-safe
        assertThreadSafe(counter);
    }
}
