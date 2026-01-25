package me.herex.farepixelduels.commands;

import me.herex.farepixelduels.replay.ReplayManagerImpl;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReplayCommand implements CommandExecutor {

    private final ReplayManagerImpl replayManager;

    public ReplayCommand(ReplayManagerImpl replayManager) {
        this.replayManager = replayManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player p = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            replayManager.openReplayList(p);
            return true;
        }

        if (args[0].equalsIgnoreCase("play") && args.length == 2) {
            replayManager.playReplay(p, args[1]);
            return true;
        }

        if (args[0].equalsIgnoreCase("delete") && args.length == 2) {
            replayManager.deleteReplay(p, args[1]);
            return true;
        }

        p.sendMessage(ChatColor.YELLOW + "Usage: /replay [list|play <id>|delete <id>]");
        return true;
    }
}
