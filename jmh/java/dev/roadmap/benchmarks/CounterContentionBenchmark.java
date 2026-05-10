package dev.roadmap.benchmarks;
import org.openjdk.jmh.annotations.*; import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder; import java.util.concurrent.TimeUnit;
@BenchmarkMode(Mode.Throughput) @OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 1) @Measurement(iterations = 5, time = 1)
@Fork(2) @State(Scope.Benchmark)
public class CounterContentionBenchmark {
    private AtomicLong atomicLong; private LongAdder longAdder;
    @Setup public void setup() { atomicLong = new AtomicLong(); longAdder = new LongAdder(); }
    @Benchmark @Group("atomic") public void atomicIncrement() { atomicLong.incrementAndGet(); }
    @Benchmark @Group("adder") public void adderIncrement() { longAdder.increment(); }
}
