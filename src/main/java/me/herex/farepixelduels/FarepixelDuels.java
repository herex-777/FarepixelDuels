package me.herex.farepixelduels;

import me.herex.farepixelduels.coins.CoinManager;
import me.herex.farepixelduels.coins.CoinsCommand;
import me.herex.farepixelduels.duels.*;
import me.herex.farepixelduels.duels.commands.*;
import me.herex.farepixelduels.listeners.*;
import me.herex.farepixelduels.placeholders.CoinPlaceholders;
import me.herex.farepixelduels.spawn.SetSpawnCommand;
import me.herex.farepixelduels.spawn.SpawnCommand;
import me.herex.farepixelduels.spawn.StuckCommand;
import me.herex.farepixelduels.storage.CoinsRepository;
import me.herex.farepixelduels.storage.DatabaseManager;
import me.herex.farepixelduels.storage.StatsRepository;
import me.herex.farepixelduels.replay.ReplayManagerImpl;
import me.herex.farepixelduels.commands.ReplayCommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FarepixelDuels extends JavaPlugin {

    private static FarepixelDuels instance;

    // Database / coins / stats
    private DatabaseManager databaseManager;
    private CoinManager coinManager;
    private StatsManager statsManager;

    // Duels systems
    private ArenaManager arenaManager;
    private KitManager kitManager;
    private DuelQueueManager duelQueueManager;
    private ScoreboardManager scoreboardManager;
    private PlayerKitManager playerKitManager;

    // Admin setup sessions
    private final Map<UUID, SetupSession> setupSessions = new HashMap<UUID, SetupSession>();
    private final Map<UUID, Arena> buildingArenas = new HashMap<UUID, Arena>();
    private final Map<UUID, GameMode> kitEditors = new HashMap<UUID, GameMode>();

    // Player kit editor sessions (for /kiteditor)
    private final Map<UUID, GameMode> playerKitEditors =
            new ConcurrentHashMap<UUID, GameMode>();

    // Replay system
    private ReplayManagerImpl replayManager;

    // Duel request system
    private DuelRequestManager duelRequestManager;
    private DuelCommand duelCommand;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        consoleBanner(true);

        // === Database / coins / stats ===
        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.init();

            CoinsRepository coinsRepo = new CoinsRepository(databaseManager);
            coinsRepo.setupTables();
            coinManager = new CoinManager(coinsRepo);

            StatsRepository statsRepo = new StatsRepository(databaseManager);
            statsRepo.setupTables();
            statsManager = new StatsManager(statsRepo);

        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage("§c[FarepixelDuels] Failed to connect to database! Disabling plugin.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // === Core duels managers ===
        arenaManager = new ArenaManager(this);
        arenaManager.load();

        kitManager = new KitManager(this);
        playerKitManager = new PlayerKitManager(this);
        kitManager.setPlayerKits(playerKitManager);

        scoreboardManager = new ScoreboardManager(this, statsManager);
        duelQueueManager = new DuelQueueManager(this, arenaManager, kitManager, scoreboardManager, statsManager, coinManager);

        // === Replay manager ===
        replayManager = new ReplayManagerImpl(this);

        // === Duel request + command ===
        duelRequestManager = new DuelRequestManager(this);
        // NOTE: this DuelCommand version uses plugin.getDuelQueueManager() internally
        duelCommand = new DuelCommand(this, duelRequestManager);

        // === Commands ===
        getCommand("coins").setExecutor(new CoinsCommand(coinManager));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("stuck").setExecutor(new StuckCommand(this));

        getCommand("duels").setExecutor(new DuelsCommand(this, arenaManager, kitManager, setupSessions, buildingArenas, kitEditors));
        getCommand("play").setExecutor(new PlayCommand(duelQueueManager));
        getCommand("leave").setExecutor(new LeaveCommand(this));
        getCommand("l").setExecutor(new LeaveCommand(this));
        getCommand("kiteditor").setExecutor(new KitEditorCommand());

        getCommand("duel").setExecutor(duelCommand);
        getCommand("replay").setExecutor(new ReplayCommand(replayManager));

        // === Listeners ===
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new JoinListener(this), this);
        pm.registerEvents(new BuildProtectionListener(this), this);

        // Replay GUI
        pm.registerEvents(new ReplayGUIListener(this, replayManager), this);

        // Duel selector GUI + right-click duel
        pm.registerEvents(new DuelSelectMenuListener(this, duelCommand), this);
        pm.registerEvents(new DuelRightClickListener(this), this);

        // Return / play-again item + lobby/queue pvp
        pm.registerEvents(new ReturnItemListener(this), this);
        pm.registerEvents(new PlayAgainListener(this), this);
        pm.registerEvents(new LobbyQueuePvpListener(this, duelQueueManager), this);

        // Arena / kit editor / gameplay
        pm.registerEvents(new BedProtectionListener(), this);
        pm.registerEvents(new ArenaSetupListener(this, arenaManager, setupSessions, buildingArenas), this);
        pm.registerEvents(new KitEditorListener(kitManager, kitEditors), this);
        pm.registerEvents(new DuelGameplayListener(this, duelQueueManager), this);
        pm.registerEvents(new DuelCombatListener(duelQueueManager), this);

        // Player kit editor (normal /kiteditor)
        pm.registerEvents(new PlayerKitMenuListener(this, playerKitEditors), this);
        pm.registerEvents(new PlayerKitSaveListener(playerKitManager, playerKitEditors), this);

        // PlaceholderAPI (coins + stats)
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CoinPlaceholders(this, coinManager, statsManager).register();
        }
    }

    @Override
    public void onDisable() {
        consoleBanner(false);
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    private void consoleBanner(boolean enable) {
        String line = ChatColor.DARK_GRAY + "----------------------------------------";
        Bukkit.getConsoleSender().sendMessage(line);
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + " FarepixelDuels " + ChatColor.GRAY + "v" + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + " Status: " + (enable ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + " Author: " + ChatColor.WHITE + "Herex");
        Bukkit.getConsoleSender().sendMessage(line);
    }

    // === Getters ===

    public static FarepixelDuels getInstance() {
        return instance;
    }

    public DuelQueueManager getDuelQueueManager() {
        return duelQueueManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public PlayerKitManager getPlayerKitManager() {
        return playerKitManager;
    }

    public ReplayManagerImpl getReplayManager() {
        return replayManager;
    }
}
