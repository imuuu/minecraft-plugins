package imu.iFishing.Main;

import org.bukkit.plugin.java.JavaPlugin;

import imu.iAPI.Other.Metods;
import imus.iFishing.Events.FishingEvents;

public class ImusFishing extends JavaPlugin
{
	public static ImusFishing Instance;
	public final String _pluginNamePrefix = "&4[&b"+getName()+"&4]&r";
	@Override
	public void onEnable() 
	{
		Instance = this;
		new FishingEvents();
		
		getServer().getConsoleSender().sendMessage(Metods.msgC(_pluginNamePrefix+" &2has been activated!"));
	}
	
	
	@Override
	 public void onDisable()
	{ 
		
	}
	
	
	public void registerCommands() 
	{

	}	
		
	
	
	
}
