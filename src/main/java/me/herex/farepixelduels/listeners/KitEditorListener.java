package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.duels.GameMode;
import me.herex.farepixelduels.duels.KitManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;

public class KitEditorListener implements Listener {

    private final KitManager kitManager;
    private final Map<UUID, GameMode> editing;

    public KitEditorListener(KitManager kitManager, Map<UUID, GameMode> editing) {
        this.kitManager = kitManager;
        this.editing = editing;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        if (!editing.containsKey(p.getUniqueId())) return;

        GameMode mode = editing.remove(p.getUniqueId());
        Inventory inv = e.getInventory();

        kitManager.saveKit(mode, inv.getContents());
        p.sendMessage(ChatColor.GREEN + "Saved kit for " + mode.id() + ".");
    }
}
