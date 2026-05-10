package dev.roadmap.phase1.problems.leaderboard;

import java.util.List;

public interface Leaderboard {
    void addScore(int playerId, int score);

    List<Integer> topKPlayers(int k);

    void resetScore(int playerId);
}
