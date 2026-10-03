package me.imu.imuschallenges.SubCommands;

import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Other.Metods;
import me.imu.imuschallenges.Managers.ManagerChallengeShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SubReloadCmd implements CommandInterface
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        ManagerChallengeShop.getInstance().reload();
        sender.sendMessage(Metods.msgC("&9ImusChallenges config reloaded"));
        return true;
    }

    @Override
    public void FailedMsg(CommandSender arg0, String arg1) {

    }
}
