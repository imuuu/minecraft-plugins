package me.imu.imuschallenges.SubCommands;

import imu.iAPI.Interfaces.CommandInterface;
import me.imu.imuschallenges.Database.Tables.TablePointType;
import me.imu.imuschallenges.Managers.ManagerPlayerPoints;
import me.imu.imuschallenges.Managers.ManagerPointType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SubAddPointsCmd implements CommandInterface
{
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args)
    {
        if (args.length < 3)
        {
            commandSender.sendMessage(ChatColor.RED + "Usage: /" + label + " add points <pointType> <amount> <target>");
            return true;
        }
        String pointTypeName = args[2].toUpperCase();
        try
        {
            TablePointType tPointType = ManagerPointType.getInstance().getPointTypeByName(pointTypeName);
            if (tPointType == null)
            {
                commandSender.sendMessage(ChatColor.RED + "Invalid point type: " + pointTypeName);
                return true;
            }
            pointTypeName = tPointType.getPointTypeName();

        } catch (IllegalArgumentException e)
        {
            commandSender.sendMessage(ChatColor.RED + "Invalid point type: " + pointTypeName);
            return false;
        }

        int points;
        try
        {
            points = Integer.parseInt(args[3]);
        } catch (NumberFormatException e)
        {
            commandSender.sendMessage(ChatColor.RED + "Invalid number format for points.");
            return false;
        }

        // Get target player
        Player targetPlayer = Bukkit.getPlayer(args[4]);
        if (targetPlayer == null)
        {
            commandSender.sendMessage(ChatColor.RED + "Player not found: " + args[4]);
            return true;
        }

        // Add points
        ManagerPlayerPoints.getInstance().addPointsAsync(targetPlayer, pointTypeName, points);
        commandSender.sendMessage(ChatColor.GREEN + "Added " + points + " points to " + targetPlayer.getName());

        return true;
    }

    @Override
    public void FailedMsg(CommandSender commandSender, String s)
    {
        commandSender.sendMessage(ChatColor.RED + "Usage: /" + s + " add points <pointType> <amount> <target>");
    }
}
