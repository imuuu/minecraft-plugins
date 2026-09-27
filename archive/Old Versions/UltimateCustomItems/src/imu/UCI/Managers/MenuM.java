package imu.UCI.Managers;

import org.bukkit.entity.Player;

import imu.UCI.INVs.MainMenuINV;
import imu.UCI.Interfaces.ImenuM;
import imu.UCI.main.Main;

public abstract class MenuM implements ImenuM
{
	Main _main;
	public MenuM(Main main) 
	{
		_main = main;
	}
	
	@Override
	public void openNewInv(Player player) 
	{
		MainMenuINV inv = new MainMenuINV(_main, player, "MainMenu", 9*6);
		inv.openThis();
	}
	
	
}
