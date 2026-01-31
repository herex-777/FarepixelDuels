package me.herex.farepixelduels.quests;

import me.herex.farepixelduels.FarepixelDuels;
import me.herex.farepixelduels.duels.GameMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class QuestManager {

    public static final String GUI_TITLE = ChatColor.DARK_GRAY + "Duels Quests";

    private final FarepixelDuels plugin;

    // all quests from quests.yml
    private final Map<String, QuestDefinition> quests = new LinkedHashMap<>();

    // player -> (slot -> questId) for open GUI
    private final Map<UUID, Map<Integer, String>> openGuis = new HashMap<>();

    // persistence
    private final File dataFile;
    private FileConfiguration dataCfg;

    private final File questsFile;
    private FileConfiguration questsCfg;

    public QuestManager(FarepixelDuels plugin) {
        this.plugin = plugin;

        this.dataFile = new File(plugin.getDataFolder(), "quests-data.yml");
        this.questsFile = new File(plugin.getDataFolder(), "quests.yml");

        loadData();
        loadQuests();
    }

    // ================= LOADING =================

    private void loadData() {
        try {
            if (!dataFile.exists()) {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataCfg = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void saveData() {
        try {
            dataCfg.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadQuests() {
        loadQuests();
    }

    private void loadQuests() {
        quests.clear();

        try {
            if (!questsFile.exists()) {
                plugin.saveResource("quests.yml", false);
            }
        } catch (IllegalArgumentException ignored) {
            // file already exists or not inside jar
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        questsCfg = YamlConfiguration.loadConfiguration(questsFile);

        ConfigurationSection root = questsCfg.getConfigurationSection("quests");
        if (root == null) return;

        for (String id : root.getKeys(false)) {
            String base = "quests." + id + ".";

            String name = cc(questsCfg.getString(base + "display-name", "&aQuest"));
            String matStr = questsCfg.getString(base + "material", "PAPER");
            Material mat = Material.matchMaterial(matStr);
            if (mat == null) mat = Material.PAPER;

            int slot = questsCfg.getInt(base + "slot", -1);
            QuestType type = QuestType.fromString(questsCfg.getString(base + "type", "WIN_GAMES"));

            String modeStr = questsCfg.getString(base + "mode", "ANY");
            GameMode mode = null;
            if (!modeStr.equalsIgnoreCase("ANY")) {
                mode = GameMode.fromString(modeStr);
            }

            int required = questsCfg.getInt(base + "required", 1);
            List<String> desc = questsCfg.getStringList(base + "description");
            List<String> rewards = questsCfg.getStringList(base + "rewards");
            String repeat = questsCfg.getString(base + "repeat", "NONE");
            boolean daily = repeat.equalsIgnoreCase("DAILY");

            QuestDefinition q = new QuestDefinition(id, name, mat, slot, type, mode, required, desc, rewards, daily);
            quests.put(id, q);
        }

        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + "[FarepixelDuels] Loaded " + quests.size() + " quests.");
    }

    // ================= GUI =================

    public void openQuestsGui(Player p) {
        Inventory inv = Bukkit.createInventory(null, 44, GUI_TITLE);

        Map<Integer, String> map = new HashMap<>();

        for (QuestDefinition q : quests.values()) {
            int slot = q.getSlot();
            if (slot < 0 || slot >= inv.getSize()) continue;

            QuestProgressState st = loadState(p.getUniqueId(), q);

            ItemStack it = new ItemStack(q.getIcon());
            ItemMeta im = it.getItemMeta();
            im.setDisplayName(q.getDisplayName());

            im.addEnchant(Enchantment.DURABILITY, 1, true);
            im.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            List<String> lore = new ArrayList<>();
            for (String line : q.getDescription()) {
                line = line.replace("%progress%", String.valueOf(st.progress));
                line = line.replace("%required%", String.valueOf(q.getRequired()));
                lore.add(cc(line));
            }
            lore.add("");
            if (st.completed && st.rewardTaken) {
                lore.add(cc("&aQuest completed!"));
            } else if (st.completed) {
                lore.add(cc("&eClick to claim your rewards!"));
            } else {
                lore.add(cc("&eProgress: &b" + st.progress + "&7/&b" + q.getRequired()));
            }
            if (q.isDaily()) {
                lore.add("");
                lore.add(cc("&7Daily quest. Can be completed once per day."));
            }

            im.setLore(lore);
            it.setItemMeta(im);

            inv.setItem(slot, it);
            map.put(slot, q.getId());
        }

        // bedrock "no quests" if nothing mapped
        if (map.isEmpty()) {
            ItemStack bedrock = new ItemStack(Material.BEDROCK);
            ItemMeta meta = bedrock.getItemMeta();
            meta.setDisplayName(cc("&cNo quests configured"));
            meta.setLore(Arrays.asList(
                    cc("&7Add quests in quests.yml"),
                    cc("&7to make this menu useful.")
            ));
            bedrock.setItemMeta(meta);
            inv.setItem(22, bedrock);
        }

        openGuis.put(p.getUniqueId(), map);
        p.openInventory(inv);
    }

    public QuestDefinition getQuestFromGui(Player p, int slot) {
        Map<Integer, String> map = openGuis.get(p.getUniqueId());
        if (map == null) return null;
        String id = map.get(slot);
        if (id == null) return null;
        return quests.get(id);
    }

    public void clearOpenGui(Player p) {
        openGuis.remove(p.getUniqueId());
    }

    // ================= STATE =================

    private static class QuestProgressState {
        int progress;
        boolean completed;
        boolean rewardTaken;
        LocalDate lastDate;
    }

    private QuestProgressState loadState(UUID uuid, QuestDefinition q) {
        QuestProgressState st = new QuestProgressState();
        String base = "players." + uuid.toString() + "." + q.getId() + ".";
        st.progress = dataCfg.getInt(base + "progress", 0);
        st.completed = dataCfg.getBoolean(base + "completed", false);
        st.rewardTaken = dataCfg.getBoolean(base + "reward-taken", false);

        String dateStr = dataCfg.getString(base + "date", null);
        if (dateStr != null) {
            try {
                st.lastDate = LocalDate.parse(dateStr);
            } catch (Exception ignored) {}
        }

        // daily reset
        if (q.isDaily()) {
            LocalDate today = LocalDate.now();
            if (st.lastDate == null || !today.equals(st.lastDate)) {
                st.progress = 0;
                st.completed = false;
                st.rewardTaken = false;
                st.lastDate = today;
                saveState(uuid, q, st);
            }
        }

        return st;
    }

    private void saveState(UUID uuid, QuestDefinition q, QuestProgressState st) {
        String base = "players." + uuid.toString() + "." + q.getId() + ".";
        dataCfg.set(base + "progress", st.progress);
        dataCfg.set(base + "completed", st.completed);
        dataCfg.set(base + "reward-taken", st.rewardTaken);
        if (st.lastDate != null) {
            dataCfg.set(base + "date", st.lastDate.toString());
        }
        saveData();
    }

    // ================= PROGRESS HOOKS =================

    public void handleDuelEnd(GameMode mode, UUID winner, Collection<UUID> participants) {
        for (QuestDefinition q : quests.values()) {
            if (q.getMode() != null && q.getMode() != mode) continue;

            for (UUID u : participants) {
                boolean isWinner = (winner != null && winner.equals(u));
                addProgress(u, q, isWinner);
            }
        }
    }

    public void handleKill(GameMode mode, UUID killer) {
        if (killer == null) return;
        for (QuestDefinition q : quests.values()) {
            if (q.getMode() != null && q.getMode() != mode) continue;
            if (q.getType() != QuestType.GET_KILLS) continue;
            addProgress(killer, q, true);
        }
    }

    private void addProgress(UUID uuid, QuestDefinition q, boolean isWinner) {
        switch (q.getType()) {
            case WIN_GAMES:
                if (!isWinner) return;
                break;
            case PLAY_GAMES:
                // always counts
                break;
            case GET_KILLS:
                // handled from handleKill()
                break;
        }

        QuestProgressState st = loadState(uuid, q);
        if (st.completed) return; // already done for today or permanently

        st.progress++;
        if (st.progress >= q.getRequired()) {
            st.completed = true;
        }
        if (st.lastDate == null) {
            st.lastDate = LocalDate.now();
        }
        saveState(uuid, q, st);
    }

    // ================= CLICK HANDLING =================

    public void handleQuestClick(Player p, QuestDefinition q) {
        QuestProgressState st = loadState(p.getUniqueId(), q);

        if (!st.completed) {
            p.sendMessage(ChatColor.GREEN + "You started the quest: " + q.getDisplayName());
            return;
        }

        if (st.rewardTaken) {
            p.sendMessage(cc("&cYou have already claimed this quest's rewards."));
            return;
        }

        // run reward commands
        for (String cmdRaw : q.getRewardCommands()) {
            if (cmdRaw == null || cmdRaw.trim().isEmpty()) continue;
            String cmd = cmdRaw.replace("%player%", p.getName());
            if (cmd.startsWith("/")) cmd = cmd.substring(1);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        st.rewardTaken = true;
        saveState(p.getUniqueId(), q, st);

        p.sendMessage(cc("&aYou have completed the reward: &a" + q.getDisplayName() + "&a!"));
    }

    private String cc(String s) {
        return ChatColor.translateAlternateColorCodes('&', s == null ? "" : s);
    }
}
