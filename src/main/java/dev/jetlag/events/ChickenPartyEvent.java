package dev.jetlag.events;

import dev.jetlag.JetLagPlugin;
import dev.jetlag.util.Compat;
import dev.jetlag.util.Tags;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** You land in the middle of a chicken party. They leave after a while. */
public final class ChickenPartyEvent extends AbstractEvent {

    public ChickenPartyEvent(JetLagPlugin plugin) {
        super("chicken_party", plugin);
    }

    @Override
    public void run(Player player) {
        int count = Math.max(1, opt("count", 12));
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        List<Chicken> chickens = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Chicken chicken = Tags.mark(player.getWorld().spawn(
                    Compat.spotAround(player.getLocation(), rnd.nextDouble(1.0, 3.0)), Chicken.class));
            if (rnd.nextBoolean()) {
                chicken.setBaby();
            }
            chickens.add(chicken);
        }
        Compat.sound(player.getLocation(), "entity.chicken.ambient", 1f, 1f);

        long lifetime = Math.max(1, opt("lifetime-seconds", 30)) * 20L;
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (Chicken c : chickens) {
                if (c.isValid()) c.remove();
            }
        }, lifetime);
    }
}
