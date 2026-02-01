package me.herex.farepixelduels.replay;

import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.duels.Arena;
import me.herex.farepixelduels.duels.GameMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.SimpleDateFormat;
import java.util.*;

public class ReplayManagerImpl {

    public static final String GUI_TITLE = ChatColor.DARK_GRAY + "Your Replays";

    private final FarepixelDuels plugin;
    private final ReplayStorage storage;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public ReplayManagerImpl(FarepixelDuels plugin) {
        this.plugin = plugin;
        this.storage = new ReplayStorage(plugin);
    }

    public ReplayStorage getStorage() {
        return storage;
    }

    /**
     * Called when a duel actually STARTS (players teleported to arena).
     * Uses AdvancedReplay: "advancedreplay:replay start <id> <players>"
     */
    public void startRecording(Arena arena, GameMode mode, ArrayList<UUID> playerIds) {
        if (!plugin.getConfig().getBoolean("replay.enabled", true)) return;
        if (arena == null || mode == null || playerIds == null || playerIds.isEmpty()) return;

        String replayId = "some_unique_id";
        List<String> names = new ArrayList<String>();
        for (UUID id : playerIds) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) names.add(p.getName());
        }
        if (names.isEmpty()) return;

        String id = randomId(10);

        // This matches your Skript idea: one text argument containing player list
        String playersArg = joinNames(names); // e.g. "Herex_777,Eylone_7"

        // Start AdvancedReplay recording
        String cmd = "advancedreplay:replay start " + id + " " + playersArg;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);

        // Save metadata for our own GUI
        String map = arena.getName() != null ? arena.getName() : arena.getWorldName();
        String server = Bukkit.getServerName();
        String modeDisplay = mode.getDisplayName();

        ReplayEntry entry = new ReplayEntry(
                id,
                System.currentTimeMillis(),
                map,
                server,
                modeDisplay,
                names
        );
        storage.saveEntry(entry);
    }

    private String joinNames(List<String> names) {
        // Skript just passed one single "%{_playerlist}%" text.
        // Joining by comma is safe: AdvancedReplay just wants names.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(names.get(i));
        }
        return sb.toString();
    }

    private String randomId(int len) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // ================= GUI =================

    public void openReplayList(Player p) {
        List<ReplayEntry> list = storage.getForPlayer(p.getName());
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE);

        if (list.isEmpty()) {
            // Center-ish bedrock item, as you wanted
            int slot = 22;
            ItemStack bedrock = new ItemStack(Material.BEDROCK);
            ItemMeta meta = bedrock.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "No replays found");
            List<String> lore = new ArrayList<String>();
            lore.add(ChatColor.GRAY + "You need to play a game");
            lore.add(ChatColor.GRAY + "to create replays.");
            meta.setLore(lore);
            bedrock.setItemMeta(meta);
            inv.setItem(slot, bedrock);
            p.openInventory(inv);
            return;
        }

        int slot = 10;
        for (ReplayEntry e : list) {
            if (slot > 43) break; // keep within 4 rows visually

            ItemStack paper = new ItemStack(Material.FISHING_ROD);
            ItemMeta im = paper.getItemMeta();
            im.setDisplayName(ChatColor.GOLD + "Replay " + e.getId());
            List<String> lore = new ArrayList<String>();
            lore.add(ChatColor.GRAY + "Mode: " + ChatColor.GREEN + e.getMode());
            lore.add(ChatColor.GRAY + "Map: " + ChatColor.GREEN + e.getMap());
            lore.add(ChatColor.GRAY + "Date: " + ChatColor.GREEN + dateFormat.format(new Date(e.getCreatedAt())));
            lore.add(" ");
            lore.add(ChatColor.YELLOW + "Click to watch replay.");
            im.setLore(lore);
            paper.setItemMeta(im);
            inv.setItem(slot, paper);

            slot++;
            if (slot % 9 == 0) slot += 2; // nice spacing
        }

        p.openInventory(inv);
    }

    public void playReplay(Player p, String id) {
        if (id == null || id.isEmpty()) return;
        p.closeInventory();
        // AdvancedReplay: only players can watch, so we let the player execute the command
        p.performCommand("advancedreplay:replay play " + id);
    }

    public void deleteReplay(Player p, String id) {
        if (id == null || id.isEmpty()) return;
        storage.delete(id);
        p.sendMessage(ChatColor.RED + "Deleted replay " + id + ".");
    }

    public void stopRecording(String replayId) {
    }
}
