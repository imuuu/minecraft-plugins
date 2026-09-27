package me.imu.imusenchants.Managers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import me.imu.imusenchants.CONSTANTS;
import me.imu.imusenchants.Enchants.*;
import me.imu.imusenchants.Enums.MATERIAL_SLOT_RANGE;
import me.imu.imusenchants.Enums.TOUCH_TYPE;
import me.imu.imusenchants.Inventories.InventoryEnchanting;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.iAPI.Interfaces.IBUTTONN;
import imu.iAPI.Utilities.ItemUtilToolsArmors;
import imu.iAPI.Utilities.ItemUtilToolsArmors.ArmorMaterial;
import imu.iAPI.Utilities.ItemUtilToolsArmors.ToolMaterial;
import imu.iAPI.Utilities.ItemUtils;

public class ManagerEnchants
{
	public static ManagerEnchants Instance;
	//private ImusEnchants _main = ImusEnchants.Instance;

	public static final HashSet<Integer> REDSTRICTED_SLOTS = new HashSet<>(
			Arrays.asList(
					CONSTANTS.ENCHANT_ROWS * CONSTANTS.ENCHANT_COLUMNS - 3,
					CONSTANTS.ENCHANT_ROWS * CONSTANTS.ENCHANT_COLUMNS - 7,
					CONSTANTS.ENCHANT_ROWS * CONSTANTS.ENCHANT_COLUMNS - 13,
					CONSTANTS.ENCHANT_ROWS * CONSTANTS.ENCHANT_COLUMNS - 14,
					CONSTANTS.ENCHANT_ROWS * CONSTANTS.ENCHANT_COLUMNS - 15,
					CONSTANTS.ENCHANT_ROWS * CONSTANTS.ENCHANT_COLUMNS - 6,
					CONSTANTS.ENCHANT_ROWS * CONSTANTS.ENCHANT_COLUMNS - 4,
					CONSTANTS.ENCHANT_ROWS * CONSTANTS.ENCHANT_COLUMNS - 5

			));
	
	public static final Map<Enchantment, Integer> MAX_ENCHANT_LEVEL_CAP = new HashMap<>();

    static 
    {
        MAX_ENCHANT_LEVEL_CAP.put(Enchantment.LOOT_BONUS_BLOCKS, 4);
        MAX_ENCHANT_LEVEL_CAP.put(Enchantment.LOOT_BONUS_MOBS, 4);
        MAX_ENCHANT_LEVEL_CAP.put(Enchantment.DURABILITY, 4);
        MAX_ENCHANT_LEVEL_CAP.put(Enchantment.ARROW_INFINITE, 1);
        MAX_ENCHANT_LEVEL_CAP.put(Enchantment.SILK_TOUCH, 1);
        MAX_ENCHANT_LEVEL_CAP.put(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
        MAX_ENCHANT_LEVEL_CAP.put(Enchantment.PROTECTION_EXPLOSIONS, 4);
        MAX_ENCHANT_LEVEL_CAP.put(Enchantment.PROTECTION_FIRE, 4);
        MAX_ENCHANT_LEVEL_CAP.put(Enchantment.PROTECTION_PROJECTILE, 4);
    }

	public static final INode[] VALID_NODES = new INode[]
			{
			   new NodeBooster(),
			   new NodeEnchant(),
			   new NodeSwapper()
			};

	public ManagerEnchants()
	{
		Instance = this;
	}

	public void OpenEnchantingInventory(Player player)
	{
		new InventoryEnchanting().open(player);
	}

	public static int GetEnchantMaxLevelCap(Enchantment enchantment) 
	{
        return MAX_ENCHANT_LEVEL_CAP.getOrDefault(enchantment, -1);
    }
	
	public static MATERIAL_SLOT_RANGE GetMaterialSlotsRange(ItemStack stack)
	{
		return GetMaterialSlotsRange(stack.getType());
	}
	
//	public static ItemStack GetBooster(int power)
//	{
//		return NodeBooster.GetBoosterStack(power);
//	}
	
	public INode GetNode(ItemStack stack, EnchantedItem enchantedItem)
	{
		for(INode node : VALID_NODES)
		{
			if(node.IsValidGUIitem(TOUCH_TYPE.NONE, enchantedItem, stack))
			{
				if(node instanceof NodeBooster) return new NodeBooster(stack);
				if(node instanceof NodeEnchant) return new NodeEnchant(stack);
				if(node instanceof NodeSwapper) return new NodeSwapper();
			}
		}
		return null;
	}
	
	public INode GetNode(Class<?> nodeClass)
	{
		if(nodeClass.isInstance(NodeBooster.class)) return new NodeBooster();
		if(nodeClass.isInstance(NodeEnchant.class)) return new NodeEnchant();
		if(nodeClass.isInstance(NodeSwapper.class)) return new NodeSwapper();
		return null;
	}
	public boolean IsValidGUIitem(TOUCH_TYPE touchType, EnchantedItem enchantedItem, IBUTTONN button)
	{
		return IsValidGUIitem(touchType, enchantedItem, button.getItemStack());
	}
	public boolean IsValidGUIitem(TOUCH_TYPE touchType, EnchantedItem enchantedItem, ItemStack stack)
	{
		if(stack == null || stack.getType().isAir()) return false;
		
		for(INode node : VALID_NODES)
		{
			if(node.IsValidGUIitem(touchType, enchantedItem, stack))
			{
				return true;
			}
		}

		return false;

	}
	
	public static boolean IsInBounds(int x, int y) 
	{
        return x >= 0 && y >= 0 && x < CONSTANTS.ENCHANT_ROWS && y < CONSTANTS.ENCHANT_COLUMNS;
    }
	
	public static boolean IsInBounds(INode node) 
	{
		return IsInBounds(node.GetX(), node.GetY());
	}
	
	public static boolean IsValidToEnchant(ItemStack stack)
	{
		if (!(ItemUtils.IsTool(stack) || ItemUtils.IsArmor(stack)))
		{
			return false;
		}

		if (!EnchantedItem.HasSlots(stack) && stack.getEnchantments().size() > 0)
			return false;

		return !IsVoidUnenchantable(stack);
	}
	
	//Void stone support from imusVoidStones
	private static boolean IsVoidUnenchantable(ItemStack stack)
	{	
		return ItemUtils.GetPersistenData(stack, "VOID_UNENCHANTABLE", PersistentDataType.INTEGER) != null;
	}

	@SuppressWarnings("incomplete-switch")
	public static MATERIAL_SLOT_RANGE GetMaterialSlotsRange(Material material)
	{
		ToolMaterial toolMat = ItemUtilToolsArmors.GetToolMaterial(material);
		
		
		switch (toolMat)
		{
		case DIAMOND:
			return MATERIAL_SLOT_RANGE.DIAMOND;
		case GOLDEN:
			return MATERIAL_SLOT_RANGE.GOLD;
		case IRON:
			return MATERIAL_SLOT_RANGE.IRON;
		case NETHERITE:
			return MATERIAL_SLOT_RANGE.NETHERITE;
		case STONE:
			return MATERIAL_SLOT_RANGE.STONE;
		case WOODEN:
			return MATERIAL_SLOT_RANGE.WOOD;
		}
		
		ArmorMaterial armorMat = ItemUtilToolsArmors.GetArmorMaterial(material);
		
		switch (armorMat) 
		{
        case DIAMOND:
            return MATERIAL_SLOT_RANGE.DIAMOND;
        case GOLDEN:
            return MATERIAL_SLOT_RANGE.GOLD;
        case IRON:
            return MATERIAL_SLOT_RANGE.IRON;
        case NETHERITE:
            return MATERIAL_SLOT_RANGE.NETHERITE;
        case LEATHER:
            return MATERIAL_SLOT_RANGE.LEATHER;
        default:
            break;
		}
		
		switch (material) 
		{
        case TRIDENT:
            return MATERIAL_SLOT_RANGE.TRIDEN;
        case BOW:
            return MATERIAL_SLOT_RANGE.BOW;
        case CROSSBOW:
            return MATERIAL_SLOT_RANGE.CROSSBOW;
        case SHIELD:
            return MATERIAL_SLOT_RANGE.SHIELD;
        case FISHING_ROD:
            return MATERIAL_SLOT_RANGE.FISH_ROD;
        case ELYTRA:
            return MATERIAL_SLOT_RANGE.ELYTRA;
		}
		
		return MATERIAL_SLOT_RANGE.WOOD;
		
	}

}
