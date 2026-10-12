package me.imu.imusminiquests.Commands;

import imu.iAPI.Utilities.TextUtils;
import imu.iAPI.Interfaces.CommandInterface;
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

        sender.sendMessage(TextUtils.Color("&6ImusMiniQuests"));
        sender.sendMessage(TextUtils.Color("&e/" + commandLabel + " points [player] &7- quest points and the luck they give"));
        sender.sendMessage(TextUtils.Color("&e/" + commandLabel + " unlocks &7- which locked rewards are unlocked (admin)"));
        sender.sendMessage(TextUtils.Color("&e/" + commandLabel + " menu &7- browse the quests, take panels, preview rewards"));
        sender.sendMessage(TextUtils.Color("&e/" + commandLabel + " give <player> <quest> [amount] [rarity] &7- give quest panels"));
        sender.sendMessage(TextUtils.Color("&e/" + commandLabel + " list &7- list the quests"));
        sender.sendMessage(TextUtils.Color("&e/" + commandLabel + " complete &7- finish the quest panel in your hand"));
        sender.sendMessage(TextUtils.Color("&e/" + commandLabel + " economy &7- the numbers money rewards are based on"));
        sender.sendMessage(TextUtils.Color("&e/" + commandLabel + " reload &7- reload config.yml and quests.yml"));
        sender.sendMessage(TextUtils.Color("&e/" + commandLabel + " config &7- edit settings in game"));
        return true;
    }

    @Override
    public void FailedMsg(CommandSender arg0, String arg1)
    {

    }
}
