package imu.iAPI.Utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemUtilToolsArmors
{

	private static Random _random = new Random();

	public interface ItemMaterial
	{
	}

	public enum ArmorMaterial implements ItemMaterial
	{
		LEATHER, IRON, GOLDEN, DIAMOND, NETHERITE, TURTLE, CHAIN, OTHER
	}

	public enum ToolMaterial implements ItemMaterial
	{
		WOODEN, STONE, IRON, GOLDEN, DIAMOND, NETHERITE, OTHER
	}

	public static final HashSet<Material> TOOLS = new HashSet<>(Arrays.asList(
			// Wooden tools
			Material.WOODEN_PICKAXE, Material.WOODEN_SHOVEL, Material.WOODEN_AXE, Material.WOODEN_HOE,
			Material.WOODEN_SWORD,

			// Stone tools
			Material.STONE_PICKAXE, Material.STONE_SHOVEL, Material.STONE_AXE, Material.STONE_HOE, Material.STONE_SWORD,

			// Iron tools
			Material.IRON_PICKAXE, Material.IRON_SHOVEL, Material.IRON_AXE, Material.IRON_HOE, Material.IRON_SWORD,

			// Golden tools
			Material.GOLDEN_PICKAXE, Material.GOLDEN_SHOVEL, Material.GOLDEN_AXE, Material.GOLDEN_HOE,
			Material.GOLDEN_SWORD,

			// Diamond tools
			Material.DIAMOND_PICKAXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_AXE, Material.DIAMOND_HOE,
			Material.DIAMOND_SWORD,

			// Netherite tools
			Material.NETHERITE_PICKAXE, Material.NETHERITE_SHOVEL, Material.NETHERITE_AXE, Material.NETHERITE_SWORD,
			Material.NETHERITE_HOE,

			// Other tools
			Material.SHIELD, Material.BOW, Material.CROSSBOW, Material.TRIDENT, Material.FISHING_ROD, Material.SHEARS));

	public static final HashSet<Material> TOOLS_IRON = new HashSet<>(Arrays.asList(Material.IRON_PICKAXE,
			Material.IRON_SHOVEL, Material.IRON_AXE, Material.IRON_HOE, Material.IRON_SWORD

	));

	public static final HashSet<Material> TOOLS_DIAMOND = new HashSet<>(Arrays.asList(Material.DIAMOND_PICKAXE,
			Material.DIAMOND_SHOVEL, Material.DIAMOND_AXE, Material.DIAMOND_HOE, Material.DIAMOND_SWORD

	));
	public static final HashSet<Material> TOOLS_NETHERITE = new HashSet<>(Arrays.asList(Material.NETHERITE_PICKAXE,
			Material.NETHERITE_SHOVEL, Material.NETHERITE_AXE, Material.NETHERITE_SWORD, Material.NETHERITE_HOE));

	public static final HashSet<Material> TOOLS_WOODEN = new HashSet<>(Arrays.asList(Material.WOODEN_PICKAXE,
			Material.WOODEN_SHOVEL, Material.WOODEN_AXE, Material.WOODEN_HOE, Material.WOODEN_SWORD));

	public static final HashSet<Material> TOOLS_STONE = new HashSet<>(Arrays.asList(Material.STONE_PICKAXE,
			Material.STONE_SHOVEL, Material.STONE_AXE, Material.STONE_HOE, Material.STONE_SWORD));

	public static final HashSet<Material> TOOLS_GOLDEN = new HashSet<>(Arrays.asList(Material.GOLDEN_PICKAXE,
			Material.GOLDEN_SHOVEL, Material.GOLDEN_AXE, Material.GOLDEN_HOE, Material.GOLDEN_SWORD));

	public static final HashSet<Material> TOOLS_OTHER = new HashSet<>(Arrays.asList(Material.SHIELD, Material.BOW,
			Material.CROSSBOW, Material.TRIDENT, Material.FISHING_ROD, Material.SHEARS));

	public static final HashSet<Material> ARMORS = new HashSet<>(Arrays.asList(
			// Leather armor
			Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,

			// Chainmail armor
			Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS,
			Material.CHAINMAIL_BOOTS,

			// Iron armor
			Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS,

			// Golden armor
			Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS,

			// Diamond armor
			Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,

			// Netherite armor
			Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS,
			Material.NETHERITE_BOOTS,

			// Other armors
			Material.TURTLE_HELMET, Material.ELYTRA));

	public static final HashSet<Material> ARMORS_IRON = new HashSet<>(
			Arrays.asList(Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS));

	public static final HashSet<Material> ARMORS_DIAMONDS = new HashSet<>(Arrays.asList(Material.DIAMOND_HELMET,
			Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS));
	public static final HashSet<Material> ARMORS_NETHERITE = new HashSet<>(Arrays.asList(Material.NETHERITE_HELMET,
			Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS));

	public static final HashSet<Material> ARMORS_LEATHER = new HashSet<>(Arrays.asList(Material.LEATHER_HELMET,
			Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS));

	public static final HashSet<Material> ARMORS_CHAINMAIL = new HashSet<>(Arrays.asList(Material.CHAINMAIL_HELMET,
			Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS));

	public static final HashSet<Material> ARMORS_GOLDEN = new HashSet<>(Arrays.asList(Material.GOLDEN_HELMET,
			Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS));

	public static boolean IsValid(ItemStack stack)
	{
		return stack != null && stack.getType() != Material.AIR;
	}

	public static boolean IsArmor(ItemStack stack)
	{
		if (!IsValid(stack))
			return false;

		return ARMORS.contains(stack.getType());
	}

	public static boolean IsTool(ItemStack stack)
	{
		if (!IsValid(stack))
			return false;

		return TOOLS.contains(stack.getType());

	}

	public static ItemStack GetRandomArmor()
	{
		List<Material> armorList = new ArrayList<>(ARMORS);
		return new ItemStack(armorList.get(_random.nextInt(armorList.size())));
	}

	public static ItemStack GetRandomTool()
	{
		List<Material> toolList = new ArrayList<>(TOOLS);
		return new ItemStack(toolList.get(_random.nextInt(toolList.size())));
	}

	public static ItemStack GetRandomToolOrArmor()
	{
		if (_random.nextBoolean())
		{
			return GetRandomTool();
		} else
		{
			return GetRandomArmor();
		}
	}
	
	public static ItemStack GetRandomToolOrArmor(ItemMaterial[] rolls) 
	{
        int roll = _random.nextInt(rolls.length);
        return RandomMaterial(rolls[roll]);
    }

	public static ItemStack RandomMaterial(ItemMaterial mat)
	{
		List<Material> list;
		if (mat instanceof ArmorMaterial)
		{

			switch ((ArmorMaterial) mat)
			{
			case DIAMOND:
			{
				list = new ArrayList<>(ARMORS_DIAMONDS);
				return new ItemStack(list.get(_random.nextInt(list.size())));
			}
			case IRON:
			{
				list = new ArrayList<>(ARMORS_IRON);
				return new ItemStack(list.get(_random.nextInt(list.size())));
			}

			case NETHERITE:
			{
				list = new ArrayList<>(ARMORS_NETHERITE);
				return new ItemStack(list.get(_random.nextInt(list.size())));
			}
			case GOLDEN:
			{
				list = new ArrayList<>(ARMORS_GOLDEN);
				return new ItemStack(list.get(_random.nextInt(list.size())));
			}
			case LEATHER:
			{
				list = new ArrayList<>(ARMORS_LEATHER);
				return new ItemStack(list.get(_random.nextInt(list.size())));
			}
			case CHAIN:
			{
				list = new ArrayList<>(ARMORS_CHAINMAIL);
				return new ItemStack(list.get(_random.nextInt(list.size())));
			}
			case OTHER:
				break;
			case TURTLE:
				return new ItemStack(Material.TURTLE_HELMET);

			default:
				break;

			}
		}

		if (mat instanceof ToolMaterial)
		{
			switch ((ToolMaterial) mat)
			{
			case DIAMOND:
			{
				list = new ArrayList<>(TOOLS_DIAMOND);
				return new ItemStack(list.get(_random.nextInt(list.size())));
			}
			case IRON:
			{
				list = new ArrayList<>(TOOLS_IRON);
				return new ItemStack(list.get(_random.nextInt(list.size())));
			}
			case NETHERITE:
			{
				list = new ArrayList<>(TOOLS_NETHERITE);
				return new ItemStack(list.get(_random.nextInt(list.size())));
			}
			case GOLDEN:
			{
				list = new ArrayList<>(TOOLS_GOLDEN);
				return new ItemStack(list.get(_random.nextInt(list.size())));
			}

			case STONE:
			{
				list = new ArrayList<>(TOOLS_STONE);
				return new ItemStack(list.get(_random.nextInt(list.size())));
			}
			case WOODEN:
			{
				list = new ArrayList<>(TOOLS_WOODEN);
				return new ItemStack(list.get(_random.nextInt(list.size())));
			}
			case OTHER:
				break;
			default:
				break;

			}
		}

		return null;
	}

	public static ArmorMaterial GetArmorMaterial(ItemStack stack)
	{
		if (!IsValid(stack))
			return ArmorMaterial.OTHER;
		return GetArmorMaterial(stack.getType());
	}

	public static ArmorMaterial GetArmorMaterial(Material material)
	{
		if (IsNetheriteArmor(material))
			return ArmorMaterial.NETHERITE;
		if (IsDiamondArmor(material))
			return ArmorMaterial.DIAMOND;
		if (IsIronArmor(material))
			return ArmorMaterial.IRON;
		if (IsGoldenArmor(material))
			return ArmorMaterial.GOLDEN;
		if (IsLeatherArmor(material))
			return ArmorMaterial.LEATHER;
		if (IsTurtleArmor(material))
			return ArmorMaterial.TURTLE;

		return ArmorMaterial.OTHER;
	}

	public static Material GetArmorMainMaterial(ItemStack stack)
	{
		switch (GetArmorMaterial(stack))
		{
		case DIAMOND:
			return Material.DIAMOND;
		case GOLDEN:
			return Material.GOLD_INGOT;
		case IRON:
			return Material.IRON_INGOT;
		case NETHERITE:
			return Material.NETHERITE_INGOT;
		case LEATHER:
			return Material.LEATHER;
		case TURTLE:
			return Material.SCUTE;
		case OTHER:
			return Material.DIAMOND;
		default:
			return Material.DIAMOND;
		}
	}

	public static boolean IsLeatherArmor(ItemStack stack)
	{
		if (!IsValid(stack))
			return false;
		return IsLeatherArmor(stack.getType());
	}

	public static boolean IsChainArmor(Material material)
	{
		return ARMORS_CHAINMAIL.contains(material);
	}

	public static boolean IsLeatherArmor(Material material)
	{
		return ARMORS_LEATHER.contains(material);
	}

	public static boolean IsIronArmor(Material material)
	{
		return ARMORS_IRON.contains(material);
	}

	public static boolean IsGoldenArmor(Material material)
	{
		return ARMORS_GOLDEN.contains(material);
	}

	public static boolean IsDiamondArmor(Material material)
	{
		return ARMORS_DIAMONDS.contains(material);
	}

	public static boolean IsNetheriteArmor(Material material)
	{
		return ARMORS_NETHERITE.contains(material);
	}

	public static boolean IsTurtleArmor(Material material)
	{
		switch (material)
		{
		case TURTLE_HELMET:
			return true;
		default:
			return false;
		}
	}

	public static boolean IsWoodenTool(ItemStack stack)
	{
		if (!IsValid(stack))
			return false;
		return IsWoodenTool(stack.getType());
	}

	public static boolean IsWoodenTool(Material material)
	{
		return TOOLS_WOODEN.contains(material);
	}

	public static boolean IsStoneTool(ItemStack stack)
	{
		if (!IsValid(stack))
			return false;
		return IsStoneTool(stack.getType());
	}

	public static boolean IsStoneTool(Material material)
	{
		return TOOLS_STONE.contains(material);
	}

	public static boolean IsIronTool(ItemStack stack)
	{
		if (!IsValid(stack))
			return false;
		return IsIronTool(stack.getType());
	}

	public static boolean IsIronTool(Material material)
	{
		return TOOLS_IRON.contains(material);
	}

	public static boolean IsGoldenTool(ItemStack stack)
	{
		if (!IsValid(stack))
			return false;
		return IsGoldenTool(stack.getType());
	}

	public static boolean IsGoldenTool(Material material)
	{
		return TOOLS_GOLDEN.contains(material);
	}

	public static boolean IsDiamondTool(ItemStack stack)
	{
		if (!IsValid(stack))
			return false;
		return IsDiamondTool(stack.getType());
	}

	public static boolean IsDiamondTool(Material material)
	{
		return TOOLS_DIAMOND.contains(material);
	}

	public static boolean IsNetheriteTool(ItemStack stack)
	{
		if (!IsValid(stack))
			return false;
		return IsNetheriteTool(stack.getType());
	}

	public static boolean IsNetheriteTool(Material material)
	{
		return TOOLS_NETHERITE.contains(material);
	}

	public static ToolMaterial GetToolMaterial(ItemStack stack)
	{
		if (!IsValid(stack))
			return ToolMaterial.OTHER;
		return GetToolMaterial(stack.getType());
	}

	public static ToolMaterial GetToolMaterial(Material material)
	{
		if (IsNetheriteTool(material))
			return ToolMaterial.NETHERITE;
		if (IsDiamondTool(material))
			return ToolMaterial.DIAMOND;
		if (IsIronTool(material))
			return ToolMaterial.IRON;
		if (IsGoldenTool(material))
			return ToolMaterial.GOLDEN;
		if (IsStoneTool(material))
			return ToolMaterial.STONE;
		if (IsWoodenTool(material))
			return ToolMaterial.WOODEN;

		return ToolMaterial.OTHER;
	}

	public static Material GetToolMainMaterial(ItemStack stack)
	{
		switch (GetToolMaterial(stack))
		{
		case DIAMOND:
			return Material.DIAMOND;
		case GOLDEN:
			return Material.GOLD_INGOT;
		case IRON:
			return Material.IRON_INGOT;
		case NETHERITE:
			return Material.NETHERITE_INGOT;
		case STONE:
			return Material.STONE;
		case WOODEN:
			return Material.OAK_WOOD;
		case OTHER:
			return Material.DIAMOND;
		default:
			return Material.DIAMOND;

		}
	}

}
