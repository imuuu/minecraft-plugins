package me.imu.imusminiquests.Managers;

import me.imu.imusminiquests.CONSTANTS;
import me.imu.imusminiquests.ImusMiniQuests;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

/**
 * Quest points: one for every quest a player claims. They are kept in the player's own data
 * (saved with the player file), and they give luck that makes the player's rewards better:
 * rare pool entries, high amounts and better tool tiers come up more often.
 */
public class ManagerQuestPoints
{
    private final ImusMiniQuests _plugin;
    private final NamespacedKey _key;

    public ManagerQuestPoints(ImusMiniQuests plugin)
    {
        _plugin = plugin;
        _key = new NamespacedKey(plugin, CONSTANTS.KEY_QUEST_POINTS);
    }

    public boolean isEnabled()
    {
        return _plugin.getConfig().getBoolean("quest-points.enabled", true);
    }

    public int getPoints(Player player)
    {
        Integer points = player.getPersistentDataContainer().get(_key, PersistentDataType.INTEGER);
        return points == null ? 0 : points;
    }

    public void setPoints(Player player, int points)
    {
        player.getPersistentDataContainer().set(_key, PersistentDataType.INTEGER, Math.max(0, points));
    }

    /** Adds points and returns the new total. */
    public int addPoints(Player player, int amount)
    {
        int points = getPoints(player) + amount;
        setPoints(player, points);
        return points;
    }

    /**
     * Luck from 0 to quest-points.max-luck: quest-points.luck-per-point for every point.
     * 0 when quest points are turned off.
     */
    public double getLuck(int points)
    {
        if (!isEnabled()) return 0;

        FileConfiguration config = _plugin.getConfig();
        double perPoint = Math.max(0, config.getDouble("quest-points.luck-per-point", 0.01));
        double max = Math.max(0, Math.min(1, config.getDouble("quest-points.max-luck", 0.5)));
        return Math.min(max, points * perPoint);
    }

    public double getLuck(Player player)
    {
        return getLuck(getPoints(player));
    }

    public double getMaxLuck()
    {
        return Math.max(0, Math.min(1, _plugin.getConfig().getDouble("quest-points.max-luck", 0.5)));
    }

    /** Luck as a whole percent for messages, e.g. 0.12 -> "12%". */
    public static String formatLuck(double luck)
    {
        return Math.round(luck * 100) + "%";
    }
}
