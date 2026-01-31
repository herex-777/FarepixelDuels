package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.duels.DuelMatch;
import me.herex.farepixelduels.duels.DuelQueueManager;
import me.herex.farepixelduels.spawn.SpawnUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DuelGameplayListener implements Listener {

    private final JavaPlugin plugin;
    private final DuelQueueManager queueManager;
    private final Set<UUID> forceLobbyRespawn = new HashSet<UUID>();

    public DuelGameplayListener(JavaPlugin plugin, DuelQueueManager queueManager) {
        this.plugin = plugin;
        this.queueManager = queueManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        Player dead = e.getEntity();
        DuelMatch match = queueManager.getMatch(dead);
        if (match == null) return;

        e.setDeathMessage(null);
        e.getDrops().clear();
        e.setDroppedExp(0);
        e.setKeepInventory(true);
        e.setKeepLevel(true);

        Player killer = dead.getKiller();
        match.handleDeath(dead, killer);

        forceLobbyRespawn.add(dead.getUniqueId());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (!forceLobbyRespawn.remove(p.getUniqueId())) return;

        Location lobby = SpawnUtil.getLobbySpawn(plugin);
        if (lobby != null) e.setRespawnLocation(lobby);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        DuelMatch match = queueManager.getMatch(p);
        if (match == null) return;
        match.handleQuit(p);
    }
}
