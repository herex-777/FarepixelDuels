package me.herex.farepixelduels.duels;

import me.herex.farepixelduels.FarepixelDuels;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DuelRequestManager {

    private final FarepixelDuels plugin;

    // sender -> target
    private final Map<UUID, UUID> waitingFor = new ConcurrentHashMap<UUID, UUID>();
    // sender -> mode
    private final Map<UUID, GameMode> modeBySender = new ConcurrentHashMap<UUID, GameMode>();

    public DuelRequestManager(FarepixelDuels plugin) {
        this.plugin = plugin;
    }

    public boolean hasPendingFrom(Player sender) {
        return sender != null && waitingFor.containsKey(sender.getUniqueId());
    }

    public void sendRequest(Player sender, Player target, GameMode mode) {
        UUID s = sender.getUniqueId();
        UUID t = target.getUniqueId();

        waitingFor.put(s, t);
        modeBySender.put(s, mode);

        // expire in 60 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                UUID current = waitingFor.get(s);
                if (current == null) return;
                if (!current.equals(t)) return;

                waitingFor.remove(s);
                modeBySender.remove(s);

                Player sp = Bukkit.getPlayer(s);
                Player tp = Bukkit.getPlayer(t);
                if (sp != null) {
                    sp.sendMessage("§6§l§m-------------------------------------------");
                    sp.sendMessage("§eYour Duel request to §f" + (tp != null ? tp.getName() : "that player") + "§e has expired.");
                    sp.sendMessage("§6§l§m-------------------------------------------");
                }
                if (tp != null) {
                    tp.sendMessage("§6§l§m-------------------------------------------");
                    tp.sendMessage("§eThe Duel request from §f" + (sp != null ? sp.getName() : "that player") + "§e has expired.");
                    tp.sendMessage("§6§l§m-------------------------------------------");
                }
            }
        }.runTaskLater(plugin, 20L * 60L);
    }

    // target checks: does target have a pending duel FROM challenger?
    public boolean isWaitingTarget(Player target, Player challenger) {
        if (target == null || challenger == null) return false;
        UUID s = challenger.getUniqueId();
        UUID t = waitingFor.get(s);
        return t != null && t.equals(target.getUniqueId());
    }

    public GameMode getPendingMode(Player challenger) {
        if (challenger == null) return null;
        return modeBySender.get(challenger.getUniqueId());
    }

    public void clear(Player challenger) {
        if (challenger == null) return;
        UUID s = challenger.getUniqueId();
        waitingFor.remove(s);
        modeBySender.remove(s);
    }
}
