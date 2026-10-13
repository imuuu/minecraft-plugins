package me.imu.imuschallenges.Inventories;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * What the material collection menu needs of the server. {@link PaperCollectMenuGame} is the real one; tests give
 * a stand-in. The lists come from the database, so they arrive later, on the main thread.
 */
public interface CollectMenuGame
{
    record PlayerCount(UUID id, String name, int count) {}

    /**
     * A material a player was first to collect, and when (epoch milliseconds).
     */
    record Collected(String material, long time) {}

    /**
     * The online players and how many materials each was first to collect.
     */
    void playerCounts(Consumer<List<PlayerCount>> done);

    /**
     * The materials the player was first to collect, newest first; null when the player is offline.
     */
    void collected(UUID player, Consumer<List<Collected>> done);

    /**
     * Materials nobody has collected yet.
     */
    void uncollected(Consumer<List<String>> done);

    void removeCollected(UUID player, String material);

    /**
     * Leaves the material out of the challenge for good.
     */
    void exclude(String material, Runnable done);
}
