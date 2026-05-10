package dev.roadmap.phase1.concepts.basicthread;

import dev.roadmap.utils.ConcurrentTestHarness;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSynchronizedMethodBasicCounterConcurrency extends AbstractCounterTest {

    @Test
    public void testCorrectBasicCounterConcurrency() throws InterruptedException {
        SynchronizedMethodBasicCounter counter = new SynchronizedMethodBasicCounter();
        assertThreadSafe(counter);
    }
}
