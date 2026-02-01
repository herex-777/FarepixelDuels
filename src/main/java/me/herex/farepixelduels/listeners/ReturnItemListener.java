package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.duels.DuelQueueManager;
import me.herex.farepixelduels.duels.ReturnItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ReturnItemListener implements Listener {

    private final FarepixelDuels plugin;

    public ReturnItemListener(FarepixelDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = e.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        if (!ReturnItemUtil.isReturnItem(item, plugin)) return;

        e.setCancelled(true);
        e.setUseItemInHand(org.bukkit.event.Event.Result.DENY);

        Player p = e.getPlayer();

        DuelQueueManager qm = plugin.getDuelQueueManager();
        if (qm == null) return;

        boolean touched = qm.leaveNormal(p);

        // Always update inventory to stop "ghost remove" glitches
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                p.updateInventory();
            }
        });

        if (touched) {
            ReturnItemUtil.remove(p, plugin);
        }
    }
}
