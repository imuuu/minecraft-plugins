package me.imu.imuschallenges.SubCommands;

import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Other.Methods;
import me.imu.imuschallenges.ImusChallenges;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SubReloadCmd implements CommandInterface
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        ImusChallenges.getInstance().reloadSettings();
        sender.sendMessage(Methods.msgC("&9ImusChallenges config reloaded"));
        return true;
    }

    @Override
    public void FailedMsg(CommandSender arg0, String arg1) {

    }
}
