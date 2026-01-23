package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.duels.DuelMatch;
import me.herex.farepixelduels.duels.DuelQueueManager;
import me.herex.farepixelduels.duels.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DuelCombatListener implements Listener {

    private final DuelQueueManager queueManager;

    public DuelCombatListener(DuelQueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof Player)) return;

        Player victim = (Player) e.getEntity();
        Player attacker = (Player) e.getDamager();

        DuelMatch match = queueManager.getMatch(attacker);
        if (match == null) return;
        if (queueManager.getMatch(victim) != match) return;

        if (match.getMode() == GameMode.BOXING) {
            e.setDamage(0.0);
            e.setCancelled(true);
            if (match.isPlaying()) {
                match.addBoxingHit(attacker);
            }
        }
    }
}
