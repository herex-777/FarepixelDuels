package me.herex.farepixelduels.duels.commands;

import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.duels.DuelQueueManager;
import me.herex.farepixelduels.duels.DuelRequestManager;
import me.herex.farepixelduels.duels.GameMode;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class DuelCommand implements CommandExecutor {

    private final FarepixelDuels plugin;
    private final DuelRequestManager requests;

    public DuelCommand(FarepixelDuels plugin, DuelRequestManager requests) {
        this.plugin = plugin;
        this.requests = requests;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player p = (Player) sender;

        if (args.length == 0) {
            p.sendMessage("§cUsage: /duel <player> [mode]");
            p.sendMessage("§cUsage: /duel accept <player>");
            p.sendMessage("§cUsage: /duel deny <player>");
            return true;
        }

        // always resolve queue from plugin at runtime
        DuelQueueManager queue = plugin.getDuelQueueManager();

        // /duel accept <player>
        if (args[0].equalsIgnoreCase("accept") && args.length >= 2) {
            Player challenger = Bukkit.getPlayer(args[1]);
            if (challenger == null) {
                bar(p, "§cThat player is not online!");
                return true;
            }

            if (!requests.isWaitingTarget(p, challenger)) {
                bar(p, "§cYou don't have any duel requests from that player.");
                return true;
            }

            if (queue == null) {
                bar(p, "§cDuels are not configured correctly. Please contact an administrator.");
                return true;
            }

            // don't allow accept while in queue/match
            if (queue.isInMatch(p) || queue.isInQueue(p)) {
                bar(p, "§cYou are in-game! Please try again later.");
                return true;
            }
            if (queue.isInMatch(challenger) || queue.isInQueue(challenger)) {
                bar(p, "§cThat player is in-game! Please try again later.");
                return true;
            }

            GameMode mode = requests.getPendingMode(challenger);
            if (mode == null) mode = GameMode.CLASSIC;

            requests.clear(challenger);

            bar(p, "§aYou have accepted §f" + challenger.getName() + "§a's Duel request!");
            bar(challenger, "§a" + p.getName() + " accepted your duel request!");

            // start private 1v1 duel, stats OFF (ranked = false)
            queue.startPrivateDuel(challenger, p, mode);
            return true;
        }

        // /duel deny <player>
        if (args[0].equalsIgnoreCase("deny") && args.length >= 2) {
            Player challenger = Bukkit.getPlayer(args[1]);
            if (challenger == null) {
                bar(p, "§cThat player is not online!");
                return true;
            }

            if (!requests.isWaitingTarget(p, challenger)) {
                bar(p, "§cYou don't have any duel requests from that player.");
                return true;
            }

            requests.clear(challenger);

            bar(p, "§eYou have denied §f" + challenger.getName() + "§e's Duel request.");
            bar(challenger, "§eThe Duel request to §f" + p.getName() + "§e has been denied.");
            return true;
        }

        // /duel <player> [mode]
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            bar(p, "§cThat player isn't online!");
            return true;
        }
        if (target.equals(p)) {
            bar(p, "§cYou cannot duel yourself!");
            return true;
        }

        if (queue != null) {
            if (queue.isInMatch(target) || queue.isInQueue(target)) {
                bar(p, "§cThat player is in-game! Please try again later.");
                return true;
            }
            if (queue.isInMatch(p) || queue.isInQueue(p)) {
                bar(p, "§cYou are in-game! Please try again later.");
                return true;
            }
        }

        // /duel <player> <mode> -> direct request
        if (args.length >= 2) {
            GameMode mode = GameMode.fromString(args[1]);
            if (mode == null) mode = GameMode.CLASSIC;
            sendInviteFromMenu(p, target, mode);
            return true;
        }

        // /duel <player> -> open selector GUI
        DuelSelectMenu.open(plugin, p, target);
        return true;
    }

    public void sendInviteFromMenu(Player sender, Player target, GameMode mode) {
        if (sender == null || target == null || mode == null) return;

        if (requests.hasPendingFrom(sender)) {
            bar(sender, "§cYou already send a request to that player!");
            return;
        }

        requests.sendRequest(sender, target, mode);

        target.sendMessage("§6§l§m-----------------------------------------------");
        target.sendMessage("§b" + sender.getName() + " §bhas invited you to a §f" + mode.getDisplayName() + " §bDuel!");
        target.sendMessage("§eUse §6/duel accept " + sender.getName() + " §eor §6/duel deny " + sender.getName());
        target.sendMessage("§6§l§m-----------------------------------------------");

        sender.sendMessage("§6§l§m-------------------------------------------");
        sender.sendMessage("§eYou have invited §f" + target.getName() + "§e to a §f" + mode.getDisplayName() + "§e Duel");
        sender.sendMessage("§eThey have 60 seconds to accept.");
        sender.sendMessage("§6§l§m-------------------------------------------");
    }

    private void bar(Player p, String mid) {
        p.sendMessage("§6§l§m-------------------------------------------");
        p.sendMessage(mid);
        p.sendMessage("§6§l§m-------------------------------------------");
    }
}
