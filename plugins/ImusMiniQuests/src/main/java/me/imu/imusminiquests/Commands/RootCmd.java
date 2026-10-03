package me.imu.imusminiquests.Commands;

import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Other.Metods;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * /imq without arguments: lists the sub-commands. With arguments it returns false so the
 * CommandHandler runs the matching sub-command.
 */
public class RootCmd implements CommandInterface
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        if (args.length > 0)
            return false;

        sender.sendMessage(Metods.msgC("&6ImusMiniQuests"));
        sender.sendMessage(Metods.msgC("&e/" + commandLabel + " give <player> <quest> [amount] &7- give quest panels"));
        sender.sendMessage(Metods.msgC("&e/" + commandLabel + " list &7- list the quests"));
        sender.sendMessage(Metods.msgC("&e/" + commandLabel + " complete &7- finish the quest panel in your hand"));
        sender.sendMessage(Metods.msgC("&e/" + commandLabel + " reload &7- reload config.yml and quests.yml"));
        sender.sendMessage(Metods.msgC("&e/" + commandLabel + " config &7- edit settings in game"));
        return true;
    }

    @Override
    public void FailedMsg(CommandSender arg0, String arg1)
    {

    }
}
