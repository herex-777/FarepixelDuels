package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.duels.GameMode;
import me.herex.farepixelduels.duels.KitManager;
import me.herex.farepixelduels.duels.commands.KitEditorCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class KitEditorListener implements Listener {

    private final KitManager kitManager;
    private final Map<UUID, GameMode> editing;

    public KitEditorListener(KitManager kitManager, Map<UUID, GameMode> editing) {
        this.kitManager = kitManager;
        this.editing = editing;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player p = (Player) e.getWhoClicked();
        Inventory inv = e.getInventory();
        if (inv == null || inv.getTitle() == null) return;

        String title = inv.getTitle();

        // Main kit editor menu
        if (title.equals(KitEditorCommand.GUI_TITLE)) {
            e.setCancelled(true); // don't move GUI items

            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            ItemMeta meta = clicked.getItemMeta();
            if (!meta.hasLore()) return;

            GameMode mode = null;
            for (String line : meta.getLore()) {
                String stripped = ChatColor.stripColor(line);
                if (stripped.startsWith("MODE:")) {
                    String id = stripped.substring("MODE:".length()).trim();
                    try {
                        mode = GameMode.valueOf(id);
                    } catch (IllegalArgumentException ex) {
                        mode = null;
                    }
                    break;
                }
            }
            if (mode == null) {
                p.sendMessage(ChatColor.RED + "Could not determine mode for this kit.");
                return;
            }

            openKitEditInventory(p, mode);
        }
    }

    private void openKitEditInventory(Player p, GameMode mode) {
        String title = ChatColor.DARK_GRAY + "Edit Kit: " + mode.getDisplayName();
        Inventory editInv = Bukkit.createInventory(p, 36, title);

        // Load existing kit (global or per-player, see KitManager)
        List<ItemStack> saved = kitManager.getKit(mode);
        for (int i = 0; i < saved.size() && i < editInv.getSize(); i++) {
            ItemStack it = saved.get(i);
            if (it != null) editInv.setItem(i, it);
        }

        editing.put(p.getUniqueId(), mode);
        p.openInventory(editInv);
        p.sendMessage(ChatColor.YELLOW + "Edit your kit for " + mode.getDisplayName() + " then close the inventory to save.");
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player p = (Player) e.getPlayer();
        UUID id = p.getUniqueId();

        if (!editing.containsKey(id)) return;

        Inventory inv = e.getInventory();
        if (inv == null || inv.getTitle() == null) return;

        // Only save if this is an 'Edit Kit: ...' inventory, not the main menu
        String stripped = ChatColor.stripColor(inv.getTitle());
        if (!stripped.startsWith("Edit Kit:")) return;

        GameMode mode = editing.remove(id);

        // Save kit (currently global; see DB notes below)
        kitManager.saveKit(mode, inv.getContents());
        p.sendMessage(ChatColor.GREEN + "Saved kit for " + mode.getDisplayName() + ".");
    }
}
