package me.imu.imusminiquests.SubCommands;

import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Other.Metods;
import me.imu.imusminiquests.ImusMiniQuests;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SubReloadCmd implements CommandInterface
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        ImusMiniQuests plugin = ImusMiniQuests.getInstance();
        plugin.reloadSettings();
        sender.sendMessage(Metods.msgC("&9ImusMiniQuests reloaded, &e" + plugin.getQuests().getQuests().size() + "&9 quests"));
        return true;
    }

    @Override
    public void FailedMsg(CommandSender arg0, String arg1)
    {

    }
}
