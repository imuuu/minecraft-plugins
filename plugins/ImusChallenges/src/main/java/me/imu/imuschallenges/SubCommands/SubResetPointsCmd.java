package me.imu.imuschallenges.SubCommands;

import imu.iAPI.Interfaces.CommandInterface;
import me.imu.imuschallenges.ImusChallenges;
import me.imu.imuschallenges.Managers.ManagerPlayerPoints;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * /ic reset points &lt;player|all&gt; [confirm]: sets challenge points and lifetime points back to zero.
 */
public class SubResetPointsCmd implements CommandInterface
{
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length < 3)
        {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " reset points <player|all>");
            return true;
        }

        String target = args[2];
        boolean everyone = target.equalsIgnoreCase("all");
        if (everyone && (args.length < 4 || !args[3].equalsIgnoreCase("confirm")))
        {
            sender.sendMessage(ChatColor.RED + "This sets everyone's challenge points and lifetime points to 0.");
            sender.sendMessage(ChatColor.RED + "Run " + ChatColor.YELLOW + "/" + label + " reset points all confirm" + ChatColor.RED + " to do it.");
            return true;
        }

        ManagerPlayerPoints.getInstance().resetPointsAsync(everyone ? null : target, (rows, playerName) ->
        {
            if (!everyone && playerName == null)
            {
                sender.sendMessage(ChatColor.RED + "No player called " + target + " has ever had points");
                return;
            }

            String who = everyone ? "everyone (" + rows + " players)" : playerName;
            ImusChallenges.getInstance().getLogger().info(sender.getName() + " reset the challenge points of " + who);
            sender.sendMessage(ChatColor.GREEN + "Reset the challenge points of " + who);
        });
        return true;
    }

    @Override
    public void FailedMsg(CommandSender commandSender, String s)
    {
        commandSender.sendMessage(ChatColor.RED + "Usage: /" + s + " reset points <player|all>");
    }
}
