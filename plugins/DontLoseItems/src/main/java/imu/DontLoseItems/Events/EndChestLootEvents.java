package imu.DontLoseItems.Events;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import com.magmaguy.betterstructures.api.ChestFillEvent;

import imu.DontLoseItems.CustomEnd.UnstableEnd;
import imu.DontLoseItems.CustomItems.VoidStones.Reforge_Void_Stone;
import imu.DontLoseItems.CustomItems.VoidStones.Unanointment_Void_Stone;
import imu.DontLoseItems.CustomItems.VoidStones.Unstable_Void_Stone;
import imu.DontLoseItems.Enums.VOID_STONE_TIER;
import imu.DontLoseItems.Managers.Manager_HellArmor;
import imu.DontLoseItems.Managers.Manager_LegendaryUpgrades;
import imu.DontLoseItems.Managers.Manager_VoidStones;
import imu.DontLoseItems.main.DontLoseItems;
import imu.iAPI.LootTables.ImusLootTable;
import imu.iAPI.Other.ConfigMaker;
import imu.iAPI.Other.Metods;

public class EndChestLootEvents implements Listener
{
	public static EndChestLootEvents Instance;

	private ImusLootTable<ItemStack> _lootTable_valuables;
	private ImusLootTable<ItemStack> _lootTable_food;
	private ImusLootTable<ItemStack> _lootTable_leg_upgrades;
	private ImusLootTable<Enchantment> _lootTable_enchants_armor;
	private ImusLootTable<Enchantment> _lootTable_enchants_tool;

	private ImusLootTable<Integer> _lootTable_stackMaxAmounts;

	private int _chestRollMaxAmount = 10;

	private boolean _chestDEBUG = false;

	private boolean _lootBoost = false;

	/// setblock ~ ~ ~ minecraft:chest{LootTable:"chests/bastion_bridge"}
	public EndChestLootEvents()
	{
		Instance = this;

		InitLootTable();
	}

	private void InitLootTable()
	{

		_lootTable_stackMaxAmounts = new ImusLootTable<>();
		_lootTable_valuables = new ImusLootTable<>();
		_lootTable_food = new ImusLootTable<>();
		_lootTable_enchants_armor = new ImusLootTable<>();
		_lootTable_enchants_tool = new ImusLootTable<>();
		_lootTable_leg_upgrades = new ImusLootTable<>();

		_lootTable_stackMaxAmounts.add(5, 90);
		_lootTable_stackMaxAmounts.add(10, 70);
		_lootTable_stackMaxAmounts.add(13, 50);
		_lootTable_stackMaxAmounts.add(30, 15);
		_lootTable_stackMaxAmounts.add(40, 7);
		_lootTable_stackMaxAmounts.add(64, 5);

//		int common = 90;
//		int unCommon = 90;
//		int rare = 80;
//		int epic = 73;
//		int mythic = 34;
//		int lege = 20;

		_lootTable_valuables.add(new ItemStack(Material.DIAMOND), 22);
		_lootTable_valuables.add(new ItemStack(Material.IRON_BLOCK), 50);
		_lootTable_valuables.add(new ItemStack(Material.GOLD_BLOCK), 40);
		_lootTable_valuables.add(new ItemStack(Material.EMERALD_BLOCK), 40);
		_lootTable_valuables.add(new ItemStack(Material.LAPIS_BLOCK), 25);
		_lootTable_valuables.add(new ItemStack(Material.DIAMOND_BLOCK), 4);
		_lootTable_valuables.add(new ItemStack(Material.ENDER_PEARL), 60);
		

		_lootTable_valuables.add(new ItemStack(Material.NETHER_STAR), 1);
		//_lootTable_valuables.add(new ItemStack(Material.NETHERITE_INGOT), 1);
		_lootTable_valuables.add(new ItemStack(Material.NETHERITE_SCRAP), 3);
		
        _lootTable_valuables.add(new ItemStack(Material.ELYTRA), 1);
        _lootTable_valuables.add(new ItemStack(Material.TOTEM_OF_UNDYING), 3);
        _lootTable_valuables.add(new ItemStack(Material.SHULKER_SHELL), 2);
        _lootTable_valuables.add(new ItemStack(Material.DRAGON_BREATH), 7);


        _lootTable_valuables.add(new ItemStack(Material.PRISMARINE_CRYSTALS), 10);
        _lootTable_valuables.add(new ItemStack(Material.END_STONE), 50);
        _lootTable_valuables.add(new ItemStack(Material.END_STONE_BRICKS), 50);
        _lootTable_valuables.add(new ItemStack(Material.OBSIDIAN), 40);
        _lootTable_valuables.add(new ItemStack(Material.TURTLE_HELMET), 6);
        _lootTable_valuables.add(new ItemStack(Material.TRIDENT), 1);
        _lootTable_valuables.add(new ItemStack(Material.NAME_TAG), 12);
        _lootTable_valuables.add(new ItemStack(Material.END_ROD), 15);
        _lootTable_valuables.add(new ItemStack(Material.END_CRYSTAL), 2);
        
        Unstable_Void_Stone voidStone = new Unstable_Void_Stone();
        _lootTable_valuables.add(voidStone.GetVoidStoneWithTier(VOID_STONE_TIER.NORMAL), 2);
        //_lootTable_valuables.add(voidStone.GetVoidStoneWithTier(VOID_STONE_TIER.RARE), 1);
        _lootTable_valuables.add(new Reforge_Void_Stone().GetVoidStoneWithTier(VOID_STONE_TIER.NORMAL), 1);
        _lootTable_valuables.add(new Unanointment_Void_Stone().GetVoidStoneWithTier(VOID_STONE_TIER.NORMAL), 2);

		// _lootTable_valuables.add(new ItemStack(Material.TNT), 120);

		_lootTable_food.add(new ItemStack(Material.APPLE), 15);
		_lootTable_food.add(new ItemStack(Material.COOKED_PORKCHOP), 13);
		_lootTable_food.add(new ItemStack(Material.BAKED_POTATO), 8);
		_lootTable_food.add(new ItemStack(Material.CHORUS_FRUIT), 9);
		_lootTable_food.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE), 1);
		_lootTable_food.add(new ItemStack(Material.GOLDEN_APPLE), 3);
		// _lootTable_food.add(Manager_HellArmor.Instance.CreateHellReflectShield(),
		// 100);

		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("protection")), 8);
		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("fire_protection")), 9);
		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("blast_protection")), 11);
		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("projectile_protection")), 10);
		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("respiration")), 8);
		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("aqua_affinity")), 8);
		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("thorns")), 5);
		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("unbreaking")), 7);
		_lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("mending")), 3);
		// _lootTable_enchants_armor.add(Enchantment.getByKey(NamespacedKey.minecraft("binding_curse")),
		// 1);

		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("efficiency")), 10);
		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("unbreaking")), 10);
		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("silk_touch")), 2);
		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("fortune")), 3);
		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("power")), 4);
		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("punch")), 7);
		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("flame")), 5);
		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("looting")), 3);
		_lootTable_enchants_tool.add(Enchantment.getByKey(NamespacedKey.minecraft("mending")), 1);
		
		_lootTable_leg_upgrades.add(Manager_LegendaryUpgrades.Instance.Get_UpgradeHellHelmet(), 	1);
		_lootTable_leg_upgrades.add(Manager_LegendaryUpgrades.Instance.Get_UpgradeHellChest(), 		1);
		_lootTable_leg_upgrades.add(Manager_LegendaryUpgrades.Instance.Get_UpgradeHellLeggings(), 	1);
		_lootTable_leg_upgrades.add(Manager_LegendaryUpgrades.Instance.Get_UpgradeHellBoots(), 		1);
		
		
		_lootTable_leg_upgrades.add(Manager_LegendaryUpgrades.Instance.Get_UpgradeHellHoe(), 1);
		_lootTable_leg_upgrades.add(Manager_LegendaryUpgrades.Instance.Get_UpgradeHellPickaxe(), 1);
		_lootTable_leg_upgrades.add(Manager_LegendaryUpgrades.Instance.Get_UpgradeHellAxe(), 1);
		_lootTable_leg_upgrades.add(Manager_LegendaryUpgrades.Instance.Get_UpgradeHellShield(), 1);
		_lootTable_leg_upgrades.add(Manager_LegendaryUpgrades.Instance.Get_UpgradeHellSword(), 1);

	}
	
	
	public LinkedList<ItemStack> GenerateEndLoot(int baseRollAmount,int rollIncrease, int rollChances)
	{
		LinkedList<ItemStack> stacks = new LinkedList<>();

		int totalRolls = 0+baseRollAmount;
		int startChance = 98;
		int reduceChance = 8;
		for (int i = 0; i < rollChances; i++)
		{
			if (ThreadLocalRandom.current().nextInt(100) >= startChance)
				break;

			startChance -= reduceChance;
			totalRolls += rollIncrease;
		}
		if (totalRolls <= 0)
			totalRolls = 1;

		int valuableChance = 36;
		int foodChance = 2; // 2
		int enchantedBook = 6;
		int legendaryUpgrades = 1;
		
		if(_chestDEBUG && _lootBoost)
		{
			valuableChance = 80;
			foodChance = 80;
			enchantedBook = 80;
			legendaryUpgrades = 80;
		}
		for (int i = 0; i < totalRolls; i++)
		{
			ItemStack stack;
//			if(ThreadLocalRandom.current().nextInt(100) < blockLootChance)
//			{
//				stack = GetValidAmountStack(_lootTable_blocks.getLoot().clone());
//				stacks.add(stack);				
//			}
			if (ThreadLocalRandom.current().nextInt(100) < valuableChance)
			{
				stack = GetValidAmountStack(_lootTable_valuables.getLoot().clone());
				stacks.add(stack);
			}

			if (ThreadLocalRandom.current().nextInt(100) < foodChance)
			{
				stack = GetValidAmountStack(_lootTable_food.getLoot().clone());
				stacks.add(stack);
			}

			if (ThreadLocalRandom.current().nextInt(100) < enchantedBook)
			{
				stack = EnchantBook(2);
				stacks.add(stack);
			}
			
			if (ThreadLocalRandom.current().nextInt(100) < legendaryUpgrades)
			{
				stack = GetValidAmountStack(_lootTable_leg_upgrades.getLoot().clone());
				stacks.add(stack);
			}

		}
		return stacks;
	}

	private ItemStack GetValidAmountStack(ItemStack stack)
	{
		stack.setAmount(ThreadLocalRandom.current().nextInt((_lootTable_stackMaxAmounts.getLoot()) + 1));

		if (stack.getType() == Material.NETHER_STAR)
		{
			stack.setAmount(1);
		}

		if (stack.getType() == Material.NETHERITE_INGOT)
		{
			stack.setAmount(1);
		}
		
		if (stack.getType() == Material.TOTEM_OF_UNDYING)
		{
			stack.setAmount(1);
		}
		
		if (stack.getType() == Material.SHULKER_SHELL)
		{
			stack.setAmount(1);
		}
		
		if (stack.getType() == Material.ELYTRA)
		{
			stack.setAmount(1);
		}
		
		if (Manager_VoidStones.IsVoidStone(stack))
		{
			stack.setAmount(1);
		}

		if (stack.getType() == Material.ENDER_PEARL)
		{
			if (stack.getAmount() > 16)
				stack.setAmount(16);
		}

		if (Metods._ins.isArmor(stack))
		{
			stack.setAmount(1);
			
			if(!Manager_LegendaryUpgrades.Instance.IsUpgrade(stack)) EnchantArmor(stack, 5, 2);

			if (Metods._ins.HasEnchant(stack, Enchantment.DURABILITY) && ThreadLocalRandom.current().nextInt(100) < 20)
			{
				stack.removeEnchantment(Enchantment.DURABILITY);
				stack.addUnsafeEnchantment(Enchantment.DURABILITY, 4);
			}
		}

		if (Metods._ins.isTool(stack))
		{
			stack.setAmount(1);
			
			if(!Manager_LegendaryUpgrades.Instance.IsUpgrade(stack)) EnchantTool(stack, 5, 2);

			if (Metods._ins.HasEnchant(stack, Enchantment.DURABILITY) && ThreadLocalRandom.current().nextInt(100) < 20)
			{
				stack.removeEnchantment(Enchantment.DURABILITY);
				stack.addUnsafeEnchantment(Enchantment.DURABILITY, 4);
			}
		}

		if (Manager_HellArmor.Instance.IsHellArrow(stack))
		{
			// System.out.println("it was hell arrow");
			if (stack.getAmount() > 5)
				stack.setAmount(5);

			Metods._ins.AddGlow(stack);
		}

		if (stack.getType() == Material.ENCHANTED_GOLDEN_APPLE)
		{
			if (stack.getAmount() > 16)
				stack.setAmount(3);
			else
				stack.setAmount(1);

		}
		

		return stack;
	}

	private ItemStack EnchantBook(int miniumLevel)
	{
		ItemStack stack = new ItemStack(Material.ENCHANTED_BOOK);

		if (ThreadLocalRandom.current().nextInt(100) > 50)
			EnchantTool(stack, 3, miniumLevel);
		else
			EnchantArmor(stack, 3, miniumLevel);

		return stack;
	}

	private void EnchantTool(ItemStack stack, int rolls, int miniumLevel)
	{
		for (int i = 0; i < rolls; i++)
		{
			if (ThreadLocalRandom.current().nextInt(100) > 50)
				break;

			Enchantment ench = _lootTable_enchants_tool.getLoot();
			int level = ThreadLocalRandom.current().nextInt(ench.getMaxLevel()) + miniumLevel;

			if (level == 0)
				level = 1;

			if (level > ench.getMaxLevel())
				level = ench.getMaxLevel();

			if (stack.getType() != Material.ENCHANTED_BOOK && !ench.canEnchantItem(stack))
				continue;

			if (stack.getType() == Material.ENCHANTED_BOOK)
			{

				EnchantmentStorageMeta meta = (EnchantmentStorageMeta) stack.getItemMeta();
				meta.addStoredEnchant(ench, level, true);
				stack.setItemMeta(meta);
				return;
			}
			stack.addEnchantment(ench, level);

		}

		if (stack.getType() == Material.ENCHANTED_BOOK && stack.getEnchantments().size() == 0)
		{
			EnchantTool(stack, rolls, miniumLevel);
		}
	}

	private void EnchantArmor(ItemStack stack, int rolls, int miniumLevel)
	{

		for (int i = 0; i < rolls; i++)
		{
			boolean done = false;

			if (ThreadLocalRandom.current().nextInt(100) > 55)
				break;

			Enchantment ench = _lootTable_enchants_armor.getLoot();
			int level = ThreadLocalRandom.current().nextInt(ench.getMaxLevel()) + miniumLevel;

			if (level == 0)
				level = 1;

			if (level > ench.getMaxLevel())
				level = ench.getMaxLevel();

			if (stack.getType() != Material.ENCHANTED_BOOK && !ench.canEnchantItem(stack))
				continue;

			if (stack.getType() == Material.ENCHANTED_BOOK)
			{
				EnchantmentStorageMeta meta = (EnchantmentStorageMeta) stack.getItemMeta();
				meta.addStoredEnchant(ench, level, true);
				stack.setItemMeta(meta);
				return;
			}

			int counter = 0;

			for (Enchantment en : stack.getEnchantments().keySet())
			{
				if (en.conflictsWith(ench))
				{
					counter++;
				}

				if (counter >= 2)
				{
					done = true;
					break;
				}

			}

			if (done)
				continue;

			stack.addEnchantment(ench, level);

		}

		if (stack.getType() == Material.ENCHANTED_BOOK && stack.getEnchantments().size() == 0)
		{
			EnchantArmor(stack, rolls, miniumLevel);
		}

	}
	
	
	//if better structure is enabled on the server this will be triggered
	@EventHandler
	public void OnBetterStructureLoot(ChestFillEvent e)
	{
		if (!(DontLoseItems.IsEnd(e.getContainer().getWorld())))
			return;

		if (e.isCancelled())
			return;
		
		//System.out.println("For betterstructures has generated end loot ");
		Bukkit.getLogger().info("For betterstructures has generated end loot ");
		Inventory inv = e.getContainer().getInventory();
		inv = e.getContainer().getSnapshotInventory();
		List<ItemStack> stacks = GenerateEndLoot(1, 2, _chestRollMaxAmount);
		
		for (ItemStack stack : stacks)
		{
			inv.addItem(stack);
		}
	}
	
	@EventHandler
	public void OnInventoryOpen(LootGenerateEvent e)
	{

		if (!(DontLoseItems.IsEnd(e.getWorld())))
			return;

		if (e.isCancelled())
			return;

		Inventory inv = e.getInventoryHolder().getInventory();

		List<ItemStack> stacks = null;
		
		if(e.getEntity() instanceof Player)
		{
			stacks = GenerateEndLoot(UnstableEnd.Instance.GetPlayerBaseRollAmount((Player)e.getEntity())+1, 2,_chestRollMaxAmount);
			UnstableEnd.Instance.RemovePlayerChestLootBase((Player)e.getEntity());
		}
		else
		{
			stacks = GenerateEndLoot(0, 2,_chestRollMaxAmount);
		}
		
		Bukkit.getLogger().info("Player: "+e.getEntity()+ " Generated loot by opened  chest");
		
		for (ItemStack stack : stacks)
		{
			inv.addItem(stack);
		}

	}

	@EventHandler
	public void OnInventoryOpen(InventoryOpenEvent e)
	{

		if (!_chestDEBUG)
			return;

		Block[] block = { null, null };
		Inventory inv = null;

		if (e.getInventory().getHolder() instanceof Chest)
		{
			Chest chest = (Chest) e.getInventory().getHolder();
			block[0] = chest.getBlock();
			inv = chest.getInventory();
		}
//		
		if (block[0] == null)
			return;
//		 
		inv.clear();

//		
		
		List<ItemStack> stacks =GenerateEndLoot(UnstableEnd.Instance.GetPlayerBaseRollAmount((Player)e.getPlayer()), 2,_chestRollMaxAmount);
		UnstableEnd.Instance.RemovePlayerChestLootBase((Player)e.getPlayer());
		
		System.out.println("Player: "+e.getPlayer()+ " Generated loot by opened  chest");
		
		
		for (ItemStack stack : stacks)
		{
			inv.addItem(stack);
		}

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
