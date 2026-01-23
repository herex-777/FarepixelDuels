package me.herex.farepixelduels.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class BuildProtectionListener implements Listener {

    private final JavaPlugin plugin;

    public BuildProtectionListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isProtectedWorld(String worldName) {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.getBoolean("block_place_and_break.enabled", true)) return false;

        List<String> worlds = cfg.getStringList("block_place_and_break.worlds");
        if (worlds == null || worlds.isEmpty()) return false;

        for (String w : worlds) {
            if (w != null && w.equalsIgnoreCase(worldName)) return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!isProtectedWorld(event.getBlock().getWorld().getName())) return;
        if (event.getPlayer().hasPermission("farepixelduels.build.bypass")) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!isProtectedWorld(event.getBlock().getWorld().getName())) return;
        if (event.getPlayer().hasPermission("farepixelduels.build.bypass")) return;
        event.setCancelled(true);
    }
}
