package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.duels.DuelQueueManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayAgainListener implements Listener {

    private final FarepixelDuels plugin;

    public PlayAgainListener(FarepixelDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.PAPER) return;
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String cfgName = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("duels.play_again.name", "&aPlay Again &7(Right Click)")
        );

        if (!ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase(ChatColor.stripColor(cfgName))) return;

        e.setCancelled(true);

        Player p = e.getPlayer();
        DuelQueueManager qm = plugin.getDuelQueueManager();
        if (qm == null) return;

        // join same mode/type as last duel
        qm.queuePlayAgain(p);
    }
}
