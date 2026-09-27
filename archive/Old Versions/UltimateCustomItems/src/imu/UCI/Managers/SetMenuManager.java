package imu.UCI.Managers;

import org.bukkit.entity.Player;

import imu.UCI.INVs.SetMenuINV;
import imu.UCI.Interfaces.ImenuM;
import imu.UCI.main.Main;

public final class SetMenuManager implements ImenuM
{
	Main _main = null;
	public SetMenuManager(Main main) 
	{
		_main = main;
	}
	
	@Override
	public void openNewInv(Player player) 
	{
		SetMenuINV inv = new SetMenuINV(_main, player, "SetMenu", 9, null);
		inv.openThis();
	}
	
	
}
