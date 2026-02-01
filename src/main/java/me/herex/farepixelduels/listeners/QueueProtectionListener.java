package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.duels.DuelQueueManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.UUID;

public class QueueProtectionListener implements Listener {

    private final FarepixelDuels plugin;

    public QueueProtectionListener(FarepixelDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof Player)) return;

        Player victim = (Player) e.getEntity();
        Player damager = (Player) e.getDamager();

        DuelQueueManager qm = plugin.getDuelQueueManager();
        if (qm == null) return;

        UUID vId = victim.getUniqueId();
        UUID dId = damager.getUniqueId();

        // Both in queue => no damage between them
        if (qm.isInQueue(vId) && qm.isInQueue(dId)) {
            e.setCancelled(true);
        }
    }
}
