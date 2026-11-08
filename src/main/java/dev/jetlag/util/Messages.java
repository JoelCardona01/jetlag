package dev.jetlag.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Loads plugins/JetLag/lang/&lt;code&gt;.yml. Missing keys fall back to the copy
 * bundled in the jar, so updating the plugin never shows blank messages.
 * Colors use the classic '&amp;' codes, which work on every version.
 */
public final class Messages {

    private static final String[] BUNDLED = {"en", "es"};

    private final JavaPlugin plugin;
    private YamlConfiguration lang = new YamlConfiguration();

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load(String code) {
        for (String bundled : BUNDLED) {
            File f = new File(plugin.getDataFolder(), "lang/" + bundled + ".yml");
            if (!f.exists()) {
                plugin.saveResource("lang/" + bundled + ".yml", false);
            }
        }

        File file = new File(plugin.getDataFolder(), "lang/" + code + ".yml");
        if (!file.exists()) {
            plugin.getLogger().warning("Language '" + code + "' not found, using English.");
            file = new File(plugin.getDataFolder(), "lang/en.yml");
        }
        lang = YamlConfiguration.loadConfiguration(file);

        InputStream defaults = plugin.getResource("lang/en.yml");
        if (defaults != null) {
            lang.setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaults, StandardCharsets.UTF_8)));
        }
    }

    /**
     * Returns a colored message. Placeholders are passed as pairs:
     * {@code get("key", "player", "Steve")} replaces {player} with Steve.
     */
    public String get(String key, String... placeholders) {
        // getString(path) also checks the bundled English defaults;
        // getString(path, def) would skip them.
        String raw = lang.getString(key);
        if (raw == null) raw = key;
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            raw = raw.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        return color(raw);
    }

    /** Same as {@link #get} but with the configured prefix in front. */
    public String prefixed(String key, String... placeholders) {
        return get("prefix") + get(key, placeholders);
    }

    public void send(CommandSender to, String key, String... placeholders) {
        to.sendMessage(prefixed(key, placeholders));
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
