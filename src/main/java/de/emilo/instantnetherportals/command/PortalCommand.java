package de.emilo.instantnetherportals.command;

import de.emilo.instantnetherportals.config.ConfigManager;
import de.emilo.instantnetherportals.lang.Lang;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public class PortalCommand implements CommandExecutor, TabCompleter {

    private static final String ADMIN_PERMISSION = "instantnetherportals.admin";
    private static final List<String> SUBCOMMANDS = List.of("on", "off", "reload", "status");

    private final JavaPlugin plugin;
    private final ConfigManager config;
    private Lang lang;

    public PortalCommand(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.lang = new Lang(plugin, config.getLanguage());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage(ChatColor.RED + lang.get("no-permission"));
            return true;
        }

        if (args.length == 0) {
            boolean enabled = !config.isEnabled();
            config.setEnabled(enabled);
            sender.sendMessage(prefix() + (enabled
                    ? ChatColor.GREEN + lang.get("toggled-on")
                    : ChatColor.RED + lang.get("toggled-off")));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "on", "enable" -> {
                config.setEnabled(true);
                sender.sendMessage(prefix() + ChatColor.GREEN + lang.get("enabled"));
            }
            case "off", "disable" -> {
                config.setEnabled(false);
                sender.sendMessage(prefix() + ChatColor.RED + lang.get("disabled"));
            }
            case "reload", "rl" -> {
                config.load();
                lang = new Lang(plugin, config.getLanguage());
                sender.sendMessage(prefix() + ChatColor.GREEN + lang.get("reloaded"));
            }
            case "status" -> {
                boolean enabled = config.isEnabled();
                String state = enabled ? lang.get("status-active") : lang.get("status-inactive");
                ChatColor stateColor = enabled ? ChatColor.GREEN : ChatColor.RED;
                sender.sendMessage(prefix() + ChatColor.YELLOW + lang.get("status-state") + stateColor + state);
                sender.sendMessage(prefix() + ChatColor.YELLOW + lang.get("status-delay",
                        "ticks", config.getTeleportDelay(),
                        "secs", String.format("%.1f", config.getTeleportDelay() / 20.0)));
            }
            default -> sender.sendMessage(prefix() + ChatColor.YELLOW + lang.get("unknown", "label", label));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION) || args.length != 1) return Collections.emptyList();
        String typed = args[0].toLowerCase();
        return SUBCOMMANDS.stream().filter(sub -> sub.startsWith(typed)).toList();
    }

    private String prefix() {
        return ChatColor.DARK_AQUA + "[INP] " + ChatColor.RESET;
    }
}
