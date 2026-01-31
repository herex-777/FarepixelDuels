package me.herex.farepixelduels.storage;

public enum DatabaseType {
    SQLITE,
    MYSQL;

    public static DatabaseType fromString(String s) {
        if (s == null) return SQLITE;
        try {
            return DatabaseType.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return SQLITE;
        }
    }
}
