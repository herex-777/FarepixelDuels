package me.herex.farepixelduels.spawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class SpawnUtil {

    private SpawnUtil() {}

    public static boolean isLobbySpawnSet(JavaPlugin plugin) {
        FileConfiguration cfg = plugin.getConfig();
        String worldName = cfg.getString("lobby_spawn.world", "");
        return worldName != null && !worldName.trim().isEmpty() && Bukkit.getWorld(worldName) != null;
    }

    public static Location getLobbySpawn(JavaPlugin plugin) {
        FileConfiguration cfg = plugin.getConfig();
        String worldName = cfg.getString("lobby_spawn.world", "");
        if (worldName == null || worldName.trim().isEmpty()) return null;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        double x = cfg.getDouble("lobby_spawn.x");
        double y = cfg.getDouble("lobby_spawn.y");
        double z = cfg.getDouble("lobby_spawn.z");
        float yaw = (float) cfg.getDouble("lobby_spawn.yaw");
        float pitch = (float) cfg.getDouble("lobby_spawn.pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public static void teleportToLobby(Player p, JavaPlugin plugin) {

    }
}
