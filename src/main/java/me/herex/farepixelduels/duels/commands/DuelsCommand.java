package me.herex.farepixelduels.duels.commands;

import me.herex.farepixelduels.duels.Arena;
import me.herex.farepixelduels.duels.ArenaManager;
import me.herex.farepixelduels.duels.DuelType;
import me.herex.farepixelduels.duels.GameMode;
import me.herex.farepixelduels.duels.KitManager;
import me.herex.farepixelduels.duels.SetupSession;
import me.herex.farepixelduels.listeners.KitEditorListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DuelsCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final ArenaManager arenaManager;
    private final KitManager kitManager;
    private final Map<UUID, SetupSession> setupSessions;
    private final Map<UUID, Arena> buildingArenas;
    private final Map<UUID, GameMode> kitEditors;

    public DuelsCommand(JavaPlugin plugin,
                        ArenaManager arenaManager,
                        KitManager kitManager,
                        Map<UUID, SetupSession> setupSessions,
                        Map<UUID, Arena> buildingArenas,
                        Map<UUID, GameMode> kitEditors) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.kitManager = kitManager;
        this.setupSessions = setupSessions;
        this.buildingArenas = buildingArenas;
        this.kitEditors = kitEditors;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player p = (Player) sender;

        if (!p.hasPermission("farepixelduels.admin")) {
            p.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(ChatColor.RED + "Admin Duels Commands:");
            p.sendMessage(ChatColor.YELLOW + "/duels adminsetup <arena> <gamemode> <type> <world>");
            p.sendMessage(ChatColor.YELLOW + "/duels adminkiteditor <gamemode>");
            p.sendMessage(ChatColor.YELLOW + "/duels reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            arenaManager.load();
            p.sendMessage(ChatColor.GREEN + "FarepixelDuels reloaded.");
            return true;
        }

        if (args[0].equalsIgnoreCase("adminsetup")) {
            if (args.length != 5) {
                p.sendMessage(ChatColor.RED + "Usage: /duels adminsetup <arena> <gamemode> <type> <world>");
                return true;
            }
            String arenaName = args[1];
            GameMode mode = GameMode.fromString(args[2]);
            DuelType type = DuelType.fromString(args[3]);
            String worldName = args[4];

            if (mode == null) {
                p.sendMessage(ChatColor.RED + "Unknown gamemode: " + args[2]);
                return true;
            }
            if (type == null) {
                p.sendMessage(ChatColor.RED + "Unknown duel type: " + args[3]);
                return true;
            }

            World w = Bukkit.getWorld(worldName);
            if (w == null) {
                p.sendMessage(ChatColor.RED + "World not found: " + worldName);
                return true;
            }

            setupSessions.put(p.getUniqueId(), new SetupSession(arenaName, mode, type, worldName));
            Arena arena = new Arena(arenaName, mode, type, worldName);
            buildingArenas.put(p.getUniqueId(), arena);

            p.teleport(w.getSpawnLocation());
            p.sendMessage(ChatColor.GOLD + "Arena setup mode enabled for '" + arenaName + "' (" + mode.id() + " " + type.display() + ").");
            p.sendMessage(ChatColor.YELLOW + "Right-click the block where Player #1 should spawn.");
            if (type == DuelType.TWO_VS_TWO) {
                p.sendMessage(ChatColor.GRAY + "You will set 4 spawn points (P1, P2, P3, P4).");
            } else {
                p.sendMessage(ChatColor.GRAY + "You will set 2 spawn points (P1, P2).");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("adminkiteditor")) {
            if (args.length != 2) {
                p.sendMessage(ChatColor.RED + "Usage: /duels adminkiteditor <gamemode>");
                return true;
            }

            GameMode mode = GameMode.fromString(args[1]);
            if (mode == null) {
                p.sendMessage(ChatColor.RED + "Unknown gamemode: " + args[1]);
                return true;
            }

            // 54-slot admin editor, last 4 slots used as armor when applied
            String title = KitEditorListener.ADMIN_TITLE_PREFIX + mode.getDisplayName();
            Inventory inv = Bukkit.createInventory(p, 54, title);

            List<org.bukkit.inventory.ItemStack> kit = kitManager.getKit(mode);
            for (int i = 0; i < kit.size() && i < inv.getSize(); i++) {
                inv.setItem(i, kit.get(i));
            }

            kitEditors.put(p.getUniqueId(), mode);
            p.openInventory(inv);
            p.sendMessage(ChatColor.YELLOW + "Edit the default kit for " + mode.getDisplayName() + ", then close the inventory to save.");
            return true;
        }

        p.sendMessage(ChatColor.RED + "Unknown subcommand.");
        return true;
    }
}
