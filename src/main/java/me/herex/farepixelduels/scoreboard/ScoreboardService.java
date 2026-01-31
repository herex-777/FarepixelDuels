package me.herex.farepixelduels.scoreboard;

import me.herex.farepixelduels.FarepixelDuels;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ScoreboardService implements Listener {

    private final FarepixelDuels plugin;
    private final SidebarManager sidebarManager;

    private BukkitTask updateTask;

    public ScoreboardService(FarepixelDuels plugin) {
        this.plugin = plugin;
        this.sidebarManager = new SidebarManager(plugin);
    }

    public void start() {
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Apply to currently online players (e.g., /reload)
        for (Player p : Bukkit.getOnlinePlayers()) {
            refreshFor(p);
        }

        startUpdateTask();
    }

    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            sidebarManager.remove(p);
        }
    }

    private void startUpdateTask() {
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) {
            return;
        }

        long interval = plugin.getConfig().getLong("scoreboard.update_interval_ticks", 20L);
        if (interval < 1L) interval = 20L;

        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    refreshFor(p);
                }
            }
        }, 1L, interval);
    }

    private boolean isEnabledInWorld(World world) {
        List<String> worlds = plugin.getConfig().getStringList("scoreboard.enabled_worlds");
        if (worlds == null || worlds.isEmpty()) {
            return true;
        }

        String w = world.getName();
        for (String allowed : worlds) {
            if (allowed != null && allowed.equalsIgnoreCase(w)) {
                return true;
            }
        }
        return false;
    }

    public void refreshFor(Player player) {
        if (player == null || !player.isOnline()) return;

        boolean enabled = plugin.getConfig().getBoolean("scoreboard.enabled", true);
        if (!enabled || !isEnabledInWorld(player.getWorld())) {
            sidebarManager.remove(player);
            return;
        }

        sidebarManager.update(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        // Delay 1 tick so Spigot has fully initialized player state
        final Player p = e.getPlayer();
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                refreshFor(p);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        sidebarManager.remove(e.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        refreshFor(e.getPlayer());
    }
}
