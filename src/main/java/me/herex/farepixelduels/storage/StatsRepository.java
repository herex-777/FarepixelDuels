package me.herex.farepixelduels.storage;

import java.sql.*;
import java.util.UUID;

public class StatsRepository {

    private final DatabaseManager db;

    public StatsRepository(DatabaseManager db) {
        this.db = db;
    }

    public void setupTables() throws SQLException {
        try (Connection con = db.getConnection();
             Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS fp_duels_stats (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "wins INT NOT NULL," +
                    "kills INT NOT NULL," +
                    "winstreak INT NOT NULL," +
                    "best_winstreak INT NOT NULL" +
                    ")");
        }
    }

    private void ensureRow(UUID uuid) throws SQLException {
        // Insert default row if missing
        if (db.getType() == me.herex.farepixelduels.storage.DatabaseType.MYSQL) {
            try (Connection con = db.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "INSERT IGNORE INTO fp_duels_stats (uuid, wins, kills, winstreak, best_winstreak) VALUES (?,0,0,0,0)"
                 )) {
                ps.setString(1, uuid.toString());
                ps.executeUpdate();
            }
            return;
        }

        // SQLITE: try select then insert
        try (Connection con = db.getConnection();
             PreparedStatement sel = con.prepareStatement("SELECT uuid FROM fp_duels_stats WHERE uuid=?")) {
            sel.setString(1, uuid.toString());
            try (ResultSet rs = sel.executeQuery()) {
                if (rs.next()) return;
            }
        }
        try (Connection con = db.getConnection();
             PreparedStatement ins = con.prepareStatement("INSERT INTO fp_duels_stats (uuid, wins, kills, winstreak, best_winstreak) VALUES (?,0,0,0,0)")) {
            ins.setString(1, uuid.toString());
            ins.executeUpdate();
        }
    }

    public StatsData get(UUID uuid) throws SQLException {
        ensureRow(uuid);
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT wins,kills,winstreak,best_winstreak FROM fp_duels_stats WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new StatsData(rs.getInt("wins"), rs.getInt("kills"), rs.getInt("winstreak"), rs.getInt("best_winstreak"));
                }
            }
        }
        return new StatsData(0,0,0,0);
    }

    public void set(UUID uuid, StatsData data) throws SQLException {
        ensureRow(uuid);
        if (db.getType() == me.herex.farepixelduels.storage.DatabaseType.MYSQL) {
            try (Connection con = db.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "UPDATE fp_duels_stats SET wins=?, kills=?, winstreak=?, best_winstreak=? WHERE uuid=?")) {
                ps.setInt(1, data.wins);
                ps.setInt(2, data.kills);
                ps.setInt(3, data.winstreak);
                ps.setInt(4, data.bestWinstreak);
                ps.setString(5, uuid.toString());
                ps.executeUpdate();
            }
            return;
        }

        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE fp_duels_stats SET wins=?, kills=?, winstreak=?, best_winstreak=? WHERE uuid=?")) {
            ps.setInt(1, data.wins);
            ps.setInt(2, data.kills);
            ps.setInt(3, data.winstreak);
            ps.setInt(4, data.bestWinstreak);
            ps.setString(5, uuid.toString());
            ps.executeUpdate();
        }
    }

    public static class StatsData {
        public int wins;
        public int kills;
        public int winstreak;
        public int bestWinstreak;

        public StatsData(int wins, int kills, int winstreak, int bestWinstreak) {
            this.wins = wins;
            this.kills = kills;
            this.winstreak = winstreak;
            this.bestWinstreak = bestWinstreak;
        }
    }
}
