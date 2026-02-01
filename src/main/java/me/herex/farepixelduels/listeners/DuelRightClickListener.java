package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.duels.commands.DuelSelectMenu;
import me.herex.farepixelduels.spawn.SpawnUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import net.citizensnpcs.api.CitizensAPI;

public class DuelRightClickListener implements Listener {

    private final FarepixelDuels plugin;

    public DuelRightClickListener(FarepixelDuels plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof Player)) return;

        if (!plugin.getConfig().getBoolean("duels.duel_right_click.enabled", true)) return;

        Player player = e.getPlayer();
        Player clicker = e.getPlayer();
        Player target = (Player) e.getRightClicked();

        Entity ClickedEntity = e.getRightClicked();
        Object clickedEntity = e.getRightClicked();
        if (CitizensAPI.getNPCRegistry().isNPC((Entity) clickedEntity)) {
            return;
        }
        Location lobby = SpawnUtil.getLobbySpawn(plugin);
        if (lobby == null) return;
        DuelSelectMenu.open(plugin, player, (Player) clickedEntity);
        
        // only in lobby world
        if (clicker.getWorld() == null || lobby.getWorld() == null) return;
        if (!clicker.getWorld().getName().equalsIgnoreCase(lobby.getWorld().getName())) return;

        // open menu
        DuelSelectMenu.open(plugin, clicker, target);
        e.setCancelled(true);
    }
}
