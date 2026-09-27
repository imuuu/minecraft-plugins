package imu.HomeTele;

import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import imu.HomeTele.Commands.Commands;
import imu.HomeTele.Events.EventClass;


public class Main extends JavaPlugin implements Listener 
{
	private Commands commands;
	
	@Override
	public void onEnable() 
	{		
		commands = new Commands(this);
		for(String cmd : commands.cmds){getCommand(cmd).setExecutor(commands);}
						
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN +" HomeTele has been activated!");
		getServer().getPluginManager().registerEvents(new EventClass(commands), this);
	}

}
