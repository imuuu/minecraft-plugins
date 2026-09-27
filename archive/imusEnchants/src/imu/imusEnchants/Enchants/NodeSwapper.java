package imu.imusEnchants.Enchants;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Utilities.ItemUtilToolsArmors;
import imu.iAPI.Utilities.ItemUtils;
import imu.iAPI.Utilities.ItemUtils.DisplayNamePosition;
import imu.imusEnchants.Enums.TOUCH_TYPE;
import imu.imusEnchants.Inventories.InventoryEnchanting;
import imu.imusEnchants.Managers.ManagerEnchants;
import imu.imusEnchants.main.CONSTANTS;

public class NodeSwapper extends NodeDirectional
{
	private INode _swapped;
	@Override
	public int InitDirectionAmount()
	{
		return 1;
	}
	
	@Override
	public boolean IsValidGUIitem(TOUCH_TYPE touchType,EnchantedItem enchantedItem, ItemStack stack)
	{
		if (enchantedItem == null)
			return false;

		Material toolMateril = ItemUtilToolsArmors.GetToolMainMaterial(enchantedItem.GetItemStack());
		
		if (toolMateril.isAir())
			return false;

		if (stack.getType() != toolMateril)
			return false;
		
		
		return true;

	}

	@Override
	public ItemStack GetGUIitemLoad(EnchantedItem enchantedItem)
	{
		ItemStack stack = new ItemStack(ItemUtilToolsArmors.GetToolMainMaterial(enchantedItem.GetItemStack()));
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
		return new ItemStack(ItemUtilToolsArmors.GetToolMainMaterial(enchantedItem.GetItemStack()));
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
