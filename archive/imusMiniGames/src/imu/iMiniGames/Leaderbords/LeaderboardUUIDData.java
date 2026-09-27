package imu.iMiniGames.Leaderbords;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iAPI.Other.ConfigMaker;
import imu.iMiniGames.Main.ImusMiniGames;

public class LeaderboardUUIDData implements Listener
{
	HashMap<UUID, String> _names = new HashMap<>();
	String _path = "UUIDandNames.yml";
	private ImusMiniGames _main;
	
	public LeaderboardUUIDData(ImusMiniGames main)
	{
		_main = main;
		_main.getServer().getPluginManager().registerEvents(this, main);
		loadFile();
	}
	
	public void onDisabled()
	{
		saveFile();
	}
	
	public String getName(UUID uuid)
	{
		if(!_names.containsKey(uuid))
			return "no name";
		
		return _names.get(uuid);
	}
	
	@EventHandler
	void onJoin(PlayerJoinEvent event)
	{
		_names.put(event.getPlayer().getUniqueId(), event.getPlayer().getName());
	}
	
	void loadFile()
	{
		new BukkitRunnable() 
		{
			
			@Override
			public void run() 
			{
				ConfigMaker cm = new ConfigMaker(_main, _path);
				if(cm.isExists())
				{
					FileConfiguration config = cm.getConfig();
					for (String key : config.getConfigurationSection("").getKeys(false)) 
					{
						_names.put(UUID.fromString(key), config.getString(key));
					}
				}
			}
		}.runTaskAsynchronously(_main);
		
	}
	
	void saveFile()
	{
		ConfigMaker cm = new ConfigMaker(_main, _path);
		FileConfiguration config = cm.getConfig();
		for(Entry<UUID,String> entry : _names.entrySet())
		{
			config.set(entry.getKey().toString(), entry.getValue());
		}
		cm.saveConfig();
	}
}
