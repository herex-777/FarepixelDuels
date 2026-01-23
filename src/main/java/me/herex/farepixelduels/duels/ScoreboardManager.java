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

    // ✅ FIXED METHOD
    public void showWaiting(Player p, Arena arena, int current, int max, String status) {
        GameMode mode = arena != null ? arena.getGameMode() : null;
        String modeName = (mode == null ? "N/A" : mode.getDisplayName());

        List<String> rawLines = plugin.getConfig().getStringList("duels.scoreboard.waiting_lines");
        if (rawLines == null || rawLines.isEmpty()) return;

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("duels_wait", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(ColorUtil.cc(plugin.getConfig().getString("duels.scoreboard.waiting_title", "&e&lDUELS")));

        int score = rawLines.size();
        for (String raw : rawLines) {
            String line = raw;

            line = line.replace("%arena%", arena != null ? arena.getName() : "N/A");
            line = line.replace("%players%", String.valueOf(current));
            line = line.replace("%max%", String.valueOf(max));
            line = line.replace("%status%", status);
            line = line.replace("%mode%", modeName);
            line = line.replace("%server%", plugin.getConfig().getString("duels.scoreboard.server_name", "Server"));

            obj.getScore(ColorUtil.cc(line)).setScore(score--);
        }

        p.setScoreboard(board);
    }

    public void showPlaying(Player p, Arena arena, String time, String opponents) {
        if (!plugin.getConfig().getBoolean("duels.scoreboard.enabled", true)) return;

        List<String> lines = plugin.getConfig().getStringList("duels.scoreboard.playing_lines");
        String title = ColorUtil.cc(plugin.getConfig().getString("duels.scoreboard.playing_title", "&e&lDUELS"));

        Map<String, String> repl = baseReplacements(p, arena, 0, 0);
        repl.put("%time%", time);
        repl.put("%opponents%", opponents);
        repl.put("%winstreak%", String.valueOf(statsManager.getWinstreak(p.getUniqueId())));
        repl.put("%best_winstreak%", String.valueOf(statsManager.getBestWinstreak(p.getUniqueId())));

        apply(p, title, replaceLines(p, lines, repl));
    }

    private Map<String, String> baseReplacements(Player p, Arena arena, int players, int max) {
        Map<String, String> r = new HashMap<String, String>();
        r.put("%date%", dateFmt.format(new Date()));
        r.put("%tag%", tag(p.getUniqueId()));
        r.put("%server%", ColorUtil.cc(plugin.getConfig().getString("duels.scoreboard.server_name", "server")));
        r.put("%arena%", arena != null ? arena.getName() : "Searching...");
        r.put("%players%", String.valueOf(players));
        r.put("%max%", String.valueOf(max));
        return r;
    }

    private List<String> replaceLines(Player p, List<String> lines, Map<String, String> repl) {
        List<String> out = new ArrayList<String>();
        for (String raw : lines) {
            String s = raw;
            for (Map.Entry<String, String> e : repl.entrySet()) {
                s = s.replace(e.getKey(), e.getValue());
            }
            s = ColorUtil.papi(plugin, p, s);
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
            String l = line == null ? "" : line;
            if (l.length() > 40) l = l.substring(0, 40);
            while (used.contains(l)) l = l + ChatColor.RESET;
            used.add(l);
            obj.getScore(l).setScore(score--);
        }

        boards.put(p.getUniqueId(), sb);
        p.setScoreboard(sb);
    }

    public void showPlaying(Player p, DuelMatch match) {
        GameMode mode = match.getMode();
        String modeName = mode.getDisplayName();

        List<String> rawLines = plugin.getConfig().getStringList("duels.scoreboard.playing_lines");
        if (rawLines == null || rawLines.isEmpty()) return;

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("duels_play", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(ColorUtil.cc(plugin.getConfig().getString("duels.scoreboard.playing_title", "&e&lDUELS")));

        int score = rawLines.size();
        for (String raw : rawLines) {
            String line = raw;

            line = line.replace("%mode%", modeName);
            line = line.replace("%arena%", match.getArena().getName());
            line = line.replace("%time%", match.getFormattedTimeLeft());
            line = line.replace("%opponents%", match.getOpponentsOf(p));

            obj.getScore(ColorUtil.cc(line)).setScore(score--);
        }

        p.setScoreboard(board);
    }
}
