package me.imu.imusminiquests.SubCommands;

import imu.iAPI.Interfaces.CommandInterface;
import me.imu.imusminiquests.Inventories.InventoryQuestBrowser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /imq menu: browse every quest, take panels and preview their rewards.
 */
public class SubMenuCmd implements CommandInterface
{
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        new InventoryQuestBrowser().open((Player) sender);
        return true;
    }

    @Override
    public void FailedMsg(CommandSender arg0, String arg1)
    {

    }
}
