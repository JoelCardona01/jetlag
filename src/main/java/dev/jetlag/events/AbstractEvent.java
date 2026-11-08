package dev.jetlag.events;

import dev.jetlag.JetLagPlugin;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Base class with helpers to read per-event options from
 * {@code events.<id>.*} in config.yml. Options are read every time the event
 * runs, so /jetlag reload applies them instantly.
 */
public abstract class AbstractEvent implements JetLagEvent {

    private final String id;
    protected final JetLagPlugin plugin;

    protected AbstractEvent(String id, JetLagPlugin plugin) {
        this.id = id;
        this.plugin = plugin;
    }

    @Override
    public final String id() {
        return id;
    }

    private ConfigurationSection section() {
        return plugin.getConfig().getConfigurationSection("events." + id);
    }

    protected int opt(String key, int def) {
        ConfigurationSection s = section();
        return s == null ? def : s.getInt(key, def);
    }

    protected double opt(String key, double def) {
        ConfigurationSection s = section();
        return s == null ? def : s.getDouble(key, def);
    }
}
