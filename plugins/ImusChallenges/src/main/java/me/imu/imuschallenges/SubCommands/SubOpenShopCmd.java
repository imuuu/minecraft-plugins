package me.imu.imuschallenges.SubCommands;

import imu.iAPI.CmdUtil.CmdData;
import imu.iAPI.Interfaces.CommandInterface;
import me.imu.imuschallenges.Inventories.InventoryCCollectMaterial;
import me.imu.imuschallenges.Managers.ManagerChallengeShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SubOpenShopCmd implements CommandInterface
{

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
    {
        Player player = ((Player)sender);
        ManagerChallengeShop.getInstance().openShop(player);

        return false;
    }


    @Override
    public void FailedMsg(CommandSender arg0, String arg1) {

    }



}