package me.imu.imuschallenges.Managers;

import me.imu.imuschallenges.ImusChallenges;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;
import java.util.Locale;

/**
 * Shows each player's lifetime challenge points on the server scoreboard: after the name in the tab list,
 * under the name tag, or in the sidebar, as set in config.yml.
 */
public class ManagerPointsScoreboard implements Listener
{
    public static final List<String> DISPLAY_OPTIONS = List.of("PLAYER_LIST", "BELOW_NAME", "SIDEBAR", "NONE");
    private static final String OBJECTIVE_NAME = "ic_points";

    private static ManagerPointsScoreboard _instance;

    public static ManagerPointsScoreboard getInstance()
    {
        return _instance;
    }

    public ManagerPointsScoreboard()
    {
        _instance = this;
        applyConfig();
        for (Player player : Bukkit.getOnlinePlayers())
            loadPoints(player);
    }

    private Objective getObjective()
    {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
        if (objective == null)
            objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, Criteria.DUMMY, Component.text("Challenge Points", NamedTextColor.GOLD));
        return objective;
    }

    /**
     * Moves the points to the display slot in config.yml. Main thread.
     */
    public void applyConfig()
    {
        String display = ImusChallenges.getInstance().getConfig().getString("scoreboard.display", "PLAYER_LIST").toUpperCase(Locale.ROOT);
        Objective objective = getObjective();
        if (display.equals("NONE"))
        {
            objective.setDisplaySlot(null);
            return;
        }

        DisplaySlot slot = switch (display)
        {
            case "BELOW_NAME" -> DisplaySlot.BELOW_NAME;
            case "SIDEBAR" -> DisplaySlot.SIDEBAR;
            case "PLAYER_LIST" -> DisplaySlot.PLAYER_LIST;
            default ->
            {
                ImusChallenges.getInstance().getLogger().warning("Unknown scoreboard.display '" + display
                        + "', use one of " + DISPLAY_OPTIONS);
                yield DisplaySlot.PLAYER_LIST;
            }
        };
        objective.setDisplaySlot(slot);
    }

    /**
     * Main thread.
     */
    public void setPoints(String playerName, int lifetimePoints)
    {
        if (playerName == null)
            return;
        getObjective().getScore(playerName).setScore(lifetimePoints);
    }

    /**
     * Clears every score, then shows zero for the players online. Main thread.
     */
    public void resetAll()
    {
        Objective objective = getObjective();
        Scoreboard scoreboard = objective.getScoreboard();
        if (scoreboard != null)
        {
            for (String entry : scoreboard.getEntries())
                objective.getScore(entry).resetScore();
        }
        for (Player player : Bukkit.getOnlinePlayers())
            setPoints(player.getName(), 0);
    }

    private void loadPoints(Player player)
    {
        ManagerPlayerPoints.getInstance().getLifetimePointsAsync(player, points ->
        {
            if (player.isOnline())
                setPoints(player.getName(), points);
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        loadPoints(event.getPlayer());
    }
}
