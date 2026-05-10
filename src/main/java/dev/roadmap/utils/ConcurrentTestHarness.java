package dev.roadmap.utils;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
public class ConcurrentTestHarness {
    public static void runConcurrently(int threads, int iterationsPerThread, IntConsumer task) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        AtomicInteger errors = new AtomicInteger();
        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < iterationsPerThread; i++) task.accept(i);
                } catch (Exception e) { errors.incrementAndGet(); e.printStackTrace(); }
                finally { doneLatch.countDown(); }
            });
        }
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdownNow();
        if (errors.get() > 0) throw new AssertionError("Concurrent test failed with " + errors.get() + " errors");
    }
}
