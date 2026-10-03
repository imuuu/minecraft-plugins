package me.imu.imusminiquests.Hooks;

import me.imu.imuschallenges.Enums.POINT_TYPE;
import me.imu.imuschallenges.Managers.ManagerPlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Gives ImusChallenges challenge points for completed quests. ImusChallenges is only a soft
 * dependency, so check {@link #isEnabled()} first; the other method loads its classes.
 */
public final class ImusChallengesHook
{
    private ImusChallengesHook() {}

    public static boolean isEnabled()
    {
        return Bukkit.getPluginManager().isPluginEnabled("ImusChallenges") && ManagerPlayerPointsReady.check();
    }

    /**
     * Adds challenge points, lifetime points included, the same way ImusChallenges' own
     * challenges do. Written to its database in the background.
     */
    public static void addChallengePoints(Player player, int amount)
    {
        ManagerPlayerPoints.getInstance().addPointsAsync(player, POINT_TYPE.CHALLENGE_POINT, amount);
    }

    // Separate class so isEnabled() only touches ImusChallenges' classes once the plugin is known
    // to be there
    private static final class ManagerPlayerPointsReady
    {
        static boolean check()
        {
            return ManagerPlayerPoints.getInstance() != null;
        }
    }
}
