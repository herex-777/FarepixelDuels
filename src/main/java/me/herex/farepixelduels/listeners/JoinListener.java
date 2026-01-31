package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.spawn.SpawnUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class JoinListener implements Listener {

    private final JavaPlugin plugin;

    public JoinListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        Location spawn = SpawnUtil.getLobbySpawn(plugin);
        if (spawn == null) return;

        // Teleport next tick to avoid conflicts with other plugins
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (!p.isOnline()) return;
                spawn.getWorld().loadChunk(spawn.getBlockX() >> 4, spawn.getBlockZ() >> 4);
                p.teleport(spawn);
            }
        });
    }
}
