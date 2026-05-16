package dev.roadmap.l4_structures.leaderboard;

import dev.roadmap.utils.ConcurrentTestHarness;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public abstract class AbstractLeaderboardTest {

    protected void assertThreadSafe(Leaderboard leaderboard) throws InterruptedException {
        int threads = 10;
        int iterations = 1000;
        int playerId = 1;
        int totalExpectedScore = threads * iterations;

        // 1. Concurrent updates AND concurrent reads
        ConcurrentTestHarness.runConcurrently(threads, iterations, (i) -> {
            leaderboard.addScore(playerId, 1);
            
            // Periodically perform a "Top K" read while writing
            if (i % 100 == 0) {
                leaderboard.topKPlayers(5);
            }
        });

        // 2. Verify final state
        leaderboard.addScore(2, totalExpectedScore - 1);
        List<Integer> top = leaderboard.topKPlayers(2);
        
        assertEquals(Integer.valueOf(playerId), top.get(0), 
            "Race condition detected! Player 1 should be #1 but likely lost updates. " +
            "Expected score ~" + totalExpectedScore + ", but rank was lost.");
    }

    protected void assertFunctionalCorrectness(Leaderboard leaderboard) {
        leaderboard.addScore(1, 100);
        leaderboard.addScore(2, 200);
        leaderboard.addScore(3, 150);

        List<Integer> top2 = leaderboard.topKPlayers(2);
        assertEquals(List.of(2, 3), top2, "topKPlayers returned incorrect order or players");

        leaderboard.resetScore(2);
        List<Integer> topAfterReset = leaderboard.topKPlayers(1);
        assertEquals(List.of(3), topAfterReset, "Score reset failed");
    }
}
