package dev.jetlag;

import dev.jetlag.events.JetLagEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Listens to every teleport. When the dice roll succeeds, it picks a random
 * Jet Lag event, lets it change the destination (optional) and then runs the
 * event a couple of ticks after the player has arrived.
 */
public final class TeleportListener implements Listener {

    /** Ticks to wait after arrival so the client has loaded the destination. */
    private static final long ARRIVAL_DELAY_TICKS = 2L;

    private final JetLagPlugin plugin;
    private final Map<UUID, Long> lastTrigger = new HashMap<>();

    public TeleportListener(JetLagPlugin plugin) {
        this.plugin = plugin;
    }

    // HIGH so we run after most plugins have decided the destination, but we
    // can still change it (MONITOR must not modify events).
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        JetLagSettings s = plugin.settings();
        Player player = event.getPlayer();
        Location to = event.getTo();

        if (!s.enabled || to == null || to.getWorld() == null) return;
        if (!s.causes.contains(event.getCause())) return;
        if (plugin.registry().isSuppressed(player)) return;
        if (player.hasPermission("jetlag.bypass")) return;
        if (s.disabledWorlds.contains(to.getWorld().getName().toLowerCase(Locale.ROOT))) return;
        if (!farEnough(event.getFrom(), to, s.minDistance)) return;

        long now = System.currentTimeMillis();
        Long last = lastTrigger.get(player.getUniqueId());
        if (last != null && now - last < s.cooldownMillis) return;

        if (ThreadLocalRandom.current().nextDouble(100.0) >= s.chancePercent) return;

        JetLagEvent picked = plugin.registry().pickRandom(player, to);
        if (picked == null) return;

        lastTrigger.put(player.getUniqueId(), now);

        Location redirected = picked.redirect(player, to);
        if (redirected != null) {
            event.setTo(redirected);
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                plugin.registry().fire(picked, player);
            }
        }, ARRIVAL_DELAY_TICKS);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastTrigger.remove(event.getPlayer().getUniqueId());
    }

    private static boolean farEnough(Location from, Location to, double min) {
        if (min <= 0) return true;
        if (from == null || from.getWorld() == null || !from.getWorld().equals(to.getWorld())) {
            return true; // changing worlds always counts as a "real" trip
        }
        return from.distanceSquared(to) >= min * min;
    }
}
