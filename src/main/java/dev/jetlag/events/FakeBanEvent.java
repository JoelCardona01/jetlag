package dev.jetlag.events;

import dev.jetlag.JetLagPlugin;
import dev.jetlag.util.Compat;
import org.bukkit.entity.Player;

/**
 * Shows a scary "BANNED" title, then reveals it was a joke. Nobody is kicked.
 * The chat announcement is delayed so it doesn't spoil the prank.
 */
public final class FakeBanEvent extends AbstractEvent {

    private static final long REVEAL_TICKS = 80L;

    public FakeBanEvent(JetLagPlugin plugin) {
        super("fake_ban", plugin);
    }

    @Override
    public long announceDelayTicks() {
        return REVEAL_TICKS;
    }

    @Override
    public void run(Player player) {
        player.sendTitle(plugin.messages().get("titles.fake-ban.title"),
                plugin.messages().get("titles.fake-ban.subtitle"), 5, 70, 10);
        Compat.sound(player.getLocation(), "block.anvil.land", 1f, 0.5f);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                plugin.messages().send(player, "fake-ban-reveal");
            }
        }, REVEAL_TICKS);
    }
}
