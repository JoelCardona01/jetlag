package dev.jetlag.events;

import dev.jetlag.JetLagPlugin;
import dev.jetlag.util.Compat;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** The rare lucky outcome: an upgrade to first class (and some diamonds). */
public final class FirstClassEvent extends AbstractEvent {

    public FirstClassEvent(JetLagPlugin plugin) {
        super("first_class", plugin);
    }

    @Override
    public void run(Player player) {
        int diamonds = Math.max(1, opt("diamonds", 2));
        for (ItemStack left : player.getInventory().addItem(new ItemStack(Material.DIAMOND, diamonds)).values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), left);
        }
        Compat.celebrate(player.getLocation().add(0, 2, 0), Color.AQUA, Color.YELLOW);
        Compat.sound(player.getLocation(), "ui.toast.challenge_complete", 1f, 1f);
    }
}
