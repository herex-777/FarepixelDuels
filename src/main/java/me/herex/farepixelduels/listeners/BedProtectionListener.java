package me.herex.farepixelduels.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BedProtectionListener implements Listener {

    @EventHandler
    public void onBedBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.BED_BLOCK) {
            // Prevent players from breaking the bed – it will no longer disappear
            e.setCancelled(true);
        }
    }
}
