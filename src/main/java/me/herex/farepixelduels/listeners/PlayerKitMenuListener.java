package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.duels.GameMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;

public class PlayerKitMenuListener implements Listener {

    private final FarepixelDuels plugin;
    private final Map<UUID, GameMode> editing;

    public PlayerKitMenuListener(FarepixelDuels plugin, Map<UUID, GameMode> editing) {
        this.plugin = plugin;
        this.editing = editing;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof org.bukkit.entity.Player)) return;
        Inventory inv = e.getInventory();
        if (inv == null) return;

        if (!ChatColor.stripColor(inv.getTitle()).equalsIgnoreCase("Kit Editor")) return;

        e.setCancelled(true);
        if (e.getCurrentItem() == null) return;

        // Only classic for now (slot 4)
        if (e.getSlot() != 4) return;

        org.bukkit.entity.Player p = (org.bukkit.entity.Player) e.getWhoClicked();

        Inventory kitInv = Bukkit.createInventory(null, 36, ChatColor.DARK_GRAY + "Edit Kit: classic");
        java.util.List<org.bukkit.inventory.ItemStack> current = plugin.getPlayerKitManager().get(p, GameMode.CLASSIC);
        if (current != null) {
            for (int i = 0; i < current.size() && i < kitInv.getSize(); i++) {
                kitInv.setItem(i, current.get(i));
            }
        } else {
            // load default kit
            java.util.List<org.bukkit.inventory.ItemStack> def = plugin.getKitManager().getKit(GameMode.CLASSIC);
            for (int i = 0; i < def.size() && i < kitInv.getSize(); i++) kitInv.setItem(i, def.get(i));
        }

        editing.put(p.getUniqueId(), GameMode.CLASSIC);
        p.openInventory(kitInv);
        p.sendMessage(ChatColor.YELLOW + "Edit your kit and close to save.");
    }
}
