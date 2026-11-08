package dev.jetlag.util;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Small helpers that hide API differences between Minecraft versions.
 *
 * <p>Rule of thumb used across the plugin: anything Mojang has renamed over the
 * years (sounds, potion effects, particles) is looked up by <em>name</em> at
 * runtime instead of referencing a Java constant, so the same jar runs on
 * 1.16.5 and on the newest releases.</p>
 */
public final class Compat {

    private Compat() {
    }

    /**
     * Finds a potion effect trying several names, e.g. ("NAUSEA", "CONFUSION")
     * because the effect was renamed in 1.20.5. Returns null if none exists.
     */
    public static PotionEffectType potion(String... names) {
        for (String name : names) {
            try {
                PotionEffectType type = PotionEffectType.getByName(name);
                if (type != null) return type;
            } catch (Throwable ignored) {
                // try the next name
            }
        }
        return null;
    }

    /** Applies a potion effect if it exists on this server version. */
    public static void effect(LivingEntity entity, int seconds, int amplifier, String... names) {
        PotionEffectType type = potion(names);
        if (type != null) {
            entity.addPotionEffect(new PotionEffect(type, seconds * 20, amplifier));
        }
    }

    /** Launches a harmless firework burst (damage is cancelled by ProtectionListener). */
    public static void celebrate(Location loc, Color... colors) {
        World world = loc.getWorld();
        if (world == null) return;
        Firework fw = Tags.mark(world.spawn(loc, Firework.class));
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL_LARGE)
                .withColor(colors)
                .withFlicker()
                .build());
        meta.setPower(0);
        fw.setFireworkMeta(meta);
        fw.detonate();
    }

    /**
     * Plays a sound by its namespaced key (e.g. "entity.creeper.primed").
     * String keys have been stable since 1.9, unlike the Sound enum constants.
     */
    public static void sound(Location loc, String key, float volume, float pitch) {
        World world = loc.getWorld();
        if (world == null) return;
        try {
            world.playSound(loc, key, volume, pitch);
        } catch (Throwable ignored) {
            // A missing sound should never break an event.
        }
    }

    /**
     * Picks a random spot around {@code center} at the given distance where a
     * mob can stand (air at feet and head level). Falls back to the center.
     */
    public static Location spotAround(Location center, double distance) {
        World world = center.getWorld();
        if (world == null) return center.clone();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        for (int attempt = 0; attempt < 8; attempt++) {
            double angle = rnd.nextDouble(Math.PI * 2);
            double x = center.getX() + Math.cos(angle) * distance;
            double z = center.getZ() + Math.sin(angle) * distance;
            int baseY = center.getBlockY();

            for (int dy = 2; dy >= -3; dy--) {
                Block feet = world.getBlockAt((int) Math.floor(x), baseY + dy, (int) Math.floor(z));
                Block head = feet.getRelative(0, 1, 0);
                Block ground = feet.getRelative(0, -1, 0);
                if (feet.isPassable() && head.isPassable() && ground.getType().isSolid()) {
                    return new Location(world, x, feet.getY(), z);
                }
            }
        }
        return center.clone();
    }
}
