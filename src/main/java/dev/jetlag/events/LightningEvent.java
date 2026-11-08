package dev.jetlag.events;

import dev.jetlag.JetLagPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Zeus disapproves of your travel plans. Uses the visual-only lightning plus
 * manual damage so no fire spreads to builds.
 */
public final class LightningEvent extends AbstractEvent {

    public LightningEvent(JetLagPlugin plugin) {
        super("lightning", plugin);
    }

    @Override
    public void run(Player player) {
        Location loc = player.getLocation();
        player.getWorld().strikeLightningEffect(loc);
        double damage = opt("damage", 4.0);
        if (damage > 0) {
            player.damage(damage);
        }
        player.setFireTicks(opt("fire-seconds", 3) * 20);
    }
}
