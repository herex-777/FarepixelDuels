package me.herex.farepixelduels.duels;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class ColorUtil {
    private ColorUtil() {}

    public static String cc(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }

    public static String papi(Plugin plugin, Player p, String s) {
        if (s == null) return "";
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return cc(s.replace("%player%", p != null ? p.getName() : ""));
        }
        return cc(PlaceholderAPI.setPlaceholders(p, s.replace("%player%", p != null ? p.getName() : "")));
    }
}
