package me.herex.farepixelduels.duels.commands;

import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.duels.DuelQueueManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements CommandExecutor {

    private final FarepixelDuels plugin;

    public LeaveCommand(FarepixelDuels plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player p = (Player) sender;

        DuelQueueManager qm = plugin.getDuelQueueManager();
        if (qm == null) {
            p.sendMessage(ChatColor.RED + "Duels are not available right now.");
            return true;
        }

        boolean touched = qm.leaveNormal(p);
        if (!touched) {
            // Silent if not in game/queue, as you asked earlier
            // just comment this out completely or keep a soft message
            // p.sendMessage(ChatColor.RED + "You are not in a duels game.");
        }
        return true;
    }
}
