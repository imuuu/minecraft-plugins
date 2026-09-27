package imu.imusSpawners.Other;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;
import imu.imusSpawners.Managers.Manager_Spawners;

public class CustomSpawner
{
	
	private Location _loc;
	private EntityType _entityType;
	private UUID _uuid = null;
	private final String PD_OWNER_UUID = "owner_uuid";
	
	public CustomSpawner(Location loc, EntityType entityType)
	{
		_loc = loc;
		_entityType = entityType;
	}
	
	public CustomSpawner(String id, FileConfiguration config)
	{
		LoadFromConfig(id, config);
	}
	
	public CustomSpawner SetOwner(Player player)
	{
		return SetOwner(player.getUniqueId());
	}
	
	public CustomSpawner SetOwner(UUID uuid)
	{
		_uuid = uuid;
		return this;
	}
	public Location GetLocation()
	{
		return _loc;
	}
	
	public EntityType GetEntityType()
	{
		return _entityType;
	}
	
	public String GetID()
	{
		return _loc.getWorld().getName()+";"+_loc.getBlockX()+";"+_loc.getBlockY()+";"+_loc.getBlockZ();
	}
	
	public ItemStack GetSpawnerItemStack()
	{
		ItemStack stack = new ItemStack(Material.SPAWNER);
		Metods.setDisplayName(stack, ChatColor.GOLD + "SPAWNER: " + ChatColor.DARK_PURPLE + _entityType.toString());
		ImusAPI._metods.setPersistenData(stack, Manager_Spawners.PD_SPAWNER_TYPE, PersistentDataType.STRING, _entityType.toString());
		
		if(_uuid != null) ImusAPI._metods.setPersistenData(stack, PD_OWNER_UUID, PersistentDataType.STRING, _uuid.toString());
		
		return stack;
	}
	
	public void SaveToConfig(FileConfiguration config)
	{
		final String id = GetID();
		
		config.set(id+".location", _loc);
		config.set(id+".ownerUUID", _uuid  == null ? "NONE" : _uuid.toString());
		config.set(id+".type", _entityType.toString());
	}
	public void RemoveFromConfig(FileConfiguration config)
	{
		config.set(GetID(), null);
	}
	public void LoadFromConfig(String id, FileConfiguration config)
	{
		_loc = config.getLocation(id+".location");
		
		String str_uuid = config.getString(id+".ownerUUID");
		
		if(!str_uuid.matches("NONE") ) _uuid = UUID.fromString(str_uuid);
		
		_entityType = EntityType.valueOf(config.getString(id+".type"));
		
	}
}
