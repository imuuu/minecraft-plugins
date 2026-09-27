package me.bullterrier292;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockCube extends JavaPlugin{
	
	private Commands commands = new Commands();

	@Override
	public void onEnable() {
		getCommand(commands.cmd1).setExecutor(commands);
		getServer().getPluginManager().registerEvents(new EventsClass(), this );
		getServer().getPluginManager().registerEvents(new oreInsideOfcube(), this );
		
		
	}
	@Override
	public void onDisable() {
		
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		return false;
	}
	

		
	


}
