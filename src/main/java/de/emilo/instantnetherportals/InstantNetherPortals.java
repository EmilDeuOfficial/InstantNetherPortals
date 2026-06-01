package de.emilo.instantnetherportals;

import org.bukkit.plugin.java.JavaPlugin;

public class InstantNetherPortals extends JavaPlugin {

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);

        getServer().getPluginManager().registerEvents(
                new PortalListener(this, configManager), this);

        PortalCommand cmd = new PortalCommand(configManager);
        getCommand("instantportal").setExecutor(cmd);
        getCommand("instantportal").setTabCompleter(cmd);

        getLogger().info("InstantNetherPortals v" + getDescription().getVersion() + " aktiviert.");
    }

    @Override
    public void onDisable() {
        getLogger().info("InstantNetherPortals deaktiviert.");
    }
}
