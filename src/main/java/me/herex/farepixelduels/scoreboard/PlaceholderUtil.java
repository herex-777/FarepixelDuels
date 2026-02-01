package me.herex.farepixelduels.scoreboard;

import me.herex.farepixelduels.FarepixelDuels;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class PlaceholderUtil {

    private static final SimpleDateFormat DATE = new SimpleDateFormat("MM/dd/yyyy");

    private PlaceholderUtil() {}

    /**
     * Very small placeholder system for now (we can expand later when duels/stats exist).
     *
     * Supported:
     *  %player%  %world%  %online%
     *  %date%    %server_name%
     *  %coins% %wins% %kills% %winstreak% %best_winstreak% %loot_chests%
     */
    public static String apply(Player player, String line, FarepixelDuels plugin) {
        if (line == null) return "";

        String out = line;

        out = out.replace("%player%", player.getName());
        out = out.replace("%world%", player.getWorld().getName());
        out = out.replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        out = out.replace("%date%", DATE.format(new Date()));

        String serverName = plugin.getConfig().getString("server_name", "server");
        out = out.replace("%server_name%", serverName);

        // Not implemented yet (defaults to 0)
        out = out.replace("%coins%", "0");
        out = out.replace("%wins%", "0");
        out = out.replace("%kills%", "0");
        out = out.replace("%winstreak%", "0");
        out = out.replace("%best_winstreak%", "0");
        out = out.replace("%loot_chests%", "0");

        return out;
    }
}
