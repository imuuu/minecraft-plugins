package me.imu.imusenchants.Enchants;

import me.imu.imusenchants.Enums.TOUCH_TYPE;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Utilities.ItemUtilToolsArmors;
import imu.iAPI.Utilities.ItemUtils;
import imu.iAPI.Utilities.ItemUtils.DisplayNamePosition;
import me.imu.imusenchants.Enums.TOUCH_TYPE;
import me.imu.imusenchants.Inventories.InventoryEnchanting;
import me.imu.imusenchants.Managers.ManagerEnchants;
import me.imu.imusenchants.CONSTANTS;

public class NodeSwapper extends NodeDirectional
{
	private INode _swapped;
	@Override
	public int InitDirectionAmount()
	{
		return 1;
	}
	
	@Override
	public boolean IsValidGUIitem(TOUCH_TYPE touchType, EnchantedItem enchantedItem, ItemStack stack)
	{
		if (enchantedItem == null)
			return false;

		Material material = MainMaterial(enchantedItem);

		if (material.isAir())
			return false;

		return stack.getType() == material;

	}

	@Override
	public ItemStack GetGUIitemLoad(EnchantedItem enchantedItem)
	{
		ItemStack stack = new ItemStack(MainMaterial(enchantedItem));

		ItemUtils.AddTextToDisplayName(stack, " &8(&9Swapper&8)", DisplayNamePosition.BACK);
		ItemUtils.AddLore(stack, "&6Activate by &bM2", true);
		ItemUtils.AddLore(stack, "&9Swaps place random direction", true);
		ItemUtils.AddLore(stack, "&9Swapped &6slot &9is cover to", true);
		ItemUtils.AddLore(stack, "&cempty &6slot!", true);
		ItemUtils.SetTag(stack, InventoryEnchanting.PD_SWAPPER);
		
		return stack;
	}
	
	@Override
	public ItemStack GetGUIitemUnLoad(EnchantedItem enchantedItem, ItemStack stack)
	{
		return new ItemStack(MainMaterial(enchantedItem));
	}

	// The material the swapper is paid with, shown as and given back as. These used to differ
	// for armor, which turned a diamond into a netherite ingot.
	private static Material MainMaterial(EnchantedItem enchantedItem)
	{
		ItemStack item = enchantedItem.GetItemStack();
		return ItemUtils.IsTool(item)
				? ItemUtilToolsArmors.GetToolMainMaterial(item)
				: ItemUtilToolsArmors.GetArmorMainMaterial(item);
	}
	
	@Override
	public void Activate(EnchantedItem enchantedItem)
	{
		INode node = new Node(GetX(), GetY());
		node.SetLock(true);
		enchantedItem.SetNode(node, GetFlatIndex());
		int currentX = GetX();
		int currentY = GetY();
		switch (_directions[0])
		{
		case UP:
			currentX--;
			break;
		case DOWN:
			currentX++;
			break;
		case LEFT:
			currentY--;
			break;
		case RIGHT:
			currentY++;
			break;
		}

		if (currentX < 0 
				|| currentY < 0 
				|| currentX >= CONSTANTS.ENCHANT_ROWS 
				|| currentY >= CONSTANTS.ENCHANT_COLUMNS
				|| ManagerEnchants.REDSTRICTED_SLOTS.contains(Node.GetFlatIndex(currentX, currentY))
				|| enchantedItem.GetNodeBySlot(Node.GetFlatIndex(currentX, currentY)) instanceof NodeDirectional
				|| !enchantedItem.GetNodeBySlot(Node.GetFlatIndex(currentX, currentY)).IsLocked())
				
		{
			_swapped = ManagerEnchants.IsInBounds(currentX,currentY) ? 
					enchantedItem.GetNode(currentX, currentY) : null;
			
			enchantedItem.SetSlots(enchantedItem.Get_slots() - 1);
			return;
		}
		node = new Node(currentX, currentY); 
		node.SetLock(false);
		_swapped = node;
		enchantedItem.SetNode(node);

	}
	
	public INode GetSwappedNode()
	{
		return _swapped;
	}

	
}
