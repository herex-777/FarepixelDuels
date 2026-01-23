package me.herex.farepixelduels.duels;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public final class ReturnItemUtil {
    private ReturnItemUtil() {}

    public static int slot(JavaPlugin plugin) {
        int s = plugin.getConfig().getInt("duels.return_item.slot", 8);
        return Math.min(8, Math.max(0, s));
    }

    public static ItemStack item(JavaPlugin plugin) {
        ItemStack bed = new ItemStack(Material.BED, 1);
        ItemMeta meta = bed.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("duels.return_item.name", "&cReturn to Lobby &7(Right Click)")));
        meta.setLore(Arrays.asList(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("duels.return_item.lore", "&7Right-click to leave to the lobby!"))));
        bed.setItemMeta(meta);
        return bed;
    }

    public static boolean isReturnItem(ItemStack it, JavaPlugin plugin) {
        if (it == null || it.getType() != Material.BED) return false;
        if (!it.hasItemMeta() || !it.getItemMeta().hasDisplayName()) return false;
        String want = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("duels.return_item.name", "&cReturn to Lobby &7(Right Click)")));
        String have = ChatColor.stripColor(it.getItemMeta().getDisplayName());
        return have != null && want != null && have.equalsIgnoreCase(want);
    }

    public static void give(Player p, JavaPlugin plugin) {
        int s = slot(plugin);
        p.getInventory().setItem(s, item(plugin));
        p.updateInventory();
    }

    public static void remove(Player p, JavaPlugin plugin) {
        int s = slot(plugin);
        p.getInventory().setItem(s, null);
        p.updateInventory();
    }
}
