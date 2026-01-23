package me.herex.farepixelduels.duels;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class KitManager {

    private final JavaPlugin plugin;
    private PlayerKitManager playerKits;

    public KitManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setPlayerKits(PlayerKitManager playerKits) {
        this.playerKits = playerKits;
    }

    @SuppressWarnings("unchecked")
    public List<ItemStack> getKit(GameMode mode) {
        FileConfiguration cfg = plugin.getConfig();
        List<ItemStack> kit = (List<ItemStack>) cfg.get("kits." + mode.id());
        if (kit == null) return new ArrayList<ItemStack>();
        return kit;
    }

    public void applyKit(Player p, GameMode mode) {
        // player custom kit overrides default
        List<ItemStack> kit = null;
        if (playerKits != null) {
            kit = playerKits.get(p, mode);
        }
        if (kit == null) kit = getKit(mode);

        p.getInventory().clear();
        p.getInventory().setArmorContents(null);

        for (int i = 0; i < kit.size(); i++) {
            ItemStack it = kit.get(i);
            if (it != null) p.getInventory().setItem(i, it);
        }
        p.updateInventory();
    }

    public void saveKit(GameMode mode, ItemStack[] contents) {
        List<ItemStack> list = new ArrayList<ItemStack>();
        for (ItemStack it : contents) list.add(it);
        plugin.getConfig().set("kits." + mode.id(), list);
        plugin.saveConfig();
    }
}
