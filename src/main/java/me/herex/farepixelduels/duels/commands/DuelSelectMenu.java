package me.herex.farepixelduels.duels.commands;

import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.duels.ColorUtil;
import me.herex.farepixelduels.duels.GameMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuelSelectMenu {

    public static final String TITLE_PREFIX = ChatColor.DARK_GRAY + "Duel: ";

    // viewer -> target
    public static final Map<UUID, UUID> VIEW_TARGET = new HashMap<>();

    public static void open(FarepixelDuels plugin, Player viewer, Player target) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_PREFIX + target.getName());

        // 7 modes, 6 rows inventory, items on first row
        setMode(plugin, inv, 10, Material.FISHING_ROD,       "§aClassic Duel",   GameMode.CLASSIC,  target);
        setMode(plugin, inv, 11, Material.BOW,               "§aBow Duel",       GameMode.BOW,      target);
        setMode(plugin, inv, 12, Material.RAW_FISH,          "§aBoxing Duel",    GameMode.BOXING,   target);
        setMode(plugin, inv, 13, Material.POTION,            "§aNoDebuff Duel",  GameMode.NODEBUFF, target);
        setMode(plugin, inv, 14, Material.DIAMOND_CHESTPLATE,"§aOP Duel",        GameMode.OP,       target);
        setMode(plugin, inv, 15, Material.SLIME_BALL,        "§aSumo Duel",      GameMode.SUMO,     target);
        setMode(plugin, inv, 16, Material.GOLDEN_APPLE,      "§aUHC Duel",       GameMode.UHC,      target);

        // Close barrier in bottom row center (slot 49)
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta cm = close.getItemMeta();
        cm.setDisplayName(ChatColor.RED + "Close");
        close.setItemMeta(cm);
        inv.setItem(49, close);

        VIEW_TARGET.put(viewer.getUniqueId(), target.getUniqueId());
        viewer.openInventory(inv);
    }

    private static void setMode(FarepixelDuels plugin,
                                Inventory inv,
                                int slot,
                                Material mat,
                                String name,
                                GameMode mode,
                                Player target) {

        // luckperms prefix + target name
        String displayTarget = ColorUtil.papi(
                plugin,
                target,
                "%luckperms_prefix%" + target.getName()
        );

        ItemStack it = new ItemStack(mat);
        ItemMeta im = it.getItemMeta();
        im.setDisplayName(name);
        im.setLore(Arrays.asList(
                ChatColor.GRAY + "Click to invite " + ChatColor.RESET + displayTarget + ChatColor.GRAY +" to this Gamemode!"
        ));
        it.setItemMeta(im);
        inv.setItem(slot, it);
    }
}
