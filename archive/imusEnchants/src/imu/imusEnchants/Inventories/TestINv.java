package imu.imusEnchants.Inventories;



import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.ENCHANTMENT_TIER;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.InvUtil.CustomInventory;
import imu.imusEnchants.main.ImusEnchants;

public class TestINv extends CustomInventory
{
	public TestINv()
	{
		super(ImusEnchants.Instance, "testing", 6*9);
	}

	@Override
	public INVENTORY_AREA SetInventoryLock()
	{
		return INVENTORY_AREA.UPPER_INV;
	}
	
	@Override
	public void OnOpen()
	{
		super.OnOpen();
		
		InitButtons();
	}
	
	private int _pageID = 0;
	private int _maxPages = 2;
	
	private void InitButtons()
	{
		Button button;
		
		button = new Button(0, new ItemStack(Material.STONE), inventoryClickEvent -> 
		{
			SwitchPage();
	    });
		AddButton(button);
		
		GetButtonHandler().TakeSnapshot("0");
		GetButtonHandler().ClearButtons();
		
		button = new Button(1, new ItemStack(Material.STONE), inventoryClickEvent -> 
		{
			SwitchPage();
	    });
		AddButton(button);
		
		GetButtonHandler().TakeSnapshot("1");
		GetButtonHandler().ClearButtons();
		
		
		button = new Button(2, new ItemStack(Material.STONE), inventoryClickEvent -> 
		{
			SwitchPage();
	    });
		AddButton(button);
		
		GetButtonHandler().TakeSnapshot("2");
		GetButtonHandler().ClearButtons();
		
		GetButtonHandler().RestoreSnapshot("0");
	}

	private void SwitchPage()
	{
		IncrementPage();
		GetButtonHandler().RestoreSnapshot(String.valueOf(_pageID));
	}
	
	private void IncrementPage() 
	{
	    _pageID++;
	    if (_pageID > _maxPages) {
	        _pageID = 0;
	    }
	}

}
