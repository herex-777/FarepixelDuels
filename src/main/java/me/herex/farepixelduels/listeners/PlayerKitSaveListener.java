package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.duels.GameMode;
import me.herex.farepixelduels.duels.PlayerKitManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.Map;
import java.util.UUID;

public class PlayerKitSaveListener implements Listener {

    private final Map<UUID, GameMode> editing;

    public PlayerKitSaveListener(PlayerKitManager playerKits, Map<UUID, GameMode> editing) {
        // playerKits not needed here; saving happens in PlayerKitMenuListener
        this.editing = editing;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;

        String title = ChatColor.stripColor(e.getView().getTitle());
        // Only clear map for the player kit editor inventories, not admin kit editor
        if (!title.startsWith("Edit Kit:")) return;

        Player p = (Player) e.getPlayer();
        editing.remove(p.getUniqueId());
    }
}
