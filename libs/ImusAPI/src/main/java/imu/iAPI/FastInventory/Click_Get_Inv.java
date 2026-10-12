package imu.iAPI.FastInventory;

import imu.iAPI.Utilities.InvUtil;
import imu.iAPI.Utilities.TextUtils;
import imu.iAPI.Utilities.ItemUtils;
import java.util.ArrayList;

import imu.iAPI.Other.CustomInvLayout;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Main.ImusAPI;

/**
 * @deprecated the old inventory system; new menus use {@link imu.iAPI.InvUtil.CustomInventory}
 */
@Deprecated
public class Click_Get_Inv extends CustomInvLayout
{
	public ArrayList<ItemStack> _stacks;
	
	private int _currentPage = 0;
	public Click_Get_Inv(Player player, String name, ArrayList<ItemStack> stacks)
	{
		super(ImusAPI._instance, player, name+ " &3=> &bClick &3to Copy", stacks.size());
		_stacks = stacks;
	}

	enum BUTTON implements IButton
	{
		NONE, 
		ITEM,
		GO_LEFT,
		GO_RIGHT
	}

	@Override
	public void invClosed(InventoryCloseEvent e)
	{

	}

	@Override
	public void onClickInsideInv(InventoryClickEvent e)
	{
		BUTTON button = GetButtonPress(e);
		Integer slot = GetSLOT(e.getCurrentItem());
		if (slot == null)
			return;
		
		int nextPage = 0;
		switch (button)
		{
		case ITEM:
			int index = slot+(_currentPage*(_size-9));
			ItemStack stack = _stacks.get(index).clone();
			int amount = 1;
			
			if(		e.getClick() == ClickType.RIGHT 
					&& !ItemUtils.IsArmor(stack)
					&& !ItemUtils.IsTool(stack)) amount = 64;
			
			InvUtil.AddItemOrDropGetSlots(stack, _player, amount);
			return;

		case NONE:
			return;
		case GO_LEFT:
			nextPage = PageChance(_currentPage, -1, _stacks.size(), _size-9);
			if(nextPage > _currentPage) return;
			_currentPage = nextPage;
			setupButtons();
			return;
		case GO_RIGHT:
			nextPage = PageChance(_currentPage, 1, _stacks.size(), _size-9);
			if(nextPage < _currentPage) return;
			_currentPage = nextPage;
			setupButtons();
			return;

		default:
			break;

		}
	}

	BUTTON GetButtonPress(InventoryClickEvent e)
	{
		ItemStack stack = e.getCurrentItem();
		if (stack == null)
			return BUTTON.NONE;

		String buttonName = getButtonName(e.getCurrentItem());
		if (buttonName == null)
			return BUTTON.NONE;

		return BUTTON.valueOf(buttonName);
	}

	@Override
	public void setupButtons()
	{
		ItemStack empty = ItemUtils.SetDisplayName(new ItemStack(Material.PURPLE_STAINED_GLASS_PANE), " ");
		boolean needPages = _stacks.size() >= 6*9-9 && _stacks.size() > 9;
		int theSize = needPages ? 6*9-9 : _size;
		for (int i = 0; i < theSize; i++)
		{
			int index = i+(_currentPage*(theSize));
			if (index >= _stacks.size())
			{

				SetITEM(i, empty);
				continue;
			}
			ItemStack stack = _stacks.get(index).clone();
			SetButton(stack, BUTTON.ITEM);
			SetITEM(i, stack);
		}
		
		if(!needPages) return;
		
		ItemStack redLine = ItemUtils.SetDisplayName(new ItemStack(Material.RED_STAINED_GLASS_PANE), " ");
		for(int i = 0 ; i < 9; i++)
		{
			
			SetButton(redLine, BUTTON.NONE);
			SetITEM(_size-i-1, redLine);
		}
		
		int indicator = _currentPage % (9-2);
		setupButton(BUTTON.NONE, Material.END_CRYSTAL, " ", _size-8+indicator);
		setupButton(BUTTON.GO_LEFT, Material.DARK_OAK_SIGN, TextUtils.Color("&9<< Next"), _size-9);
		setupButton(BUTTON.GO_RIGHT, Material.DARK_OAK_SIGN, TextUtils.Color("&9Next >>"), _size-1);
	}

	@Override
	public void openThis()
	{
		super.openThis();
		setupButtons();
	}

}
