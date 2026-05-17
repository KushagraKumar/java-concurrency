package dev.roadmap.benchmarks;

import dev.roadmap.l2_safety.counters.*;
import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class CounterReadBenchmark {

    private ReentrantLockBasicCounter lockCounter;
    private AtomicCounter atomicCounter;
    private SynchronizedMethodBasicCounter syncCounter;

    @Setup
    public void setup() {
        lockCounter = new ReentrantLockBasicCounter();
        atomicCounter = new AtomicCounter();
        syncCounter = new SynchronizedMethodBasicCounter();
    }

    @Benchmark
    public int readAtomicCounter() {
        // AtomicInteger.get() (Volatile read performance)
        return atomicCounter.get();
    }

    @Benchmark
    public int readLockCounterSafe() {
        // Safe read using ReentrantLock
        return lockCounter.getSafe();
    }

    @Benchmark
    public int readLockCounterUnsafe() {
        // Unsafe read (No lock)
        return lockCounter.get();
    }

    @Benchmark
    public int readSynchronizedSafe() {
        // Safe read using synchronized
        return syncCounter.getSafe();
    }

    @Benchmark
    public int readSynchronizedUnsafe() {
        // Unsafe read (No sync)
        return syncCounter.get();
    }
}
