package me.herex.farepixelduels.duels;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerKitManager {

    private final JavaPlugin plugin;
    private final File file;
    private FileConfiguration cfg;

    public PlayerKitManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "playerkits.yml");
        reload();
    }

    public void reload() {
        try {
            if (!file.exists()) {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            }
        } catch (IOException ignored) {}
        cfg = YamlConfiguration.loadConfiguration(file);
    }

    public void save() {
        try {
            cfg.save(file);
        } catch (IOException ignored) {}
    }

    @SuppressWarnings("unchecked")
    public List<ItemStack> get(Player p, GameMode mode) {
        String path = "kits." + p.getUniqueId().toString() + "." + mode.id();
        Object o = cfg.get(path);
        if (o instanceof List) return (List<ItemStack>) o;
        return null;
    }

    public void set(UUID u, GameMode mode, ItemStack[] contents) {
        List<ItemStack> list = new ArrayList<ItemStack>();
        for (ItemStack it : contents) list.add(it);
        cfg.set("kits." + u.toString() + "." + mode.id(), list);
        save();
    }
}
