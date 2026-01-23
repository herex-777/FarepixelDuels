package me.herex.farepixelduels;

import me.herex.farepixelduels.coins.CoinManager;
import me.herex.farepixelduels.coins.CoinsCommand;
import me.herex.farepixelduels.duels.*;
import me.herex.farepixelduels.duels.commands.DuelsCommand;
import me.herex.farepixelduels.duels.commands.PlayCommand;
import me.herex.farepixelduels.listeners.WeatherListener;
import me.herex.farepixelduels.listeners.ReturnItemListener;
import me.herex.farepixelduels.listeners.PlayerKitSaveListener;
import me.herex.farepixelduels.listeners.PlayerKitMenuListener;
import me.herex.farepixelduels.duels.commands.LeaveCommand;
import me.herex.farepixelduels.duels.commands.KitEditorCommand;
import me.herex.farepixelduels.duels.PlayerKitManager;
import me.herex.farepixelduels.listeners.*;
import me.herex.farepixelduels.placeholders.CoinPlaceholders;
import me.herex.farepixelduels.spawn.SetSpawnCommand;
import me.herex.farepixelduels.spawn.SpawnCommand;
import me.herex.farepixelduels.spawn.StuckCommand;
import me.herex.farepixelduels.storage.CoinsRepository;
import me.herex.farepixelduels.storage.DatabaseManager;
import me.herex.farepixelduels.storage.StatsRepository;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FarepixelDuels extends JavaPlugin {

    private static FarepixelDuels instance;

    private DatabaseManager databaseManager;
    private CoinManager coinManager;

    // Duels systems
    private ArenaManager arenaManager;
    private KitManager kitManager;
    private DuelQueueManager duelQueueManager;
    private StatsManager statsManager;
    private ScoreboardManager scoreboardManager;
    private PlayerKitManager playerKitManager;

    private final java.util.Map<java.util.UUID, GameMode> playerKitEditors = new java.util.HashMap<java.util.UUID, GameMode>();

    // admin sessions
    private final Map<UUID, SetupSession> setupSessions = new HashMap<UUID, SetupSession>();
    private final Map<UUID, Arena> buildingArenas = new HashMap<UUID, Arena>();
    private final Map<UUID, GameMode> kitEditors = new HashMap<UUID, GameMode>();

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        consoleBanner(true);

        // Init database (coins + stats + future)
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

        // Commands (existing)
        getCommand("coins").setExecutor(new CoinsCommand(coinManager));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("stuck").setExecutor(new StuckCommand(this));

        // Base listeners
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new BuildProtectionListener(this), this);

        // Duels init
        arenaManager = new ArenaManager(this);
        arenaManager.load();

        kitManager = new KitManager(this);
        playerKitManager = new PlayerKitManager(this);
        kitManager.setPlayerKits(playerKitManager);
        scoreboardManager = new ScoreboardManager(this, statsManager);
        duelQueueManager = new DuelQueueManager(this, arenaManager, kitManager, scoreboardManager, statsManager, coinManager);

        getCommand("duels").setExecutor(new DuelsCommand(this, arenaManager, kitManager, setupSessions, buildingArenas, kitEditors));
        getCommand("play").setExecutor(new PlayCommand(duelQueueManager));

        getCommand("leave").setExecutor(new LeaveCommand(this));
        getCommand("l").setExecutor(new LeaveCommand(this));
        getCommand("kiteditor").setExecutor(new KitEditorCommand());

        getServer().getPluginManager().registerEvents(new ArenaSetupListener(this, arenaManager, setupSessions, buildingArenas), this);
        getServer().getPluginManager().registerEvents(new KitEditorListener(kitManager, kitEditors), this);
        getServer().getPluginManager().registerEvents(new DuelGameplayListener(this, duelQueueManager), this);
        getServer().getPluginManager().registerEvents(new ReturnItemListener(this), this);
        getServer().getPluginManager().registerEvents(new WeatherListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DuelCombatListener(duelQueueManager), this);
        getServer().getPluginManager().registerEvents(new PlayerKitMenuListener(this, playerKitEditors), this);
        getServer().getPluginManager().registerEvents(new PlayerKitSaveListener(playerKitManager, playerKitEditors), this);

        // PlaceholderAPI (coins + stats)
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CoinPlaceholders(this, coinManager, statsManager).register();
        }
    }

    @Override
    public void onDisable() {
        consoleBanner(false);
        if (databaseManager != null) databaseManager.close();
    }

    private void consoleBanner(boolean enable) {
        String line = ChatColor.DARK_GRAY + "----------------------------------------";
        Bukkit.getConsoleSender().sendMessage(line);
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + " FarepixelDuels " + ChatColor.GRAY + "v" + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + " Status: " + (enable ? ChatColor.GREEN + "ENABLED" : ChatColor.RED + "DISABLED"));
        Bukkit.getConsoleSender().sendMessage(ChatColor.GRAY + " Author: " + ChatColor.WHITE + "Herex");
        Bukkit.getConsoleSender().sendMessage(line);
    }

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
}
