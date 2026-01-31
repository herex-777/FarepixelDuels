package me.herex.farepixelduels.duels;

import me.herex.farepixelduels.storage.StatsRepository;

import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.UUID;

public class StatsManager {

    private final StatsRepository repo;

    public StatsManager(StatsRepository repo) {
        this.repo = repo;
    }

    public int getWins(UUID u) {
        try { return repo.get(u).wins; } catch (SQLException e) { return 0; }
    }
    public int getKills(UUID u) {
        try { return repo.get(u).kills; } catch (SQLException e) { return 0; }
    }
    public int getWinstreak(UUID u) {
        try { return repo.get(u).winstreak; } catch (SQLException e) { return 0; }
    }
    public int getBestWinstreak(UUID u) {
        try { return repo.get(u).bestWinstreak; } catch (SQLException e) { return 0; }
    }

    public void addKill(UUID u) {
        try {
            StatsRepository.StatsData d = repo.get(u);
            d.kills += 1;
            repo.set(u, d);
        } catch (SQLException ignored) {}
    }

    public void applyWin(UUID winner, UUID loser) {
        try {
            StatsRepository.StatsData w = repo.get(winner);
            StatsRepository.StatsData l = repo.get(loser);

            w.wins += 1;
            w.winstreak += 1;
            if (w.winstreak > w.bestWinstreak) w.bestWinstreak = w.winstreak;

            l.winstreak = 0;

            repo.set(winner, w);
            repo.set(loser, l);
        } catch (SQLException ignored) {}
    }

    public static String commas(int n) {
        return NumberFormat.getInstance().format(n);
    }
}
