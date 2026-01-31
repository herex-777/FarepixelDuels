package me.herex.farepixelduels.spawn;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SetSpawnCommand implements CommandExecutor {

    private final JavaPlugin plugin;

    public SetSpawnCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        Location loc = player.getLocation();

        plugin.getConfig().set("lobby_spawn.world", loc.getWorld().getName());
        plugin.getConfig().set("lobby_spawn.x", loc.getX());
        plugin.getConfig().set("lobby_spawn.y", loc.getY());
        plugin.getConfig().set("lobby_spawn.z", loc.getZ());
        plugin.getConfig().set("lobby_spawn.yaw", loc.getYaw());
        plugin.getConfig().set("lobby_spawn.pitch", loc.getPitch());
        plugin.saveConfig();

        player.sendMessage(ChatColor.GREEN + "Duels lobby spawn has been set to your current location.");
        return true;
    }
}
