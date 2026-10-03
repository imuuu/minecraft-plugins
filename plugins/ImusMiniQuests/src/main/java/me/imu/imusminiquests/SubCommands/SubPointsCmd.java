package me.imu.imusminiquests.SubCommands;

import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Other.Metods;
import me.imu.imusminiquests.CONSTANTS;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Managers.ManagerQuestPoints;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /imq points                    your quest points and luck
 * /imq points &lt;player&gt;           someone else's (imq.points.others)
 * /imq points &lt;player&gt; set &lt;n&gt;   change them (imq.points.others)
 */
public class SubPointsCmd implements CommandInterface
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        ManagerQuestPoints questPoints = ImusMiniQuests.getInstance().getQuestPoints();
        Player target = (Player) sender;

        if (args.length > 1)
        {
            if (!sender.hasPermission(CONSTANTS.PERM_POINTS_OTHERS))
            {
                sender.sendMessage(ChatColor.RED + "You can only see your own quest points");
                return true;
            }
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null)
            {
                sender.sendMessage(ChatColor.RED + "Player " + args[1] + " is not online");
                return true;
            }
        }

        if (args.length > 3 && args[2].equalsIgnoreCase("set"))
        {
            try
            {
                questPoints.setPoints(target, Integer.parseInt(args[3]));
            }
            catch (NumberFormatException e)
            {
                sender.sendMessage(ChatColor.RED + args[3] + " is not a number");
                return true;
            }
        }

        int points = questPoints.getPoints(target);
        String who = target == sender ? "Your" : target.getName() + "'s";
        sender.sendMessage(Metods.msgC("&6" + who + " quest points: &e" + points
                + " &8(luck " + ManagerQuestPoints.formatLuck(questPoints.getLuck(points))
                + ", most " + ManagerQuestPoints.formatLuck(questPoints.getMaxLuck()) + ")"));
        sender.sendMessage(Metods.msgC("&7Every quest you open gives a point. Luck makes rare rewards,"));
        sender.sendMessage(Metods.msgC("&7bigger amounts and better tool tiers come up more often."));
        return true;
    }

    @Override
    public void FailedMsg(CommandSender sender, String s)
    {

    }
}
