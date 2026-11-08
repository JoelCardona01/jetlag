package dev.jetlag.events;

import dev.jetlag.JetLagPlugin;
import dev.jetlag.util.Compat;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * You arrive way above your destination with a water bucket in hand.
 * MLG or die. Disabled in the Nether, where water evaporates.
 */
public final class SkyDropEvent extends AbstractEvent {

    public SkyDropEvent(JetLagPlugin plugin) {
        super("sky_drop", plugin);
    }

    @Override
    public boolean canRun(Player player, Location destination) {
        World world = destination.getWorld();
        return world != null && world.getEnvironment() != World.Environment.NETHER;
    }

    @Override
    public Location redirect(Player player, Location destination) {
        Location high = destination.clone().add(0, opt("height", 60), 0);
        double top = destination.getWorld().getMaxHeight() - 2;
        if (high.getY() > top) {
            high.setY(top);
        }
        return high;
    }

    @Override
    public void run(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack held = inv.getItemInMainHand();
        inv.setItemInMainHand(new ItemStack(Material.WATER_BUCKET));
        if (held != null && held.getType() != Material.AIR) {
            for (ItemStack left : inv.addItem(held).values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), left);
            }
        }
        player.sendTitle(plugin.messages().get("titles.sky-drop.title"),
                plugin.messages().get("titles.sky-drop.subtitle"), 5, 50, 10);
        Compat.sound(player.getLocation(), "entity.elytra.flying", 1f, 1f);
    }
}
