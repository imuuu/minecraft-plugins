package me.imu.imusenchants;

import me.imu.imusenchants.Enchants.NodeDirectional;
import me.imu.imusenchants.Enums.CALCULATION_MODE;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Enums.ENCHANTMENT_TIER;

public class CONSTANTS
{
	public static final int ENCHANT_ROWS = 6;
	public static final int ENCHANT_COLUMNS = 9;
	public static final int MAX_SLOTS = 40;
	
	public static final Material BOOSTER_MATERIAL = Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE;
	public static final Material ENCHANT_MATERIAL = Material.ENCHANTED_BOOK;

	// true: items stay vanilla until a Slot Core is used on them and the enchanting table is vanilla
	// unless clicked with a slotted item. false: old behavior, every crafted/found item gets slots.
	public static final boolean SLOT_CORE_MODE = true;
	public static final Material SLOT_CORE_MATERIAL = Material.AMETHYST_SHARD;
	public static double SLOT_CORE_CHEST_CHANCE = 0.05; // 0.0-1.0, per unopened loot chest

	public static double CUSTOM_BOOK_CHEST_CHANCE = 0.05; // 0.0-1.0, per unopened loot chest, always level I
	public static double CUSTOM_BOOK_SHOP_CHANCE = 0.15; // 0.0-1.0, chance a bought book is a custom one
	public static double CURSED_BOOK_CHANCE = 0.20; // 0.0-1.0, chance a custom book found in loot carries a curse
	
	public static final boolean ENABLE_MULTIPLE_SAME_ENCHANTS = false;
	
	public static int COST_FIRST_1_BOOSTER = 10;
	public static int CAP_FIRST_1_BOOSTER = 15;
	
	public static int COST_FIRST_2_BOOSTER = 10;
	public static int CAP_FIRST_2_BOOSTER = 30;
	
	public static int COST_FIRST_3_BOOSTER = 10;
	public static int CAP_FIRST_3_BOOSTER = 40;
	
	public static int COST_FIRST_1_ENCHANTS = 5;
	public static int CAP_FIRST_1_ENCHANTS = 20;
	
	
	public static int COST_FIRST_2_ENCHANTS = 5;
	public static int CAP_FIRST_2_ENCHANTS = 30;
	
	
	public static int COST_FIRST_3_ENCHANTS = 5;
	public static int CAP_FIRST_3_ENCHANTS = 40;
	
	public static double NORMAL_CHANCE_1_TO_BE_TIER_2 = 0.10;
	//public static final double MEDIUM_CHANCE_1_TO_BE_TIER_2 = 28;
	public static double HIGH_CHANCE_1_TO_BE_TIER_2 = 0.65;
	
	//public static final double NORMAL_CHANCE_2_TO_BE_TIER_3 = 10;
	//public static final double MEDIUM_CHANCE_2_TO_BE_TIER_3 = 28;
	public static double HIGH_CHANCE_2_TO_BE_TIER_3 = 0.60;
	
	public static final CALCULATION_MODE CRAFTING_SLOT_CALCULATION = CALCULATION_MODE.AVERAGE;
	
	public static final int BOOSTER_MIN_DIRECTIONS = 2;
	public static final int BOOSTER_MAX_DIRECTIONS = 4;
	
	public static boolean CROSSBOW_CONSUME_ARROW_WITH_INFINITY = false;
	
	public static boolean SWAPPER_ANIMATION = true;
	public static int DELAY_SWAPPER_ANIMATION = 12; //ticks
	
	public static double MENDING_INCREASE_BY_LEVEL = 0.2;
	
	public static boolean DISABLE_ENCHANTED_BOOKS_VILLAGERS = true;
	public static boolean ENABLE_BUY_BOOSTERS_VILLAGERS = true;
	public static final boolean ENABLE_BUY_SLOT_ITEMS_VILLAGERS = true;
	public static boolean ENABLE_SELL_BOOSTERS_FOR_TOOLS_ARMOR_VILLAGERS = true;
	
	public static double UNLOCKING_NODES_RECURSIVE_REDUCE = 0.8; // 0.0-1.0f
	public static int UNLOCKING_NODES_RECURSIVE_DEPTH = 5;
	public static double UNLOCKING_NODES_RECURSIVE_START_CHANCE = 0.20; //0.0-1.0f
	
	public static boolean SET_FOUND_ENCHANTED_BOOKS_LEVEL_ONE = true;
	public static boolean ENABLE_MENDING_FOUND_ONLY_END = true;

	public static final int ENCHANT_FORCE_LEVEL = 1;

	public static double BUY_MORE_LIKE_TOOL_CHANCE = 0.7;
	public static double BUY_MORE_LIKE_ARMOR_CHANCE = 0.7;
	public static double BUY_MORE_LIKE_TOOL_TO_BE_TOOL_CHANCE = 0.65;


	// The values above that aren't final come from config.yml and can be changed in game
	// (/ien config). The value in the code is the default when config.yml doesn't have it.
	public static void Load(FileConfiguration config)
	{
		SLOT_CORE_CHEST_CHANCE = config.getDouble("loot.slot-core-chance", SLOT_CORE_CHEST_CHANCE);
		CUSTOM_BOOK_CHEST_CHANCE = config.getDouble("loot.custom-book-chance", CUSTOM_BOOK_CHEST_CHANCE);
		CURSED_BOOK_CHANCE = config.getDouble("loot.cursed-book-chance", CURSED_BOOK_CHANCE);
		SET_FOUND_ENCHANTED_BOOKS_LEVEL_ONE = config.getBoolean("loot.found-books-level-one", SET_FOUND_ENCHANTED_BOOKS_LEVEL_ONE);
		ENABLE_MENDING_FOUND_ONLY_END = config.getBoolean("loot.mending-only-in-end", ENABLE_MENDING_FOUND_ONLY_END);
		CUSTOM_BOOK_SHOP_CHANCE = config.getDouble("shop.custom-book-chance", CUSTOM_BOOK_SHOP_CHANCE);
		CAP_FIRST_1_ENCHANTS = config.getInt("shop.books.tier-1.level", CAP_FIRST_1_ENCHANTS);
		COST_FIRST_1_ENCHANTS = config.getInt("shop.books.tier-1.cost", COST_FIRST_1_ENCHANTS);
		CAP_FIRST_2_ENCHANTS = config.getInt("shop.books.tier-2.level", CAP_FIRST_2_ENCHANTS);
		COST_FIRST_2_ENCHANTS = config.getInt("shop.books.tier-2.cost", COST_FIRST_2_ENCHANTS);
		CAP_FIRST_3_ENCHANTS = config.getInt("shop.books.tier-3.level", CAP_FIRST_3_ENCHANTS);
		COST_FIRST_3_ENCHANTS = config.getInt("shop.books.tier-3.cost", COST_FIRST_3_ENCHANTS);
		CAP_FIRST_1_BOOSTER = config.getInt("shop.boosters.tier-1.level", CAP_FIRST_1_BOOSTER);
		COST_FIRST_1_BOOSTER = config.getInt("shop.boosters.tier-1.cost", COST_FIRST_1_BOOSTER);
		CAP_FIRST_2_BOOSTER = config.getInt("shop.boosters.tier-2.level", CAP_FIRST_2_BOOSTER);
		COST_FIRST_2_BOOSTER = config.getInt("shop.boosters.tier-2.cost", COST_FIRST_2_BOOSTER);
		CAP_FIRST_3_BOOSTER = config.getInt("shop.boosters.tier-3.level", CAP_FIRST_3_BOOSTER);
		COST_FIRST_3_BOOSTER = config.getInt("shop.boosters.tier-3.cost", COST_FIRST_3_BOOSTER);
		NORMAL_CHANCE_1_TO_BE_TIER_2 = config.getDouble("shop.tier-up.tier-1-book", NORMAL_CHANCE_1_TO_BE_TIER_2);
		HIGH_CHANCE_1_TO_BE_TIER_2 = config.getDouble("shop.tier-up.tier-2-book", HIGH_CHANCE_1_TO_BE_TIER_2);
		HIGH_CHANCE_2_TO_BE_TIER_3 = config.getDouble("shop.tier-up.tier-3-book", HIGH_CHANCE_2_TO_BE_TIER_3);
		BUY_MORE_LIKE_TOOL_CHANCE = config.getDouble("shop.mixed.tool-side-chance", BUY_MORE_LIKE_TOOL_CHANCE);
		BUY_MORE_LIKE_TOOL_TO_BE_TOOL_CHANCE = config.getDouble("shop.mixed.tool-not-weapon-chance", BUY_MORE_LIKE_TOOL_TO_BE_TOOL_CHANCE);
		BUY_MORE_LIKE_ARMOR_CHANCE = config.getDouble("shop.mixed.armor-side-chance", BUY_MORE_LIKE_ARMOR_CHANCE);
		UNLOCKING_NODES_RECURSIVE_START_CHANCE = config.getDouble("slots.unlock.start-chance", UNLOCKING_NODES_RECURSIVE_START_CHANCE);
		UNLOCKING_NODES_RECURSIVE_REDUCE = config.getDouble("slots.unlock.chance-multiplier", UNLOCKING_NODES_RECURSIVE_REDUCE);
		UNLOCKING_NODES_RECURSIVE_DEPTH = config.getInt("slots.unlock.max-depth", UNLOCKING_NODES_RECURSIVE_DEPTH);
		SWAPPER_ANIMATION = config.getBoolean("slots.swapper-animation", SWAPPER_ANIMATION);
		DELAY_SWAPPER_ANIMATION = config.getInt("slots.swapper-animation-ticks", DELAY_SWAPPER_ANIMATION);
		MENDING_INCREASE_BY_LEVEL = config.getDouble("items.mending-per-level", MENDING_INCREASE_BY_LEVEL);
		CROSSBOW_CONSUME_ARROW_WITH_INFINITY = config.getBoolean("items.crossbow-infinity-uses-arrows", CROSSBOW_CONSUME_ARROW_WITH_INFINITY);
		DISABLE_ENCHANTED_BOOKS_VILLAGERS = config.getBoolean("villagers.no-enchanted-books", DISABLE_ENCHANTED_BOOKS_VILLAGERS);
		ENABLE_BUY_BOOSTERS_VILLAGERS = config.getBoolean("villagers.sell-boosters", ENABLE_BUY_BOOSTERS_VILLAGERS);
		ENABLE_SELL_BOOSTERS_FOR_TOOLS_ARMOR_VILLAGERS = config.getBoolean("villagers.gear-costs-boosters", ENABLE_SELL_BOOSTERS_FOR_TOOLS_ARMOR_VILLAGERS);
	}

	public static NodeDirectional GetDirectionalNode(ItemStack stack)
	{
		
        return null;
    }
	
	public static int GetCostEnchant(ENCHANTMENT_TIER tier)
	{
		switch (tier) 
		{
        case TIER_1: return CONSTANTS.COST_FIRST_1_ENCHANTS;
        case TIER_2: return CONSTANTS.COST_FIRST_2_ENCHANTS;
        case TIER_3: return CONSTANTS.COST_FIRST_3_ENCHANTS;
		}
        return 0;
    }

	public static int GetCostBooster(ENCHANTMENT_TIER tier)
	{
		switch (tier)
		{
			case TIER_1: return CONSTANTS.COST_FIRST_1_BOOSTER;
			case TIER_2: return CONSTANTS.COST_FIRST_2_BOOSTER;
			case TIER_3: return CONSTANTS.COST_FIRST_3_BOOSTER;
		}
		return 0;
	}
	
	public static int GetCapEnchant(ENCHANTMENT_TIER tier) 
	{
		switch (tier) 
		{
        case TIER_1: return CONSTANTS.CAP_FIRST_1_ENCHANTS;
        case TIER_2: return CONSTANTS.CAP_FIRST_2_ENCHANTS;
        case TIER_3: return CONSTANTS.CAP_FIRST_3_ENCHANTS;
		}
        return 0;
	}
	
//	public static Material GetToolMainMaterial(ItemStack stack)
//	{
//		
//	}
}
