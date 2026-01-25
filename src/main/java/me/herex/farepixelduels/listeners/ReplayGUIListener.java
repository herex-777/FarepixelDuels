package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.replay.ReplayManagerImpl;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ReplayGUIListener implements Listener {

    private final ReplayManagerImpl replayManager;

    public ReplayGUIListener(FarepixelDuels farepixelDuels, ReplayManagerImpl replayManager) {
        this.replayManager = replayManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        String title = ChatColor.stripColor(e.getView().getTitle());
        String expected = ChatColor.stripColor(ReplayManagerImpl.GUI_TITLE);
        if (!title.equalsIgnoreCase(expected)) return;

        if (e.getRawSlot() >= e.getView().getTopInventory().getSize()) return;

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (clicked.getType() == Material.BEDROCK) return; // "no replays" placeholder

        if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasDisplayName()) return;

        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        if (!name.startsWith("Replay ")) return;

        String id = name.substring("Replay ".length()).trim();
        if (id.isEmpty()) return;

        replayManager.playReplay(p, id);
    }
}
