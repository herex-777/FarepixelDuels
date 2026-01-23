package me.herex.farepixelduels.duels;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class Arena {

    private final String name;
    private final GameMode gameMode;
    private final DuelType type;
    private final String worldName;
    private final List<Location> spawns = new ArrayList<Location>();
    private Location queueSpawn;

    public Arena(String name, GameMode gameMode, DuelType type, String worldName) {
        this.name = name;
        this.gameMode = gameMode;
        this.type = type;
        this.worldName = worldName;
    }

    public String getName() { return name; }
    public GameMode getGameMode() { return gameMode; }
    public DuelType getType() { return type; }
    public String getWorldName() { return worldName; }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public List<Location> getSpawns() {
        return spawns;
    }

    public Location getQueueSpawn() {
        return queueSpawn;
    }

    public void setQueueSpawn(Location queueSpawn) {
        this.queueSpawn = queueSpawn;
    }

    public boolean isReady() {
        return spawns.size() >= type.getSize() && getWorld() != null && queueSpawn != null;
    }
}

