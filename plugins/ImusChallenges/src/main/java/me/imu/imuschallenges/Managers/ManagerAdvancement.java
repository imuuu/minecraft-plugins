package me.imu.imuschallenges.Managers;

import me.imu.imuschallenges.Datas.DataPlayerAdvancements;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementDisplay;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

import java.util.Iterator;

public class ManagerAdvancement
{
    private static ManagerAdvancement _instance;

    public static ManagerAdvancement getInstance() {return _instance;}

    public ManagerAdvancement()
    {
        _instance = this;
    }
    public DataPlayerAdvancements checkPlayerAchievements(Player player)
    {
        DataPlayerAdvancements data = new DataPlayerAdvancements();
        data.setPlayer(player);

        for (Iterator<Advancement> it = Bukkit.getServer().advancementIterator(); it.hasNext(); )
        {
            Advancement advancement = it.next();
            AdvancementProgress progress = player.getAdvancementProgress(advancement);

            if (progress.isDone())
            {
                AdvancementDisplay display = advancement.getDisplay();
                if (display != null)
                {
                    String type = display.getType().toString();

                    // Increment respective counters
                    switch (type)
                    {
                        case "CHALLENGE":
                            data.incrementCompletedChallenges();
                            break;
                        case "GOAL":
                            data.incrementCompletedGoals();
                            break;
                        case "TASK":
                            data.incrementCompletedTasks();
                            break;
                    }

                    //System.out.println("Player " + player.getName() + " has completed " + advancement.getKey() + " (" + type + "), earning " + points + " points");
                }
            }
        }
        return data;
    }

    public DataPlayerAdvancements getPoints(Player player, Advancement advancement)
    {
        DataPlayerAdvancements data = checkPlayerAchievements(player);
        AdvancementDisplay display = advancement.getDisplay();
        if (display != null)
        {
            String type = display.getType().toString();
            switch (type)
            {
                case "CHALLENGE":
                    data.incrementCompletedChallenges();
                    break;
                case "GOAL":
                    data.incrementCompletedGoals();
                    break;
                case "TASK":
                    data.incrementCompletedTasks();
                    break;
            }
        }
        return data;
    }


}
