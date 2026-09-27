package imu.DontLoseItems.Managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import imu.DontLoseItems.CustomItems.Hell_ReflectShieldController;
import imu.DontLoseItems.Enums.CATEGORY;
import imu.DontLoseItems.Enums.ITEM_RARITY;
import imu.DontLoseItems.main.DontLoseItems;
import imu.DontLoseItems.other.RarityItem;
import imu.iAPI.FastInventory.Manager_FastInventories;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ImusUtilities;
import net.minecraft.world.item.enchantment.Enchantment.Rarity;

public class Manager_HellArmor implements Listener
{
	public static Manager_HellArmor Instance;
	
	private HashMap<UUID, Location> _lastLocations;
	

	private final String _PD_HELL_BOOTS = "HELL_BOOTS";
	private final String _PD_HELL_LEGGINS = "HELL_LEGGINS";
	private final String _PD_HELL_CHESTPLATE = "HELL_CHESTPLATE";
	private final String _PD_HELL_HELMET = "HELL_HELMET";
	private final String _PD_HELL_TIER = "HELL_TIER";
	private final String _PD_HELL_FEAR_REDUCE = "HELL_FEAR_REDUCE";
	private final String _PD_HELL_ARROW = "HELL_ARROW";
	private final String _PD_HELL_TORCH = "HELL_TORCH";
	
	


	private final String _CD_ON_LAVA = "onLava";
	private final String _CD_IN_LAVA = "inLava";
	private final String _CD_CREATED_SAFE_PLAT = "safe_plat";
	
	private Cooldowns _cds;
	//private Random _rand;
	private World _nether;
	
	private final int _hellBootsRadius = 3;
	public final int ReflectShieldDurabilityLost = 3;
	

	@SuppressWarnings("unused")
	private BukkitTask _asyncTask;
	private int _asyncTaskTicks = 1;
	
	@SuppressWarnings("unused")
	private BukkitTask _task;
	private int _taskTicks = 1;

	//public final Hell_ReflectShieldController ReflectArrowController;
	
	private boolean _workInProgress = false;
	public Manager_HellArmor()
	{
		Instance = this;
		_lastLocations = new HashMap<>();
	
		_cds = new Cooldowns();
		//_rand = new Random();
		_nether = Bukkit.getWorld("world_nether");
		//RunnableAsync();
		
		Runnable();
		//AddVoidArmorRecipies();
		
		
	}
	
	public void SetArmorTestItems()
	{
		for(RarityItem rarityItem : _hellHelmet)
		{
			Manager_FastInventories.Instance.TryToAdd(CATEGORY.Hell_Armor.toString(), CreateHellHelmet(rarityItem.Rarity));
		}
		Manager_FastInventories.Instance.TryToAdd(CATEGORY.Hell_Armor.toString(), CreateVOIDHelmet());
		
		for(RarityItem rarityItem : _hellChest)
		{
			Manager_FastInventories.Instance.TryToAdd(CATEGORY.Hell_Armor.toString(), CreateHellChestplate(rarityItem.Rarity));
		}
		Manager_FastInventories.Instance.TryToAdd(CATEGORY.Hell_Armor.toString(), CreateVOIDChestplate());
		
		for(RarityItem rarityItem : _hellLeggins)
		{
			Manager_FastInventories.Instance.TryToAdd(CATEGORY.Hell_Armor.toString(), CreateHellLeggins(rarityItem.Rarity));
		}
		Manager_FastInventories.Instance.TryToAdd(CATEGORY.Hell_Armor.toString(), CreateVOIDLeggins());
		
		for(RarityItem rarityItem : _hellBoots)
		{
			Manager_FastInventories.Instance.TryToAdd(CATEGORY.Hell_Armor.toString(), CreateHellBoots(rarityItem.Rarity));
		}
		Manager_FastInventories.Instance.TryToAdd(CATEGORY.Hell_Armor.toString(), CreateVOIDBoots());
		
		for(RarityItem rarityItem : _hellArrows)
		{
			Manager_FastInventories.Instance.TryToAdd("Hell_Other", CreateHellArrow(rarityItem.Rarity));
		}
	}
	
	private void Runnable()
	{
		_task = new BukkitRunnable() 
		{				
			@Override
			public void run() 
			{
				Manager_HellTools.Instance.HellSword_Controller.OnThrowLoop();
				Manager_HellTools.Instance.HellAxe_Controller.OnThrowLoop();
			}

			
		}.runTaskTimer(DontLoseItems.Instance, 20 * 3, _taskTicks);	
	}

	
	private RarityItem[] _hellBoots = 
		{
			new RarityItem(new ItemStack(Material.LEATHER_BOOTS), ChatColor.DARK_RED+"Hell Boots", ITEM_RARITY.Common, 		new double[] {0.0, -14,  1, 0, 0, 2}),	
			new RarityItem(new ItemStack(Material.GOLDEN_BOOTS), ChatColor.DARK_RED+"Hell Boots", ITEM_RARITY.Uncommon, 	new double[] {0.00, -12, 1, 0, 0, 2}),	
			new RarityItem(new ItemStack(Material.CHAINMAIL_BOOTS), ChatColor.DARK_RED+"Hell Boots", ITEM_RARITY.Rare, 		new double[] {0.01, -10, 1, 1, 0, 2}),	
			new RarityItem(new ItemStack(Material.IRON_BOOTS), ChatColor.DARK_RED+"Hell Boots", ITEM_RARITY.Epic, 			new double[] {0.01, -8,  1, 1, 0, 2}),	
			new RarityItem(new ItemStack(Material.DIAMOND_BOOTS), ChatColor.DARK_RED+"Hell Boots", ITEM_RARITY.Mythic, 		new double[] {0.02, -6,  1, 1, 0, 2.2}),	
			new RarityItem(new ItemStack(Material.NETHERITE_BOOTS), ChatColor.DARK_RED+"Hell Boots", ITEM_RARITY.Legendary, new double[] {0.05, -4,  3, 3, 0, 3}),	
		};
	
	private RarityItem[] _hellLeggins = 
		{
			new RarityItem(new ItemStack(Material.LEATHER_LEGGINGS), ChatColor.DARK_RED+"Hell Leggins", ITEM_RARITY.Common, 		new double[] {0.0, 	-8,  1, 0, 0, 2}),	
			new RarityItem(new ItemStack(Material.GOLDEN_LEGGINGS), ChatColor.DARK_RED+"Hell Leggins", ITEM_RARITY.Uncommon, 		new double[] {0.00, -7, 2, 0 , 0, 2}),	
			new RarityItem(new ItemStack(Material.CHAINMAIL_LEGGINGS), ChatColor.DARK_RED+"Hell Leggins", ITEM_RARITY.Rare, 		new double[] {0.00, -6, 2, 1 , 0, 2}),	
			new RarityItem(new ItemStack(Material.IRON_LEGGINGS), ChatColor.DARK_RED+"Hell Leggins", ITEM_RARITY.Epic, 				new double[] {0.00, -5,  3, 1, 0, 2}),	
			new RarityItem(new ItemStack(Material.DIAMOND_LEGGINGS), ChatColor.DARK_RED+"Hell Leggins", ITEM_RARITY.Mythic, 		new double[] {0.01, -2,  5, 1, 0, 3}),	
			new RarityItem(new ItemStack(Material.NETHERITE_LEGGINGS), ChatColor.DARK_RED+"Hell Leggins", ITEM_RARITY.Legendary, 	new double[] {0.01,  0,  6, 3, 0, 3}),	
		};
	
	private RarityItem[] _hellChest = 
		{
			new RarityItem(new ItemStack(Material.LEATHER_CHESTPLATE), ChatColor.DARK_RED+"Hell Chestplate", ITEM_RARITY.Common, 		new double[] {0.0, 	-5,  3, 0, 0,   3}),	
			new RarityItem(new ItemStack(Material.GOLDEN_CHESTPLATE), ChatColor.DARK_RED+"Hell Chestplate", ITEM_RARITY.Uncommon, 		new double[] {0.00, -4,  5, 0, 0,   3}),	
			new RarityItem(new ItemStack(Material.CHAINMAIL_CHESTPLATE), ChatColor.DARK_RED+"Hell Chestplate", ITEM_RARITY.Rare, 		new double[] {0.00, -3,  5, 1, 0,   3}),	
			new RarityItem(new ItemStack(Material.IRON_CHESTPLATE), ChatColor.DARK_RED+"Hell Chestplate", ITEM_RARITY.Epic, 			new double[] {0.00, -2,  5, 1, 0,   3}),	
			new RarityItem(new ItemStack(Material.DIAMOND_CHESTPLATE), ChatColor.DARK_RED+"Hell Chestplate", ITEM_RARITY.Mythic, 		new double[] {0.00, -1,  7, 1, 0.1, 4}),	
			new RarityItem(new ItemStack(Material.NETHERITE_CHESTPLATE), ChatColor.DARK_RED+"Hell Chestplate", ITEM_RARITY.Legendary, 	new double[] {0.00, 0,  8, 2, 0.6,   5}),	
		};
	
	private RarityItem[] _hellHelmet = 
		{
			new RarityItem(new ItemStack(Material.LEATHER_HELMET), ChatColor.DARK_RED+"Hell Helmet", ITEM_RARITY.Common, 		new double[] {0.0, 	-4,  1, 0, 0, 2}),	
			new RarityItem(new ItemStack(Material.GOLDEN_HELMET), ChatColor.DARK_RED+"Hell Helmet", ITEM_RARITY.Uncommon, 		new double[] {0.00, -4,  1, 0, 0, 2}),	
			new RarityItem(new ItemStack(Material.CHAINMAIL_HELMET), ChatColor.DARK_RED+"Hell Helmet", ITEM_RARITY.Rare, 		new double[] {0.00, -3,  1, 0, 0, 2  }),	
			new RarityItem(new ItemStack(Material.IRON_HELMET), ChatColor.DARK_RED+"Hell Helmet", ITEM_RARITY.Epic, 			new double[] {0.00, -3,  1, 0, 0, 2}),	
			new RarityItem(new ItemStack(Material.DIAMOND_HELMET), ChatColor.DARK_RED+"Hell Helmet", ITEM_RARITY.Mythic, 		new double[] {0.00, -1,  2, 1, 0, 2}),	
			new RarityItem(new ItemStack(Material.NETHERITE_HELMET), ChatColor.DARK_RED+"Hell Helmet", ITEM_RARITY.Legendary, 	new double[] {0.01,  0,  2, 2, 0, 3}),	
		};
	
	private RarityItem[] _hellArrows = 
		{
			new RarityItem(new ItemStack(Material.ARROW), ChatColor.DARK_RED+"Hell Arrow", ITEM_RARITY.Common, 		new double[] {1}),	
			new RarityItem(new ItemStack(Material.ARROW), ChatColor.DARK_RED+"Hell Arrow", ITEM_RARITY.Uncommon, 	new double[] {2}),	
			new RarityItem(new ItemStack(Material.ARROW), ChatColor.DARK_RED+"Hell Arrow", ITEM_RARITY.Rare, 		new double[] {3}),	
			new RarityItem(new ItemStack(Material.ARROW), ChatColor.DARK_RED+"Hell Arrow", ITEM_RARITY.Epic, 		new double[] {5}),	
			new RarityItem(new ItemStack(Material.ARROW), ChatColor.DARK_RED+"Hell Arrow", ITEM_RARITY.Mythic, 		new double[] {9}),	
			new RarityItem(new ItemStack(Material.ARROW), ChatColor.DARK_RED+"Hell Arrow", ITEM_RARITY.Legendary, 	new double[] {20}),	
		};
	
	

	public ItemStack RemoveArmorData(ItemStack stack)
	{
		String[] hellEquipment = {
			    _PD_HELL_BOOTS,
			    _PD_HELL_LEGGINS,
			    _PD_HELL_CHESTPLATE,
			    _PD_HELL_HELMET,
			    _PD_HELL_TIER,

			};
		
		for(String pd : hellEquipment)
		{
			stack = Metods._ins.removePersistenData(stack, pd);
		}
		
		return stack;
	}
	public ItemStack CreateHellBoots(ITEM_RARITY rarity)
	{
		
		RarityItem rarityItem = null;
		
		for(RarityItem boots : _hellBoots)
		{
			if(boots.Rarity == rarity) {rarityItem = boots;}
		}
		
		ItemStack stack = rarityItem.GetItemStack();
		
		String[] lores = 
			{ 
				" ",
				"&9Able to walk on &cLava",
				" ",
				"&9Reduce &5Fear &9Build up by &2"+rarityItem.Values[5],
				" ",
				"&7'Some say these boots were created by",
				"&7a mad wizard in an attempt to control the power",
				"&7of the volcanoes. Others say he just wanted",
				"&7a comfy pair of slippers'",
				
				};

		Metods._ins.SetLores(stack, lores, false);

		ItemMeta meta = stack.getItemMeta();
 
	
		if(rarityItem.Values[0] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(), "generic.movementSpeed", rarityItem.Values[0],AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));
		if(rarityItem.Values[1] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID.randomUUID(), "generic.health", rarityItem.Values[1], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));
		if(rarityItem.Values[2] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.armor", rarityItem.Values[2], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));
		if(rarityItem.Values[3] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(), "generic.armor_toughness", rarityItem.Values[3], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));

		stack.setItemMeta(meta);

		Metods._ins.setPersistenData(stack, _PD_HELL_BOOTS, PersistentDataType.INTEGER, 1);
		Metods._ins.setPersistenData(stack, _PD_HELL_TIER, PersistentDataType.STRING, rarity.toString());
		Metods._ins.setPersistenData(stack, _PD_HELL_FEAR_REDUCE, PersistentDataType.DOUBLE, rarityItem.Values[5]);

		return stack;
	}
	
	public ItemStack CreateHellLeggins(ITEM_RARITY rarity)
	{
		
		RarityItem rarityItem = null;
		
		for(RarityItem leggins : _hellLeggins)
		{
			if(leggins.Rarity == rarity) {rarityItem = leggins;}
		}
		
		ItemStack stack = rarityItem.GetItemStack();
		
		String[] lores = 
			{ 
				" ",
				"&9Half &cFire &9Damage Taken",
				" ",
				"&9Reduce &5Fear &9Build up by &2"+rarityItem.Values[5],
				" ",
				"&7'Looking for leggings that can withstand",
				"&7the heat?. Look no further, the Hell",
				"&7Leggings have got you covered...",
				"&7half-covered, that is'",
				
				};

		Metods._ins.SetLores(stack, lores, false);

		ItemMeta meta = stack.getItemMeta();
 
	
		if(rarityItem.Values[0] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(), "generic.movementSpeed", rarityItem.Values[0],AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.LEGS));
		if(rarityItem.Values[1] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID.randomUUID(), "generic.health", rarityItem.Values[1], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.LEGS));
		if(rarityItem.Values[2] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.armor", rarityItem.Values[2], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.LEGS));
		if(rarityItem.Values[3] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(), "generic.armor_toughness", rarityItem.Values[3], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.LEGS));

		stack.setItemMeta(meta);

		Metods._ins.setPersistenData(stack, _PD_HELL_LEGGINS, PersistentDataType.INTEGER, 1);
		Metods._ins.setPersistenData(stack, _PD_HELL_TIER, PersistentDataType.STRING, rarity.toString());
		Metods._ins.setPersistenData(stack, _PD_HELL_FEAR_REDUCE, PersistentDataType.DOUBLE, rarityItem.Values[5]);

		return stack;
	}
	
	public ItemStack CreateHellChestplate(ITEM_RARITY rarity)
	{
		
		RarityItem rarityItem = null;
		
		for(RarityItem chest : _hellChest)
		{
			if(chest.Rarity == rarity) {rarityItem = chest;}
		}
		
		ItemStack stack = rarityItem.GetItemStack();
		
		String[] lores = 
			{ 
				" ",
				"&9Half &cFire &9Damage Taken.",
				" ",
				"&9If &4Hell Leggins &9Equip too",
				"&9then &5immunity &cFire &9damage",
				" ",
				"&9Reduce &5Fear &9Build up by &2"+rarityItem.Values[5],
				" ",
				"&7'When it comes to fire protection,",
				"&7the Hell Chestplate is worth its weight in,",
				"&7well, not gold, but it will save you ",
				"&7from getting burned!'",
				
				};

		Metods._ins.SetLores(stack, lores, false);

		ItemMeta meta = stack.getItemMeta();
 
	
		if(rarityItem.Values[0] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(), "generic.movementSpeed", rarityItem.Values[0],AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST));
		if(rarityItem.Values[1] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID.randomUUID(), "generic.health", rarityItem.Values[1], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST));
		if(rarityItem.Values[2] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.armor", rarityItem.Values[2], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST));
		if(rarityItem.Values[3] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(), "generic.armor_toughness", rarityItem.Values[3], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST));
		if(rarityItem.Values[4] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.randomUUID(), "generic.armor_knocback", rarityItem.Values[4], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST));

		stack.setItemMeta(meta);

		Metods._ins.setPersistenData(stack, _PD_HELL_CHESTPLATE, PersistentDataType.INTEGER, 1);
		Metods._ins.setPersistenData(stack, _PD_HELL_TIER, PersistentDataType.STRING, rarity.toString());
		Metods._ins.setPersistenData(stack, _PD_HELL_FEAR_REDUCE, PersistentDataType.DOUBLE, rarityItem.Values[5]);

		return stack;
	}
	
	
	public ItemStack CreateHellHelmet(ITEM_RARITY rarity)
	{
		
		RarityItem rarityItem = null;
		
		for(RarityItem helmet : _hellHelmet)
		{
			if(helmet.Rarity == rarity) {rarityItem = helmet;}
		}
		
		ItemStack stack = rarityItem.GetItemStack();
		
		String[] lores = 
			{ 
				" ",
				"&9Gives &aNight Vision",
				//" ",
				//"&4Hells &8Darkness &9Doesn't effect on you",
				" ",
				"&9Reduce &5Fear &9Build up by &2"+rarityItem.Values[5],
				" ",
				"&7'No more fumbling around in",
				"&7the dark with the Hell Helmet.",
				"&7It's like a built-in night light!",
				
				};

		Metods._ins.SetLores(stack, lores, false);

		ItemMeta meta = stack.getItemMeta();
 
	
		if(rarityItem.Values[0] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(), "generic.movementSpeed", rarityItem.Values[0],AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD));
		if(rarityItem.Values[1] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID.randomUUID(), "generic.health", rarityItem.Values[1], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD));
		if(rarityItem.Values[2] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.armor", rarityItem.Values[2], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD));
		if(rarityItem.Values[3] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(), "generic.armor_toughness", rarityItem.Values[3], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD));
		if(rarityItem.Values[4] != 0.00) meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.randomUUID(), "generic.armor_toughness", rarityItem.Values[4], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD));

		stack.setItemMeta(meta);

		Metods._ins.setPersistenData(stack, _PD_HELL_HELMET, PersistentDataType.INTEGER, 1);
		Metods._ins.setPersistenData(stack, _PD_HELL_TIER, PersistentDataType.STRING, rarity.toString());
		Metods._ins.setPersistenData(stack, _PD_HELL_FEAR_REDUCE, PersistentDataType.DOUBLE, rarityItem.Values[5]);

		return stack;
	}
	
	
	public ItemStack CreateHellArrow(ITEM_RARITY rarity)
	{
		
		RarityItem rarityItem = null;
		
		for(RarityItem arrow : _hellArrows)
		{
			if(arrow.Rarity == rarity) {rarityItem = arrow;}
		}
		
		ItemStack stack = rarityItem.GetItemStack();
		
		String[] lores = 
			{ 
				" ",
				"&9Explodes Ores in Blast area of &2"+rarityItem.Values[0],
				"&9Explode Ores Rolls with Fortune IV",
				"&9Ores comes to your inv",
				" ",
				"&9Infinity enchant doesn't work for this",
				" ",
				"&7'Blast your way to riches with",
				"&7the Hell Arrow. It's like a miniature",
				"&7mining explosion in every shot.",
				
				};
		
		if(rarity == ITEM_RARITY.Common) lores[2] 		= "&9Explode Oress";
		if(rarity == ITEM_RARITY.Uncommon) lores[2] 	= "&9Explode Ores Rolls with Fortune I";
		if(rarity == ITEM_RARITY.Epic) lores[2] 		= "&9Explode Ores Rolls with Fortune II";
		if(rarity == ITEM_RARITY.Mythic) lores[2] 		= "&9Explode Ores Rolls with Fortune III";
		if(rarity == ITEM_RARITY.Legendary) lores[2]	= "&9Explode Ores Rolls with Fortune V";
		
		Metods._ins.SetLores(stack, lores, false);

		ItemMeta meta = stack.getItemMeta();
 
		stack.setItemMeta(meta);

		Metods._ins.setPersistenData(stack, _PD_HELL_ARROW, PersistentDataType.INTEGER, (int)rarityItem.Values[0]);
		Metods._ins.setPersistenData(stack, _PD_HELL_TIER, PersistentDataType.STRING, rarity.toString());

		return stack;
	}
	
	public ItemStack CreateHellTorch()
	{
		
		ItemStack stack = new ItemStack(Material.TORCH);
		
		Metods.setDisplayName(stack, ChatColor.DARK_RED+"Hell Torch");
		
		String[] lores = 
			{ 
				" ",
				"&9Doesn't get knocked",
				"&9off by &8Darkness,",
				"&9but can be used only once",
				" ",
				"&7'Light up the darkness with the",
				"&7Hell Torch. It's like a little piece",
				"&7of Hell, in a good way.",
				
				};

		Metods._ins.SetLores(stack, lores, false);

		ItemMeta meta = stack.getItemMeta();
 
		stack.setItemMeta(meta);

		Metods._ins.setPersistenData(stack, _PD_HELL_TORCH, PersistentDataType.INTEGER, 1);
		return stack;
	}
	
	public ItemStack CreateHellStoneShield()
	{
		
		ItemStack stack = new ItemStack(Material.TORCH);
		
		Metods.setDisplayName(stack, ChatColor.DARK_RED+"Hell Stone Shield");
		
		String[] lores = 
			{ 
				" ",
				"&9Creates Stone Wall Front of You",
				" ",
				"&7'Light up the darkness with the",
				"&7Hell Torch. It's like a little piece",
				"&7of Hell, in a good way.",
				
				};

		Metods._ins.SetLores(stack, lores, false);

		ItemMeta meta = stack.getItemMeta();
 
		stack.setItemMeta(meta);

		Metods._ins.setPersistenData(stack, _PD_HELL_TORCH, PersistentDataType.INTEGER, 1);
		return stack;
	}
	
	
	
	
	///VOID
	public ItemStack CreateVOIDBoots()
	{
		RarityItem hellBoots = _hellBoots[_hellBoots.length-1];
				
		RarityItem rarityItem = new RarityItem(new ItemStack(Material.NETHERITE_BOOTS), "&5Boots", ITEM_RARITY.Void, new double[] {});
		
		ItemStack stack = rarityItem.GetItemStack();
		
		double fearReduce = hellBoots.Values[5]+2;
		String[] lores = 
			{ 
				" ",
				"&9Able to walk on &cLava",
				" ",
				"&9Reduce &5Fear &9Build up by &2"+fearReduce,
				" ",
				//"&9Fixed armor, thoughness and little bit more speed",
				"&0Some health and a little bit more speed.",
				" ",
				"&7'Some say these boots were created by",
				"&7a mad wizard in an attempt to control the power",
				"&7of the volcanoes. Others say he just wanted",
				"&7a comfy pair of slippers'",
				
				};

		Metods._ins.SetLores(stack, lores, false);

		ItemMeta meta = stack.getItemMeta();
 
	
		meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(), "generic.movementSpeed", hellBoots.Values[0]+0.01,AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));
		meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID.randomUUID(), "generic.health", 1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));
		meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.armor", hellBoots.Values[2], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));
		meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(), "generic.armor_toughness", hellBoots.Values[3], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));
		meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.randomUUID(), "generic.knockback_res", 0.2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));

		stack.setItemMeta(meta);

		Metods._ins.setPersistenData(stack, _PD_HELL_BOOTS, PersistentDataType.INTEGER, 2);
		Metods._ins.setPersistenData(stack, _PD_HELL_TIER, PersistentDataType.STRING, rarityItem.Rarity.toString());
		Metods._ins.setPersistenData(stack, _PD_HELL_FEAR_REDUCE, PersistentDataType.DOUBLE, fearReduce);

		return stack;
	}
	
	public ItemStack CreateVOIDLeggins()
	{
		RarityItem helllegg = _hellLeggins[_hellLeggins.length-1];
		
		RarityItem rarityItem = new RarityItem(new ItemStack(Material.NETHERITE_LEGGINGS), "&5Leggings", ITEM_RARITY.Void, new double[] {});
		
		ItemStack stack = rarityItem.GetItemStack();
		
		double fearReduce = helllegg.Values[5]+1;
		String[] lores = 
			{ 
				" ",
				"&9Half &cFire &9Damage Taken",
				" ",
				"&9Reduce &5Fear &9Build up by &2"+fearReduce,
				" ",
				"&0Some health and a little bit more speed.",
				" ",
				"&7'Looking for leggings that can withstand",
				"&7the heat?. Look no further, the Void",
				"&7Leggings have got you covered...",
				"&7half-covered, that is'",
				
				};

		Metods._ins.SetLores(stack, lores, false);

		ItemMeta meta = stack.getItemMeta();
 
		// {0.01,  0,  6, 3, 0, 3}),	
		meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(), "generic.movementSpeed", 
				helllegg.Values[0]+0.01,AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.LEGS));
		
		meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID.randomUUID(), "generic.health",
				2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.LEGS));
		
		meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.armor", 
				helllegg.Values[2], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.LEGS));
		
		meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(), "generic.armor_toughness", 
				helllegg.Values[3], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.LEGS));
		
		meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.randomUUID(), "generic.knockback_res",
				0.2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.LEGS));
		stack.setItemMeta(meta);

		Metods._ins.setPersistenData(stack, _PD_HELL_LEGGINS, PersistentDataType.INTEGER, 2);
		Metods._ins.setPersistenData(stack, _PD_HELL_TIER, PersistentDataType.STRING, rarityItem.Rarity.toString());
		Metods._ins.setPersistenData(stack, _PD_HELL_FEAR_REDUCE, PersistentDataType.DOUBLE, fearReduce);

		return stack;
	}
	

	public ItemStack CreateVOIDChestplate()
	{
		
		RarityItem helllchess = _hellChest[_hellChest.length-1];
		
		RarityItem rarityItem = new RarityItem(new ItemStack(Material.ELYTRA), "&5Elytra", ITEM_RARITY.Void, new double[] {});
		
		ItemStack stack = rarityItem.GetItemStack();
		
		double fearReduce = helllchess.Values[5]+1;
		String[] lores = 
			{ 
				" ",
				"&9Half &cFire &9Damage Taken.",
				" ",
				"&9If &0Void &4Leggins &9Equip too",
				"&9then &5immunity &cFire &9damage",
				" ",
				"&9Reduce &5Fear &9Build up by &2"+fearReduce,
				" ",
				"&0Some health, armor and flight",
				" ",
				"&7'When it comes to fire protection,",
				"&7the Void Elytra is worth its weight in,",
				"&7well, not gold, but it will save you ",
				"&7from getting burned!'",
				
				};

		Metods._ins.SetLores(stack, lores, false);

		ItemMeta meta = stack.getItemMeta();
 
		//{0.00, 0,  8, 2, 0.6,   5}
//		meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(), "generic.movementSpeed", 
//				helllchess.Values[0]+0.01,AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST));
		
		meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID.randomUUID(), "generic.health",
				2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST));
		
		meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.armor", 
				helllchess.Values[2]+1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST));
		
		meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(), "generic.armor_toughness", 
				helllchess.Values[3]+1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST));
		
		meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.randomUUID(), "generic.knockback_res",
				0.2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST));
		stack.setItemMeta(meta);

		

		Metods._ins.setPersistenData(stack, _PD_HELL_CHESTPLATE, PersistentDataType.INTEGER, 2);
		Metods._ins.setPersistenData(stack, _PD_HELL_TIER, PersistentDataType.STRING, rarityItem.Rarity.toString());
		Metods._ins.setPersistenData(stack, _PD_HELL_FEAR_REDUCE, PersistentDataType.DOUBLE, fearReduce);

		return stack;
	}
	
	public ItemStack CreateVOIDHelmet()
	{
		
		RarityItem hellHelmet = _hellHelmet[_hellHelmet.length-1];
		
		RarityItem rarityItem = new RarityItem(new ItemStack(Material.NETHERITE_HELMET), "&5Helmet", ITEM_RARITY.Void, new double[] {});
		
		ItemStack stack = rarityItem.GetItemStack();
		
		double fearReduce = hellHelmet.Values[5]+1;
		
		String[] lores = 
			{ 
				" ",
				"&9Gives &aNight Vision",
				//" ",
				//"&4Hells &8Darkness &9Doesn't effect on you",
				" ",
				"&9Reduce &5Fear &9Build up by &2"+fearReduce,
				" ",
				"&0Some health and a little bit more speed",
				" ",
				"&7'No more fumbling around in",
				"&7the dark with the Void Helmet.",
				"&7It's like a built-in night light!",
				
				};

		Metods._ins.SetLores(stack, lores, false);

		ItemMeta meta = stack.getItemMeta();
 
		//{0.01,  0,  2, 2, 0, 3}),	
		meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(), "generic.movementSpeed", 
				hellHelmet.Values[0]+0.01,AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD));

		meta.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, new AttributeModifier(UUID.randomUUID(), "generic.health",
				1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD));

		meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.armor", 
				hellHelmet.Values[2]+1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD));

		meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(), "generic.armor_toughness", 
				hellHelmet.Values[3]+1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD));

		meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.randomUUID(), "generic.knockback_res",
				0.2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD));
		stack.setItemMeta(meta);


		Metods._ins.setPersistenData(stack, _PD_HELL_HELMET, PersistentDataType.INTEGER, 2);
		Metods._ins.setPersistenData(stack, _PD_HELL_TIER, PersistentDataType.STRING, rarityItem.Rarity.toString());
		Metods._ins.setPersistenData(stack, _PD_HELL_FEAR_REDUCE, PersistentDataType.DOUBLE, fearReduce);

		return stack;
	}
	
	
	
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		RemoveLastLocation(e.getPlayer());

		_cds.removeCooldown(e.getPlayer().getUniqueId().toString() + _CD_IN_LAVA);
		_cds.removeCooldown(e.getPlayer().getUniqueId().toString() + _CD_ON_LAVA);
	}

	private void RemoveLastLocation(Player player)
	{
		if (_lastLocations.containsKey(player.getUniqueId()))
			_lastLocations.remove(player.getUniqueId());
	}
	

	@EventHandler(priority = EventPriority.HIGH)
	public void OnEntityDamage(EntityDamageEvent e)
	{
		Entity entity = e.getEntity();
		
		if(!(entity instanceof Player)) return;
		
		Player player = (Player) entity;
		
		DamageCause cause = e.getCause();
		
		
		if(cause != DamageCause.FIRE  && cause != DamageCause.FIRE_TICK && cause != DamageCause.LAVA) return;
		
		if (IsHellLeggins(player.getInventory().getLeggings()))
		{
			if(IsHellChestplate(player.getInventory().getChestplate())) 
			{
				player.setFireTicks(0);
				e.setCancelled(true); 
				return;
			}
			
			e.setDamage(e.getDamage() * 0.5);
			return;
		}
		
		if (IsHellChestplate(player.getInventory().getChestplate()))
		{

			e.setDamage(e.getDamage() * 0.5);
			
			return;
		}
		

	}
	
	@EventHandler
	public void OnProtectileHit(ProjectileHitEvent e)
	{
		Projectile projectile = e.getEntity();
		
		if(projectile instanceof Arrow)
		{
			if(e.getHitBlock() == null) return;
			
			Arrow arrow = (Arrow)projectile;
			
			if(!(arrow.getShooter() instanceof Player)) return;
			
			Integer radius = GetHellArrow(arrow);
			
			if(radius <= 0) return;
			
			ExplodeHellArrow((Player)arrow.getShooter(),GetHellArrowTier(arrow),e.getHitBlock(), radius);
			arrow.remove();
				
		}
	}
	
	@EventHandler
    public void OnPlayerShoot(EntityShootBowEvent e) 
	{
		//System.out.println("Player shoots");
        Entity entity = e.getEntity();
        
        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player)e.getEntity();
        Arrow arrow = (Arrow) e.getProjectile();
        
        ItemStack arrowItemStack = null;
        for(ItemStack stack : player.getInventory().getContents())
        {
        	if(stack == null || stack.getType() == Material.AIR) continue;
        	

        	if(stack.getType() != Material.ARROW) continue;
        	
        	arrowItemStack = stack;
        	break;
        	
        }

        if(arrowItemStack == null) return;
        
        SetHellArrow(arrowItemStack,arrow, player,player.getGameMode() != GameMode.CREATIVE);
        //e.getProjectile().remove();

    }

	@EventHandler
	public void OnPlayerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();

		Location lastLocation = _lastLocations.get(player.getUniqueId());
		Location currentLocation = player.getLocation();
		
		if (IsHellHelmet(player.getInventory().getHelmet())) // night vision effect
		{ 
		     PotionEffect existingEffect = player.getPotionEffect(PotionEffectType.NIGHT_VISION);

		     if (existingEffect != null && existingEffect.getDuration() > 300) 
		     {
		    	 
		     }
		     else
		     {
		    	 PotionEffect nightVision = new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 0, false);
			     player.addPotionEffect(nightVision);
		     }
		       
		     
		}
		

		if (!(lastLocation == null || lastLocation.getWorld() != currentLocation.getWorld()
				|| currentLocation.distance(lastLocation) >= 1))
		{
			return;
		}
			

		if (!IsHellBoots(player.getInventory().getBoots()))
		{
			RemoveLastLocation(player);
			return;
		}

		Block block = currentLocation.getBlock().getRelative(BlockFace.DOWN);
		
		if(block.getType() != Material.LAVA && block.getType() != Material.STONE && block.getType() != Material.AIR)
		{
			//System.out.println("noni");
			_cds.removeCooldown(player.getUniqueId().toString() + _CD_ON_LAVA);
		}
		
		if (currentLocation.getBlock().getType() == Material.LAVA)
		{
			return;
		}
		
		//CHECKING FALLING 
		if(player.getVelocity().getY() < -0.5 && _cds.isCooldownReady(player.getUniqueId().toString()+_CD_CREATED_SAFE_PLAT))
		{

			for(int i = 0; i < 6; i++)
			{
				Block b = currentLocation.clone().add(0,-1*i,0).getBlock();
				
				if(b.getType() != Material.LAVA) continue;
				
				if(!CreateStonePlatform(player, b,_hellBootsRadius)) continue;
				
				_cds.setCooldownInSeconds(player.getUniqueId().toString()+_CD_CREATED_SAFE_PLAT, 2);
				return;
			}
		}
		
		if(CreateStonePlatform(player, block,_hellBootsRadius))
		{
			_lastLocations.put(player.getUniqueId(), currentLocation);
		}
	}
	
	private Block SetBlockType(Player player,Block block, Material mat)
	{
	    BlockPlaceEvent event = new BlockPlaceEvent(block, block.getState(), block.getRelative(BlockFace.DOWN), new ItemStack(mat), player, true, EquipmentSlot.HAND);
	    //new BlockPl
	    Bukkit.getServer().getPluginManager().callEvent(event);
	    
	    if(event.isCancelled()) return block;
	    
	    block.setType(mat);
	    
		return block;
	}
	
	private boolean CreateStonePlatform(Player player,Block block, int radius)
	{
		if (block.getType() == Material.LAVA || !_cds.isCooldownReady(player.getUniqueId().toString() + _CD_ON_LAVA))
		{
			//System.out.println("set lava");
			
			Block center = block.getRelative(player.getFacing(), 1);

			_cds.setCooldownInSeconds(player.getUniqueId().toString() + _CD_ON_LAVA, 3);
			int x = center.getX();
			int y = center.getY();
			int z = center.getZ();
			int r = radius;
			
			for (int i = x - r; i <= x + r; i++)
			{
				for (int k = z - r; k <= z + r; k++)
				{
					Block b = center.getWorld().getBlockAt(i, y, k);
					
					if (b.getType() != Material.LAVA) continue;
	
					if(!IsInCircle(i, y, k, x, y, z, r)) continue;
					
					SetBlockType(player, b, Material.STONE);
				}

			}
			return true;
		}
		
		return false;
	}
	private boolean IsInCircle(int x, int y, int z, int cx, int cy, int cz, int r)
	{
		return (x - cx) * (x - cx) + (y - cy) * (y - cy) + (z - cz) * (z - cz) <= r * r;
	}
	
	public void ExplodeHellArrow(Player player, ITEM_RARITY rarity,Block block, int radius)
	{
		//if(rarity == null) rarity = ITEM_RARITY.Uncommon;
		
		
		new BukkitRunnable() {
			
			@Override
			public void run()
			{
				LinkedList<Location> locs = ImusUtilities.CreateSphere(block.getLocation(), radius,null, ImusAPI.Ores);
				
				new BukkitRunnable() {
					
					ITEM_RARITY rary = rarity;
					@Override
					public void run()
					{
						if(rary == null) rary = ITEM_RARITY.Uncommon;
						
						int FortuneLevel = rarity.GetIndex();
						
						for(Location loc : locs)
						{
							Block b = loc.getBlock();
							
							if(b.getType() == Material.OBSIDIAN) continue;
//							
//							if(!_ores.contains(b.getType())) continue;
							//if(IsPlayerDebris(b)) continue;
							
							BlockBreakEvent bBreakEvent = new BlockBreakEvent(b, player);
							Bukkit.getServer().getPluginManager().callEvent(bBreakEvent);
							
							if(bBreakEvent.isCancelled()) continue;
							
							Collection<ItemStack> stacks = b.getDrops();
							
							if(b.getType() == Material.ANCIENT_DEBRIS)
							{
								stacks = new ArrayList<ItemStack>();
								stacks.add(new ItemStack(Material.ANCIENT_DEBRIS));
							}
							
							for(ItemStack stack : stacks)
							{
								Metods._ins.InventoryAddItemOrDrop(Metods._ins.FortuneSimulation(stack, FortuneLevel), player);
							}
							
							
							
							b.setType(Material.OBSIDIAN);
						}
					}
				}.runTask(DontLoseItems.Instance);
			}
		}.runTaskAsynchronously(DontLoseItems.Instance);
		
		
		
		
	}
	
	
	
	
	public boolean IsHellBoots(ItemStack stack)
	{
		return Metods._ins.getPersistenData(stack, _PD_HELL_BOOTS, PersistentDataType.INTEGER) != null;
	}
	
	public boolean IsHellLeggins(ItemStack stack)
	{
		return Metods._ins.getPersistenData(stack, _PD_HELL_LEGGINS, PersistentDataType.INTEGER) != null;
	}
	
	public boolean IsHellChestplate(ItemStack stack)
	{
		return Metods._ins.getPersistenData(stack, _PD_HELL_CHESTPLATE, PersistentDataType.INTEGER) != null;
	}
	
	public boolean IsHellHelmet(ItemStack stack)
	{
		return Metods._ins.getPersistenData(stack, _PD_HELL_HELMET, PersistentDataType.INTEGER) != null;
	}
	
	
	
	public boolean IsVoidBoots(ItemStack stack)
	{
		Integer i = Metods._ins.getPersistenData(stack, _PD_HELL_BOOTS, PersistentDataType.INTEGER);
		
		if(i != null && i == 2) return true;
		
		return false;
	}
	
	public boolean IsVoidLeggins(ItemStack stack)
	{
		Integer i = Metods._ins.getPersistenData(stack, _PD_HELL_LEGGINS, PersistentDataType.INTEGER);
		
		if(i != null && i == 2) return true;
		
		return false;
	}
	
	public boolean IsVoidChestplate(ItemStack stack)
	{
		Integer i = Metods._ins.getPersistenData(stack, _PD_HELL_CHESTPLATE, PersistentDataType.INTEGER);
		if(i != null && i == 2) return true;
		
		return false;
	}
	
	public boolean IsVoidHelmet(ItemStack stack)
	{
		Integer i = Metods._ins.getPersistenData(stack, _PD_HELL_HELMET, PersistentDataType.INTEGER);
		if(i != null && i == 2) return true;
		
		return false;
	}
	
	public boolean IsHellItem(ItemStack stack) 
	{
	    return IsHellBoots(stack) || IsHellLeggins(stack) || IsHellChestplate(stack) || IsHellHelmet(stack);
	}
	
	public boolean IsVoidItem(ItemStack stack) 
	{
	    return IsVoidBoots(stack) || IsVoidLeggins(stack) || IsVoidChestplate(stack) || IsVoidHelmet(stack);
	}
	
	public boolean IsVoidOrHellItem(ItemStack stack) 
	{
	    return IsVoidItem(stack) || IsHellItem(stack);
	}
	
	@EventHandler
	public void Anvil(PrepareAnvilEvent e)
	{
		AnvilInventory inv = e.getInventory();
		ItemStack stack1 = inv.getItem(0);
		ItemStack stack2 = inv.getItem(1);
		if(stack1 == null || stack2 == null) return;
			
		if(GetRarity(stack1) != ITEM_RARITY.Legendary) return;
		
		ItemStack result = null;
		
		//ARMORS
		if(IsHellHelmet(stack1) && Manager_LegendaryUpgrades.Instance.IsUpgradeHellHelmet(stack2))
		{
			result = CreateVOIDHelmet();
			Metods.CloneEnchantments(stack1, result);
			e.setResult(result);
			return;
		}
		
		if(IsHellChestplate(stack1) && Manager_LegendaryUpgrades.Instance.IsUpgradeHellChest(stack2))
		{
			result = CreateVOIDChestplate();
			Metods.CloneEnchantments(stack1, result);
			e.setResult(result);
			return;
		}
		
		if(IsHellLeggins(stack1) && Manager_LegendaryUpgrades.Instance.IsUpgradeHellLegg(stack2))
		{
			result = CreateVOIDLeggins();
			Metods.CloneEnchantments(stack1, result);
			e.setResult(result);
			return;
		}
		
		if(IsHellBoots(stack1) && Manager_LegendaryUpgrades.Instance.IsUpgradeHellBoots(stack2))
		{
			result = CreateVOIDBoots();
			Metods.CloneEnchantments(stack1, result);
			e.setResult(result);
			return;
		}

	}
	
	@EventHandler
	public void AnvilClick(InventoryClickEvent e)
	{
		
		if(e.isCancelled()) return;
		
		if(!(e.getInventory() instanceof AnvilInventory)) return;
		
		if(e.getSlotType() != SlotType.RESULT) return;
		
		AnvilInventory inv = (AnvilInventory)e.getInventory();
				
		if(inv.getItem(0) == null || inv.getItem(1) == null) return;
		
		boolean found = false;
		
		if(IsVoidHelmet(e.getCurrentItem())) 		found = true;
		if(IsVoidChestplate(e.getCurrentItem())) 	found = true;
		if(IsVoidLeggins(e.getCurrentItem())) 		found = true;
		if(IsVoidBoots(e.getCurrentItem())) 		found = true;

		if(!found) return;

		 GiveVoidArmor((Player)e.getWhoClicked(), inv.getItem(0), e.getCurrentItem());

		inv.setItem(0, new ItemStack(Material.AIR));
		inv.setItem(1, new ItemStack(Material.AIR));
	}
	
	private void GiveVoidArmor(Player player, ItemStack baseStack,ItemStack stack)
	{
		Metods.CloneEnchantments(baseStack, stack);
		Metods._ins.InventoryAddItemOrDrop(stack, player );
	}
	public boolean IsHellTorch(ItemStack stack)
	{
		return Metods._ins.getPersistenData(stack, _PD_HELL_TORCH, PersistentDataType.INTEGER) != null;
	}
	
	public ITEM_RARITY GetRarity(ItemStack stack)
	{
		return RarityItem.GetRarity(stack);
	}
	
	public double GetFearReduceAmount(ItemStack stack)
	{
		Double x = Metods._ins.getPersistenData(stack, _PD_HELL_FEAR_REDUCE, PersistentDataType.DOUBLE);
		
		if(x != null) return x;
		
		return 0;
	}
	
	public boolean IsHellArmor(ItemStack stack)
	{
		return 		IsHellHelmet(stack) 
				|| 	IsHellChestplate(stack) 
				|| 	IsHellLeggins(stack)
				|| 	IsHellBoots(stack);
	}
	
	public ItemStack IncreaseTier(ItemStack stack, int increase)
	{
		ITEM_RARITY current_rarity = GetRarity(stack);
		ITEM_RARITY now_rarity = current_rarity.GetIncreaseRarity(increase);
		ItemStack newStack = null;
		
		//if used more rapidly then should be use if else	
		if(IsHellHelmet(stack)) 	newStack = CreateHellHelmet(now_rarity);
		
		if(IsHellChestplate(stack)) newStack = CreateHellChestplate(now_rarity);
		
		if(IsHellLeggins(stack))	newStack = CreateHellLeggins(now_rarity);
		
		if(IsHellBoots(stack)) 		newStack = CreateHellBoots(now_rarity);
		
		Metods.CloneEnchantments(stack, newStack);
		return newStack;
	}
	
	public boolean IsHellArrow(ItemStack stack)
	{
		return (GetHellArrow(stack) == 0 ? false : true);
	}
	
	
	
	public int GetHellArrow(ItemStack stack)
	{
		Integer x = Metods._ins.getPersistenData(stack, _PD_HELL_ARROW, PersistentDataType.INTEGER);
		
		if(x != null) return x;
		
		return 0;
	}
	
	public void SetHellArrow(ItemStack arrow, Entity entity, Player shooter, boolean reduceStackAmount)
	{
		Integer radius = Manager_HellArmor.Instance.GetHellArrow(arrow);

        if(radius <= 0) return;
        
        if(reduceStackAmount) 
        {
        	int extra = 0;
        	//System.out.println("item: "+shooter.getInventory().getItemInMainHand());
        	if(shooter != null && shooter.getInventory().getItemInMainHand().getType() == Material.CROSSBOW)
        	{
        		extra++;
        	}
        	arrow.setAmount(arrow.getAmount()-1+extra);
        }
        
        Metods._ins.setPersistenData(entity, _PD_HELL_ARROW, PersistentDataType.INTEGER, radius);
        Metods._ins.setPersistenData(entity, _PD_HELL_TIER, PersistentDataType.STRING, GetRarity(arrow).toString());
	}
	
	public Integer GetHellArrow(Entity entity)
	{
		Integer x = Metods._ins.getPersistenData(entity, _PD_HELL_ARROW, PersistentDataType.INTEGER);
		
		if(x == null || x <= 0) return 0;
		
		return x;
		
	}
	
	public ITEM_RARITY GetHellArrowTier(Entity entity)
	{
		String x = Metods._ins.getPersistenData(entity, _PD_HELL_TIER, PersistentDataType.STRING);
		
		if(x == null ) return null;
		
		return ITEM_RARITY.valueOf(x);
		
	}
	
	@SuppressWarnings("unused")
	private boolean IsNether(World world)
	{
		if(world == null) return false;
		
		return world == _nether;
	}

	@SuppressWarnings("unused")
	private boolean IsNether(Entity entity)
	{
		if(entity == null) return false;
		
		return entity.getWorld() == _nether;
	}

	@SuppressWarnings("unused")
	private boolean IsNether(Block block)
	{
		if(block == null) return false;
		
		return block.getWorld() == _nether;
	}

	@SuppressWarnings("unused")
	private boolean IsNether(Location loc)
	{
		if(loc == null) return false;
		
		return loc.getWorld() == _nether;
	}
	
	
}
