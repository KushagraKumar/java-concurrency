package dev.roadmap.phase1.problems.leaderboard;

import dev.roadmap.utils.ConcurrentTestHarness;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

public class TestThreadUnsafeLeaderboard extends AbstractLeaderboardTest {

    @Test
    public void testFunctionalCorrectness() {
        assertFunctionalCorrectness(new ThreadUnsafeLeaderboard());
    }

    /**
     * This test demonstrates that even simple replacement (put) is unsafe.
     * It triggers ConcurrentModificationException by reading while writing.
     */
    @Test
    public void testConcurrentReadWriteFailure() throws InterruptedException {
        ThreadUnsafeLeaderboard leaderboard = new ThreadUnsafeLeaderboard();
        AtomicInteger exceptionCount = new AtomicInteger(0);

        // Start a writer thread
        Thread writer = new Thread(() -> {
            for (int i = 0; i < 5000; i++) {
                leaderboard.addScore(i, i);
            }
        });

        // Start a reader thread that calls topKPlayers (which streams the map)
        Thread reader = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                try {
                    leaderboard.topKPlayers(10);
                } catch (java.util.ConcurrentModificationException e) {
                    exceptionCount.incrementAndGet();
                }
            }
        });

        writer.start();
        reader.start();
        writer.join();
        reader.join();

        System.out.println("Caught " + exceptionCount.get() + " ConcurrentModificationExceptions");
        // In a thread-unsafe implementation, this should be > 0
        assertTrue(exceptionCount.get() > 0, "Failed to expose ConcurrentModificationException!");
    }

    /**
     * This test demonstrates structural corruption (data loss) during resizing.
     */
    @Test
    public void testStructuralCorruption() throws InterruptedException {
        ThreadUnsafeLeaderboard leaderboard = new ThreadUnsafeLeaderboard();
        int threads = 10;
        int iterations = 1000;
        int totalExpected = threads * iterations;

        // Concurrent puts of UNIQUE keys
        ConcurrentTestHarness.runConcurrently(threads, iterations, (i) -> {
            // Create a unique ID for every single put
            int uniqueId = Thread.currentThread().hashCode() + i;
            leaderboard.addScore(uniqueId, i);
        });

        int actualSize = leaderboard.topKPlayers(totalExpected + 100).size();
        System.out.println("Expected size: " + totalExpected + ", Actual size: " + actualSize);
        
        // This often fails because HashMap's internal resize is not thread-safe
        // and entries are lost or pointers corrupted.
        if (actualSize != totalExpected) {
            System.out.println("CONFIRMED: Structural corruption occurred. Lost " + (totalExpected - actualSize) + " entries.");
        }
    }
}
