package me.imu.imuschallenges.SubCommands;

import imu.iAPI.Interfaces.CommandInterface;
import me.imu.imuschallenges.ImusChallenges;
import me.imu.imuschallenges.Managers.ManagerPlayerPoints;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /ic set points &lt;amount&gt; [player]: sets the challenge points a player can spend.
 * /ic set lifetime &lt;amount&gt; [player]: sets the lifetime points shown on the scoreboard.
 */
public class SubSetPointsCmd implements CommandInterface
{
    private final boolean _lifetime;

    public SubSetPointsCmd(boolean lifetime)
    {
        _lifetime = lifetime;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        String usage = "Usage: /" + label + " set " + (_lifetime ? "lifetime" : "points") + " <amount> [player]";
        if (args.length < 3)
        {
            sender.sendMessage(ChatColor.RED + usage);
            return true;
        }

        int amount;
        try
        {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e)
        {
            sender.sendMessage(ChatColor.RED + "'" + args[2] + "' is not a whole number");
            return true;
        }
        if (amount < 0)
        {
            sender.sendMessage(ChatColor.RED + "Points can't be negative");
            return true;
        }

        String target;
        if (args.length >= 4)
            target = args[3];
        else if (sender instanceof Player player)
            target = player.getName();
        else
        {
            sender.sendMessage(ChatColor.RED + usage);
            return true;
        }

        ManagerPlayerPoints.getInstance().setPointsAsync(target, amount, _lifetime, playerName ->
        {
            if (playerName == null)
            {
                sender.sendMessage(ChatColor.RED + "No player called " + target + " has joined the server");
                return;
            }

            String what = _lifetime ? "lifetime points" : "challenge points";
            ImusChallenges.getInstance().getLogger().info(sender.getName() + " set the " + what + " of " + playerName + " to " + amount);
            sender.sendMessage(ChatColor.GREEN + "Set the " + what + " of " + playerName + " to " + amount);
        });
        return true;
    }

    @Override
    public void FailedMsg(CommandSender commandSender, String s)
    {
    }
}
