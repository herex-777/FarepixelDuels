package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.duels.ReturnItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ReturnItemListener implements Listener {

    private final FarepixelDuels plugin;

    public ReturnItemListener(FarepixelDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        ItemStack cur = e.getCurrentItem();
        ItemStack cursor = e.getCursor();
        if (ReturnItemUtil.isReturnItem(cur, plugin) || ReturnItemUtil.isReturnItem(cursor, plugin)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (ReturnItemUtil.isReturnItem(e.getOldCursor(), plugin)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        ItemStack it = e.getItem();
        if (!ReturnItemUtil.isReturnItem(it, plugin)) return;

        e.setCancelled(true);
        Player p = e.getPlayer();

        // This will handle countdown vs playing properly (no fake wins)
        plugin.getDuelQueueManager().leaveNormal(p);
        ReturnItemUtil.remove(p, plugin);
    }
}
