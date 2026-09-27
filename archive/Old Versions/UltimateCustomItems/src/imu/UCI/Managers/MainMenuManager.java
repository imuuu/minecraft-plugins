package imu.UCI.Managers;

import org.bukkit.entity.Player;

import imu.UCI.INVs.MainMenuINV;
import imu.UCI.main.Main;

public final class MainMenuManager extends MenuM
{
	public MainMenuManager(Main main) {
		super(main);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void openNewInv(Player player)
	{
		MainMenuINV inv = new MainMenuINV(_main, player, "MainMenu", 9*6);
		inv.openThis();
	}
}
