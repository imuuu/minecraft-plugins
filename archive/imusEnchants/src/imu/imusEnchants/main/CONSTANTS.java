package imu.imusEnchants.main;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Enums.ENCHANTMENT_TIER;
import imu.imusEnchants.Enchants.NodeDirectional;
import imu.imusEnchants.Enums.CALCULATION_MODE;

public class CONSTANTS
{
	public static final int ENCHANT_ROWS = 6;
	public static final int ENCHANT_COLUMNS = 9;
	public static final int MAX_SLOTS = 40;
	
	public static final Material BOOSTER_MATERIAL = Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE;
	public static final Material ENCHANT_MATERIAL = Material.ENCHANTED_BOOK;
	
	public static final boolean ENABLE_MULTIPLE_SAME_ENCHANTS = false;
	
	public static final int COST_FIRST_1_BOOSTER = 10;
	public static final int CAP_FIRST_1_BOOSTER = 15;
	
	public static final int COST_FIRST_2_BOOSTER = 10;
	public static final int CAP_FIRST_2_BOOSTER = 30;
	
	public static final int COST_FIRST_3_BOOSTER = 10;
	public static final int CAP_FIRST_3_BOOSTER = 40;
	
	public static final int COST_FIRST_1_ENCHANTS = 5;
	public static final int CAP_FIRST_1_ENCHANTS = 20;
	
	
	public static final int COST_FIRST_2_ENCHANTS = 5;
	public static final int CAP_FIRST_2_ENCHANTS = 30;
	
	
	public static final int COST_FIRST_3_ENCHANTS = 5;
	public static final int CAP_FIRST_3_ENCHANTS = 40;
	
	public static final double NORMAL_CHANCE_1_TO_BE_TIER_2 = 10;
	//public static final double MEDIUM_CHANCE_1_TO_BE_TIER_2 = 28;
	public static final double HIGH_CHANCE_1_TO_BE_TIER_2 = 65;
	
	//public static final double NORMAL_CHANCE_2_TO_BE_TIER_3 = 10;
	//public static final double MEDIUM_CHANCE_2_TO_BE_TIER_3 = 28;
	public static final double HIGH_CHANCE_2_TO_BE_TIER_3 = 60;
	
	public static final CALCULATION_MODE CRAFTING_SLOT_CALCULATION = CALCULATION_MODE.AVERAGE;
	
	public static final int BOOSTER_MIN_DIRECTIONS = 2;
	public static final int BOOSTER_MAX_DIRECTIONS = 4;
	
	public static final boolean CROSSBOW_CONSUME_ARROW_WITH_INFINITY = false;
	
	public static final boolean SWAPPER_ANIMATION = true;
	public static final int DELAY_SWAPPER_ANIMATION = 12; //ticks
	
	public static final double MENDING_INCREASE_BY_LEVEL = 0.2f;
	
	public static final boolean DISABLE_ENCHANTED_BOOKS_VILLAGERS = true;
	public static final boolean ENABLE_BUY_BOOSTERS_VILLAGERS = true;
	public static final boolean ENABLE_BUY_SLOT_ITEMS_VILLAGERS = true;
	public static final boolean ENABLE_SELL_BOOSTERS_FOR_TOOLS_ARMOR_VILLAGERS = true;
	
	public static final double UNLOCKING_NODES_RECURSIVE_REDUCE = 0.8; // 0.0-1.0f
	public static final int UNLOCKING_NODES_RECURSIVE_DEPTH = 5;
	public static final double UNLOCKING_NODES_RECURSIVE_START_CHANCE = 0.20; //0.0-1.0f
	
	public static final boolean SET_FOUND_ENCHANTED_BOOKS_LEVEL_ONE = true;
	public static final boolean ENABLE_MENDING_FOUND_ONLY_END = true;
	
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
