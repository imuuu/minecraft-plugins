package me.imu.imuschallenges.Datas;

import imu.iAPI.Utilities.ImusUtilities;
import me.imu.imuschallenges.CONSTANTS;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DataPlayerAdvancements {
    private Player player;
    private int completedChallenges = 0;
    private int completedTasks = 0;
    private int completedGoals = 0;

    // Getters and possibly setters
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getCompletedChallenges() {
        return completedChallenges;
    }

    public void incrementCompletedChallenges() {
        this.completedChallenges++;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public void incrementCompletedTasks() {
        this.completedTasks++;
    }

    public int getCompletedGoals() {
        return completedGoals;
    }

    public void incrementCompletedGoals() {
        this.completedGoals++;
    }

    public int getPoints()
    {
        int points = 0;
        points += getCompletedChallenges() * CONSTANTS.POINTS_PER_ADVANCEMENT_CHALLENGE;
        points += getCompletedGoals() * CONSTANTS.POINTS_PER_GOAL_CHALLENGE;
        points += getCompletedTasks() * CONSTANTS.POINTS_PER_TASK_CHALLENGE;
        return points;
    }

    public void sendMessage(Player player)
    {
        ImusUtilities.SendCenteredMessage(player, "&b========= &5Achievement &6Summary &b==========");
        player.sendMessage(" ");
        ImusUtilities.SendCenteredMessage(player, "&6&lChallenges");
        ImusUtilities.SendCenteredMessage(player, "&6"+getCompletedChallenges());
        player.sendMessage(" ");
        ImusUtilities.SendCenteredMessage(player, "&5&lGoals");
        ImusUtilities.SendCenteredMessage(player, "&5"+getCompletedGoals());
        player.sendMessage(" ");
        ImusUtilities.SendCenteredMessage(player, "&9&lTasks");
        ImusUtilities.SendCenteredMessage(player, "&9"+getCompletedTasks());
        player.sendMessage(" ");
        ImusUtilities.SendCenteredMessage(player, "&b========= &5Achievement &6Summary &b==========");
    }

    @Override
    public String toString() {
        return "DataPlayerAdvancements{" +
                "player=" + (player != null ? player.getName() : "null") +
                ", completedChallenges=" + completedChallenges +
                ", completedTasks=" + completedTasks +
                ", completedGoals=" + completedGoals +
                '}';
    }
}
