package me.bullterrier292;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class WorldRestore extends JavaPlugin{
	
	public WorldGuardPlugin worldGuardPlugin;

	@Override
	public void onEnable() {
		worldGuardPlugin = getWorldGuard();
		
		WorldRestoreCommands commands = new WorldRestoreCommands();
		
		getCommand(commands.cmd1).setExecutor(commands);
		getCommand(commands.cmd2).setExecutor(commands);
		getCommand(commands.cmd3).setExecutor(commands);
		getCommand(commands.cmd4).setExecutor(commands);
		getCommand(commands.cmd5).setExecutor(commands);
		getCommand(commands.cmd6).setExecutor(commands);
		getCommand(commands.cmd7).setExecutor(commands);
		getCommand(commands.cmd8).setExecutor(commands);
		getCommand(commands.cmd9).setExecutor(commands);
		getCommand(commands.cmd10).setExecutor(commands);
		
		getServer().getPluginManager().registerEvents(new WorldRestoreEvents(), this );
		loadConfig();
		WorldRestoreCommands commands1 = new WorldRestoreCommands();
		//whole chunk will be updated 
		commands1.autochecker(0, (1000*60*60*24*7));
		
		commands1.autoUpdater(0);
		
		//surface chunk will be updated
		commands1.autochecker(1, (1000*60*60*24*3));
		
		commands1.autoUpdater(1);
		
		//fixing not fixed
		commands1.fixNotfixedChunks(60*30, 2);
		
		
		
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
	public WorldGuardPlugin getWorldGuard() {
		Plugin plugin = this.getServer().getPluginManager().getPlugin("WorldGuard");
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return null;
		}
	return (WorldGuardPlugin) plugin;
	}
	
	
	
	

		
	


}
