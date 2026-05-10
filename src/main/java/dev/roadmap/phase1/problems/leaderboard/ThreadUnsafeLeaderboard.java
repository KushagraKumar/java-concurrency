package dev.roadmap.phase1.problems.leaderboard;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A deliberately thread-unsafe implementation to demonstrate race conditions.
 */
public class ThreadUnsafeLeaderboard implements Leaderboard {
    private final Map<Integer, Integer> playerScore = new HashMap<>();

    @Override
    public void addScore(int playerId, int score) {
        // Even a simple put is unsafe in a HashMap due to potential 
        // internal corruption (e.g., during resizing) or race conditions
        // where the map structure is modified by multiple threads.
        playerScore.put(playerId, score);
    }

    @Override
    public List<Integer> topKPlayers(int k) {
        // ConcurrentModificationException is likely here if addScore is called during streaming
        return playerScore.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(k)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public void resetScore(int playerId) {
        playerScore.remove(playerId);
    }
}
