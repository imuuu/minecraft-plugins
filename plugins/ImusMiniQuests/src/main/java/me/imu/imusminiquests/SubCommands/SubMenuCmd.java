package me.imu.imusminiquests.SubCommands;

import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Menus.Paper.PaperMenus;
import me.imu.imusminiquests.ImusMiniQuests;
import me.imu.imusminiquests.Inventories.InventoryQuestBrowser;
import me.imu.imusminiquests.Inventories.PaperQuestMenuGame;
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
        PaperMenus.open((Player) sender, new InventoryQuestBrowser(new PaperQuestMenuGame(ImusMiniQuests.getInstance())));
        return true;
    }

    @Override
    public void FailedMsg(CommandSender arg0, String arg1)
    {

    }
}
