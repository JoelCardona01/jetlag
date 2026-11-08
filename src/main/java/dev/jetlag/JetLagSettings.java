package dev.jetlag;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Immutable snapshot of config.yml. A new instance is created on every reload,
 * so the rest of the code never has to read raw config paths.
 */
public final class JetLagSettings {

    public final boolean enabled;
    public final double chancePercent;
    public final long cooldownMillis;
    public final double minDistance;
    public final Set<TeleportCause> causes;
    public final Set<String> disabledWorlds;
    public final boolean broadcast;
    public final boolean blockDamage;

    JetLagSettings(JetLagPlugin plugin, FileConfiguration cfg) {
        enabled = cfg.getBoolean("enabled", true);
        chancePercent = clamp(cfg.getDouble("chance", 15.0), 0, 100);
        cooldownMillis = Math.max(0, cfg.getLong("cooldown-seconds", 10)) * 1000L;
        minDistance = Math.max(0, cfg.getDouble("min-distance", 0));
        broadcast = cfg.getBoolean("broadcast", true);
        blockDamage = cfg.getBoolean("block-damage", false);

        // Teleport causes are read by name. Unknown names (e.g. a cause that
        // only exists in newer Minecraft versions) are skipped with a warning,
        // which keeps one config file valid across many server versions.
        causes = EnumSet.noneOf(TeleportCause.class);
        for (String name : cfg.getStringList("trigger-causes")) {
            try {
                causes.add(TeleportCause.valueOf(name.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Unknown teleport cause in config: " + name);
            }
        }

        disabledWorlds = new HashSet<>();
        for (String w : cfg.getStringList("disabled-worlds")) {
            disabledWorlds.add(w.toLowerCase(Locale.ROOT));
        }
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
