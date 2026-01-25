package me.herex.farepixelduels.spawn;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SpawnCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public SpawnCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!sender.hasPermission("farepixelduels.spawn")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        Location spawn = SpawnUtil.getLobbySpawn(plugin);
        if (spawn == null) {
            sender.sendMessage(ChatColor.RED + "Duels lobby spawn is not set yet.");
            return true;
        }

        spawn.getWorld().loadChunk(spawn.getBlockX() >> 4, spawn.getBlockZ() >> 4);
        ((Player) sender).teleport(spawn);
        sender.sendMessage(ChatColor.GREEN + "Teleported to duels lobby spawn.");
        return true;
    }
}
