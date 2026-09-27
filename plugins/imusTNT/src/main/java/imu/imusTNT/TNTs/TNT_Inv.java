package imu.imusTNT.TNTs;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;
import imu.imusTNT.main.ImusTNT;

public class TNT_Inv extends CustomInvLayout
{

	public TNT_Inv(Player player)
	{
		super(ImusTNT.Instance, player, "&3Custom &4TNTs &3=> &bClick &3to Copy", 9);

	}

	enum BUTTON implements IButton
	{
		NONE, ITEM,
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

		switch (button)
		{
		case ITEM:
			ItemStack stack = TNT_Mananger.Instance.GetAllTnts().get(slot).GetItemStack();
			int amount = 1;
			
			if(e.getClick() == ClickType.RIGHT) amount = 64;
			
			Metods._ins.InventoryAddItemOrDrop(stack, _player, amount);
			return;

		case NONE:
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
		ItemStack empty = Metods.setDisplayName(new ItemStack(Material.PURPLE_STAINED_GLASS_PANE), " ");

		for (int i = 0; i < _size; i++)
		{
			if (i >= TNT_Mananger.Instance.GetAllTnts().size())
			{

				SetITEM(i, empty);
				continue;
			}
			ItemStack stack = TNT_Mananger.Instance.GetAllTnts().get(i).GetItemStack();
			SetButton(stack, BUTTON.ITEM);
			SetITEM(i, stack);
		}
	}

	@Override
	public void openThis()
	{
		super.openThis();
		setupButtons();
	}

}
