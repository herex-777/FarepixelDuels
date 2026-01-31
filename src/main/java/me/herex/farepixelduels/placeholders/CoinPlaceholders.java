package me.herex.farepixelduels.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.coins.CoinManager;
import me.herex.farepixelduels.duels.GameMode;
import me.herex.farepixelduels.duels.StatsManager;
import org.bukkit.OfflinePlayer;

import java.text.NumberFormat;

public class CoinPlaceholders extends PlaceholderExpansion {

    private final FarepixelDuels plugin;
    private final CoinManager coinManager;
    private final StatsManager statsManager;

    public CoinPlaceholders(FarepixelDuels plugin, CoinManager coinManager, StatsManager statsManager) {
        this.plugin = plugin;
        this.coinManager = coinManager;
        this.statsManager = statsManager;
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
        if (params == null) return "0";

        // Coins
        if (params.equalsIgnoreCase("coins")) {
            return player == null ? "0" : String.valueOf(coinManager.getCoins(player));
        }

        if (params.equalsIgnoreCase("coins_commas")) {
            return player == null ? "0" : NumberFormat.getInstance().format(coinManager.getCoins(player));
        }

        // Playing counts
        if (params.equalsIgnoreCase("totalplaying")) {
            if (plugin.getDuelQueueManager() == null) return "0";
            return String.valueOf(plugin.getDuelQueueManager().getTotalPlaying());
        }

        if (params.toLowerCase().startsWith("playing_")) {
            if (plugin.getDuelQueueManager() == null) return "0";
            String modeName = params.substring("playing_".length());
            GameMode mode = GameMode.fromString(modeName);
            return String.valueOf(plugin.getDuelQueueManager().getPlayingInMode(mode));
        }

        // Duels stats
        if (player != null && statsManager != null) {
            if (params.equalsIgnoreCase("wins")) return String.valueOf(statsManager.getWins(player.getUniqueId()));
            if (params.equalsIgnoreCase("kills")) return String.valueOf(statsManager.getKills(player.getUniqueId()));
            if (params.equalsIgnoreCase("winstreak")) return String.valueOf(statsManager.getWinstreak(player.getUniqueId()));
            if (params.equalsIgnoreCase("best_winstreak")) return String.valueOf(statsManager.getBestWinstreak(player.getUniqueId()));

            if (params.equalsIgnoreCase("wins_commas")) return NumberFormat.getInstance().format(statsManager.getWins(player.getUniqueId()));
            if (params.equalsIgnoreCase("kills_commas")) return NumberFormat.getInstance().format(statsManager.getKills(player.getUniqueId()));
            if (params.equalsIgnoreCase("winstreak_commas")) return NumberFormat.getInstance().format(statsManager.getWinstreak(player.getUniqueId()));
            if (params.equalsIgnoreCase("best_winstreak_commas")) return NumberFormat.getInstance().format(statsManager.getBestWinstreak(player.getUniqueId()));
        }

        return null;
    }
}
