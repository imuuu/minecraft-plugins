package imu.DontLoseItems.Events;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import com.magmaguy.betterstructures.api.ChestFillEvent;

import imu.DontLoseItems.Enums.DIFFICULT;
import imu.DontLoseItems.Enums.ITEM_RARITY;
import imu.DontLoseItems.Managers.Manager_Difficult;
import imu.DontLoseItems.Managers.Manager_HellArmor;
import imu.DontLoseItems.Managers.Manager_HellTools;
import imu.DontLoseItems.main.DontLoseItems;
import imu.iAPI.LootTables.ImusLootTable;
import imu.iAPI.Other.ConfigMaker;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ItemUtils;

public class ChestLootEvents implements Listener
{
	private static final double BETTER_STRUCTURE_LOOT_NERF = 0.35f; // 0-1
	public static ChestLootEvents Instance;
	private Random _rand;

	private World _nether;

	
	//private final String META_OPENED_CHEST = "chestOpened";
	

	private ImusLootTable<ItemStack> _lootTable_hellArmor;
	private ImusLootTable<ItemStack> _lootTable_hellArrows;
	//private ImusLootTable<ItemStack> _lootTable_hellShields;
	private ImusLootTable<ItemStack> _lootTable_hellTools;
	private ImusLootTable<ItemStack> _lootTable_blocks;
	private ImusLootTable<ItemStack> _lootTable_valuables;
	private ImusLootTable<ItemStack> _lootTable_food;
	
	
	private ImusLootTable<Enchantment> _lootTable_enchants_armor;
	private ImusLootTable<Enchantment> _lootTable_enchants_tool;
	
	private ImusLootTable<Integer> _lootTable_stackMaxAmounts;
	
	private int _chestRollMaxAmount = 10;
	private int _noFearRollMaxAmount = 1;
	
	private boolean _chestDEBUG = false;
	private boolean _lootBoost = false;
	
	
	///setblock ~ ~ ~ minecraft:chest{LootTable:"chests/bastion_bridge"}
	public ChestLootEvents()
	{
		Instance = this;
		_rand = new Random();
		
		// GetSettings();

		_nether = Bukkit.getWorld("world_nether");

		// GetSettings();

		InitLootTable();
	}

	private void InitLootTable()
	{
		_lootTable_hellArmor = new ImusLootTable<>();
		_lootTable_hellArrows = new ImusLootTable<>();
		//_lootTable_hellShields = new ImusLootTable<>();
		_lootTable_blocks = new ImusLootTable<>();
		_lootTable_hellTools = new ImusLootTable<>();
		_lootTable_stackMaxAmounts = new ImusLootTable<>();
		_lootTable_valuables = new ImusLootTable<>();
		_lootTable_food = new ImusLootTable<>();
		_lootTable_enchants_armor = new ImusLootTable<>();
		_lootTable_enchants_tool = new ImusLootTable<>();
		
		_lootTable_stackMaxAmounts.add(5, 90);
		_lootTable_stackMaxAmounts.add(10, 70);
		_lootTable_stackMaxAmounts.add(13, 50);
		_lootTable_stackMaxAmounts.add(30, 15);
		_lootTable_stackMaxAmounts.add(40, 7);
		_lootTable_stackMaxAmounts.add(64, 5);
		
		
		
		int common = 100;
		int unCommon = 90;
		int rare = 80;
		int epic = 70;
		int mythic = 32;
		int lege = 14; //20
		
		_lootTable_hellArmor.add(new ItemStack(Material.GREEN_WOOL), common);
		_lootTable_hellArmor.add(new ItemStack(Material.LIME_WOOL), unCommon);
		
		_lootTable_hellArmor.add(new ItemStack(Material.LAPIS_ORE), common);
		_lootTable_hellArmor.add(new ItemStack(Material.BLACK_WOOL), unCommon);
		
		_lootTable_hellArmor.add(new ItemStack(Material.BLUE_WOOL), common);
		_lootTable_hellArmor.add(new ItemStack(Material.CYAN_WOOL), unCommon);
		
		_lootTable_hellArmor.add(new ItemStack(Material.BROWN_WOOL), common);
		_lootTable_hellArmor.add(new ItemStack(Material.CYAN_WOOL), unCommon);
		
		//_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellBoots(ITEM_RARITY.Common), common);
		//_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellBoots(ITEM_RARITY.Uncommon), unCommon);
		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellBoots(ITEM_RARITY.Rare), rare);
		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellBoots(ITEM_RARITY.Epic), epic);
		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellBoots(ITEM_RARITY.Mythic), mythic);
		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellBoots(ITEM_RARITY.Legendary), lege);
		
//		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellLeggins(ITEM_RARITY.Common), common);
//		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellLeggins(ITEM_RARITY.Uncommon), unCommon);
		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellLeggins(ITEM_RARITY.Rare), rare);
		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellLeggins(ITEM_RARITY.Epic), epic);
		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellLeggins(ITEM_RARITY.Mythic), mythic);
		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellLeggins(ITEM_RARITY.Legendary), lege);
		
//		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellChestplate(ITEM_RARITY.Common), common);
//		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellChestplate(ITEM_RARITY.Uncommon), unCommon);
		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellChestplate(ITEM_RARITY.Rare), rare);
		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellChestplate(ITEM_RARITY.Epic), epic);
		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellChestplate(ITEM_RARITY.Mythic), mythic);
		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellChestplate(ITEM_RARITY.Legendary), lege);
		
//		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellHelmet(ITEM_RARITY.Common), common);
//		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellHelmet(ITEM_RARITY.Uncommon), unCommon);
		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellHelmet(ITEM_RARITY.Rare), rare);
		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellHelmet(ITEM_RARITY.Epic), epic);
		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellHelmet(ITEM_RARITY.Mythic), mythic);
		_lootTable_hellArmor.add(Manager_HellArmor.Instance.CreateHellHelmet(ITEM_RARITY.Legendary), lege);
		
		
		_lootTable_hellArrows.add(Manager_HellArmor.Instance.CreateHellTorch(), 85);
		_lootTable_hellArrows.add(new ItemStack(Material.ARROW), 80);
		_lootTable_hellArrows.add(Manager_HellArmor.Instance.CreateHellArrow(ITEM_RARITY.Common), 60);
		_lootTable_hellArrows.add(Manager_HellArmor.Instance.CreateHellArrow(ITEM_RARITY.Uncommon), 60);
		_lootTable_hellArrows.add(Manager_HellArmor.Instance.CreateHellArrow(ITEM_RARITY.Rare), 40);
		_lootTable_hellArrows.add(Manager_HellArmor.Instance.CreateHellArrow(ITEM_RARITY.Epic), 28);
		_lootTable_hellArrows.add(Manager_HellArmor.Instance.CreateHellArrow(ITEM_RARITY.Mythic), 18);
		_lootTable_hellArrows.add(Manager_HellArmor.Instance.CreateHellArrow(ITEM_RARITY.Legendary), 8);
		
		
		//_lootTable_hellTools.add(new ItemStack(Material.SHIELD), rare);
		_lootTable_hellTools.add(Manager_HellTools.Instance.CreateHellReflectShield(ITEM_RARITY.Epic), epic);
		_lootTable_hellTools.add(Manager_HellTools.Instance.CreateHellReflectShield(ITEM_RARITY.Mythic), mythic);
		_lootTable_hellTools.add(Manager_HellTools.Instance.CreateHellReflectShield(ITEM_RARITY.Legendary), lege);
		
		//_lootTable_hellTools.add(new ItemStack(Material.GOLDEN_PICKAXE), rare);
		_lootTable_hellTools.add(Manager_HellTools.Instance.CreateHellPickaxe(ITEM_RARITY.Epic), epic);
		_lootTable_hellTools.add(Manager_HellTools.Instance.CreateHellPickaxe(ITEM_RARITY.Mythic), mythic);
		_lootTable_hellTools.add(Manager_HellTools.Instance.CreateHellPickaxe(ITEM_RARITY.Legendary), lege);
		
		//_lootTable_hellTools.add(new ItemStack(Material.GOLDEN_SWORD), rare);
		_lootTable_hellTools.add(Manager_HellTools.Instance.CreateHellTripleSword(ITEM_RARITY.Epic), epic);
		_lootTable_hellTools.add(Manager_HellTools.Instance.CreateHellTripleSword(ITEM_RARITY.Mythic), mythic);
		_lootTable_hellTools.add(Manager_HellTools.Instance.CreateHellTripleSword(ITEM_RARITY.Legendary), lege);
		
		_lootTable_hellTools.add(Manager_HellTools.Instance.CreateHellDoubleAxe(ITEM_RARITY.Epic), epic);
		_lootTable_hellTools.add(Manager_HellTools.Instance.CreateHellDoubleAxe(ITEM_RARITY.Mythic), mythic);
		_lootTable_hellTools.add(Manager_HellTools.Instance.CreateHellDoubleAxe(ITEM_RARITY.Legendary), lege);
		
		_lootTable_hellTools.add(Manager_HellTools.Instance.CreateHellHoe(ITEM_RARITY.Epic), epic);
		_lootTable_hellTools.add(Manager_HellTools.Instance.CreateHellHoe(ITEM_RARITY.Mythic), mythic);
		_lootTable_hellTools.add(Manager_HellTools.Instance.CreateHellHoe(ITEM_RARITY.Legendary), lege);
		
		
		//_lootTable_hellTools.add(null, lege);
		// _lootTable.add(new ItemStack(Material.DIAMOND), 1);
		// _lootTable.add(new ItemStack(Material.GOLD_INGOT), 10);
		// _lootTable.add(new ItemStack(Material.IRON_INGOT), 20);
		
		
		_lootTable_blocks.add(new ItemStack(Material.STONE), 160);
		_lootTable_blocks.add(new ItemStack(Material.NETHER_BRICK), 160);
		_lootTable_blocks.add(new ItemStack(Material.GRAVEL), 130);
		_lootTable_blocks.add(new ItemStack(Material.GLOWSTONE), 80);
		_lootTable_blocks.add(new ItemStack(Material.SHROOMLIGHT), 60);
		_lootTable_blocks.add(new ItemStack(Material.BASALT), 160);
		_lootTable_blocks.add(new ItemStack(Material.BLACK_STAINED_GLASS), 150);
		_lootTable_blocks.add(new ItemStack(Material.SOUL_SAND), 150);
		_lootTable_blocks.add(new ItemStack(Material.MAGMA_BLOCK), 120);
		_lootTable_blocks.add(new ItemStack(Material.OBSIDIAN), 110);
		_lootTable_blocks.add(new ItemStack(Material.CRYING_OBSIDIAN), 40);
		_lootTable_blocks.add(new ItemStack(Material.GILDED_BLACKSTONE), 40);
		_lootTable_blocks.add(new ItemStack(Material.CRIMSON_STEM), 130);
		_lootTable_blocks.add(new ItemStack(Material.WARPED_STEM), 130);
		//_lootTable_blocks.add(new ItemStack(Material.), 120);
		
		
		_lootTable_valuables.add(new ItemStack(Material.DIAMOND), 33);
		_lootTable_valuables.add(new ItemStack(Material.GOLD_INGOT), 80);
		_lootTable_valuables.add(new ItemStack(Material.IRON_INGOT), 90);
		//_lootTable_valuables.add(new ItemStack(Material.EMERALD), 10);
		_lootTable_valuables.add(new ItemStack(Material.LAPIS_LAZULI), 100);
		_lootTable_valuables.add(new ItemStack(Material.MAGMA_CREAM), 90);
		_lootTable_valuables.add(new ItemStack(Material.GUNPOWDER), 70);
		_lootTable_valuables.add(new ItemStack(Material.REDSTONE), 110);
		_lootTable_valuables.add(new ItemStack(Material.IRON_BLOCK), 45);
		_lootTable_valuables.add(new ItemStack(Material.GOLD_BLOCK), 45);
		_lootTable_valuables.add(new ItemStack(Material.EMERALD_BLOCK), 45);
		_lootTable_valuables.add(new ItemStack(Material.LAPIS_BLOCK), 40);
		_lootTable_valuables.add(new ItemStack(Material.DIAMOND_BLOCK), 4);
		_lootTable_valuables.add(new ItemStack(Material.ENDER_PEARL), 60);
		_lootTable_valuables.add(new ItemStack(Material.BLAZE_ROD), 40);
		_lootTable_valuables.add(new ItemStack(Material.GHAST_TEAR), 14);
		//_lootTable_valuables.add(new ItemStack(Material.NETHER_STAR), 2);
		//_lootTable_valuables.add(new ItemStack(Material.NETHERITE_INGOT), 1);
		//_lootTable_valuables.add(new ItemStack(Material.NETHERITE_SCRAP), 18);
		
		//_lootTable_valuables.add(new ItemStack(Material.TNT), 120);
		
		
		_lootTable_food.add(new ItemStack(Material.APPLE), 20);
		_lootTable_food.add(new ItemStack(Material.COOKED_PORKCHOP), 13);
		_lootTable_food.add(new ItemStack(Material.BAKED_POTATO), 8);
		_lootTable_food.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE), 1);
		_lootTable_food.add(new ItemStack(Material.GOLDEN_APPLE), 3);
		//_lootTable_food.add(Manager_HellArmor.Instance.CreateHellReflectShield(), 100);
		
		
		
		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("protection")), 8);
		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("fire_protection")), 9);
		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("blast_protection")), 11);
		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("projectile_protection")), 10);
		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("respiration")), 8);
		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("aqua_affinity")), 8);
		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("thorns")), 5);
		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("unbreaking")), 7);
		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("mending")), 3);
		//_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("binding_curse")), 1);
		
		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("efficiency")), 10);
		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("unbreaking")), 10);
		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("silk_touch")), 2);
		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("fortune")), 3);
		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("power")), 4);
		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("punch")), 7);
		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("flame")), 5);
		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("looting")), 3);
		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("mending")), 1);
		//_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("vanishing_curse")), 1);

	}

	@SuppressWarnings("unused")
	private boolean IsNether(World world)
	{
		return world == _nether;
	}

	@SuppressWarnings("unused")
	private boolean IsNether(Entity entity)
	{
		return entity.getWorld() == _nether;
	}

	@SuppressWarnings("unused")
	private boolean IsNether(Block block)
	{
		return block.getWorld() == _nether;
	}

	@SuppressWarnings("unused")
	private boolean IsNether(Location loc)
	{
		if(loc == null) return false;
		
		return loc.getWorld() == _nether;
	}
	
	
	
	public LinkedList<ItemStack> GenerateNetherLoot(int rollIncrease, int rollChances)
	{
		LinkedList<ItemStack> stacks = new LinkedList<>();

		int totalRolls = 0;
		int startChance = 92;
		int reduceChance = 8;
		for(int i = 0; i < rollChances; i++)
		{
			if(_rand.nextInt(100) >= startChance) break;
			
			startChance -= reduceChance;
			totalRolls += rollIncrease;
		}	
		if(totalRolls <= 0) totalRolls = 1;
		
		int blockLootChance = 75;
		int valuableChance = 36;
		int hellArrowChance = 11; //11
		int foodChance = 2; //2
		int hellArmorChance = 5;
		int enchantedBook = 3;
		int toolGear = 5; //4
		
		if(_chestDEBUG && _lootBoost)
		{
			blockLootChance = 75;
			valuableChance = 36;
			hellArrowChance = 80; //11
			foodChance = 2; //2
			hellArmorChance = 80;
			enchantedBook = 80;
			toolGear = 80; //4
		}
		boolean hasHellTool = false;
		boolean hasHellArmor = false;
		for(int i = 0; i < totalRolls; i++)
		{
			ItemStack stack;
			if(ThreadLocalRandom.current().nextInt(100) < blockLootChance)
			{
				stack = GetValidAmountStack(_lootTable_blocks.getLoot().clone());
				stacks.add(stack);				
			}
			if(ThreadLocalRandom.current().nextInt(100) < valuableChance)
			{
				stack = GetValidAmountStack(_lootTable_valuables.getLoot().clone());
				stacks.add(stack);				
			}
			
			if(ThreadLocalRandom.current().nextInt(100) < hellArrowChance)
			{
				stack = GetValidAmountStack(_lootTable_hellArrows.getLoot().clone());
				stacks.add(stack);
			}
			
			if(ThreadLocalRandom.current().nextInt(100) < foodChance)
			{
				stack = GetValidAmountStack(_lootTable_food.getLoot().clone());
				stacks.add(stack);
			}
			
			if(!hasHellArmor && ThreadLocalRandom.current().nextInt(100) < hellArmorChance)
			{
				hasHellArmor = true;
				stack = GetValidAmountStack(_lootTable_hellArmor.getLoot().clone());
				stacks.add(stack);
			}
			
			if(ThreadLocalRandom.current().nextInt(100) < enchantedBook)
			{
				stack = EnchantBook(2);
				stacks.add(stack);
			}
			
			if(!hasHellTool && ThreadLocalRandom.current().nextInt(100) < toolGear)
			{
				hasHellTool = true;
				stack = GetValidAmountStack(_lootTable_hellTools.getLoot().clone());
				stacks.add(stack);
			}
		}
		return stacks;
	}
	
	private ItemStack GetValidAmountStack(ItemStack stack)
	{
		stack.setAmount(ThreadLocalRandom.current().nextInt((_lootTable_stackMaxAmounts.getLoot())+1));
		
		if(stack.getType() == Material.NETHER_STAR) 
		{
			stack.setAmount(1);
		}
		
		if(stack.getType() == Material.NETHERITE_INGOT) 
		{
			if(stack.getAmount() > 32) stack.setAmount(16);
			else if(stack.getAmount() > 16)stack.setAmount(4);
			else stack.setAmount(1);
		}
		
		if(stack.getType() == Material.ENDER_PEARL) 
		{
			if(stack.getAmount() > 16) stack.setAmount(16);
		}
		
		if(Metods._ins.isArmor(stack))
		{
			stack.setAmount(1);
			EnchantArmor(stack,5,2);
			
			if(Metods._ins.HasEnchant(stack, Enchantment.DURABILITY) && ThreadLocalRandom.current().nextInt(100) < 20)
			{
				stack.removeEnchantment(Enchantment.DURABILITY);
				stack.addUnsafeEnchantment(Enchantment.DURABILITY, 4);
			}
		}
		
		if(Metods._ins.isTool(stack))
		{
			stack.setAmount(1);
			EnchantTool(stack,5,2);
			
			if(Metods._ins.HasEnchant(stack, Enchantment.DURABILITY) && ThreadLocalRandom.current().nextInt(100) < 20)
			{
				stack.removeEnchantment(Enchantment.DURABILITY);
				stack.addUnsafeEnchantment(Enchantment.DURABILITY, 4);
			}
		}
		
		if(Manager_HellArmor.Instance.IsHellArrow(stack))
		{
			//System.out.println("it was hell arrow");
			if(stack.getAmount() > 5) stack.setAmount(5);
			
			Metods._ins.AddGlow(stack);
		}
		
		if(stack.getType() == Material.ENCHANTED_GOLDEN_APPLE)
		{
			if(stack.getAmount() > 3) stack.setAmount(3);
			else stack.setAmount(1);
			 
		}
			
		
		return stack;
	}
	
	private ItemStack EnchantBook(int miniumLevel)
	{
		ItemStack stack = new ItemStack(Material.ENCHANTED_BOOK);
		
		if(ThreadLocalRandom.current().nextInt(100) > 50) EnchantTool(stack, 3, miniumLevel);
		else EnchantArmor(stack, 3, miniumLevel);
			
		
		return stack;
	}
	private void EnchantTool(ItemStack stack, int rolls,int miniumLevel)
	{
		for(int i = 0; i < rolls; i++)
		{
			if(ThreadLocalRandom.current().nextInt(100) > 50) break;
			
			Enchantment ench = _lootTable_enchants_tool.getLoot();
			int level = ThreadLocalRandom.current().nextInt(ench.getMaxLevel())+miniumLevel;
			
			if(level == 0) level = 1;
			
			if(level > ench.getMaxLevel()) level = ench.getMaxLevel();
			
			if(stack.getType() != Material.ENCHANTED_BOOK && !ench.canEnchantItem(stack) ) continue;
			
			if(stack.getType() == Material.ENCHANTED_BOOK)
			{

				EnchantmentStorageMeta meta = (EnchantmentStorageMeta)stack.getItemMeta();
				meta.addStoredEnchant(ench, level, true);
				stack.setItemMeta(meta);
				return;
			}
			stack.addEnchantment(ench, level);
			
		}
		
		if(stack.getType() == Material.ENCHANTED_BOOK && stack.getEnchantments().size() == 0)
		{
			EnchantTool(stack, rolls, miniumLevel);
		}
	}
	
	private void EnchantArmor(ItemStack stack,  int rolls,int miniumLevel)
	{

		for(int i = 0; i < rolls; i++)
		{
			boolean done = false;
			
			if(ThreadLocalRandom.current().nextInt(100) > 55) break;
			
			Enchantment ench = _lootTable_enchants_armor.getLoot();
			int level = ThreadLocalRandom.current().nextInt(ench.getMaxLevel())+miniumLevel;
			
			if(level == 0) level = 1;
			
			if(level > ench.getMaxLevel()) level = ench.getMaxLevel();
			
			if(stack.getType() != Material.ENCHANTED_BOOK && !ench.canEnchantItem(stack) ) continue;
			
			if(stack.getType() == Material.ENCHANTED_BOOK)
			{
				EnchantmentStorageMeta meta = (EnchantmentStorageMeta)stack.getItemMeta();
				meta.addStoredEnchant(ench, level, true);
				stack.setItemMeta(meta);
				return;
			}
			
			int counter = 0;
			
			for(Enchantment en : stack.getEnchantments().keySet())
			{
				if(en.conflictsWith(ench))
				{
					counter++;
				}
				
				if(counter >= 2) {done = true; break;}
				
			}
			
			
			
			if(done) continue;
			
			stack.addEnchantment(ench, level);
			
			
		}
		
		if(stack.getType() == Material.ENCHANTED_BOOK && stack.getEnchantments().size() == 0)
		{
			EnchantArmor(stack, rolls, miniumLevel);
		}
		
	}
	
	
	//if better structure is enabled on the server this will be triggered
	@EventHandler
	public void OnBetterStructureLoot(ChestFillEvent e)
	{
//		if (!(IsNether(e.getContainer().getWorld())))
//			return;
		
		World world = e.getContainer().getWorld();
		if ((DontLoseItems.IsEnd(world)))
			return;

		if (e.isCancelled())
			return;
				
		Location chestLocation = e.getContainer().getLocation();
	    double closeByDistance = 500; 
	    StringBuilder closeByPlayerNames = new StringBuilder();
	    boolean isFearPlayerNearby = false;

	    for (Player player : world.getPlayers()) 
	    {
	        if (player.getLocation().distance(chestLocation) <= closeByDistance) 
	        {
	            if (closeByPlayerNames.length() > 0) {
	                closeByPlayerNames.append(", ");
	            }
	            
	            if(getPlayerDifficulty(player) == DIFFICULT.NO_FEAR)
	            {
	            	isFearPlayerNearby = true;
	            }
	            
	            closeByPlayerNames.append(player.getName());
	        }
	    }

	    String closeByPlayersStr = String.join(", ", closeByPlayerNames);

		Inventory inv = e.getContainer().getInventory();
		inv = e.getContainer().getSnapshotInventory();
		
		int lootRollAmount = _chestRollMaxAmount;
		
		if(isFearPlayerNearby && IsNether(world))
		{
			lootRollAmount = _noFearRollMaxAmount;
		}
		
		List<ItemStack> stacks = GenerateNetherLoot(2, lootRollAmount);
		
		double betterStructureNerf = stacks.size() * (IsNether(world) ? 1 :BETTER_STRUCTURE_LOOT_NERF);
		
		
		StringBuilder itemNames = new StringBuilder();
		int counter = 0;
	    for (ItemStack stack : stacks) 
	    {
	    	if(world.getEnvironment() != Environment.NETHER)
	    	{
	    		if(counter > betterStructureNerf)
	    			break;
	    	}
	    	
	        if (Manager_HellArmor.Instance.IsVoidOrHellItem(stack) || Manager_HellTools.Instance.IsHellTool(stack)) 
	        { 
	            if (itemNames.length() > 0) 
	            {
	                itemNames.append(", ");
	            }
	            
	           
	            String displayName = ItemUtils.GetDisplayName(stack); 
	            itemNames.append(displayName);

	            if(Manager_HellArmor.Instance.GetRarity(stack) == ITEM_RARITY.Legendary || Manager_HellTools.Instance.GetRarity(stack) == ITEM_RARITY.Legendary)
	            {
	            	
	            	continue;
	            }
	            
	        }
	        
	        inv.addItem(stack);
	        counter++;
	    }

	    Bukkit.getLogger().info("[bs] has gen w:" + world.getEnvironment() + " loot {i: " + itemNames + "} {cbp: " + closeByPlayersStr + "}");
	}
	
	@EventHandler
	public void OnInventoryOpen(LootGenerateEvent e)
	{
		
		if (!(IsNether(e.getWorld())))
			return;
		
		if(e.isCancelled()) return;
		
		
		
		Inventory inv = e.getInventoryHolder().getInventory();
		//var inv = e.getLoot();
		//inv.add
		
//		if(!isDouble) System.out.println("Player: "+e.getPlayer().getName()+ " Generated loot by opened chest");
//		else System.out.println("Player: "+e.getPlayer().getName()+ " Generated loot by opened DOUBLE chest");
		
		Bukkit.getLogger().info("Player: "+e.getEntity()+ " Generated loot by opened  chest");
		
		
		int lootRollAmount = _chestRollMaxAmount;
		
		if(e.getEntity() instanceof Player)
		{
			Player player = (Player)e.getEntity();
			if(getPlayerDifficulty(player) == DIFFICULT.NO_FEAR)
			{
				lootRollAmount = _noFearRollMaxAmount;
			}
		}
		
		for(ItemStack stack : GenerateNetherLoot(2,lootRollAmount)) 
		{ 
			inv.addItem(stack); 
			//e.getLoot().add(stack);
		}
		
		//if(isDouble) for(ItemStack stack : GenerateNetherLoot(2,_chestRollMaxAmount+2)) { inv.addItem(stack); }
		//GenerateNetherLoot(chestInventory);

		
	}
	@EventHandler
	public void OnInventoryOpen(InventoryOpenEvent e)
	{

		if(!_chestDEBUG) return;
		
		Block[] block = {null,null};
		Inventory inv = null;

		if (e.getInventory().getHolder() instanceof Chest)	
		{
			Chest chest = (Chest) e.getInventory().getHolder();
			block[0] = chest.getBlock();
			inv = chest.getInventory();
		}		
//		
		if(block[0] == null) return;
//		 
		inv.clear();

		for(ItemStack stack : GenerateNetherLoot(2,_chestRollMaxAmount)) { inv.addItem(stack); }

	}
	
	private DIFFICULT getPlayerDifficulty(Player player)
	{
		return getPlayerDifficulty(player.getUniqueId());
	}
	
	private DIFFICULT getPlayerDifficulty(UUID uuid)
	{
		return Manager_Difficult.Instance.getPlayerSettings(uuid).NetherDifficulty;
	}
	
	void GetSettings()
	{
		final String netherSettings = "NetherSettings";
		ConfigMaker cm = new ConfigMaker(DontLoseItems.Instance, netherSettings + ".yml");
		FileConfiguration config = cm.getConfig();

		// String dot_dmg = netherSettings+".durabilityDamageFromDot";

		if (!config.contains(netherSettings + "."))
		{
			// default values
			DontLoseItems.Instance.getServer().getConsoleSender()
					.sendMessage(ChatColor.AQUA + "DontLoseItems : Default config made!");

			cm.saveConfig();
			// return;
		}

	}

}
