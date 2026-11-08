package dev.jetlag.events;

import dev.jetlag.JetLagPlugin;
import dev.jetlag.util.Compat;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** The GPS failed: you land next to a random other player instead. */
public final class WrongDestinationEvent extends AbstractEvent {

    /** Remembers who each victim was sent to, between redirect() and run(). */
    private final Map<UUID, UUID> sentTo = new HashMap<>();

    public WrongDestinationEvent(JetLagPlugin plugin) {
        super("wrong_destination", plugin);
    }

    @Override
    public boolean canRun(Player player, Location destination) {
        return !others(player).isEmpty();
    }

    @Override
    public Location redirect(Player player, Location destination) {
        List<Player> others = others(player);
        if (others.isEmpty()) return null;
        Player target = others.get(ThreadLocalRandom.current().nextInt(others.size()));
        sentTo.put(player.getUniqueId(), target.getUniqueId());
        return target.getLocation();
    }

    @Override
    public void run(Player player) {
        UUID targetId = sentTo.remove(player.getUniqueId());
        Player target = targetId == null ? null : plugin.getServer().getPlayer(targetId);
        if (target == null) return;
        plugin.messages().send(player, "wrong-destination.victim", "target", target.getName());
        plugin.messages().send(target, "wrong-destination.target", "player", player.getName());
        Compat.sound(target.getLocation(), "entity.villager.no", 1f, 1f);
    }

    private static List<Player> others(Player player) {
        List<Player> list = new ArrayList<>();
        for (Player p : player.getServer().getOnlinePlayers()) {
            if (!p.equals(player)) list.add(p);
        }
        return list;
    }
}
