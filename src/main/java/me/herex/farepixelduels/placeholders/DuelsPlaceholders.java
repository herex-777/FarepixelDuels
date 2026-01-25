package me.herex.farepixelduels.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.duels.StatsManager;
import org.bukkit.OfflinePlayer;

import java.text.NumberFormat;

public class DuelsPlaceholders extends PlaceholderExpansion {

    private final FarepixelDuels plugin;
    private final StatsManager stats;

    public DuelsPlaceholders(FarepixelDuels plugin, StatsManager stats) {
        this.plugin = plugin;
        this.stats = stats;
    }

    @Override
    public String getIdentifier() {
        return "farepixelduels";
    }

    @Override
    public String getAuthor() {
        return "Herex";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) return "0";

        if (params.equalsIgnoreCase("wins")) {
            return String.valueOf(stats.getWins(player.getUniqueId()));
        }
        if (params.equalsIgnoreCase("kills")) {
            return String.valueOf(stats.getKills(player.getUniqueId()));
        }
        if (params.equalsIgnoreCase("winstreak")) {
            return String.valueOf(stats.getWinstreak(player.getUniqueId()));
        }
        if (params.equalsIgnoreCase("best_winstreak")) {
            return String.valueOf(stats.getBestWinstreak(player.getUniqueId()));
        }

        if (params.equalsIgnoreCase("wins_commas")) {
            return NumberFormat.getInstance().format(stats.getWins(player.getUniqueId()));
        }
        if (params.equalsIgnoreCase("kills_commas")) {
            return NumberFormat.getInstance().format(stats.getKills(player.getUniqueId()));
        }
        if (params.equalsIgnoreCase("winstreak_commas")) {
            return NumberFormat.getInstance().format(stats.getWinstreak(player.getUniqueId()));
        }
        if (params.equalsIgnoreCase("best_winstreak_commas")) {
            return NumberFormat.getInstance().format(stats.getBestWinstreak(player.getUniqueId()));
        }

        return null;
    }
}
