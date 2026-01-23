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

    private final PlayerKitManager playerKits;
    private final Map<UUID, GameMode> editing;

    public PlayerKitSaveListener(PlayerKitManager playerKits, Map<UUID, GameMode> editing) {
        this.playerKits = playerKits;
        this.editing = editing;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        GameMode mode = editing.remove(p.getUniqueId());
        if (mode == null) return;

        playerKits.set(p.getUniqueId(), mode, e.getInventory().getContents());
        p.sendMessage(ChatColor.GREEN + "Saved your kit for " + mode.id() + ".");
    }
}
