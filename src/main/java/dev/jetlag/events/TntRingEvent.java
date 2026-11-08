package dev.jetlag.events;

import dev.jetlag.JetLagPlugin;
import dev.jetlag.util.Compat;
import dev.jetlag.util.Tags;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

/** A ring of lit TNT appears around you. Run. */
public final class TntRingEvent extends AbstractEvent {

    public TntRingEvent(JetLagPlugin plugin) {
        super("tnt_ring", plugin);
    }

    @Override
    public void run(Player player) {
        Location center = player.getLocation();
        World world = player.getWorld();
        int count = Math.max(1, opt("count", 4));
        double radius = opt("radius", 5.0);
        int fuse = Math.max(1, (int) Math.round(opt("fuse-seconds", 3.0) * 20));

        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            Location spot = center.clone().add(Math.cos(angle) * radius, 0.5, Math.sin(angle) * radius);
            TNTPrimed tnt = Tags.mark(world.spawn(spot, TNTPrimed.class));
            tnt.setFuseTicks(fuse);
        }
        Compat.sound(center, "entity.tnt.primed", 1f, 1f);
    }
}
