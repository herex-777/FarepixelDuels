package me.herex.farepixelduels.duels;

public class SetupSession {
    private final String arenaName;
    private final GameMode mode;
    private final DuelType type;
    private final String worldName;

    public SetupSession(String arenaName, GameMode mode, DuelType type, String worldName) {
        this.arenaName = arenaName;
        this.mode = mode;
        this.type = type;
        this.worldName = worldName;
    }

    public String getArenaName() { return arenaName; }
    public GameMode getMode() { return mode; }
    public DuelType getType() { return type; }
    public String getWorldName() { return worldName; }
}
