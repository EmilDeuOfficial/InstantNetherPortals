package de.emilo.instantnetherportals;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PortalCommand implements CommandExecutor, TabCompleter {

    private final ConfigManager config;

    public PortalCommand(ConfigManager config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("instantnetherportals.admin")) {
            sender.sendMessage(ChatColor.RED + "Du hast keine Berechtigung für diesen Befehl.");
            return true;
        }

        if (args.length == 0) {
            boolean newState = !config.isEnabled();
            config.setEnabled(newState);
            sender.sendMessage(prefix() + (newState
                    ? ChatColor.GREEN + "InstantNetherPortals aktiviert."
                    : ChatColor.RED   + "InstantNetherPortals deaktiviert."));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "on", "enable", "an", "aktivieren" -> {
                config.setEnabled(true);
                sender.sendMessage(prefix() + ChatColor.GREEN + "Instant Portals aktiviert.");
            }
            case "off", "disable", "aus", "deaktivieren" -> {
                config.setEnabled(false);
                sender.sendMessage(prefix() + ChatColor.RED + "Instant Portals deaktiviert.");
            }
            case "reload", "rl" -> {
                config.load();
                sender.sendMessage(prefix() + ChatColor.GREEN + "Config neu geladen.");
            }
            case "status" -> {
                sender.sendMessage(prefix()
                        + "Status: " + (config.isEnabled()
                            ? ChatColor.GREEN + "Aktiv"
                            : ChatColor.RED   + "Inaktiv"));
                sender.sendMessage(prefix()
                        + ChatColor.YELLOW + "Teleport-Verzögerung: "
                        + ChatColor.WHITE + config.getTeleportDelay()
                        + ChatColor.YELLOW + " Ticks ("
                        + ChatColor.WHITE + String.format("%.1f", config.getTeleportDelay() / 20.0)
                        + ChatColor.YELLOW + " Sek.)");
            }
            default -> sender.sendMessage(prefix()
                    + ChatColor.YELLOW + "Verwendung: /" + label
                    + " [on|off|reload|status]");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("instantnetherportals.admin")) return Collections.emptyList();
        if (args.length == 1) {
            return Arrays.asList("on", "off", "reload", "status");
        }
        return Collections.emptyList();
    }

    private String prefix() {
        return ChatColor.DARK_AQUA + "[INP] " + ChatColor.RESET;
    }
}
