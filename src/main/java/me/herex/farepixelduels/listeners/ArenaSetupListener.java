package me.herex.farepixelduels.listeners;

import me.herex.farepixelduels.duels.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

public class ArenaSetupListener implements Listener {

    private final JavaPlugin plugin;
    private final ArenaManager arenaManager;
    private final Map<UUID, SetupSession> sessions;
    private final Map<UUID, Arena> building;
    private final Map<UUID, Boolean> waitingQueueSpawn = new java.util.HashMap<UUID, Boolean>();

    public ArenaSetupListener(JavaPlugin plugin, ArenaManager arenaManager, Map<UUID, SetupSession> sessions, Map<UUID, Arena> building) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.sessions = sessions;
        this.building = building;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!sessions.containsKey(p.getUniqueId())) return;

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;

        e.setCancelled(true);

        SetupSession s = sessions.get(p.getUniqueId());
        Arena arena = building.get(p.getUniqueId());
        if (arena == null) return;

        Location loc = e.getClickedBlock().getLocation().add(0.5, 1.0, 0.5);
        loc.setYaw(p.getLocation().getYaw());
        loc.setPitch(p.getLocation().getPitch());

        int needed = s.getType().getSize();

        // After spawns set, we ask for queue spawn
        boolean needQueue = waitingQueueSpawn.containsKey(p.getUniqueId());

        if (needQueue) {
            arena.setQueueSpawn(loc);
            arenaManager.saveArena(arena);
            sessions.remove(p.getUniqueId());
            building.remove(p.getUniqueId());
            waitingQueueSpawn.remove(p.getUniqueId());

            p.sendMessage(ChatColor.GOLD + "Arena setup complete! Saved arena '" + arena.getName() + "' (" + s.getMode().id() + " " + s.getType().display() + ").");
            p.sendMessage(ChatColor.GREEN + "Queue spawn saved too.");
            return;
        }

        int current = arena.getSpawns().size();
        if (current >= needed) {
            p.sendMessage(ChatColor.YELLOW + "Now right-click the block for the queue spawn (where players wait before the duel).");
            waitingQueueSpawn.put(p.getUniqueId(), true);
            return;
        }

        arena.getSpawns().add(loc);
        p.sendMessage(ChatColor.GREEN + "Set spawn point " + (current + 1) + "/" + needed + ".");

        if (arena.getSpawns().size() >= needed) {
            p.sendMessage(ChatColor.YELLOW + "Right-click the block for the queue spawn (where players wait before the duel).");
            waitingQueueSpawn.put(p.getUniqueId(), true);
        } else {
            p.sendMessage(ChatColor.YELLOW + "Right-click the next block for spawn point " + (current + 2) + ".");
        }
    }
}
