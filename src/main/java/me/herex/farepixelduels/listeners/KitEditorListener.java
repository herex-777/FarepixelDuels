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

    // Used by DuelsCommand to build the admin inventory title
    public static final String ADMIN_TITLE_PREFIX = ChatColor.DARK_GRAY + "Admin Kit: ";

    public KitEditorListener(KitManager kitManager, Map<UUID, GameMode> editing) {
        this.kitManager = kitManager;
        this.editing = editing;
    }

    /**
     * We don't need to listen to clicks here; DuelsCommand directly opens the
     * 54-slot editor for the chosen mode. We just save on close.
     */
    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;

        Player p = (Player) e.getPlayer();
        UUID id = p.getUniqueId();

        if (!editing.containsKey(id)) return;

        Inventory inv = e.getInventory();
        if (inv == null || inv.getTitle() == null) return;

        String stripped = ChatColor.stripColor(inv.getTitle());
        String prefix = ChatColor.stripColor(ADMIN_TITLE_PREFIX);
        if (!stripped.startsWith(prefix)) return;

        GameMode mode = editing.remove(id);
        if (mode == null) return;

        kitManager.saveKit(mode, inv.getContents());
        p.sendMessage(ChatColor.GREEN + "Saved default kit for " + mode.getDisplayName() + ".");
    }
}
