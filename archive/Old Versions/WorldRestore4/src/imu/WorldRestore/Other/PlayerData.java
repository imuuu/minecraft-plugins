package imu.WorldRestore.Other;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import imu.WorldRestore.main.Main;
import imu.iAPI.Other.Tuple;

public class PlayerData 
{
	String _playerDataYAML = "playerData.yml";
	String _quitData = "QuitLocs";
	
	HashMap<Chunk, Boolean> _quitChunks = new HashMap<>();
	HashMap<UUID, Tuple<Location, Boolean>> _quitLocations = new HashMap<>();
	
	Main _main = null;
	public PlayerData(Main main) 
	{
		_main = main;
		loadPlayerQuitData();
	}
	
	public String getPlayerDataYAML()
	{
		return _playerDataYAML;
	}
	
	public void savePlayerQuitLocation(Player player, boolean tagged)
	{
		_quitLocations.put(player.getUniqueId(), new Tuple<Location, Boolean>(player.getLocation(), false));
		_quitChunks.put(player.getLocation().getChunk(), false);
		savePlayerQuitToFile(player.getUniqueId(),player.getLocation(), tagged);
		
	}
	public void savePlayerQuitToFile(UUID uuid, Location loc, boolean tagged)
	{
		ConfigMaker cm = new ConfigMaker(_main, getPlayerDataYAML());
		FileConfiguration config = cm.getConfig();
		
		config.set(_quitData+"."+uuid+".loc", loc);
		config.set(_quitData+"."+uuid+".tagged", tagged);
		
		cm.saveConfig();
	}
	
	
	void removePlayerQuitLocation(Player player)
	{
		_quitLocations.remove(player.getUniqueId());
		if(!isAnyPlayerThatChunk(player.getLocation().getChunk()))
		{
			_quitChunks.remove(player.getLocation().getChunk());
		}
		ConfigMaker cm = new ConfigMaker(_main, getPlayerDataYAML());
		FileConfiguration config = cm.getConfig();
		config.set(_quitData+"."+player.getUniqueId(), null);
		cm.saveConfig();

	}
	public boolean checkIfPlayerIsTagged(Player player)
	{
		boolean tele = false;
		if(_quitLocations.containsKey(player.getUniqueId()))
		{
			if(_quitLocations.get(player.getUniqueId()).GetValue())
			{
				//tele
				
				tele = teleportPlayerSafe(player);
			}
		}
		removePlayerQuitLocation(player);
		return tele;
	}
	
	public boolean teleportPlayerSafe(Player player)
	{
		
		Location loc = player.getLocation();
		if(!_main.getChunkManager().isLocationInRegion(loc))
		{
			Location loc2 = loc.clone();
			loc2 = loc2.add(0, 1, 0);
			if(!loc2.getBlock().getType().isInteractable() && loc2.getBlock().getType() != Material.AIR)
			{
				player.teleport(loc.getWorld().getSpawnLocation());
				return true;
			}
			
		}
		
		return false;
	}
	
	boolean isAnyPlayerThatChunk(Chunk chunk)
	{
		for(Tuple<Location, Boolean> t : _quitLocations.values())
		{
			if(t.GetKey().getChunk() == chunk)
			{
				return true;
			}
		}
		return false;
	}
	public boolean hasPlayersInThisChunk(Chunk chunk)
	{
		if(_quitChunks.containsKey(chunk))
			return true;
		
		return false;
	}
	
	public void setPlayersTaggedInChunk(Chunk chunk)
	{
		Iterator<Map.Entry<UUID, Tuple<Location, Boolean>>> it = _quitLocations.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry<UUID, Tuple<Location, Boolean>> entry = it.next();
			Location loc = entry.getValue().GetKey();
			if(loc.getChunk() == chunk)
			{
				entry.setValue(new Tuple<Location, Boolean>(loc, true));
				savePlayerQuitToFile(entry.getKey(),loc, true);
				
			}

		}
	}
		
	void loadPlayerQuitData()
	{
		ConfigMaker cm = new ConfigMaker(_main, getPlayerDataYAML());
		FileConfiguration config = cm.getConfig();
		if(config.contains(_quitData))
		{
			for (String key : config.getConfigurationSection(_quitData).getKeys(false)) 
			{
				UUID uuid = UUID.fromString(key);
				Location loc = config.getLocation(_quitData+"."+key+".loc");
				Boolean tag = config.getBoolean(_quitData+"."+key+".tagged");
				_quitChunks.put(loc.getChunk(), false);
				_quitLocations.put(uuid, new Tuple<Location, Boolean>(loc, tag));
			}
		}
		
	}
	
}
