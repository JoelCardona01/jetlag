package dev.jetlag.events;

import dev.jetlag.JetLagPlugin;
import dev.jetlag.util.Compat;
import dev.jetlag.util.Tags;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/** A creeper hisses behind you... and pops into harmless fireworks. Gotcha. */
public final class FakeCreeperEvent extends AbstractEvent {

    public FakeCreeperEvent(JetLagPlugin plugin) {
        super("fake_creeper", plugin);
    }

    @Override
    public void run(Player player) {
        Location here = player.getLocation();
        Vector facing = here.getDirection().setY(0);
        // Looking straight up/down gives a zero vector; then just pick any side.
        Location spot = facing.lengthSquared() < 1.0E-4
                ? Compat.spotAround(here, 2.0)
                : Compat.spotAround(here.clone().subtract(facing.normalize().multiply(2)), 0.5);

        Creeper creeper = Tags.mark(player.getWorld().spawn(spot, Creeper.class));
        creeper.setExplosionRadius(0); // just in case it ever explodes
        creeper.setAI(false);
        Compat.sound(spot, "entity.creeper.primed", 1.5f, 1f);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!creeper.isValid()) return;
            Location pop = creeper.getLocation().add(0, 1, 0);
            creeper.remove();
            Compat.celebrate(pop, Color.LIME, Color.WHITE);
        }, Math.max(10, opt("fuse-ticks", 30)));
    }
}
