package me.herex.farepixelduels.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WeatherListener implements Listener {

    private final JavaPlugin plugin;

    public WeatherListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent e) {
        // if it tries to start raining, cancel and set clear
        if (e.toWeatherState()) {
            e.setCancelled(true);
            e.getWorld().setStorm(false);
            e.getWorld().setThundering(false);
            e.getWorld().setWeatherDuration(0);
        }
    }
}
