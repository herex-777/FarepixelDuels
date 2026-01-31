package me.herex.farepixelduels.coins;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;

public class CoinsCommand implements CommandExecutor {

    private final CoinManager coinManager;

    public CoinsCommand(CoinManager coinManager) {
        this.coinManager = coinManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 3) {
            sender.sendMessage("§cUsage: /coins <give|remove|set> <player> <amount>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        int amount;

        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cAmount must be a number.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                coinManager.addCoins(target, amount);
                break;
            case "remove":
                coinManager.removeCoins(target, amount);
                break;
            case "set":
                coinManager.setCoins(target, amount);
                break;
            default:
                sender.sendMessage("§cInvalid action.");
                return true;
        }

        sender.sendMessage("§aCoins updated for " + target.getName());
        return true;
    }
}
