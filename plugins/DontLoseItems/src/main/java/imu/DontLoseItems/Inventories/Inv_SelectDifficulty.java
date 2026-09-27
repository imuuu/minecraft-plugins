package imu.DontLoseItems.Inventories;


import java.util.Date;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import imu.DontLoseItems.Enums.DIFFICULT;
import imu.DontLoseItems.Managers.Manager_Difficult;
import imu.DontLoseItems.main.DontLoseItems;
import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.Interfaces.IBUTTONN;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Utilities.ItemUtils;

public class Inv_SelectDifficulty extends CustomInventory
{
	
	public Inv_SelectDifficulty()
	{
		super(DontLoseItems.Instance, "&9Select your difficulty, last 4 days", 3*9);
		
	}

	@Override
	public INVENTORY_AREA setInventoryLock()
	{
		return INVENTORY_AREA.UPPER_LOWER_INV;
	}

	@Override
	public void onOpen()
	{
		super.onOpen();
		intButtons();
	}
	
	@Override
	public void onClose()
	{
		super.onClose();
		
		Manager_Difficult.Instance.restorePortal(getPlayer().getUniqueId());
	}

	private void intButtons()
	{
		for(int i = 0; i < getSize(); i++)
		{
			IBUTTONN ibuttonn = getEmptyButton(i, Material.BLACK_STAINED_GLASS_PANE);
			addButton(ibuttonn);
		}
		
		ItemStack stack = new ItemStack(Material.IRON_HORSE_ARMOR);
		ItemUtils.SetDisplayName(stack, "&cNO FEAR SYSTEM");
		ItemUtils.SetLores(stack, DIFFICULT.getLore(DIFFICULT.NO_FEAR), false);
		Button button = new Button(9+2, stack, inventoryClickEvent ->
		{
			System.out.println("NO FEAR: "+getPlayer().getName());
			Manager_Difficult.Instance.setPlayerDifficulty(getPlayer().getUniqueId(), DIFFICULT.NO_FEAR, new Date());
			getButton(9+2).setEnableAction(false);
			OnButtonCloseInv();
			
	    });
		addButton(button);
		
		stack = new ItemStack(Material.DIAMOND_HORSE_ARMOR);
		ItemUtils.SetDisplayName(stack, "&4FEAR SYSTEM ENABLED");
		ItemUtils.SetLores(stack, DIFFICULT.getLore(DIFFICULT.FEAR), false);
		button = new Button(9+6, stack, inventoryClickEvent ->
		{
			System.out.println("FEAR! "+getPlayer().getName());
			Manager_Difficult.Instance.setPlayerDifficulty(getPlayer().getUniqueId(), DIFFICULT.FEAR, new Date());
			getButton(9+6).setEnableAction(false);
			OnButtonCloseInv();
	    });
		addButton(button);
		
		updateButtons(false);
	}
	
	private void OnButtonCloseInv()
	{
		new BukkitRunnable() 
		{
			
			@Override
			public void run()
			{
				getPlayer().closeInventory();
			}
		}.runTaskLater(DontLoseItems.Instance, 10);
	}
	

}
