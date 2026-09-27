package imu.imusSpawners.Managers;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iAPI.FastInventory.Fast_Inventory;
import imu.iAPI.FastInventory.Manager_FastInventories;
import imu.iAPI.Other.ConfigMaker;
import imu.imusSpawners.Other.CustomSpawner;
import imu.imusSpawners.main.ImusSpawners;

public class Manager_Spawners
{
	public static Manager_Spawners Instance;
	public static final String PD_SPAWNER_TYPE = "spawner_type";
	private HashMap<Location, CustomSpawner> _spawners;
	
	private final String configFileName = "spawnerData";
	
	public Manager_Spawners()
	{
		Instance = this;
		_spawners = new HashMap<>();
		AddTestItems();
		LoadAll();
	}
	
	public void OnDisabled()
	{
		SaveAll();
	}
	
	private void AddTestItems()
	{
		Fast_Inventory fastInv = new Fast_Inventory("Spawners", "&5Spawners", null);
		for(EntityType entityType : EntityType.values())
		{
			if(!entityType.isAlive()) continue;
			
			if(!entityType.isSpawnable()) continue;
			

			fastInv.AddStack(new CustomSpawner(null, entityType).GetSpawnerItemStack());
			
		}
		Manager_FastInventories.Instance.RegisterFastInventory(fastInv);
		
	}
	public void AddSpawner(CustomSpawner customSpawner, boolean saveData)
	{
		_spawners.put(customSpawner.GetLocation(), customSpawner);
		
		if(saveData) SaveAllAsync();
	}
	public void RemoveSpawner(Location loc)
	{
		if(_spawners.containsKey(loc))
		{
			RemoveSingleAsync(_spawners.get(loc));
			_spawners.remove(loc);
			 
		}
	}
	public void RemoveSpawner(CustomSpawner customSpawner)
	{
		RemoveSpawner(customSpawner.GetLocation());
	}
	public CustomSpawner GetCustomSpawner(Location loc)
	{
		if(!_spawners.containsKey(loc)) return null;
		
		return _spawners.get(loc);
	}
	public boolean HasSpawner(Location loc)
	{
		return _spawners.containsKey(loc);
	}
	
//	public ItemStack GetSpawner(EntityType type)
//	{
//		ItemStack stack = new ItemStack(Material.SPAWNER);
//		Metods.setDisplayName(stack, ChatColor.GOLD + "SPAWNER: " + ChatColor.DARK_PURPLE + type.toString());
//		ImusAPI._metods.setPersistenData(stack, PD_SPAWNER_TYPE, PersistentDataType.STRING, type.toString());
//		return stack;
//	}
	public void SaveAll()
	{
		if(_spawners == null || _spawners.isEmpty()) return;
		
		ConfigMaker cm = new ConfigMaker(ImusSpawners.Instance, configFileName + ".yml");
		FileConfiguration config = cm.getConfig();
		
		for(CustomSpawner spawner : _spawners.values())
		{
			spawner.SaveToConfig(config);
		}
		
		cm.saveConfig();
		
		
	}
	
	public void LoadAll()
	{
		
		ConfigMaker cm = new ConfigMaker(ImusSpawners.Instance, configFileName + ".yml");
		FileConfiguration config = cm.getConfig();
		
		if(_spawners == null) _spawners = new HashMap<>();
			
		_spawners.clear();
		
		for(String spawner_id : config.getKeys(false))
		{
			CustomSpawner spawner = new CustomSpawner(spawner_id, config);
			AddSpawner(spawner, false);
		}
	}
	
	public void SaveSingleAsync(CustomSpawner spawner)
	{
		new BukkitRunnable() 
		{
			
			@Override
			public void run()
			{
				ConfigMaker cm = new ConfigMaker(ImusSpawners.Instance, configFileName + ".yml");
				FileConfiguration config = cm.getConfig();
				spawner.SaveToConfig(config);
				cm.saveConfig();
			}
		}.runTaskAsynchronously(ImusSpawners.Instance);
	}
	
	public void RemoveSingleAsync(CustomSpawner spawner)
	{
		new BukkitRunnable() 
		{
			
			@Override
			public void run()
			{
				ConfigMaker cm = new ConfigMaker(ImusSpawners.Instance, configFileName + ".yml");
				FileConfiguration config = cm.getConfig();
				spawner.RemoveFromConfig(config);
				cm.saveConfig();
			}
		}.runTaskAsynchronously(ImusSpawners.Instance);
	}
	
 	public void SaveAllAsync()
	{
		new BukkitRunnable() {
			
			@Override
			public void run()
			{
				SaveAll();
			}
		}.runTaskAsynchronously(ImusSpawners.Instance);
	}
	
	public void LoadAllAsync()
	{
		new BukkitRunnable() 
		{
			
			@Override
			public void run()
			{
				LoadAll();
			}
		}.runTaskAsynchronously(ImusSpawners.Instance);
	}
	
	
	
}
