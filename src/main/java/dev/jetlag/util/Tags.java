package dev.jetlag.util;

import org.bukkit.entity.Entity;

/**
 * Marks entities spawned by Jet Lag with a scoreboard tag. Scoreboard tags
 * exist since Minecraft 1.10 and survive restarts, so this works everywhere.
 */
public final class Tags {

    public static final String MARK = "jetlag";

    private Tags() {
    }

    public static <T extends Entity> T mark(T entity) {
        entity.addScoreboardTag(MARK);
        return entity;
    }

    public static boolean isOurs(Entity entity) {
        return entity != null && entity.getScoreboardTags().contains(MARK);
    }
}
