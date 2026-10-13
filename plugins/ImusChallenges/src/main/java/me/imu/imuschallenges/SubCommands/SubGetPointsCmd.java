package me.imu.imuschallenges.SubCommands;

import imu.iAPI.Utilities.TextUtils;

import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Utilities.ImusUtilities;
import me.imu.imuschallenges.Database.Tables.TablePlayerPoints;
import me.imu.imuschallenges.ImusChallenges;
import me.imu.imuschallenges.Managers.ManagerPlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class SubGetPointsCmd implements CommandInterface
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        Player targetPlayer = null;

        if (args.length > 2) {

            // Determine the target player
            String playerName =  args[args.length - 1];
            targetPlayer = Bukkit.getPlayer(playerName);
        }

        if (targetPlayer == null) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Usage from the console: /" + s + " view points <player>");
                return true;
            }
            targetPlayer = (Player) sender;
        }
        Player finalTargetPlayer = targetPlayer;
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    List<TablePlayerPoints> pointsList = ManagerPlayerPoints.getInstance().getPoints(finalTargetPlayer);
                    sendPointsMessage(sender, pointsList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(ImusChallenges.getInstance());
        return true;
    }

    private void sendPointsMessage(CommandSender player, List<TablePlayerPoints> pointsList)
    {
        if(pointsList == null || pointsList.isEmpty())
        {
            SendLine(player, "&cNo points found");

            return;
        }
        SendLine(player, "&b========= &5Points &6Summary &b==========");
        player.sendMessage(" ");
        for (TablePlayerPoints points : pointsList) {
            String pointType = points.getPoint_type().getPointTypeName();
            String message = ChatColor.GOLD + pointType + ": " + ChatColor.AQUA + points.getPoints() +
                    " (Lifetime: " + points.getLifetimePoints() + ")";
            SendLine(player, message);
        }
        player.sendMessage(" ");
        SendLine(player, "&b========= &5Points &6Summary &b==========");
    }

    // Centred for players; the console gets the text as it is
    private static void SendLine(CommandSender to, String message)
    {
        if (to instanceof Player player)
            ImusUtilities.SendCenteredMessage(player, message);
        else
            to.sendMessage(TextUtils.Color(message));
    }

    @Override
    public void FailedMsg(CommandSender commandSender, String s) {
        // Implement a message for a failed command execution
    }
}
