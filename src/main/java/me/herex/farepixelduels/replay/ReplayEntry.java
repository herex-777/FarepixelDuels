package me.herex.farepixelduels.replay;

import java.util.List;

public class ReplayEntry {

    private final String id;
    private final long createdAt;
    private final String map;
    private final String server;
    private final String mode;
    private final List<String> players;

    public ReplayEntry(String id, long createdAt, String map, String server, String mode, List<String> players) {
        this.id = id;
        this.createdAt = createdAt;
        this.map = map;
        this.server = server;
        this.mode = mode;
        this.players = players;
    }

    public String getId() {
        return id;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getMap() {
        return map;
    }

    public String getServer() {
        return server;
    }

    public String getMode() {
        return mode;
    }

    public List<String> getPlayers() {
        return players;
    }
}
