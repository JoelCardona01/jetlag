package dev.jetlag.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * One "side effect" of teleporting. To add a new one: implement this interface
 * (or extend {@link AbstractEvent}), register it in {@link EventRegistry} and
 * add its weight to config.yml plus a message in each lang file.
 */
public interface JetLagEvent {

    /** Stable id used in config.yml, lang files and /jetlag test. */
    String id();

    /** Whether the event makes sense right now (e.g. needs another player). */
    default boolean canRun(Player player, Location destination) {
        return true;
    }

    /**
     * Optionally changes where the player lands. Called <em>before</em> the
     * teleport happens. Return null to keep the original destination.
     */
    default Location redirect(Player player, Location destination) {
        return null;
    }

    /**
     * Ticks to wait before the chat announcement. Useful for pranks that would
     * be spoiled by an instant message (see fake_ban).
     */
    default long announceDelayTicks() {
        return 0L;
    }

    /** Runs the effect. Called on the main thread shortly after arrival. */
    void run(Player player);
}
