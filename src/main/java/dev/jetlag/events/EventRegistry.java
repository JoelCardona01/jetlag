package dev.jetlag.events;

import dev.jetlag.JetLagPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Holds every Jet Lag event and their weights, chooses one at random and fires
 * it (effect + stats + chat announcement).
 */
public final class EventRegistry {

    private final JetLagPlugin plugin;
    private final Map<String, JetLagEvent> events = new LinkedHashMap<>();
    private final Map<String, Integer> weights = new HashMap<>();
    /** Players being teleported by /jetlag test, so the listener ignores them. */
    private final Set<UUID> suppressed = new HashSet<>();

    public EventRegistry(JetLagPlugin plugin) {
        this.plugin = plugin;
        register(new ChargedCreeperEvent(plugin));
        register(new LightningEvent(plugin));
        register(new TntRingEvent(plugin));
        register(new AnvilRainEvent(plugin));
        register(new SkyDropEvent(plugin));
        register(new FakeCreeperEvent(plugin));
        register(new FakeBanEvent(plugin));
        register(new ChickenPartyEvent(plugin));
        register(new WrongDestinationEvent(plugin));
        register(new HotbarShuffleEvent(plugin));
        register(new DrunkEvent(plugin));
        register(new FirstClassEvent(plugin));
    }

    private void register(JetLagEvent event) {
        events.put(event.id(), event);
    }

    /** Reads {@code events.<id>.enabled} and {@code events.<id>.weight}. */
    public void load(FileConfiguration cfg) {
        weights.clear();
        for (String id : events.keySet()) {
            boolean enabled = cfg.getBoolean("events." + id + ".enabled", true);
            int weight = cfg.getInt("events." + id + ".weight", 10);
            weights.put(id, enabled ? Math.max(0, weight) : 0);
        }
    }

    public int enabledCount() {
        int n = 0;
        for (int w : weights.values()) {
            if (w > 0) n++;
        }
        return n;
    }

    public List<String> ids() {
        return Collections.unmodifiableList(new ArrayList<>(events.keySet()));
    }

    public JetLagEvent byId(String id) {
        return events.get(id);
    }

    public boolean isSuppressed(Player player) {
        return suppressed.contains(player.getUniqueId());
    }

    /**
     * Weighted random pick among events that are enabled and can run for this
     * player/destination. Returns null if none qualifies.
     */
    public JetLagEvent pickRandom(Player player, Location destination) {
        List<JetLagEvent> pool = new ArrayList<>();
        int total = 0;
        for (JetLagEvent e : events.values()) {
            int w = weights.getOrDefault(e.id(), 0);
            if (w > 0 && e.canRun(player, destination)) {
                pool.add(e);
                total += w;
            }
        }
        if (total == 0) return null;

        int roll = ThreadLocalRandom.current().nextInt(total);
        for (JetLagEvent e : pool) {
            roll -= weights.get(e.id());
            if (roll < 0) return e;
        }
        return pool.get(pool.size() - 1); // unreachable, keeps the compiler happy
    }

    /** Runs the effect, counts it and announces it in chat. */
    public void fire(JetLagEvent event, Player player) {
        try {
            event.run(player);
        } catch (Throwable t) {
            // One broken event (e.g. an API missing on an old version) must
            // never break teleporting for everyone.
            plugin.getLogger().warning("Event '" + event.id() + "' failed: " + t);
            return;
        }
        plugin.stats().increment(player);

        Runnable announce = () -> {
            String msg = plugin.messages().prefixed("events." + event.id(), "player", player.getName());
            if (plugin.settings().broadcast) {
                plugin.getServer().broadcastMessage(msg);
            } else if (player.isOnline()) {
                player.sendMessage(msg);
            }
        };
        long delay = event.announceDelayTicks();
        if (delay > 0) {
            plugin.getServer().getScheduler().runTaskLater(plugin, announce, delay);
        } else {
            announce.run();
        }
    }

    /**
     * Used by /jetlag test: applies the redirect (if any) with a real teleport,
     * then fires the event, exactly like a natural trigger would.
     */
    public boolean trigger(JetLagEvent event, Player player) {
        Location here = player.getLocation();
        if (!event.canRun(player, here)) {
            return false;
        }
        Location redirected = event.redirect(player, here);
        if (redirected != null) {
            suppressed.add(player.getUniqueId());
            try {
                player.teleport(redirected);
            } finally {
                suppressed.remove(player.getUniqueId());
            }
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) fire(event, player);
        }, 2L);
        return true;
    }
}
