package me.imu.imuschallenges.Managers;

import me.imu.imuschallenges.CONSTANTS;
import me.imu.imuschallenges.ImusChallenges;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Tells the server when someone takes the lead in lifetime challenge points, to get others competing.
 * {@link ManagerPlayerPoints} decides who leads; this class decides whether to announce it.
 */
public class ManagerChallengeLeader
{
    private static ManagerChallengeLeader _instance;

    public static ManagerChallengeLeader getInstance()
    {
        return _instance;
    }

    private long _lastBroadcast = 0;

    public ManagerChallengeLeader()
    {
        _instance = this;
    }

    /**
     * Main thread. Called when a player's lifetime challenge points pass the previous leader's.
     *
     * @param previousLeader null when nobody had points before
     */
    public void onNewLeader(String leader, int points, String previousLeader)
    {
        FileConfiguration config = ImusChallenges.getInstance().getConfig();
        if (!config.getBoolean("leader-broadcast.enabled"))
            return;
        if (points < config.getInt("leader-broadcast.min-points"))
            return;

        long cooldownMillis = config.getLong("leader-broadcast.cooldown-minutes") * 60_000L;
        long now = System.currentTimeMillis();
        if (now - _lastBroadcast < cooldownMillis)
            return;
        _lastBroadcast = now;

        String message = "&6&l[Challenges] &e" + leader + " &9has taken the lead with &a" + points + " &9challenge points";
        if (previousLeader != null && !previousLeader.equals(leader))
            message += ", passing &e" + previousLeader;
        message += "&9! &7Collect new materials and complete advancements to catch up.";

        var component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        for (Player player : Bukkit.getOnlinePlayers())
        {
            if (player.hasPermission(CONSTANTS.PERM_BROADCAST_LEADER))
                player.sendMessage(component);
        }
        Bukkit.getConsoleSender().sendMessage(component);
    }
}
