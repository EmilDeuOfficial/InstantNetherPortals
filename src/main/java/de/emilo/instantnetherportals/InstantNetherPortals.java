package de.emilo.instantnetherportals;

import de.emilo.instantnetherportals.command.PortalCommand;
import de.emilo.instantnetherportals.config.ConfigManager;
import de.emilo.instantnetherportals.portal.PortalAccelerator;
import de.emilo.instantnetherportals.preload.ChunkPreloader;
import org.bukkit.plugin.java.JavaPlugin;

public class InstantNetherPortals extends JavaPlugin {

    private static boolean paper;

    @Override
    public void onEnable() {
        paper = detectPaper();

        saveDefaultConfig();
        ConfigManager configManager = new ConfigManager(this);

        new PortalAccelerator(this, configManager).start();
        getServer().getPluginManager().registerEvents(new ChunkPreloader(this, configManager), this);

        PortalCommand command = new PortalCommand(this, configManager);
        getCommand("instantportal").setExecutor(command);
        getCommand("instantportal").setTabCompleter(command);

        String version = getDescription().getVersion();
        if (paper) {
            getLogger().info("InstantNetherPortals v" + version + " enabled (Paper).");
        } else {
            getLogger().warning("InstantNetherPortals v" + version + " loaded on Spigot/Bukkit.");
            getLogger().warning("Instant portal teleportation needs Paper, the portal acceleration is disabled.");
            getLogger().warning("Chunk pre-loading runs in basic async mode without ticket pinning.");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("InstantNetherPortals disabled.");
    }

    public static boolean isPaper() {
        return paper;
    }

    private static boolean detectPaper() {
        try {
            Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
