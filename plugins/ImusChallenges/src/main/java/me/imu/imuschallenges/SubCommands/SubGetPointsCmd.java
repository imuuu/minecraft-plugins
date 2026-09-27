package me.imu.imuschallenges.SubCommands;

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
            targetPlayer = (Player) sender;
        }
        Player finalTargetPlayer = targetPlayer;
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    List<TablePlayerPoints> pointsList = ManagerPlayerPoints.getInstance().getPoints(finalTargetPlayer);
                    sendPointsMessage((Player)sender, pointsList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(ImusChallenges.getInstance());
        return true;
    }

    private void sendPointsMessage(Player player, List<TablePlayerPoints> pointsList)
    {
        if(pointsList == null || pointsList.isEmpty())
        {
            ImusUtilities.SendCenteredMessage(player, "&cNo points found");

            return;
        }
        ImusUtilities.SendCenteredMessage(player, "&b========= &5Points &6Summary &b==========");
        player.sendMessage(" ");
        for (TablePlayerPoints points : pointsList) {
            String pointType = points.getPoint_type().getPointTypeName();
            String message = ChatColor.GOLD + pointType + ": " + ChatColor.AQUA + points.getPoints() +
                    " (Lifetime: " + points.getLifetimePoints() + ")";
            ImusUtilities.SendCenteredMessage(player, message);
        }
        player.sendMessage(" ");
        ImusUtilities.SendCenteredMessage(player, "&b========= &5Points &6Summary &b==========");
    }

    @Override
    public void FailedMsg(CommandSender commandSender, String s) {
        // Implement a message for a failed command execution
    }
}
