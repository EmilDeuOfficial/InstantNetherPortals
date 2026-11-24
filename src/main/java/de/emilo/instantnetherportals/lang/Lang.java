package de.emilo.instantnetherportals.lang;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Lang {

    private YamlConfiguration messages;

    public Lang(JavaPlugin plugin, String language) {
        load(plugin, language);
    }

    public void load(JavaPlugin plugin, String language) {
        File custom = new File(plugin.getDataFolder(), "lang/messages_" + language + ".yml");
        if (custom.exists()) {
            messages = YamlConfiguration.loadConfiguration(custom);
            return;
        }

        InputStream stream = plugin.getResource("lang/messages_" + language + ".yml");
        if (stream == null) {
            if (!language.equals("en")) {
                plugin.getLogger().warning("Language '" + language + "' not found, falling back to English.");
            }
            stream = plugin.getResource("lang/messages_en.yml");
        }

        messages = stream != null
                ? YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8))
                : new YamlConfiguration();
    }

    public String get(String key, Object... replacements) {
        String message = messages.getString(key, "?" + key + "?");
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace("{" + replacements[i] + "}", String.valueOf(replacements[i + 1]));
        }
        return message;
    }
}
