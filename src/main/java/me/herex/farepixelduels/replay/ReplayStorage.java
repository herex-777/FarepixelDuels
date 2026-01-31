package me.herex.farepixelduels.replay;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ReplayStorage {

    private final JavaPlugin plugin;
    private final File file;
    private FileConfiguration cfg;

    public ReplayStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "replays.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException ignored) {}
        }
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            cfg.save(file);
        } catch (IOException ignored) {}
    }

    public void saveEntry(ReplayEntry entry) {
        String base = "replays." + entry.getId();
        cfg.set(base + ".created", entry.getCreatedAt());
        cfg.set(base + ".map", entry.getMap());
        cfg.set(base + ".server", entry.getServer());
        cfg.set(base + ".mode", entry.getMode());
        cfg.set(base + ".players", entry.getPlayers());

        // Per-player index
        for (String name : entry.getPlayers()) {
            if (name == null) continue;
            String key = "player-index." + name.toLowerCase(Locale.ROOT);
            List<String> ids = cfg.getStringList(key);
            if (!ids.contains(entry.getId())) {
                ids.add(entry.getId());
            }
            cfg.set(key, ids);
        }

        save();
    }

    public ReplayEntry get(String id) {
        String base = "replays." + id;
        if (!cfg.contains(base)) return null;

        long created = cfg.getLong(base + ".created", 0L);
        String map = cfg.getString(base + ".map", "Unknown");
        String server = cfg.getString(base + ".server", "Unknown");
        String mode = cfg.getString(base + ".mode", "Unknown");
        List<String> players = cfg.getStringList(base + ".players");

        return new ReplayEntry(id, created, map, server, mode, players);
    }

    public List<ReplayEntry> getForPlayer(String name) {
        if (name == null) return Collections.emptyList();
        String key = "player-index." + name.toLowerCase(Locale.ROOT);
        List<String> ids = cfg.getStringList(key);

        List<ReplayEntry> out = new ArrayList<ReplayEntry>();
        for (String id : ids) {
            ReplayEntry e = get(id);
            if (e != null) out.add(e);
        }

        // newest first
        Collections.sort(out, new Comparator<ReplayEntry>() {
            @Override
            public int compare(ReplayEntry o1, ReplayEntry o2) {
                return Long.compare(o2.getCreatedAt(), o1.getCreatedAt());
            }
        });

        return out;
    }

    public void delete(String id) {
        ReplayEntry entry = get(id);
        if (entry != null) {
            for (String name : entry.getPlayers()) {
                if (name == null) continue;
                String key = "player-index." + name.toLowerCase(Locale.ROOT);
                List<String> ids = cfg.getStringList(key);
                ids.remove(id);
                cfg.set(key, ids);
            }
        }

        cfg.set("replays." + id, null);
        save();
    }
}
