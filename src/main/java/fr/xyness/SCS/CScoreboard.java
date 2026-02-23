package fr.xyness.SCS;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class CScoreboard {
    private final Scoreboard scoreboard;
    private final Objective objective;
    private static final String OBJECTIVE_NAME = "scs_sidebar";
    @SuppressWarnings("deprecation")
    public CScoreboard(String title) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective tempObjective = scoreboard.getObjective(OBJECTIVE_NAME);
        if (tempObjective == null) {
            tempObjective = scoreboard.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, title);
        }
        objective = tempObjective;
        objective.setDisplayName(title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }
    public void addLine(String line, int score) {
        Score s = objective.getScore(line != null ? line : "");
        s.setScore(score);
    }
    public void showToPlayer(Player player) {
        if (player != null) {
            player.setScoreboard(scoreboard);
        }
    }
    public void removeFromPlayer(Player player) {
        if (player != null) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }
    @Deprecated
    public void updateLine(String oldLine, String newLine, int score) {
        if (oldLine != null) {
            scoreboard.resetScores(oldLine);
        }
        addLine(newLine, score);
    }
    public void updateLines(Map<Integer, String> linesMap) {
        if (linesMap == null || linesMap.isEmpty()) {
            return;
        }
        Set<String> existingEntries = new HashSet<>(scoreboard.getEntries());
        for (Map.Entry<Integer, String> entry : linesMap.entrySet()) {
            int score = entry.getKey();
            String newLine = entry.getValue() != null ? entry.getValue() : "";
            existingEntries.stream()
                    .filter(line -> line != null && objective.getScore(line).getScore() == score)
                    .findFirst()
                    .ifPresent(scoreboard::resetScores);
            addLine(newLine, score);
        }
    }
    public void clear() {
        if (scoreboard != null) {
            scoreboard.getEntries().forEach(scoreboard::resetScores);
        }
    }
    public Scoreboard getScoreboard() {
        return scoreboard;
    }
}