package me.herex.farepixelduels.duels.commands;

import me.herex.farepixelduels.FarepixelDuels;
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

        if (plugin.getDuelQueueManager() == null) return true;

        boolean was = plugin.getDuelQueueManager().leaveAllWithResult(p);
        if (!was) {
            p.sendMessage(ChatColor.RED + "You are not in a duels game.");
        }
        return true;
    }
}
