package me.herex.farepixelduels.duels;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.text.SimpleDateFormat;
import java.util.*;

public class ScoreboardManager {

    private final JavaPlugin plugin;
    private final StatsManager statsManager;

    private final Map<UUID, Scoreboard> boards = new HashMap<UUID, Scoreboard>();
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yy");
    private final Map<UUID, String> randTag = new HashMap<UUID, String>();

    public ScoreboardManager(JavaPlugin plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
    }

    private String tag(UUID u) {
        String t = randTag.get(u);
        if (t != null) return t;

        Random r = new Random();
        int num = 10 + r.nextInt(990); // 10..999
        char a = (char) ('A' + r.nextInt(26));
        char b = (char) ('a' + r.nextInt(26));
        String out = "m" + num + "" + a + b;
        randTag.put(u, out);
        return out;
    }

    public void clear(Player p) {
        boards.remove(p.getUniqueId());
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    // ====== WAITING SCOREBOARD ======

    public void showWaiting(Player p, Arena arena, int current, int max, String status) {
        if (!plugin.getConfig().getBoolean("duels.scoreboard.enabled", true)) return;

        GameMode mode = (arena != null ? arena.getGameMode() : null);
        String title = ColorUtil.cc(plugin.getConfig().getString("duels.scoreboard.waiting_title", "&e&lDUELS"));
        List<String> lines = plugin.getConfig().getStringList("duels.scoreboard.waiting_lines");

        Map<String, String> repl = baseReplacements(p, arena, current, max);
        repl.put("%status%", status);
        repl.put("%mode%", mode != null ? mode.getDisplayName() : "N/A");

        apply(p, title, replaceLines(p, lines, repl));
    }

    // ====== PLAYING SCOREBOARD (old overload kept just in case) ======

    public void showPlaying(Player p, Arena arena, String time, String opponents) {
        if (!plugin.getConfig().getBoolean("duels.scoreboard.enabled", true)) return;

        List<String> lines = plugin.getConfig().getStringList("duels.scoreboard.playing_lines");
        String title = ColorUtil.cc(plugin.getConfig().getString("duels.scoreboard.playing_title", "&e&lDUELS"));

        Map<String, String> repl = baseReplacements(p, arena, 0, 0);
        repl.put("%time%", time);
        repl.put("%timeout%", time);
        repl.put("%opponents%", opponents);
        repl.put("%winstreak%", String.valueOf(statsManager.getWinstreak(p.getUniqueId())));
        repl.put("%best_winstreak%", String.valueOf(statsManager.getBestWinstreak(p.getUniqueId())));

        apply(p, title, replaceLines(p, lines, repl));
    }

    // ====== PLAYING SCOREBOARD USING DuelMatch ======

    public void showPlaying(Player p, DuelMatch match) {
        if (!plugin.getConfig().getBoolean("duels.scoreboard.enabled", true)) return;

        Arena arena = match.getArena();
        GameMode mode = match.getMode();

        boolean boxingSb = (mode == GameMode.BOXING &&
                plugin.getConfig().getBoolean("duels.scoreboard_boxing.enabled", true));

        List<String> lines;
        String title;

        if (boxingSb) {
            lines = plugin.getConfig().getStringList("duels.scoreboard_boxing.playing_lines");
            title = ColorUtil.cc(plugin.getConfig().getString("duels.scoreboard_boxing.playing_title", "&e&lBOXING DUEL"));
        } else {
            lines = plugin.getConfig().getStringList("duels.scoreboard.playing_lines");
            title = ColorUtil.cc(plugin.getConfig().getString("duels.scoreboard.playing_title", "&e&lDUELS"));
        }

        Map<String, String> repl = baseReplacements(p, arena, match.getAllPlayers().size(), match.getType().getSize());
        String timeStr = match.getFormattedTimeLeft();

        repl.put("%time%", timeStr);
        repl.put("%timeout%", timeStr);
        repl.put("%mode%", mode.getDisplayName());
        repl.put("%opponents%", match.getOpponentsOf(p));
        repl.put("%winstreak%", String.valueOf(statsManager.getWinstreak(p.getUniqueId())));
        repl.put("%best_winstreak%", String.valueOf(statsManager.getBestWinstreak(p.getUniqueId())));

        if (mode == GameMode.BOXING) {
            int myHits = match.getBoxingHits(p.getUniqueId());
            int oppHits = 0;
            Player opp = match.getOpponentFor(p);
            if (opp != null) {
                oppHits = match.getBoxingHits(opp.getUniqueId());
            }
            repl.put("%hits%", String.valueOf(myHits));
            repl.put("%opponent_hits%", String.valueOf(oppHits));
        }

        apply(p, title, replaceLines(p, lines, repl));
    }

    public void showLobby(Player p) {
        if (!plugin.getConfig().getBoolean("duels.scoreboard.enabled", true)) return;

        List<String> lines = plugin.getConfig().getStringList("duels.scoreboard.lobby_lines");
        if (lines == null || lines.isEmpty()) return;

        String title = ColorUtil.cc(plugin.getConfig().getString("duels.scoreboard.lobby_title", "&e&lDUELS"));

        // Reuse base replacements (date, tag, server, etc.)
        Map<String, String> repl = baseReplacements(p, null, 0, 0);

        // extra replacement: online players
        repl.put("%online%", String.valueOf(org.bukkit.Bukkit.getOnlinePlayers().size()));

        // Now apply placeholders and build lines
        apply(p, title, replaceLines(p, lines, repl));
    }

    // ====== BASE REPLACEMENTS & APPLY ======

    private Map<String, String> baseReplacements(Player p, Arena arena, int players, int max) {
        Map<String, String> r = new HashMap<String, String>();
        r.put("%date%", dateFmt.format(new Date()));
        r.put("%tag%", tag(p.getUniqueId()));
        r.put("%server%", plugin.getConfig().getString("duels.scoreboard.server_name", "server"));
        r.put("%arena%", arena != null ? arena.getName() : "Searching...");
        r.put("%players%", String.valueOf(players));
        r.put("%max%", String.valueOf(max));
        return r;
    }

    private List<String> replaceLines(Player p, List<String> lines, Map<String, String> repl) {
        List<String> out = new ArrayList<String>();
        if (lines == null) return out;

        for (String raw : lines) {
            String s = (raw == null ? "" : raw);
            for (Map.Entry<String, String> e : repl.entrySet()) {
                s = s.replace(e.getKey(), e.getValue());
            }
            s = ColorUtil.papi(plugin, p, s); // PlaceholderAPI support
            out.add(s);
        }
        return out;
    }

    private void apply(Player p, String title, List<String> lines) {
        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = sb.registerNewObjective("fpduels", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(title);

        int score = lines.size();
        Set<String> used = new HashSet<String>();
        for (String line : lines) {
            String l = (line == null ? "" : line);
            if (l.length() > 40) l = l.substring(0, 40);
            // avoid duplicate / empty issues
            if (l.isEmpty()) l = ChatColor.RESET.toString();
            while (used.contains(l)) l = l + ChatColor.RESET;
            used.add(l);
            obj.getScore(l).setScore(score--);
        }

        boards.put(p.getUniqueId(), sb);
        p.setScoreboard(sb);
    }
}
