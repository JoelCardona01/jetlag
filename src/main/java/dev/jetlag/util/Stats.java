package dev.jetlag.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Counts how many times each player has suffered Jet Lag, for /jetlag stats.
 * Stored in plugins/JetLag/stats.yml keyed by UUID (names can change).
 */
public final class Stats {

    public static final class Entry {
        public final String name;
        public final int count;

        Entry(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }

    private final JavaPlugin plugin;
    private final File file;
    private final Map<UUID, Integer> counts = new HashMap<>();
    private final Map<UUID, String> names = new HashMap<>();
    private boolean dirty;

    public Stats(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "stats.yml");
        load();
    }

    private void load() {
        if (!file.exists()) return;
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection players = yml.getConfigurationSection("players");
        if (players == null) return;
        for (String key : players.getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                counts.put(id, players.getInt(key + ".count"));
                names.put(id, players.getString(key + ".name", "?"));
            } catch (IllegalArgumentException ignored) {
                // skip broken entries
            }
        }
    }

    public void increment(Player player) {
        UUID id = player.getUniqueId();
        counts.merge(id, 1, Integer::sum);
        names.put(id, player.getName());
        dirty = true;
    }

    public int get(Player player) {
        return counts.getOrDefault(player.getUniqueId(), 0);
    }

    public List<Entry> top(int limit) {
        List<Entry> list = new ArrayList<>();
        for (Map.Entry<UUID, Integer> e : counts.entrySet()) {
            list.add(new Entry(names.getOrDefault(e.getKey(), "?"), e.getValue()));
        }
        list.sort((a, b) -> Integer.compare(b.count, a.count));
        return list.size() > limit ? list.subList(0, limit) : list;
    }

    public void save() {
        if (!dirty) return;
        YamlConfiguration yml = new YamlConfiguration();
        for (Map.Entry<UUID, Integer> e : counts.entrySet()) {
            String base = "players." + e.getKey();
            yml.set(base + ".name", names.get(e.getKey()));
            yml.set(base + ".count", e.getValue());
        }
        try {
            yml.save(file);
            dirty = false;
        } catch (IOException ex) {
            plugin.getLogger().warning("Could not save stats.yml: " + ex.getMessage());
        }
    }
}
