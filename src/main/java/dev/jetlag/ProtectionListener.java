package dev.jetlag;

import dev.jetlag.util.Tags;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * Keeps Jet Lag "funny, not griefy": entities spawned by the plugin are tagged,
 * and this listener stops them from destroying builds.
 */
public final class ProtectionListener implements Listener {

    private final JetLagPlugin plugin;

    public ProtectionListener(JetLagPlugin plugin) {
        this.plugin = plugin;
    }

    /** Creepers and TNT from Jet Lag hurt players but leave blocks intact. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {
        if (!plugin.settings().blockDamage && Tags.isOurs(event.getEntity())) {
            event.blockList().clear();
        }
    }

    /** Falling anvils from Jet Lag vanish on landing instead of becoming blocks. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLand(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof FallingBlock && Tags.isOurs(entity)) {
            event.setCancelled(true);
            entity.remove();
        }
    }

    /** "Fake creeper" fireworks are just for show and never deal damage. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFireworkDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework && Tags.isOurs(event.getDamager())) {
            event.setCancelled(true);
        }
    }
}
