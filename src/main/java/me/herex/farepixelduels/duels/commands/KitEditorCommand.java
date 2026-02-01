package me.herex.farepixelduels.duels.commands;

import me.herex.farepixelduels.duels.GameMode;
import me.herex.farepixelduels.listeners.PlayerKitMenuListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class KitEditorCommand implements CommandExecutor {

    public static final String GUI_TITLE = ChatColor.DARK_GRAY + "Kit Editor";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player p = (Player) sender;
        openMainGui(p);
        return true;
    }

    public void openMainGui(Player p) {
        Inventory inv = Bukkit.createInventory(null, 45, GUI_TITLE);

        // Add items for each game mode
        inv.setItem(10, modeItem(GameMode.CLASSIC, Material.FISHING_ROD, "&eClassic Duel"));
        inv.setItem(11, modeItem(GameMode.BOW, Material.BOW, "&eBow Duel"));
        inv.setItem(12, modeItem(GameMode.BOXING, Material.RAW_FISH, "&eBoxing Duel"));
        inv.setItem(13, modeItem(GameMode.NODEBUFF, Material.POTION, "&eNoDebuff Duel"));
        inv.setItem(14, modeItem(GameMode.OP, Material.DIAMOND_CHESTPLATE, "&eOP Duel"));
        inv.setItem(15, modeItem(GameMode.SUMO, Material.SLIME_BALL, "&eSumo Duel"));
        inv.setItem(16, modeItem(GameMode.UHC, Material.LAVA_BUCKET, "&eUHC Duel"));

        p.openInventory(inv);
    }

    private ItemStack modeItem(GameMode mode, Material material, String name) {
        ItemStack it = new ItemStack(material);
        ItemMeta meta = it.getItemMeta();

        meta.setDisplayName(color(name));

        List<String> lore = new ArrayList<>();
        lore.add(color("&7Edit your inventory layout for"));
        lore.add(color("&a" + formatModeName(mode) + ChatColor.GRAY + "."));
        lore.add("");
        lore.add(color("&eClick to edit!"));

        meta.setLore(lore);
        it.setItemMeta(meta);
        return it;
    }

    private String formatModeName(GameMode mode) {
        String s = mode.name().toLowerCase();
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
