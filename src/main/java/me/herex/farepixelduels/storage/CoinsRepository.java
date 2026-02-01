package me.herex.farepixelduels.storage;

import java.sql.*;
import java.util.UUID;

public class CoinsRepository {

    private final DatabaseManager db;

    public CoinsRepository(DatabaseManager db) {
        this.db = db;
    }

    public void setupTables() throws SQLException {
        try (Connection con = db.getConnection();
             Statement st = con.createStatement()) {

            st.executeUpdate("CREATE TABLE IF NOT EXISTS fp_duels_coins (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "coins INT NOT NULL" +
                    ")");
        }
    }

    public int getCoins(UUID uuid) throws SQLException {
        try (Connection con = db.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT coins FROM fp_duels_coins WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("coins");
            }
        }
        return 0;
    }

    public void setCoins(UUID uuid, int coins) throws SQLException {
        coins = Math.max(0, coins);

        if (db.getType() == DatabaseType.MYSQL) {
            try (Connection con = db.getConnection();
                 PreparedStatement ps = con.prepareStatement(
                         "INSERT INTO fp_duels_coins (uuid, coins) VALUES (?, ?) " +
                                 "ON DUPLICATE KEY UPDATE coins=VALUES(coins)"
                 )) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, coins);
                ps.executeUpdate();
            }
            return;
        }

        // SQLITE upsert: UPDATE first, then INSERT if needed
        try (Connection con = db.getConnection()) {
            try (PreparedStatement up = con.prepareStatement("UPDATE fp_duels_coins SET coins=? WHERE uuid=?")) {
                up.setInt(1, coins);
                up.setString(2, uuid.toString());
                int updated = up.executeUpdate();
                if (updated > 0) return;
            }
            try (PreparedStatement ins = con.prepareStatement("INSERT INTO fp_duels_coins (uuid, coins) VALUES (?, ?)")) {
                ins.setString(1, uuid.toString());
                ins.setInt(2, coins);
                ins.executeUpdate();
            }
        }
    }
}
