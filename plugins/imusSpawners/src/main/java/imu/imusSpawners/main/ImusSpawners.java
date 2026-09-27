package imu.imusSpawners.main;


import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import imu.iAPI.Handelers.CommandHandler;
import imu.imusSpawners.Commands.ExampleCmd;
import imu.imusSpawners.Events.SpawnerEvents;
import imu.imusSpawners.Managers.Manager_Spawners;


public class ImusSpawners extends JavaPlugin
{
	public static ImusSpawners Instance;
	private Manager_Spawners _managerSpawners;
    public void registerCommands() 
    {
    	
        CommandHandler handler = new CommandHandler(this);
        handler.registerCmd("drop", new ExampleCmd());       
        handler.setPermissionOnLastCmd("dontloseitems.drop");
              

    }
	
	@Override
	public void onEnable() 
	{
		Instance = this;
		_managerSpawners = new Manager_Spawners();
		registerCommands();
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN +" Imus Spawners is Activated");
		getServer().getPluginManager().registerEvents(new SpawnerEvents(), this);
		//getServer().getPluginManager().registerEvents(new FishingEvent(this), this);
	}
	
	@Override
	public void onDisable() 
	{
		_managerSpawners.OnDisabled();
	}
	
	
}
