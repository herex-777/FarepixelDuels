package me.herex.farepixelduels.duels;

import me.herex.farepixelduels.coins.CoinManager;
import me.herex.farepixelduels.spawn.SpawnUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DuelQueueManager {

    private final JavaPlugin plugin;
    private final ArenaManager arenaManager;
    private final KitManager kitManager;
    private final ScoreboardManager scoreboardManager;
    private final StatsManager statsManager;
    private final CoinManager coinManager;

    // mode+type -> waiting list
    private final Map<String, List<UUID>> queues = new HashMap<String, List<UUID>>();
    private final Map<String, Arena> selectedArena = new HashMap<String, Arena>();
    // store last mode/type for play again
    private final Map<UUID, GameMode> lastMode = new HashMap<UUID, GameMode>();
    private final Map<UUID, DuelType> lastType = new HashMap<UUID, DuelType>();
    // players currently in match
    private final Map<UUID, DuelMatch> playerMatch = new HashMap<UUID, DuelMatch>();
    private final Map<UUID, GameMode> playerMode = new HashMap<UUID, GameMode>();

    private int animTick = 0;

    public DuelQueueManager(JavaPlugin plugin,
                            ArenaManager arenaManager,
                            KitManager kitManager,
                            ScoreboardManager scoreboardManager,
                            StatsManager statsManager,
                            CoinManager coinManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.kitManager = kitManager;
        this.scoreboardManager = scoreboardManager;
        this.statsManager = statsManager;
        this.coinManager = coinManager;

        startQueueBoardTask();
    }

    private String key(GameMode m, DuelType t) {
        return m.id() + ":" + t.display();
    }

    public DuelMatch getMatch(Player p) {
        return playerMatch.get(p.getUniqueId());
    }

    public boolean isInMatch(Player p) {
        return playerMatch.containsKey(p.getUniqueId());
    }

    public boolean isInQueue(Player p) {
        UUID u = p.getUniqueId();
        for (List<UUID> list : queues.values()) {
            if (list.contains(u)) return true;
        }
        return false;
    }

    public int getTotalPlaying() {
        return playerMatch.size();
    }

    public int getPlayingInMode(GameMode mode) {
        if (mode == null) return 0;
        int c = 0;
        for (GameMode m : playerMode.values()) {
            if (m == mode) c++;
        }
        return c;
    }

    /**
     * OLD logic used by /leave or similar.
     * Now delegates to leaveInternal so we don't duplicate buggy logic.
     */
    public boolean leaveAllWithResult(Player p) {
        return leaveInternal(p, true);
    }

    public void leaveAll(Player p) {
        leaveInternal(p, true);
    }

    // ========= JOIN QUEUE =========

    public void joinQueue(Player p, GameMode mode, DuelType type) {
        if (isInMatch(p)) {
            p.sendMessage(ColorUtil.cc("&cYou are already in a duel."));
            return;
        }
        if (isInQueue(p)) {
            p.sendMessage(ColorUtil.cc("&cYou are already in a queue. Use /leave to leave it."));
            return;
        }

        List<Arena> arenas = arenaManager.getReadyArenas(mode, type);
        if (arenas.isEmpty()) {
            p.sendMessage(ColorUtil.cc("&cNo arenas are ready for this mode/type."));
            return;
        }

        String k = key(mode, type);

        // Pick/select map for this queue if none selected yet
        Arena a = selectedArena.get(k);
        if (a == null) {
            a = arenas.get(new Random().nextInt(arenas.size()));
            selectedArena.put(k, a);
        }

        List<UUID> q = queues.get(k);
        if (q == null) {
            q = new ArrayList<UUID>();
            queues.put(k, q);
        }

        q.add(p.getUniqueId());

        // Teleport to queue spawn of selected arena (world of the map)
        if (a.getQueueSpawn() != null) {
            p.teleport(a.getQueueSpawn());
        } else if (a.getWorld() != null) {
            p.teleport(a.getWorld().getSpawnLocation());
        }

        // Give return-to-lobby item while in queue
        ReturnItemUtil.give(p, plugin);

        tryStart(mode, type);
    }

    // ========= START A MATCH WHEN ENOUGH PLAYERS =========

    private void tryStart(GameMode mode, DuelType type) {
        String k = key(mode, type);
        List<UUID> q = queues.get(k);
        if (q == null) return;

        int need = type.getSize();
        if (q.size() < need) return;

        Arena arena = selectedArena.get(k);
        if (arena == null) {
            List<Arena> arenas = arenaManager.getReadyArenas(mode, type);
            if (arenas.isEmpty()) return;
            arena = arenas.get(0);
        }

        List<Player> players = new ArrayList<Player>();
        for (int i = 0; i < need; i++) {
            UUID uid = q.remove(0);
            Player pl = Bukkit.getPlayer(uid);
            if (pl != null && pl.isOnline()) players.add(pl);
        }

        // clear selected arena for next queue after using it
        selectedArena.remove(k);

        if (players.size() < need) {
            // not enough online -> put them back in queue front
            for (Player pl : players) {
                q.add(0, pl.getUniqueId());
            }
            return;
        }

        DuelMatch match = new DuelMatch(plugin, arena, mode, type,
                kitManager, this, scoreboardManager, statsManager, coinManager);

        for (Player pl : players) {
            playerMatch.put(pl.getUniqueId(), match);
            playerMode.put(pl.getUniqueId(), mode);
            match.addPlayer(pl);
        }

        match.startWaiting();
    }

    // Called by DuelMatch when a match fully ends (after teleport back)
    void onMatchEnd(DuelMatch match) {
        for (UUID u : new ArrayList<UUID>(playerMatch.keySet())) {
            if (playerMatch.get(u) == match) {
                playerMatch.remove(u);
                playerMode.remove(u);
            }
        }
    }

    public void setLastMode(Player p, GameMode mode, DuelType type) {
        lastMode.put(p.getUniqueId(), mode);
        lastType.put(p.getUniqueId(), type);
    }

    public void queuePlayAgain(Player p) {
        GameMode m = lastMode.get(p.getUniqueId());
        DuelType t = lastType.get(p.getUniqueId());
        if (m == null || t == null) {
            p.sendMessage(ColorUtil.cc("&cNo previous duel to play again."));
            return;
        }
        joinQueue(p, m, t);
    }

    // ========= LEAVE API (USED BY /leave AND BED) =========

    // Used by bed item (no extra messages, optional teleport)
    public boolean leaveAllSilent(Player p) {
        return leaveInternal(p, false);
    }

    // Used by /leave (teleport back to lobby)
    public boolean leaveNormal(Player p) {
        return leaveInternal(p, true);
    }

    /**
     * Core leave method that correctly handles:
     * - leaving queue
     * - leaving during COUNTDOWN (pre-start) => cancel match, no win/loss
     * - leaving during PLAYING => forfeit
     */
    private boolean leaveInternal(Player p, boolean teleport) {
        boolean touched = false;
        UUID u = p.getUniqueId();

        // remove from queues
        for (List<UUID> q : queues.values()) {
            if (q.remove(u)) touched = true;
        }

        DuelMatch m = null;
        if (m.getState() == DuelMatch.MatchState.PLAYING) {
            // Treat as forfeit, but don’t show DEFEAT title to the leaver
            m.markLeaverNoDefeat(p);
            m.handleQuit(p);
        } else {
            // QUEUE/COUNTDOWN — no forfeit; return remaining players to queue spawn
            cancelPreStartMatch(m, p);
        }

        // remove from match
        m = playerMatch.remove(u);
        if (m != null) {
            touched = true;
            playerMode.remove(u);

            switch (m.getState()) {
                case PLAYING:
                    // Legit forfeit
                    m.handleQuit(p);
                    break;
                case COUNTDOWN:
                    // Pre-start: cancel the match and safely return other players to queue
                    m.cancelPreStart(); // VERY IMPORTANT so countdown stops and fight never starts
                    cancelPreStartMatch(m, p);
                    break;
                default:
                    // ENDED or unknown, do nothing special
                    break;
            }
        } else {
            playerMode.remove(u);
        }

        if (touched) {
            scoreboardManager.clear(p);
            ReturnItemUtil.remove(p, plugin);
            if (teleport) {
                Location lobby = SpawnUtil.getLobbySpawn(plugin);
                if (lobby != null) p.teleport(lobby);
            }
        }
        return touched;
    }

    /**
     * Called when a pre-start match (COUNTDOWN) gets cancelled because
     * one of the players left using bed or /leave.
     *
     * Remaining players are put back into queue and teleported to queue spawn.
     */
    private void cancelPreStartMatch(DuelMatch match, Player leaver) {
        String k = key(match.getMode(), match.getType());

        // Keep the same arena selection for this queue key
        selectedArena.put(k, match.getArena());

        List<UUID> q = queues.get(k);
        if (q == null) {
            q = new ArrayList<UUID>();
            queues.put(k, q);
        }

        for (UUID u : match.getAllPlayers()) {
            if (leaver != null && u.equals(leaver.getUniqueId())) continue;

            if (!q.contains(u)) q.add(u);
            Player other = Bukkit.getPlayer(u);
            if (other != null && match.getArena().getQueueSpawn() != null) {
                other.teleport(match.getArena().getQueueSpawn());
                ReturnItemUtil.give(other, plugin);
            }
            playerMatch.remove(u);
            playerMode.remove(u);
        }
    }

    // ========= QUEUE SIDEBAR ANIMATION =========

    private void startQueueBoardTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                animTick++;
                String dots;
                int m = animTick % 4;
                if (m == 0) dots = "";
                else if (m == 1) dots = ".";
                else if (m == 2) dots = "..";
                else dots = "...";

                for (Map.Entry<String, List<UUID>> entry : queues.entrySet()) {
                    String k = entry.getKey();
                    List<UUID> q = entry.getValue();
                    if (q == null || q.isEmpty()) continue;

                    Arena arena = selectedArena.get(k);
                    int max = 2;
                    if (k.contains(":2v2")) max = 4;

                    for (UUID u : new ArrayList<UUID>(q)) {
                        Player p = Bukkit.getPlayer(u);
                        if (p == null) continue;
                        scoreboardManager.showWaiting(p, arena, q.size(), max, ColorUtil.cc("&7Waiting" + dots));
                        ReturnItemUtil.give(p, plugin);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
}
