package me.bullterrier292;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldRestore extends JavaPlugin{
	
	private Commands commands = new Commands();

	@Override
	public void onEnable() {
		
		getCommand(commands.cmd1).setExecutor(commands);
		getCommand(commands.cmd2).setExecutor(commands);
		getCommand(commands.cmd3).setExecutor(commands);
		getCommand(commands.cmd4).setExecutor(commands);
		getCommand(commands.cmd5).setExecutor(commands);
		getCommand(commands.cmd6).setExecutor(commands);
		getCommand(commands.cmd7).setExecutor(commands);
		getCommand(commands.cmd8).setExecutor(commands);
		
		getServer().getPluginManager().registerEvents(new EventsClass(), this );
		
		
		loadConfig();
		
		/*
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				System.out.println("moiii");
				commands.fixSpecificChunk("1", 0);
			}
		}, 0, 20*10);
		*/
		
		
		
	}
	@Override
	public void onDisable() {
		
		
	}
	
	public void loadConfig() {
		
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
	
	
	
	

		
	


}
