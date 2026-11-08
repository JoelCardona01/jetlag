package dev.jetlag.events;

import dev.jetlag.JetLagPlugin;
import dev.jetlag.util.Compat;
import dev.jetlag.util.Tags;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;

/** A charged, fast creeper appears next to you and wants a hug. */
public final class ChargedCreeperEvent extends AbstractEvent {

    public ChargedCreeperEvent(JetLagPlugin plugin) {
        super("charged_creeper", plugin);
    }

    @Override
    public void run(Player player) {
        Location spot = Compat.spotAround(player.getLocation(), opt("distance", 4.0));
        Creeper creeper = Tags.mark(player.getWorld().spawn(spot, Creeper.class));
        creeper.setPowered(true);
        creeper.setTarget(player);
        int speed = Math.max(0, opt("speed-level", 2) - 1);
        Compat.effect(creeper, 60, speed, "SPEED");
        Compat.sound(spot, "entity.creeper.primed", 1f, 0.8f);
    }
}
