package me.herex.farepixelduels.duels;

import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.coins.CoinManager;
import me.herex.farepixelduels.replay.ReplayManagerImpl;
import me.herex.farepixelduels.spawn.SpawnUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DuelMatch {

    public enum MatchState { COUNTDOWN, PLAYING, QUEUE, ENDED }

    private final JavaPlugin plugin;
    private final Arena arena;
    private final GameMode mode;
    private final DuelType type;
    private final KitManager kitManager;
    private final DuelQueueManager queueManager;
    private final ScoreboardManager scoreboardManager;
    private final StatsManager statsManager;
    private final CoinManager coinManager;
    /**
     * true  = ranked queue (stats counted)
     * false = unranked / private duels (no stats)
     */
    private boolean ranked = false;

    private final List<UUID> players = new ArrayList<>();
    private final Set<UUID> alive = new HashSet<>();

    private MatchState state = MatchState.COUNTDOWN;
    private int countdownSeconds;
    private int timeLeftSeconds;

    // Boxing hits
    private final Map<UUID, Integer> boxingHits = new HashMap<>();

    // player who used /leave (kept in case you ever bring defeat titles back)
    private UUID leaverNoDefeatTitle = null;

    public DuelMatch(JavaPlugin plugin,
                     Arena arena,
                     GameMode mode,
                     DuelType type,
                     KitManager kitManager,
                     DuelQueueManager queueManager,
                     ScoreboardManager scoreboardManager,
                     StatsManager statsManager,
                     CoinManager coinManager) {
        this.plugin = plugin;
        this.arena = arena;
        this.mode = mode;
        this.type = type;
        this.kitManager = kitManager;
        this.queueManager = queueManager;
        this.scoreboardManager = scoreboardManager;
        this.statsManager = statsManager;
        this.coinManager = coinManager;
        this.ranked = ranked;
    }

    // ===== GETTERS =====
    public Arena getArena() { return arena; }
    public GameMode getMode() { return mode; }
    public DuelType getType() { return type; }
    public MatchState getState() { return state; }
    public boolean isPlaying() { return state == MatchState.PLAYING; }
    public List<UUID> getAllPlayers() { return new ArrayList<>(players); }

    public void addPlayer(Player p) {
        UUID u = p.getUniqueId();
        if (!players.contains(u)) {
            players.add(u);
            alive.add(u);
        }
    }

    // Called by queue manager when we need to kill pre-start match (e.g. someone left)
    public void cancelPreStart() {
        if (state == MatchState.COUNTDOWN) {
            state = MatchState.ENDED;
        }
    }

    public void markLeaverNoDefeat(Player p) {
        this.leaverNoDefeatTitle = p.getUniqueId();
    }

    // ========= WAITING / COUNTDOWN =========

    public void startWaiting() {
        state = MatchState.COUNTDOWN;

        // Teleport to queue spawn (look forward, not floor)
        if (arena.getQueueSpawn() != null) {
            Location q = arena.getQueueSpawn().clone();
            q.setPitch(0f);
            for (UUID u : players) {
                Player p = Bukkit.getPlayer(u);
                if (p != null) p.teleport(q);
            }
        }

        int defCountdown = plugin.getConfig().getInt("duels.start_countdown", 10);
        countdownSeconds = Math.max(3, defCountdown);

        int defTime = plugin.getConfig().getInt("duels.time_limit_seconds.default", 300);
        timeLeftSeconds = plugin.getConfig().getInt("duels.time_limit_seconds." + mode.id(), defTime);

        // Initial waiting scoreboard + return item
        for (UUID u : players) {
            Player p = Bukkit.getPlayer(u);
            if (p == null) continue;
            scoreboardManager.showWaiting(p, arena, players.size(), type.getSize(), ChatColor.GRAY + "Starting...");
            ReturnItemUtil.give(p, plugin);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (state != MatchState.COUNTDOWN) {
                    cancel();
                    return;
                }

                // If someone left and we don't have enough players, cancel match
                if (players.size() < type.getSize()) {
                    state = MatchState.ENDED;
                    cancel();
                    return;
                }

                if (countdownSeconds <= 0) {
                    cancel();
                    startFight();
                    return;
                }

                // Messages + scoreboard
                for (UUID u : players) {
                    Player p = Bukkit.getPlayer(u);
                    if (p == null) continue;

                    // Only show message + sound + title for last 5 seconds
                    if (countdownSeconds <= 5) {
                        p.playSound(p.getLocation(), Sound.NOTE_STICKS, 1f, 1f);

                        // Chat message (critical)
                        String msg = plugin.getConfig().getString(
                                "duels.messages.countdown_critical",
                                "&eThe match starts in &c%seconds%&e seconds!"
                        );
                        msg = cc(msg.replace("%seconds%", String.valueOf(countdownSeconds)));
                        p.sendMessage(msg);

                        // RED title countdown
                        String title = ChatColor.RED.toString() + ChatColor.BOLD + countdownSeconds;
                        String sub = ChatColor.YELLOW + "Starting in " + countdownSeconds + "s";
                        p.sendTitle(title, sub);
                    }

                    scoreboardManager.showWaiting(
                            p, arena, players.size(), type.getSize(),
                            ChatColor.GRAY + "Starting in " + ChatColor.GREEN + countdownSeconds + "s"
                    );
                }

                countdownSeconds--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    // ========= FIGHT START =========

    private void startFight() {
        if (state != MatchState.COUNTDOWN) return;
        if (players.size() < 2) {
            state = MatchState.ENDED;
            return;
        }

        state = MatchState.PLAYING;

        // Remove bed/return item so it cannot be used in fight
        for (UUID u : players) {
            Player p = Bukkit.getPlayer(u);
            if (p != null) {
                ReturnItemUtil.remove(p, plugin);
            }
        }

        // Teleport to arena spawns + give kits
        List<Location> spawns = arena.getSpawns();
        for (int i = 0; i < players.size() && i < spawns.size(); i++) {
            Player p = Bukkit.getPlayer(players.get(i));
            if (p == null) continue;
            p.teleport(spawns.get(i));
            kitManager.applyKit(p, mode);
        }

        // Fancy start message + sound
        for (UUID u : players) {
            Player p = Bukkit.getPlayer(u);
            if (p != null) {
                sendStartMessage(p);
                p.playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 1f, 1f);
            }
        }

        // Start replay recording (AdvancedReplay)
        if (plugin instanceof FarepixelDuels) {
            FarepixelDuels fp =
                    (FarepixelDuels) plugin;
            ReplayManagerImpl rm = fp.getReplayManager();
            if (rm != null) {
                rm.startRecording(arena, mode, new ArrayList<>(players)); // Start the replay recording here
            }
        }

        // Scoreboard updater
        new BukkitRunnable() {
            @Override
            public void run() {
                if (state != MatchState.PLAYING) {
                    cancel();
                    return;
                }
                for (UUID u : players) {
                    Player p = Bukkit.getPlayer(u);
                    if (p != null) {
                        scoreboardManager.showPlaying(p, DuelMatch.this);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);

        // Time-left timer
        new BukkitRunnable() {
            @Override
            public void run() {
                if (state != MatchState.PLAYING) {
                    cancel();
                    return;
                }
                timeLeftSeconds--;
                if (timeLeftSeconds <= 0) {
                    cancel();
                    endByTimeout();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    // ========= COMBAT (BOXING HOOKS + DEATH) =========

    public void addBoxingHit(Player attacker) {
        if (mode != GameMode.BOXING || state != MatchState.PLAYING) return;
        UUID u = attacker.getUniqueId();
        int newHits = boxingHits.getOrDefault(u, 0) + 1;
        boxingHits.put(u, newHits);

        int target = plugin.getConfig().getInt("duels.boxing.hit_to_win", 100);
        if (newHits >= target) {
            endMatch(attacker);
        }
    }

    public int getBoxingHits(UUID u) {
        return boxingHits.getOrDefault(u, 0);
    }

    public void handleDeath(Player dead, Player killer) {
        if (state != MatchState.PLAYING) return;
        alive.remove(dead.getUniqueId());
        checkWinAfterChange();
    }

    public void handleQuit(Player p) {
        if (!players.contains(p.getUniqueId())) return;
        if (state != MatchState.PLAYING) return; // pre-start quit handled by queue manager
        alive.remove(p.getUniqueId());
        checkWinAfterChange();
    }

    private void sendStartMessage(Player p) {
        String opponents = getOpponentsOf(p); // "PrefixName" or "Prefix1, Prefix2"

        p.sendMessage(ColorUtil.cc("&aFight!"));
        p.sendMessage(ColorUtil.cc("&a&m-----------------------------------------------------"));
        p.sendMessage(ColorUtil.cc("                        &fDuels"));
        p.sendMessage(" ");
        p.sendMessage(ColorUtil.cc("          &e&lEliminate your opponent!"));
        p.sendMessage(" ");
        p.sendMessage(ColorUtil.cc("                &fOpponent(s): " + ChatColor.RED + opponents));
        p.sendMessage(" ");
        p.sendMessage(ColorUtil.cc("&a&m-----------------------------------------------------"));
    }

    private void checkWinAfterChange() {
        if (state != MatchState.PLAYING) return;
        if (alive.size() <= 1) {
            Player winner = null;
            if (!alive.isEmpty()) {
                winner = Bukkit.getPlayer(alive.iterator().next());
            }
            endMatch(winner);
        }
    }

    private void endByTimeout() {
        Player winner = null;

        if (mode == GameMode.BOXING) {
            int best = -1;
            for (UUID u : alive) {
                int h = getBoxingHits(u);
                if (h > best) {
                    best = h;
                    winner = Bukkit.getPlayer(u);
                }
            }
        } else {
            double bestHp = -1;
            for (UUID u : alive) {
                Player p = Bukkit.getPlayer(u);
                if (p != null && p.getHealth() > bestHp) {
                    bestHp = p.getHealth();
                    winner = p;
                }
            }
        }

        endMatch(winner);
    }

    // ========= END MATCH =========

    private void endMatch(Player winner) {
        if (state == MatchState.ENDED) return;
        state = MatchState.ENDED;

        String winnerName = (winner == null) ? "No one" : winner.getName();
        UUID winnerId = (winner == null) ? null : winner.getUniqueId();

        // Ranked stats (only for normal queue 1v1 games)
        if (ranked && statsManager != null && winnerId != null && players.size() == 2) {
            UUID a = players.get(0);
            UUID b = players.get(1);
            UUID loserId = winnerId.equals(a) ? b : a;
            statsManager.applyWin(winnerId, loserId);
        }

        // Titles from config (under duels.messages in your config)
        String vTitle = cc(plugin.getConfig().getString(
                "duels.messages.victory_title", "&6&lVICTORY!"));
        String vSub = cc(plugin.getConfig().getString(
                "duels.messages.victory_subtitle", "&7%winner%&f won the Duel!"))
                .replace("%winner%", winnerName);
        // (defeat title intentionally not used)

        // Random coins for winner
        if (winnerId != null && coinManager != null) {
            int min = plugin.getConfig().getInt("duels.wins_coins.min", 0);
            int max = plugin.getConfig().getInt("duels.wins_coins.max", 0);
            if (max < min) max = min;

            int amount = (max == min ? min : (new Random().nextInt(max - min + 1) + min));
            if (amount > 0) {
                try {
                    coinManager.addCoins(winnerId, amount);
                    Player wp = Bukkit.getPlayer(winnerId);
                    if (wp != null) {
                        wp.sendMessage(ColorUtil.cc("&aYou received &e" + amount + " &acoins for winning!"));
                    }
                } catch (Throwable ignored) { }
            }
        }

        // Stop replay recording (AdvancedReplay)
        String replayId = "";
        if (replayId != null) {
            FarepixelDuels fp =
                    (FarepixelDuels) plugin;
            ReplayManagerImpl rm = fp.getReplayManager();
            if (rm != null) {
                rm.stopRecording(replayId); // Stop the replay recording here
            }
        }


        // Quests progress (wins / games played)
        if (plugin instanceof FarepixelDuels) {
            FarepixelDuels fp =
                    (FarepixelDuels) plugin;
            if (fp.getQuestManager() != null) {
                fp.getQuestManager().handleDuelEnd(
                        mode,
                        winnerId,
                        new ArrayList<UUID>(players)
                );
            }
        }

        // Random daily bonus command for winner
        if (winnerId != null) {
            List<String> cmds = plugin.getConfig().getStringList("duels.daily_bonus_cmds");
            if (cmds != null && !cmds.isEmpty()) {
                String raw = cmds.get(new Random().nextInt(cmds.size()));
                if (raw != null && !raw.trim().isEmpty()) {
                    String cmd = raw.trim();
                    cmd = cmd.replace("%player%", winnerName);
                    cmd = cmd.replace("%mode%", mode.id());
                    if (cmd.startsWith("/")) cmd = cmd.substring(1);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                }
            }
        }

        // Clear inventories, remove scoreboard and send end messages
        for (UUID u : players) {
            Player p = Bukkit.getPlayer(u);
            if (p == null) continue;

            p.getInventory().clear();
            p.getInventory().setArmorContents(null);
            p.updateInventory();
            ReturnItemUtil.remove(p, plugin);
            scoreboardManager.clear(p);

            p.sendMessage(ColorUtil.cc("&a&m-----------------------------------------------------"));
            p.sendMessage(ColorUtil.cc("                        &fDuels"));
            p.sendMessage(" ");
            p.sendMessage(ColorUtil.cc("                  &eWinner: &f" + winnerName));
            p.sendMessage(" ");
            p.sendMessage(ColorUtil.cc("&a&m-----------------------------------------------------"));

            if (winnerId != null && u.equals(winnerId)) {
                p.sendTitle(vTitle, vSub);
            }
        }

        // Give 'Play Again' + Return-to-lobby item once, after clearing inventory
        givePlayAgainItem();
        for (UUID u : players) {
            Player p = Bukkit.getPlayer(u);
            if (p != null) {
                ReturnItemUtil.give(p, plugin);
            }
        }

        // Teleport back after 5 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                Location lobby = SpawnUtil.getLobbySpawn(plugin);
                for (UUID u : players) {
                    Player p = Bukkit.getPlayer(u);
                    if (p != null && lobby != null) {
                        p.teleport(lobby);
                    }
                }
                queueManager.onMatchEnd(DuelMatch.this);
            }
        }.runTaskLater(plugin, 20L * 5L);

        // No reuse of this match object
        leaverNoDefeatTitle = null;
    }

    // ========= SCOREBOARD HELPERS =========

    public String getFormattedTimeLeft() {
        int t = Math.max(0, timeLeftSeconds);
        int m = t / 60;
        int s = t % 60;
        return String.format("%02d:%02d", m, s);
    }

    public Player getOpponentFor(Player p) {
        if (players.size() != 2) return null;
        UUID me = p.getUniqueId();
        UUID a = players.get(0);
        UUID b = players.get(1);
        UUID other = me.equals(a) ? b : a;
        return Bukkit.getPlayer(other);
    }

    private String getDisplayNameWithPrefix(Player p) {
        if (p == null) return "";
        String base = p.getName();
        try {
            base = ColorUtil.papi(plugin, p, "%luckperms_prefix%" + base);
        } catch (Throwable ignored) { }
        return base;
    }

    public String getOpponentsOf(Player p) {
        StringBuilder sb = new StringBuilder();
        for (UUID u : players) {
            if (u.equals(p.getUniqueId())) continue;
            Player op = Bukkit.getPlayer(u);
            if (op != null) {
                String display = getDisplayNameWithPrefix(op);
                if (sb.length() > 0) sb.append(", ");
                sb.append(display);
            }
        }
        return sb.toString();
    }

    private void givePlayAgainItem() {
        if (!plugin.getConfig().getBoolean("duels.play_again.enabled", true)) return;

        int slot = plugin.getConfig().getInt("duels.play_again.slot", 4); // default slot 4

        ItemStack paper = new ItemStack(Material.PAPER);
        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName(cc(plugin.getConfig().getString(
                "duels.play_again.name", "&aPlay Again &7(Right Click)")));
        List<String> loreCfg = plugin.getConfig().getStringList("duels.play_again.lore");
        List<String> lore = new ArrayList<>();
        for (String line : loreCfg) lore.add(cc(line));
        meta.setLore(lore);
        paper.setItemMeta(meta);

        for (UUID u : players) {
            Player p = Bukkit.getPlayer(u);
            if (p == null) continue;
            p.getInventory().setItem(slot, paper);
            p.updateInventory();
        }
    }

    private String cc(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }
}
