package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.duels.GameMode;
import me.herex.farepixelduels.duels.commands.KitEditorCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class PlayerKitMenuListener implements Listener {

    private final FarepixelDuels plugin;
    private final Map<UUID, GameMode> editing;

    public PlayerKitMenuListener(FarepixelDuels plugin, Map<UUID, GameMode> editing) {
        this.plugin = plugin;
        this.editing = editing;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player p = (Player) e.getWhoClicked();

        Inventory top = e.getView().getTopInventory();
        if (top == null) return;

        String title = ChatColor.stripColor(top.getTitle());
        String mainTitle = ChatColor.stripColor(KitEditorCommand.GUI_TITLE);

        // ================= MAIN /KITEDITOR MENU =================
        if (title.equalsIgnoreCase(mainTitle)) {
            if (e.getRawSlot() >= top.getSize()) return; // only top GUI
            e.setCancelled(true);
            e.setResult(org.bukkit.event.Event.Result.DENY);

            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR) return;

            GameMode mode = null;
            switch (e.getRawSlot()) {
                case 10: mode = GameMode.CLASSIC;  break;
                case 11: mode = GameMode.BOW;      break;
                case 12: mode = GameMode.BOXING;   break;
                case 13: mode = GameMode.NODEBUFF; break;
                case 14: mode = GameMode.OP;       break;
                case 15: mode = GameMode.SUMO;     break;
                case 16: mode = GameMode.UHC;      break;
                default: break;
            }
            if (mode == null) return;

            openPlayerKitEditor(p, mode);
            return;
        }

        // ================= PER-MODE 54-SLOT EDITOR =================
        if (!title.startsWith("Edit Kit:")) return;

        UUID id = p.getUniqueId();
        GameMode mode = editing.get(id);
        if (mode == null) return;

        // Clicked in player's own inventory?
        if (e.getRawSlot() >= top.getSize()) {
            if (e.isShiftClick() || e.getClick() == ClickType.NUMBER_KEY) {
                e.setCancelled(true);
                e.setResult(org.bukkit.event.Event.Result.DENY);
            }
            return;
        }

        int rawSlot = e.getRawSlot();

        // Block glass line 36–44
        if (rawSlot >= 36 && rawSlot <= 44) {
            e.setCancelled(true);
            e.setResult(org.bukkit.event.Event.Result.DENY);
            return;
        }

        // Block bottom line except buttons (48,49,50)
        if (rawSlot >= 45 && rawSlot <= 53 &&
                rawSlot != 48 && rawSlot != 49 && rawSlot != 50) {
            e.setCancelled(true);
            e.setResult(org.bukkit.event.Event.Result.DENY);
            return;
        }

        // Buttons
        if (rawSlot == 48 || rawSlot == 49 || rawSlot == 50) {
            e.setCancelled(true);
            e.setResult(org.bukkit.event.Event.Result.DENY);
            p.setItemOnCursor(null);
        }

        // BACK
        if (rawSlot == 48) {
            editing.remove(id);
            p.closeInventory();
            Bukkit.getScheduler().runTask(plugin, () ->
                    new KitEditorCommand().openMainGui(p));
            return;
        }

        // SAVE
        if (rawSlot == 49) {
            saveLayoutFromInventory(p, mode, top);
            p.sendMessage(ChatColor.GREEN + "You have successfully saved the layout for " + mode.getDisplayName() + "s!");
            return;
        }

        // RESET
        if (rawSlot == 50) {
            resetLayout(p, mode, top);
            p.sendMessage(ChatColor.GREEN + "You have successfully reset the layout for " + mode.getDisplayName() + "s!");
            return;
        }

        // Slots 0–35: editable, do NOT cancel
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;

        Inventory top = e.getView().getTopInventory();
        if (top == null) return;

        String title = ChatColor.stripColor(top.getTitle());
        if (!title.startsWith("Edit Kit:")) return;

        for (int rawSlot : e.getRawSlots()) {
            if (rawSlot < top.getSize() && rawSlot >= 36) {
                e.setCancelled(true);
                return;
            }
        }
    }

    private void openPlayerKitEditor(Player p, GameMode mode) {
        Inventory kitInv = Bukkit.createInventory(
                null,
                54,
                ChatColor.DARK_GRAY + "Edit Kit: " + mode.getDisplayName()
        );

        // Load player kit if exists, otherwise default kit
        java.util.List<ItemStack> current = plugin.getPlayerKitManager().get(p, mode);
        if (current == null || current.isEmpty()) {
            current = plugin.getKitManager().getKit(mode);
        }

        if (current != null) {
            int limit = Math.min(current.size(), 36);
            for (int i = 0; i < limit; i++) {
                kitInv.setItem(i, current.get(i));
            }
        }

        // Line 5 (36–44) = stained glass
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta gMeta = glass.getItemMeta();
        gMeta.setDisplayName(" ");
        glass.setItemMeta(gMeta);
        for (int slot = 36; slot <= 44; slot++) {
            kitInv.setItem(slot, glass);
        }

        // Back at 48
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GREEN + "Go Back");
        backMeta.setLore(Arrays.asList(ChatColor.GRAY + "To Kit Editor"));
        back.setItemMeta(backMeta);
        kitInv.setItem(48, back);

        // Save at 49
        ItemStack save = new ItemStack(Material.CHEST);
        ItemMeta saveMeta = save.getItemMeta();
        saveMeta.setDisplayName(ChatColor.GREEN + "Save Layout");
        saveMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Save your current layout",
                ChatColor.GRAY + "for " + mode.getDisplayName() + "s.",
                "",
                ChatColor.YELLOW + "Click to save!"
        ));
        save.setItemMeta(saveMeta);
        kitInv.setItem(49, save);

        // Reset at 50
        ItemStack reset = new ItemStack(Material.BARRIER);
        ItemMeta resetMeta = reset.getItemMeta();
        resetMeta.setDisplayName(ChatColor.RED + "Reset Layout");
        resetMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Reset your layout",
                ChatColor.GRAY + "to the default kit.",
                "",
                ChatColor.YELLOW + "Click to reset!"
        ));
        reset.setItemMeta(resetMeta);
        kitInv.setItem(50, reset);

        editing.put(p.getUniqueId(), mode);
        p.openInventory(kitInv);
    }

    private void saveLayoutFromInventory(Player p, GameMode mode, Inventory inv) {
        java.util.List<ItemStack> saved = new java.util.ArrayList<ItemStack>();

        // 0–35: player layout
        for (int i = 0; i <= 35; i++) {
            ItemStack it = inv.getItem(i);
            if (it == null || it.getType() == Material.AIR) {
                saved.add(null);
            } else {
                saved.add(it);
            }
        }

        // Append armor from default kit
        java.util.List<ItemStack> def = plugin.getKitManager().getKit(mode);
        if (def != null && def.size() >= 4) {
            int size = def.size();
            saved.add(def.get(size - 4));
            saved.add(def.get(size - 3));
            saved.add(def.get(size - 2));
            saved.add(def.get(size - 1));
        }

        ItemStack[] result = saved.toArray(new ItemStack[0]);
        plugin.getPlayerKitManager().set(p.getUniqueId(), mode, result);
    }

    private void resetLayout(Player p, GameMode mode, Inventory inv) {
        inv.clear();

        java.util.List<ItemStack> def = plugin.getKitManager().getKit(mode);

        if (def != null) {
            int limit = Math.min(def.size(), 36);
            for (int i = 0; i < limit; i++) {
                inv.setItem(i, def.get(i));
            }
        }

        // Glass line
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        ItemMeta gMeta = glass.getItemMeta();
        gMeta.setDisplayName(" ");
        glass.setItemMeta(gMeta);
        for (int slot = 36; slot <= 44; slot++) {
            inv.setItem(slot, glass);
        }

        // Back
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName(ChatColor.GREEN + "Go Back");
        backMeta.setLore(Arrays.asList(ChatColor.GRAY + "To Kit Editor"));
        back.setItemMeta(backMeta);
        inv.setItem(48, back);

        // Save
        ItemStack save = new ItemStack(Material.CHEST);
        ItemMeta saveMeta = save.getItemMeta();
        saveMeta.setDisplayName(ChatColor.GREEN + "Save Layout");
        saveMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Save your current layout",
                ChatColor.GRAY + "for " + mode.getDisplayName() + "s.",
                "",
                ChatColor.YELLOW + "Click to save!"
        ));
        save.setItemMeta(saveMeta);
        inv.setItem(49, save);

        // Reset
        ItemStack reset = new ItemStack(Material.BARRIER);
        ItemMeta resetMeta = reset.getItemMeta();
        resetMeta.setDisplayName(ChatColor.RED + "Reset Layout");
        resetMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Reset your layout",
                ChatColor.GRAY + "to the default kit.",
                "",
                ChatColor.YELLOW + "Click to reset!"
        ));
        reset.setItemMeta(resetMeta);
        inv.setItem(50, reset);

        saveLayoutFromInventory(p, mode, inv);
    }
}
