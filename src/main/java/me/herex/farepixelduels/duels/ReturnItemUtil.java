package me.herex.farepixelduels.duels;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ReturnItemUtil {

    private static final String BASE = "duels.return_item.";

    public static void give(Player p, JavaPlugin plugin) {
        if (!plugin.getConfig().getBoolean(BASE + "enabled", true)) return;

        int slot = plugin.getConfig().getInt(BASE + "slot", 8);
        String name = plugin.getConfig().getString(BASE + "name", "&cReturn to Lobby &7(Right Click)");

        ItemStack bed = new ItemStack(Material.BED);
        ItemMeta meta = bed.getItemMeta();
        meta.setDisplayName(color(name));

        List<String> loreList = new ArrayList<String>();
        if (plugin.getConfig().isList(BASE + "lore")) {
            for (String line : plugin.getConfig().getStringList(BASE + "lore")) {
                loreList.add(color(line));
            }
        } else {
            String line = plugin.getConfig().getString(BASE + "lore", "");
            if (line != null && !line.isEmpty()) {
                loreList.add(color(line));
            }
        }
        meta.setLore(loreList);

        bed.setItemMeta(meta);
        p.getInventory().setItem(slot, bed);
        p.updateInventory();
    }

    public static void remove(Player p, JavaPlugin plugin) {
        int slot = plugin.getConfig().getInt(BASE + "slot", 8);
        ItemStack current = p.getInventory().getItem(slot);
        if (current != null && current.getType() == Material.BED) {
            p.getInventory().setItem(slot, null);
            p.updateInventory();
        }
    }

    public static boolean isReturnItem(ItemStack item, JavaPlugin plugin) {
        if (item == null || item.getType() != Material.BED || !item.hasItemMeta()) return false;
        String cfgName = plugin.getConfig().getString(BASE + "name", "&cReturn to Lobby &7(Right Click)");
        String expected = color(cfgName);
        String given = item.getItemMeta().getDisplayName();
        if (given == null) return false;

        // Compare without colors, case-insensitive
        String s1 = ChatColor.stripColor(expected);
        String s2 = ChatColor.stripColor(given);
        return s1.equalsIgnoreCase(s2);
    }

    private static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }
}
