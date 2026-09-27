package imu.AC.Xray.Managers;


import imu.AC.Main.Main;
import imu.AC.Xray.Events.XrayEvents;

public class XrayManager 
{
	private Main _main;
	
	public XrayManager(Main main) 
	{
		_main = main;
		main.getServer().getPluginManager().registerEvents(new XrayEvents(main), main);
	}
	
	
}
