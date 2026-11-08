package dev.jetlag.events;

import dev.jetlag.JetLagPlugin;
import dev.jetlag.util.Tags;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Anvils fall from the sky. They vanish on landing (see ProtectionListener),
 * so nothing is left behind.
 */
public final class AnvilRainEvent extends AbstractEvent {

    public AnvilRainEvent(JetLagPlugin plugin) {
        super("anvil_rain", plugin);
    }

    @Override
    public void run(Player player) {
        Location base = player.getLocation();
        World world = player.getWorld();
        int count = Math.max(1, opt("count", 5));
        int height = clearHeight(base, Math.max(3, opt("height", 12)));
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        for (int i = 0; i < count; i++) {
            // The first anvil always aims straight at the player.
            double dx = i == 0 ? 0 : rnd.nextDouble(-2.0, 2.0);
            double dz = i == 0 ? 0 : rnd.nextDouble(-2.0, 2.0);
            Location spot = base.clone().add(dx, height + i * 1.5, dz);
            FallingBlock anvil = Tags.mark(world.spawnFallingBlock(spot, Material.ANVIL.createBlockData()));
            anvil.setDropItem(false);
            anvil.setHurtEntities(true);
        }
    }

    /** Stops below the first solid block so anvils don't spawn inside cave ceilings. */
    private static int clearHeight(Location base, int wanted) {
        World world = base.getWorld();
        for (int dy = 2; dy <= wanted; dy++) {
            if (world.getBlockAt(base.getBlockX(), base.getBlockY() + dy, base.getBlockZ()).getType().isSolid()) {
                return Math.max(2, dy - 1);
            }
        }
        return wanted;
    }
}
