package de.emilo.instantnetherportals.config;

import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    private boolean enabled;
    private int teleportDelay;
    private boolean preloadEnabled;
    private int preloadRadius;
    private String language;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        plugin.reloadConfig();
        enabled = plugin.getConfig().getBoolean("enabled", true);
        teleportDelay = Math.max(0, plugin.getConfig().getInt("teleport-delay", 0));
        preloadEnabled = plugin.getConfig().getBoolean("chunk-preload.enabled", true);
        preloadRadius = Math.max(1, plugin.getConfig().getInt("chunk-preload.radius", 10));
        language = plugin.getConfig().getString("language", "en");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        plugin.getConfig().set("enabled", enabled);
        plugin.saveConfig();
    }

    public int getTeleportDelay() {
        return teleportDelay;
    }

    public boolean isPreloadEnabled() {
        return preloadEnabled;
    }

    public int getPreloadRadius() {
        return preloadRadius;
    }

    public String getLanguage() {
        return language;
    }
}
