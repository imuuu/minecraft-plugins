package me.imu.imusminiquests.SubCommands;

import imu.iAPI.Interfaces.CommandInterface;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Quests.Quest;
import me.imu.imusminiquests.Quests.QuestItem;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * /imq complete: fills the quest item in the main hand, for testing rewards.
 */
public class SubCompleteCmd implements CommandInterface
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        Player player = (Player) sender;
        ItemStack stack = player.getInventory().getItemInMainHand();
        Quest quest = ImusMiniQuests.getInstance().getQuests().getQuest(stack);
        if (quest == null)
        {
            player.sendMessage(ChatColor.RED + "Hold a quest panel in your main hand");
            return true;
        }

        QuestItem.setProgress(stack, quest, QuestItem.getRequiredAmount(stack, quest));
        player.getInventory().setItemInMainHand(stack);
        ImusMiniQuests.getInstance().getPanelCarriers().markStale(player);
        player.sendMessage(ChatColor.GREEN + "Completed " + quest.getName());
        return true;
    }

    @Override
    public void FailedMsg(CommandSender arg0, String arg1)
    {

    }
}
