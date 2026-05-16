package dev.roadmap.l2_safety;

import dev.roadmap.l2_safety.counters.VolatileCounter;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

/**
 * JCStress test for VolatileCounter.
 * Demonstrates that 'volatile' provides visibility but NOT atomicity for compound operations (value++).
 */
@JCStressTest
@Outcome(id = "2", expect = Expect.ACCEPTABLE, desc = "Both increments succeeded.")
@Outcome(id = "1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Lost update! Volatile did not prevent the race condition.")
@State
public class VolatileCounterStressTest {
    private final VolatileCounter counter = new VolatileCounter();

    @Actor
    public void actor1() {
        counter.increment();
    }

    @Actor
    public void actor2() {
        counter.increment();
    }

    @Arbiter
    public void arbiter(I_Result r) {
        r.r1 = counter.get();
    }
}
