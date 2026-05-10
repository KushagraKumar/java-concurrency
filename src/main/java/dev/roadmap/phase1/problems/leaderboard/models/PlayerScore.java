package dev.roadmap.phase1.problems.leaderboard.models;

public class PlayerScore {
    private Integer playerId;
    private Integer score;

    public PlayerScore(Integer playerId, Integer score) {
        this.playerId = playerId;
        this.score = score;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
