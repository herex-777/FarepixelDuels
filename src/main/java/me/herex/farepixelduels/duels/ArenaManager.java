package me.herex.farepixelduels.duels;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ArenaManager {

    private final JavaPlugin plugin;
    private final Map<String, Arena> arenas = new HashMap<String, Arena>();

    public ArenaManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        arenas.clear();

        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("arenas");
        if (sec == null) return;

        for (String key : sec.getKeys(false)) {
            ConfigurationSection a = sec.getConfigurationSection(key);
            if (a == null) continue;

            String modeStr = a.getString("gamemode", "classic");
            String typeStr = a.getString("type", "1v1");
            String world = a.getString("world", "world");

            Arena arena = new Arena(key, GameMode.fromString(modeStr), DuelType.fromString(typeStr), world);

            // queue spawn
            ConfigurationSection qs = a.getConfigurationSection("queue_spawn");
            if (qs != null) {
                Location qloc = new Location(
                        plugin.getServer().getWorld(world),
                        qs.getDouble("x"),
                        qs.getDouble("y"),
                        qs.getDouble("z"),
                        (float) qs.getDouble("yaw"),
                        (float) qs.getDouble("pitch")
                );
                arena.setQueueSpawn(qloc);
            }

            ConfigurationSection sp = a.getConfigurationSection("spawns");
            if (sp != null) {
                for (String sKey : sp.getKeys(false)) {
                    ConfigurationSection l = sp.getConfigurationSection(sKey);
                    if (l == null) continue;
                    Location loc = new Location(
                            plugin.getServer().getWorld(world),
                            l.getDouble("x"),
                            l.getDouble("y"),
                            l.getDouble("z"),
                            (float) l.getDouble("yaw"),
                            (float) l.getDouble("pitch")
                    );
                    arena.getSpawns().add(loc);
                }
            }

            arenas.put(key.toLowerCase(), arena);
        }
    }

    public void saveArena(Arena arena) {
        String path = "arenas." + arena.getName();
        plugin.getConfig().set(path + ".gamemode", arena.getGameMode().id());
        plugin.getConfig().set(path + ".type", arena.getType().display());
        plugin.getConfig().set(path + ".world", arena.getWorldName());

        // save queue spawn
        plugin.getConfig().set(path + ".queue_spawn", null);
        if (arena.getQueueSpawn() != null) {
            String qp = path + ".queue_spawn";
            plugin.getConfig().set(qp + ".x", arena.getQueueSpawn().getX());
            plugin.getConfig().set(qp + ".y", arena.getQueueSpawn().getY());
            plugin.getConfig().set(qp + ".z", arena.getQueueSpawn().getZ());
            plugin.getConfig().set(qp + ".yaw", arena.getQueueSpawn().getYaw());
            plugin.getConfig().set(qp + ".pitch", arena.getQueueSpawn().getPitch());
        }

        // save spawns
        plugin.getConfig().set(path + ".spawns", null);
        for (int i = 0; i < arena.getSpawns().size(); i++) {
            Location loc = arena.getSpawns().get(i);
            String lp = path + ".spawns." + (i + 1);
            plugin.getConfig().set(lp + ".x", loc.getX());
            plugin.getConfig().set(lp + ".y", loc.getY());
            plugin.getConfig().set(lp + ".z", loc.getZ());
            plugin.getConfig().set(lp + ".yaw", loc.getYaw());
            plugin.getConfig().set(lp + ".pitch", loc.getPitch());
        }

        plugin.saveConfig();
        arenas.put(arena.getName().toLowerCase(), arena);
    }

    public Arena getArena(String name) {
        if (name == null) return null;
        return arenas.get(name.toLowerCase());
    }

    public List<Arena> getReadyArenas(GameMode mode, DuelType type) {
        List<Arena> out = new ArrayList<Arena>();
        for (Arena a : arenas.values()) {
            if (a.getGameMode() == mode && a.getType() == type && a.isReady()) out.add(a);
        }
        return out;
    }

    public Collection<Arena> all() {
        return arenas.values();
    }
}
