package imu.imusSpawners.Events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.ConfigMaker;
import imu.imusSpawners.Managers.Manager_Spawners;
import imu.imusSpawners.Other.CustomSpawner;
import imu.imusSpawners.Other.PlayerSpawnerData;
import imu.imusSpawners.main.ImusSpawners;

public class SpawnerEvents implements Listener
{
	Plugin _plugin;
	private final String METADATA_OWNER_ID = "placed_by";
	

	private double _silk_touch_chance = 14;
	private double _random_entity_chance = 5;
	private double _playerChanceBonusIncrease = 13;
	Random rand;

	private List<EntityType> _spawnerTypes;
	private List<EntityType> _excludeEntityTypes = Arrays.asList(
			EntityType.WANDERING_TRADER, 
			EntityType.WARDEN, 
			EntityType.ARMOR_STAND, 
			EntityType.GIANT, 
			EntityType.ENDER_DRAGON, 
			EntityType.WITHER,
			EntityType.ELDER_GUARDIAN

	);
	
	private HashMap<UUID, PlayerSpawnerData> _spawnerDatas;
	
	public SpawnerEvents()
	{
		_plugin = ImusSpawners.Instance;
		rand = new Random();
		_spawnerDatas = new HashMap<>();
		GetSettings();
		InitiliazeSpawnerTypes();
		
	}

	private void InitiliazeSpawnerTypes()
	{
		_spawnerTypes = new ArrayList<>();
		for (EntityType entityType : EntityType.values())
		{
			if (entityType.isAlive() && entityType.isSpawnable() && !_excludeEntityTypes.contains(entityType))
			{
				//System.out.println("entityType: " + entityType);
				_spawnerTypes.add(entityType);
			}
		}

	}
	
	@EventHandler
	public void OnBlockPlace(BlockPlaceEvent e)
	{
		if(e.isCancelled()) return;
		
		if (e.getBlock().getType() != Material.SPAWNER)
			return;

		ItemStack stack = e.getItemInHand();
		String str_entityType = ImusAPI._metods.getPersistenData(stack, Manager_Spawners.PD_SPAWNER_TYPE, PersistentDataType.STRING);
		
		if (str_entityType == null)
		{
			return;
		}

		EntityType entityType = EntityType.valueOf(str_entityType);
		CreatureSpawner spawner = (CreatureSpawner) e.getBlock().getState();
		spawner.setSpawnedType(entityType);
		spawner.setMetadata(METADATA_OWNER_ID, new FixedMetadataValue(_plugin, e.getPlayer().getUniqueId().toString()));
		spawner.update();
		
		CustomSpawner customSpawner = new CustomSpawner(e.getBlock().getLocation(), entityType);
		customSpawner.SetOwner(e.getPlayer());
		Manager_Spawners.Instance.AddSpawner(customSpawner, true);
		
	}
	
	@EventHandler
	public void OnBlockBreak(BlockBreakEvent e)
	{
		if(e.isCancelled()) return;
		
		Block block = e.getBlock();

		if (block.getType() != Material.SPAWNER)
			return;
		

		boolean playerSpawner = Manager_Spawners.Instance.HasSpawner(e.getBlock().getLocation());

		Player player = e.getPlayer();
		if (!ImusAPI._metods.HasEnchant(player.getInventory().getItemInMainHand(), Enchantment.SILK_TOUCH))
		{
			Manager_Spawners.Instance.RemoveSpawner(block.getLocation());
			return;
		}
			

		CreatureSpawner spawner = (CreatureSpawner) block.getState();
		EntityType entityType = spawner.getSpawnedType();
		
		if(entityType == null)
		{
			Manager_Spawners.Instance.RemoveSpawner(block.getLocation());
			return;
		}
		
		if (playerSpawner)
		{
			e.setDropItems(false);
			e.setExpToDrop(0);
			ItemStack playerCustomSpawner = Manager_Spawners.Instance.GetCustomSpawner(e.getBlock().getLocation()).GetSpawnerItemStack();
			
			if(playerCustomSpawner == null)
			{
				Manager_Spawners.Instance.RemoveSpawner(block.getLocation());
				return;
			}
			
			block.getWorld().dropItemNaturally(block.getLocation(), playerCustomSpawner);
			Manager_Spawners.Instance.RemoveSpawner(block.getLocation());
			return;
		}
		
		Manager_Spawners.Instance.RemoveSpawner(block.getLocation());
		int chance = (int)_silk_touch_chance + _spawnerDatas.get(player.getUniqueId()).CurrentChanceBonus;
		int roll = rand.nextInt(100);
		
		String msg1 = ChatColor.BLUE + "You rolled: "+ChatColor.GOLD+roll;
		player.sendMessage("__________________________________________________");
		player.sendMessage(" ");
		if (( roll > chance))
		{
			msg1 += ChatColor.DARK_PURPLE+" | "+ChatColor.RED+""+chance;
			//player.sendMessage(ChatColor.RED+"It failed because it was higher than your chance( "+ChatColor.AQUA+chance+ChatColor.RED+" )");
			player.sendMessage(msg1);
			FailedToMineSpawner(player);
			chance = (int)_silk_touch_chance + _spawnerDatas.get(player.getUniqueId()).CurrentChanceBonus;
			player.sendMessage(ChatColor.DARK_PURPLE+"You chance has been increased by "+ChatColor.AQUA+(int)_playerChanceBonusIncrease+ChatColor.DARK_PURPLE+" You have now "+ChatColor.GOLD+chance);
			player.sendMessage("__________________________________________________");
			return;
		}

		
		//player.sendMessage(ChatColor.GREEN+"You rolled well! It was lower or equal of your chance ( "+ChatColor.AQUA+chance+ChatColor.BLUE+" )");
		msg1 += ChatColor.DARK_PURPLE+" | "+ChatColor.GREEN+""+chance;
		player.sendMessage(msg1);
		SuccessToMineSpawner(player);
		chance = (int)_silk_touch_chance + _spawnerDatas.get(player.getUniqueId()).CurrentChanceBonus;
		player.sendMessage(ChatColor.DARK_PURPLE+"You chance has been set to back to "+ChatColor.GOLD+chance);
		
		if (rand.nextInt(100) < _random_entity_chance && _spawnerTypes.size() > 0)
		{
			entityType = _spawnerTypes.get(rand.nextInt(_spawnerTypes.size()));
			player.sendMessage(
					ChatColor.GOLD + "You have got spawner drop with " + ChatColor.DARK_PURPLE + "Random type!");
		} else
		{
			player.sendMessage(ChatColor.GOLD + ""+ChatColor.BOLD+"You have got spawner drop!");
		}
		
		CustomSpawner newSpawner = new CustomSpawner(null, entityType);
		newSpawner.SetOwner(player);
		ItemStack stack = newSpawner.GetSpawnerItemStack();
		block.getWorld().dropItemNaturally(block.getLocation(), stack);
		player.sendMessage("__________________________________________________");
		
		Bukkit.getLogger().info("["+ImusSpawners.Instance.getName()+"] "+player.getName()+" Got spawner drop. Type of "+entityType);

	}
	
	@EventHandler
	public void OnLeave(PlayerQuitEvent e)
	{
		if(!_spawnerDatas.containsKey(e.getPlayer().getUniqueId())) return;
		
		_spawnerDatas.remove(e.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void OnJoin(PlayerJoinEvent e)
	{
		GetPlayerData(e.getPlayer());
	}

	

	void GetSettings()
	{
		ConfigMaker cm = new ConfigMaker(_plugin, "settings.yml");
		FileConfiguration config = cm.getConfig();

		String dropChance = "settings.spawnerDropChanceSilkTouch";
		String spawnerRandomEntityChance = "settings.spawnerRandomEntityChance";
		String playerBonusChanceIncrease = "settings.playerBonusChanceIncrease";

		if (!config.contains("settings."))
		{
			// default values
			_plugin.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "ImusSpawners : Default config made!");
			config.set(dropChance, _silk_touch_chance);
			config.set(spawnerRandomEntityChance, _random_entity_chance);

			cm.saveConfig();
			return;
		}
		
		if(!config.contains("settings."+playerBonusChanceIncrease))
		{
			config.set(playerBonusChanceIncrease, _playerChanceBonusIncrease);
			cm.saveConfig();
		}

		_silk_touch_chance = config.getDouble(dropChance);
		_random_entity_chance = config.getDouble(spawnerRandomEntityChance);
		_playerChanceBonusIncrease = config.getInt(playerBonusChanceIncrease);

	}
	
	private void FailedToMineSpawner(Player player)
	{
		PlayerSpawnerData pData = _spawnerDatas.get(player.getUniqueId());
		pData.TotalMinedSpawners += 1;
		pData.CurrentChanceBonus += _playerChanceBonusIncrease;
		
		if(pData.CurrentChanceBonus > 100) pData.CurrentChanceBonus = 100;
		
		SavePlayerData(player);
	}
	
	private void SuccessToMineSpawner(Player player)
	{
		PlayerSpawnerData pData = _spawnerDatas.get(player.getUniqueId());
		pData.TotalMinedSpawners += 1;
		pData.CurrentChanceBonus = 0;
		pData.TotalSpawnerGot += 1;
		
		SavePlayerData(player);
	}
	private void SavePlayerData(Player player)
	{
		final String syntax = "playerData";
		ConfigMaker cm = new ConfigMaker(_plugin, syntax+".yml");
		FileConfiguration config = cm.getConfig();
		
		PlayerSpawnerData pData = _spawnerDatas.get(player.getUniqueId());
		
		final String dataPath = syntax+"."+player.getUniqueId().toString();
		config.set(dataPath+".name", player.getName());
		config.set(dataPath+".currentChanceBonus", pData.CurrentChanceBonus);
		config.set(dataPath+".totalMinedSpawners", pData.TotalMinedSpawners);
		config.set(dataPath+".totalSpawnerGot", pData.TotalSpawnerGot);
		cm.saveConfig();
		//config.set
	}
	
	private void GetPlayerData(Player player)
	{
		final String syntax = "playerData";
		ConfigMaker cm = new ConfigMaker(_plugin, syntax+".yml");
		FileConfiguration config = cm.getConfig();
		final String dataPath = syntax+"."+player.getUniqueId().toString();
		
		if(!config.contains(dataPath))
		{
			_spawnerDatas.put(player.getUniqueId(), new PlayerSpawnerData(player.getUniqueId()));
			return;
		}
		
		PlayerSpawnerData data = new PlayerSpawnerData(player.getUniqueId());
		
		data.CurrentChanceBonus = config.getInt(dataPath+".currentChanceBonus");
		data.TotalMinedSpawners = config.getInt(dataPath+".totalMinedSpawners");
		data.TotalSpawnerGot = config.getInt(dataPath+".totalSpawnerGot");
		
		_spawnerDatas.put(player.getUniqueId(), data);
		
	}
	
	
//	void GetPlayerSettings(Player player)
//	{
//		final String syntax = "playerData";
//		ConfigMaker cm = new ConfigMaker(_plugin, syntax+".yml");
//		FileConfiguration config = cm.getConfig();
//		
//		String uuid = player.getUniqueId().toString();
//		String dropChance = syntax+".spawnerDropChanceSilkTouch";
//		
//		if (!config.contains("settings."))
//		{
//			// default values
//			_plugin.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "ImusSpawners : Default config made!");
//			config.set(dropChance, _silk_touch_chance);
//			//config.set(spawnerRandomEntityChance, _random_entity_chance);
//
//			cm.saveConfig();
//			return;
//		}
//
//		_silk_touch_chance = config.getDouble(dropChance);
//		//_random_entity_chance = config.getDouble(spawnerRandomEntityChance);
//
//	}
	
	

}
