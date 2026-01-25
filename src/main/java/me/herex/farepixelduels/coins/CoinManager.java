package me.herex.farepixelduels.coins;

import me.herex.farepixelduels.storage.CoinsRepository;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.UUID;

public class CoinManager {

    private final CoinsRepository repo;

    public CoinManager(CoinsRepository repo) {
        this.repo = repo;
    }

    public int getCoins(OfflinePlayer player) {
        if (player == null) return 0;
        return getCoins(player.getUniqueId());
    }

    public int getCoins(UUID uuid) {
        try {
            return repo.getCoins(uuid);
        } catch (SQLException e) {
            return 0;
        }
    }

    public void setCoins(OfflinePlayer player, int amount) {
        if (player == null) return;
        setCoins(player.getUniqueId(), amount);
    }

    public void setCoins(UUID uuid, int amount) {
        try {
            repo.setCoins(uuid, amount);
        } catch (SQLException ignored) {
        }
    }

    public void addCoins(OfflinePlayer player, int amount) {
        if (player == null) return;
        addCoins(player.getUniqueId(), amount);
    }

    public void addCoins(UUID uuid, int amount) {
        setCoins(uuid, getCoins(uuid) + amount);
    }

    public void removeCoins(OfflinePlayer player, int amount) {
        if (player == null) return;
        removeCoins(player.getUniqueId(), amount);
    }

    public void removeCoins(UUID uuid, int amount) {
        setCoins(uuid, getCoins(uuid) - amount);
    }
}
