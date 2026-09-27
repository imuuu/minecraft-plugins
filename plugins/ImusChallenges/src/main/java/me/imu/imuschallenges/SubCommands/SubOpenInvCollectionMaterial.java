package me.imu.imuschallenges.SubCommands;


import imu.iAPI.CmdUtil.CmdData;
import imu.iAPI.Interfaces.CommandInterface;
import imu.iAPI.Utilities.ImusUtilities;
import me.imu.imuschallenges.Inventories.InventoryCCollectMaterial;
import me.imu.imuschallenges.Inventories.TestInventory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SubOpenInvCollectionMaterial implements CommandInterface
{
	CmdData _data;

	public SubOpenInvCollectionMaterial(CmdData data)
	{
		_data = data;
	}
	
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
    {

    	Player player = ((Player)sender);

		new InventoryCCollectMaterial().open(player);

        return false;
    }
    
   
	@Override
	public void FailedMsg(CommandSender arg0, String arg1) {
		
	}
    
   
   
}