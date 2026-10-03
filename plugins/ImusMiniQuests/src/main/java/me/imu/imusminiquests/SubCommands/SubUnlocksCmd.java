package me.imu.imusminiquests.SubCommands;

import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Other.Metods;
import me.imu.imusminiquests.CONSTANTS;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Managers.ManagerUnlocks;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * /imq unlocks                         which rewards the server has unlocked
 * /imq unlocks &lt;name&gt; unlock|lock      change one by hand (imq.unlocks.admin)
 */
public class SubUnlocksCmd implements CommandInterface
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        ManagerUnlocks unlocks = ImusMiniQuests.getInstance().getUnlocks();

        if (args.length > 2)
        {
            if (!sender.hasPermission(CONSTANTS.PERM_UNLOCKS_ADMIN))
            {
                sender.sendMessage(ChatColor.RED + "Only admins can change unlocks");
                return true;
            }

            ManagerUnlocks.Unlock unlock = unlocks.getUnlock(args[1]);
            if (unlock == null)
            {
                sender.sendMessage(ChatColor.RED + "No unlock called " + args[1]);
                return true;
            }

            boolean unlocked = args[2].equalsIgnoreCase("unlock");
            if (!unlocked && !args[2].equalsIgnoreCase("lock"))
            {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " unlocks <name> unlock|lock");
                return true;
            }

            unlocks.setUnlocked(unlock.name(), unlocked, null);
            sender.sendMessage(Metods.msgC("&9" + unlock.name() + " is now " + (unlocked ? "&aunlocked" : "&clocked")));
            return true;
        }

        sender.sendMessage(Metods.msgC("&6Quest reward unlocks"));
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        for (ManagerUnlocks.Unlock unlock : unlocks.getUnlocks())
        {
            String state;
            if (!unlocks.isUnlocked(unlock.name()))
            {
                state = "&clocked &8- the first player to find it unlocks it";
            }
            else
            {
                String by = unlocks.getUnlockedBy(unlock.name());
                state = "&aunlocked &8" + (by != null ? "by " + by + " " : "by an admin ")
                        + "on " + date.format(new Date(unlocks.getUnlockedAt(unlock.name())));
            }
            sender.sendMessage(Metods.msgC("&e" + unlock.name() + " &7(" + unlock.display() + "&7): " + state));
        }
        return true;
    }

    @Override
    public void FailedMsg(CommandSender sender, String s)
    {

    }
}
