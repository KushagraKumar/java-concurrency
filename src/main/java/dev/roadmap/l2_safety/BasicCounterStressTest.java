package dev.roadmap.l2_safety;

import dev.roadmap.l2_safety.counters.BasicCounter;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

/**
 * JCStress test for BasicCounter.
 * Demonstrates that a plain 'int' increment is not atomic.
 */
@JCStressTest
@Outcome(id = "2", expect = Expect.ACCEPTABLE, desc = "Both increments succeeded.")
@Outcome(id = "1", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Lost update! One increment was overwritten.")
@State
public class BasicCounterStressTest {
    private final BasicCounter counter = new BasicCounter();

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
        // Arbiter runs after all actors finish to collect the final state
        r.r1 = counter.get();
    }
}
