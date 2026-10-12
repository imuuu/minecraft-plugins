package me.imu.imusminiquests.SubCommands;

import imu.iAPI.Utilities.TextUtils;
import imu.iAPI.Interfaces.CommandInterface;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Quests.Quest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collection;

/**
 * /imq list: every loaded quest with its id, objective and drop weight.
 */
public class SubListCmd implements CommandInterface
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        Collection<Quest> quests = ImusMiniQuests.getInstance().getQuests().getQuests();
        sender.sendMessage(TextUtils.Color("&6Mini quests (" + quests.size() + "):"));
        for (Quest quest : quests)
        {
            String drop = quest.getDropWeight() > 0 ? "drop weight " + quest.getDropWeight() : "does not drop";
            sender.sendMessage(TextUtils.Color("&e" + quest.getId() + " &7- " + quest.getName()
                    + " &7- " + quest.getObjective().describe() + " &8(" + drop + ")"));
        }
        return true;
    }

    @Override
    public void FailedMsg(CommandSender arg0, String arg1)
    {

    }
}
