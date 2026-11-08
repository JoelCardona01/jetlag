package dev.jetlag.events;

import dev.jetlag.JetLagPlugin;
import dev.jetlag.util.Compat;
import org.bukkit.entity.Player;

/**
 * Classic jet lag: dizzy and slow. Effect names changed in 1.20.5, so both
 * the new and the old name are tried.
 */
public final class DrunkEvent extends AbstractEvent {

    public DrunkEvent(JetLagPlugin plugin) {
        super("drunk", plugin);
    }

    @Override
    public void run(Player player) {
        int seconds = Math.max(1, opt("seconds", 10));
        Compat.effect(player, seconds, 0, "NAUSEA", "CONFUSION");
        Compat.effect(player, seconds, 1, "SLOWNESS", "SLOW");
        Compat.sound(player.getLocation(), "entity.player.burp", 1f, 0.8f);
    }
}
