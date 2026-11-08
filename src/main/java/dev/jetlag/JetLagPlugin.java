package dev.jetlag;

import dev.jetlag.events.EventRegistry;
import dev.jetlag.util.Messages;
import dev.jetlag.util.Stats;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Entry point of the plugin. Wires together the configuration, the language
 * files, the event registry, the listeners and the /jetlag command.
 */
public final class JetLagPlugin extends JavaPlugin {

    private Messages messages;
    private Stats stats;
    private EventRegistry registry;
    private JetLagSettings settings;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        messages = new Messages(this);
        stats = new Stats(this);
        registry = new EventRegistry(this);
        reloadEverything();

        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);

        JetLagCommand command = new JetLagCommand(this);
        PluginCommand cmd = getCommand("jetlag");
        if (cmd != null) {
            cmd.setExecutor(command);
            cmd.setTabCompleter(command);
        }

        // Save stats every 5 minutes (6000 ticks) so a crash loses little.
        getServer().getScheduler().runTaskTimer(this, stats::save, 6000L, 6000L);

        getLogger().info("Jet Lag enabled with " + registry.enabledCount()
                + " events. Have a nice trip!");
    }

    @Override
    public void onDisable() {
        if (stats != null) {
            stats.save();
        }
    }

    /** Re-reads config.yml, the language file and event weights. */
    public void reloadEverything() {
        reloadConfig();
        settings = new JetLagSettings(this, getConfig());
        messages.load(getConfig().getString("language", "en"));
        registry.load(getConfig());
    }

    public Messages messages() {
        return messages;
    }

    public Stats stats() {
        return stats;
    }

    public EventRegistry registry() {
        return registry;
    }

    public JetLagSettings settings() {
        return settings;
    }
}
