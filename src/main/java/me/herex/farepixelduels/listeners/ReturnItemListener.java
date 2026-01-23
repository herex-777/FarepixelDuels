package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.duels.DuelQueueManager;
import me.herex.farepixelduels.duels.ReturnItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ReturnItemListener implements Listener {

    private final JavaPlugin plugin;
    private DuelQueueManager queueManager = null;

    public ReturnItemListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.queueManager = queueManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        if (item == null) return;

        if (!ReturnItemUtil.isReturnItem(item, plugin)) return;

        e.setCancelled(true);
        queueManager.leaveNormal(p);
    }
}
