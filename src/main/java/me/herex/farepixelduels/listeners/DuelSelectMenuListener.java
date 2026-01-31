package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.duels.GameMode;
import me.herex.farepixelduels.duels.commands.DuelCommand;
import me.herex.farepixelduels.duels.commands.DuelSelectMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class DuelSelectMenuListener implements Listener {

    private final FarepixelDuels plugin;
    private final DuelCommand duelCommand;

    public DuelSelectMenuListener(FarepixelDuels plugin, DuelCommand duelCommand) {
        this.plugin = plugin;
        this.duelCommand = duelCommand;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        Inventory top = e.getView().getTopInventory();
        if (top == null) return;

        String title = ChatColor.stripColor(e.getView().getTitle());
        String prefix = ChatColor.stripColor(DuelSelectMenu.TITLE_PREFIX);

        if (!title.startsWith(prefix)) return;

        if (e.getRawSlot() >= top.getSize()) return;
        e.setCancelled(true);

        ItemStack it = e.getCurrentItem();
        if (it == null || it.getType() == Material.AIR) return;

        // Close barrier
        if (it.getType() == Material.BARRIER) {
            p.closeInventory();
            return;
        }

        UUID targetId = DuelSelectMenu.VIEW_TARGET.get(p.getUniqueId());
        if (targetId == null) return;

        Player target = Bukkit.getPlayer(targetId);
        if (target == null) {
            p.closeInventory();
            p.sendMessage("§cThat player is no longer online.");
            return;
        }

        GameMode mode = null;
        switch (e.getRawSlot()) {
            case 10: mode = GameMode.CLASSIC;  break;
            case 11: mode = GameMode.BOW;      break;
            case 12: mode = GameMode.BOXING;   break;
            case 13: mode = GameMode.NODEBUFF; break;
            case 14: mode = GameMode.OP;       break;
            case 15: mode = GameMode.SUMO;     break;
            case 16: mode = GameMode.UHC;      break;
            default: break;
        }

        if (mode == null) return;

        p.closeInventory();
        duelCommand.sendInviteFromMenu(p, target, mode);
    }
}
