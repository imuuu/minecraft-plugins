package imu.imusSpawners.Events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.ConfigMaker;
import imu.iAPI.Other.Metods;
import imu.imusSpawners.main.ImusSpawners;

public class MainEvents implements Listener
{
	Plugin _plugin;
	private final String METADATA_OWNER_ID = "placed_by";
	private final String PD_SPAWNER_TYPE = "spawner_type";

	private double _silk_touch_chance = 30;
	private double _random_entity_chance = 5;
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

	public MainEvents()
	{
		_plugin = ImusSpawners.Instance;
		rand = new Random();
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

		if (e.getBlock().getType() != Material.SPAWNER)
			return;

		ItemStack stack = e.getItemInHand();
		String str_entityType = ImusAPI._metods.getPersistenData(stack, PD_SPAWNER_TYPE, PersistentDataType.STRING);
		
		if (str_entityType == null)
		{
			return;
		}

		EntityType entityType = EntityType.valueOf(str_entityType);
		CreatureSpawner spawner = (CreatureSpawner) e.getBlock().getState();
		spawner.setSpawnedType(entityType);
		spawner.setMetadata(METADATA_OWNER_ID, new FixedMetadataValue(_plugin, e.getPlayer().getUniqueId().toString()));
		spawner.update();
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e)
	{
		Block block = e.getBlock();

		if (block.getType() != Material.SPAWNER)
			return;

		Player player = e.getPlayer();
		if (!ImusAPI._metods.HasEnchant(player.getInventory().getItemInMainHand(), Enchantment.SILK_TOUCH))
			return;

		CreatureSpawner spawner = (CreatureSpawner) block.getState();
		EntityType entityType = spawner.getSpawnedType();

		boolean playerSpawner = !block.getState().getMetadata(METADATA_OWNER_ID).isEmpty();

		if (playerSpawner)
		{
			e.setDropItems(false);
			e.setExpToDrop(0);
			ItemStack stack = GetSpawner(entityType);
			block.getWorld().dropItemNaturally(block.getLocation(), stack);
			return;
		}

		if ((rand.nextInt(100) >= _silk_touch_chance))
			return;

		if (rand.nextInt(100) < _random_entity_chance && _spawnerTypes.size() > 0)
		{
			entityType = _spawnerTypes.get(rand.nextInt(_spawnerTypes.size()));
			player.sendMessage(
					ChatColor.GOLD + "You have got spawner drop with " + ChatColor.DARK_PURPLE + "Random type!");
		} else
		{
			player.sendMessage(ChatColor.GOLD + "You have got spawner drop!");
		}

		ItemStack stack = GetSpawner(entityType);
		block.getWorld().dropItemNaturally(block.getLocation(), stack);

	}

	private ItemStack GetSpawner(EntityType type)
	{
		ItemStack stack = new ItemStack(Material.SPAWNER);
		Metods.setDisplayName(stack, ChatColor.GOLD + "SPAWNER: " + ChatColor.DARK_PURPLE + type.toString());
		ImusAPI._metods.setPersistenData(stack, PD_SPAWNER_TYPE, PersistentDataType.STRING, type.toString());
		return stack;
	}

	void GetSettings()
	{
		ConfigMaker cm = new ConfigMaker(_plugin, "settings.yml");
		FileConfiguration config = cm.getConfig();

		String dropChance = "settings.spawnerDropChanceSilkTouch";
		String spawnerRandomEntityChance = "settings.spawnerRandomEntityChance";

		if (!config.contains("settings."))
		{
			// default values
			_plugin.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "ImusSpawners : Default config made!");
			config.set(dropChance, _silk_touch_chance);
			config.set(spawnerRandomEntityChance, _random_entity_chance);

			cm.saveConfig();
			return;
		}

		_silk_touch_chance = config.getDouble(dropChance);
		_random_entity_chance = config.getDouble(spawnerRandomEntityChance);

	}

}
