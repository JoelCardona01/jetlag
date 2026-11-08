package dev.jetlag.events;

import dev.jetlag.JetLagPlugin;
import dev.jetlag.util.Compat;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Your luggage got mixed up: the hotbar is shuffled. */
public final class HotbarShuffleEvent extends AbstractEvent {

    public HotbarShuffleEvent(JetLagPlugin plugin) {
        super("hotbar_shuffle", plugin);
    }

    @Override
    public void run(Player player) {
        PlayerInventory inv = player.getInventory();
        List<ItemStack> items = new ArrayList<>();
        for (int slot = 0; slot < 9; slot++) {
            items.add(inv.getItem(slot));
        }
        Collections.shuffle(items);
        for (int slot = 0; slot < 9; slot++) {
            inv.setItem(slot, items.get(slot));
        }
        Compat.sound(player.getLocation(), "entity.item.pickup", 1f, 0.6f);
    }
}
