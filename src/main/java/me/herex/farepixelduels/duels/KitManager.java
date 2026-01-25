package me.herex.farepixelduels.duels;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    /**
     * Apply kit to player.
     * Uses player custom kit if present, otherwise the default kit.
     * Last 4 saved slots are used as helmet, chestplate, leggings, boots.
     */
    public void applyKit(Player p, GameMode mode) {
        // player custom kit overrides default
        List<ItemStack> kit = null;
        if (playerKits != null) {
            kit = playerKits.get(p, mode);
        }
        if (kit == null || kit.isEmpty()) {
            kit = getKit(mode);
        }

        p.getInventory().clear();
        p.getInventory().setArmorContents(null);

        if (kit == null || kit.isEmpty()) {
            p.updateInventory();
            return;
        }

        int size = kit.size();

        // Last 4 slots as armor: [size-4]=helmet, [size-3]=chest, [size-2]=leggings, [size-1]=boots
        ItemStack[] armor = new ItemStack[4];
        if (size >= 4) {
            armor[0] = kit.get(size - 4); // helmet
            armor[1] = kit.get(size - 3); // chestplate
            armor[2] = kit.get(size - 2); // leggings
            armor[3] = kit.get(size - 1); // boots
        }

        int limit = Math.max(0, size - 4); // everything before the last 4 is in hotbar/inventory
        for (int i = 0; i < limit && i < 36; i++) {
            ItemStack it = kit.get(i);
            if (it != null) {
                p.getInventory().setItem(i, it);
            }
        }

        p.getInventory().setArmorContents(armor);
        p.updateInventory();
    }

    public void saveKit(GameMode mode, ItemStack[] contents) {
        List<ItemStack> list = new ArrayList<ItemStack>();
        for (ItemStack it : contents) list.add(it);
        plugin.getConfig().set("kits." + mode.id(), list);
        plugin.saveConfig();
    }

    public void savePlayerKit(Player p, GameMode mode, ItemStack[] contents) {
        // Player-specific kits stored via PlayerKitManager if present,
        // otherwise fallback to global kit.
        if (playerKits != null) {
            UUID id = p.getUniqueId();
            playerKits.set(id, mode, contents);
        } else {
            saveKit(mode, contents);
        }
    }
}
