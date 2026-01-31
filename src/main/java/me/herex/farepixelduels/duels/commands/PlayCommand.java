package me.herex.farepixelduels.duels.commands;

import me.herex.farepixelduels.duels.DuelQueueManager;
import me.herex.farepixelduels.duels.DuelType;
import me.herex.farepixelduels.duels.GameMode;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayCommand implements CommandExecutor {

    private final DuelQueueManager queue;

    public PlayCommand(DuelQueueManager queue) {
        this.queue = queue;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player p = (Player) sender;

        if (args.length < 2) {
            p.sendMessage(ChatColor.RED + "Usage: /play <mode> <1v1|2v2>");
            return true;
        }

        GameMode mode = GameMode.fromString(args[0]);
        if (mode == null) {
            p.sendMessage(ChatColor.RED + "Unknown mode. Use: classic, bow, combo, boxing, op, sumo, uhc");
            return true;
        }

        DuelType type;
        String t = args[1].toLowerCase();
        if (t.equals("1v1")) type = DuelType.ONE_VS_ONE;
        else if (t.equals("2v2")) type = DuelType.TWO_VS_TWO;
        else {
            p.sendMessage(ChatColor.RED + "Unknown type. Use: 1v1 or 2v2");
            return true;
        }

        if (queue.isInQueue(p) || queue.isInMatch(p)) {
            p.sendMessage(ChatColor.RED + "You are already queued or in a duel.");
            return true;
        }

        queue.joinQueue(p, mode, type);
        return true;
    }
}
