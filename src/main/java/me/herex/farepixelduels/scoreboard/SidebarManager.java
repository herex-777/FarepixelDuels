package me.herex.farepixelduels.scoreboard;

import me.herex.farepixelduels.FarepixelDuels;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public final class SidebarManager {

    private final FarepixelDuels plugin;

    // Store per-player scoreboards
    private final Map<UUID, PlayerSidebar> sidebars = new HashMap<UUID, PlayerSidebar>();

    public SidebarManager(FarepixelDuels plugin) {
        this.plugin = plugin;
    }

    public void update(Player player) {
        PlayerSidebar ps = sidebars.get(player.getUniqueId());
        if (ps == null) {
            ps = new PlayerSidebar();
            sidebars.put(player.getUniqueId(), ps);
        }

        List<String> rawLines = plugin.getConfig().getStringList("scoreboard.lines");
        if (rawLines == null) rawLines = Collections.emptyList();

        String title = plugin.getConfig().getString("scoreboard.title", "&e&lDUELS");
        title = color(title);

        List<String> lines = new ArrayList<String>();
        for (String l : rawLines) {
            lines.add(color(PlaceholderUtil.apply(player, l, plugin)));
        }

        ps.apply(player, title, lines);
    }

    public void remove(Player player) {
        if (player == null) return;

        PlayerSidebar ps = sidebars.remove(player.getUniqueId());
        if (ps != null) {
            ps.clear(player);
        }
    }

    private static String color(String s) {
        if (s == null) return "";
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private static final class PlayerSidebar {
        private Scoreboard scoreboard;
        private Objective objective;
        private final List<String> entries = new ArrayList<String>();

        void apply(Player player, String title, List<String> lines) {
            if (scoreboard == null) {
                scoreboard = BukkitScoreboardFactory.newScoreboard();
                objective = scoreboard.registerNewObjective("fpduels", "dummy");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                player.setScoreboard(scoreboard);
            }

            if (objective == null) {
                objective = scoreboard.registerNewObjective("fpduels", "dummy");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            }

            objective.setDisplayName(trimToMax(title, 32));

            // Max 15 lines in sidebar
            if (lines.size() > 15) {
                lines = lines.subList(0, 15);
            }

            // Clear old entries/teams
            for (String entry : new ArrayList<String>(entries)) {
                scoreboard.resetScores(entry);
            }
            entries.clear();

            // Remove old teams we manage
            for (Team t : scoreboard.getTeams()) {
                if (t.getName().startsWith("fp_")) {
                    t.unregister();
                }
            }

            int score = lines.size();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                LineParts parts = splitLine(line);

                String teamName = "fp_" + i;
                Team team = scoreboard.registerNewTeam(teamName);

                String entry = getUniqueEntry(i);
                team.addEntry(entry);
                team.setPrefix(parts.prefix);
                team.setSuffix(parts.suffix);

                objective.getScore(entry).setScore(score--);
                entries.add(entry);
            }

            // Ensure player has our scoreboard
            if (player.getScoreboard() != scoreboard) {
                player.setScoreboard(scoreboard);
            }
        }

        void clear(Player player) {
            // Reset to main scoreboard
            player.setScoreboard(player.getServer().getScoreboardManager().getMainScoreboard());
        }

        private static String trimToMax(String s, int max) {
            if (s == null) return "";
            if (s.length() <= max) return s;
            return s.substring(0, max);
        }

        private static final class LineParts {
            final String prefix;
            final String suffix;

            LineParts(String prefix, String suffix) {
                this.prefix = prefix;
                this.suffix = suffix;
            }
        }

        private static LineParts splitLine(String line) {
            // Scoreboard team prefix/suffix limits (legacy): 16 chars each, and color codes
            if (line == null) line = "";
            if (line.length() <= 16) {
                return new LineParts(line, "");
            }

            String prefix = line.substring(0, 16);
            String rest = line.substring(16);

            // Preserve colors at the split point
            String color = ChatColor.getLastColors(prefix);
            String suffix = color + rest;
            if (suffix.length() > 16) {
                suffix = suffix.substring(0, 16);
            }

            // Avoid cutting a color code in half at the end of prefix
            if (prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
                prefix = prefix.substring(0, prefix.length() - 1);
                suffix = ChatColor.COLOR_CHAR + suffix;
                if (suffix.length() > 16) suffix = suffix.substring(0, 16);
            }

            return new LineParts(prefix, suffix);
        }

        private static String getUniqueEntry(int index) {
            // Use ChatColor values as unique dummy entries.
            // There are 16 chat colors; index is limited to 0..14 (15 lines).
            ChatColor[] colors = ChatColor.values();
            // Find a stable formatting color entry
            int safe = Math.min(index, 14);
            return colors[safe].toString();
        }
    }

    /**
     * Minimal scoreboard factory for legacy compatibility.
     */
    private static final class BukkitScoreboardFactory {
        static Scoreboard newScoreboard() {
            return org.bukkit.Bukkit.getScoreboardManager().getNewScoreboard();
        }
    }
}
