package me.herex.farepixelduels.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private final JavaPlugin plugin;

    private DatabaseType type;

    // MySQL pool (Hikari)
    private HikariDataSource mysqlDataSource;

    // SQLite direct JDBC url
    private String sqliteJdbcUrl;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        this.type = DatabaseType.fromString(plugin.getConfig().getString("database.type"));

        if (type == DatabaseType.SQLITE) {
            String fileName = plugin.getConfig().getString("database.sqlite.file", "farepixelduels.db");
            File dbFile = new File(plugin.getDataFolder(), fileName);
            if (!plugin.getDataFolder().exists()) {
                //noinspection ResultOfMethodCallIgnored
                plugin.getDataFolder().mkdirs();
            }
            this.sqliteJdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            // Ensure driver is loaded
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException ignored) {
            }
            return;
        }

        // MYSQL (MariaDB) using HikariCP
        String host = plugin.getConfig().getString("database.mysql.host", "localhost");
        int port = plugin.getConfig().getInt("database.mysql.port", 3306);
        String database = plugin.getConfig().getString("database.mysql.database", "farepixelduels");
        String username = plugin.getConfig().getString("database.mysql.username", "root");
        String password = plugin.getConfig().getString("database.mysql.password", "");
        boolean useSSL = plugin.getConfig().getBoolean("database.mysql.useSSL", false);

        String jdbcUrl = "jdbc:mariadb://" + host + ":" + port + "/" + database
                + "?useSSL=" + useSSL
                + "&autoReconnect=true"
                + "&useUnicode=true"
                + "&characterEncoding=utf8";

        HikariConfig cfg = new HikariConfig();
        cfg.setPoolName("FarepixelDuels-DB");
        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(2);
        cfg.setConnectionTimeout(10000);

        cfg.setJdbcUrl(jdbcUrl);
        cfg.setDriverClassName("org.mariadb.jdbc.Driver");
        cfg.setUsername(username);
        cfg.setPassword(password);

        this.mysqlDataSource = new HikariDataSource(cfg);
    }

    public DatabaseType getType() {
        return type;
    }

    public Connection getConnection() throws SQLException {
        if (type == DatabaseType.SQLITE) {
            if (sqliteJdbcUrl == null) throw new SQLException("SQLite not initialized");
            return DriverManager.getConnection(sqliteJdbcUrl);
        }
        if (mysqlDataSource == null) throw new SQLException("MySQL not initialized");
        return mysqlDataSource.getConnection();
    }

    public void close() {
        if (mysqlDataSource != null && !mysqlDataSource.isClosed()) {
            mysqlDataSource.close();
        }
    }
}
