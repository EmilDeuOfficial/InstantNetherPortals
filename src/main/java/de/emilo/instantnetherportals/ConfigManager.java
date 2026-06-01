package de.emilo.instantnetherportals;

import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    private boolean enabled;
    private int teleportDelay;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        plugin.reloadConfig();
        enabled = plugin.getConfig().getBoolean("enabled", true);
        teleportDelay = Math.max(0, plugin.getConfig().getInt("teleport-delay", 0));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        plugin.getConfig().set("enabled", enabled);
        plugin.saveConfig();
    }

    /** Returns the configured delay in ticks (0 = instant). */
    public int getTeleportDelay() {
        return teleportDelay;
    }
}
