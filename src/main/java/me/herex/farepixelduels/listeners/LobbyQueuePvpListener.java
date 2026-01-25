package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.duels.DuelQueueManager;
import me.herex.farepixelduels.duels.DuelMatch;
import me.herex.farepixelduels.spawn.SpawnUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class LobbyQueuePvpListener implements Listener {

    private final FarepixelDuels plugin;
    private final DuelQueueManager queue;

    public LobbyQueuePvpListener(FarepixelDuels plugin, DuelQueueManager queue) {
        this.plugin = plugin;
        this.queue = queue;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof Player)) return;

        Player victim = (Player) e.getEntity();
        Player attacker = (Player) e.getDamager();

        // block PvP in lobby world
        Location lobby = SpawnUtil.getLobbySpawn(plugin);
        if (lobby != null && lobby.getWorld() != null && attacker.getWorld() != null) {
            if (attacker.getWorld().getName().equalsIgnoreCase(lobby.getWorld().getName())) {
                e.setCancelled(true);
                return;
            }
        }

        if (queue == null) return;

        // block PvP for anyone in queue
        if (queue.isInQueue(attacker) || queue.isInQueue(victim)) {
            e.setCancelled(true);
            return;
        }

        // block PvP if not same match
        DuelMatch am = queue.getMatch(attacker);
        DuelMatch vm = queue.getMatch(victim);
        if (am == null || vm == null || am != vm) return;

        // allow normal combat in matches (boxing handled elsewhere)
    }
}
