package me.imu.imusenchants.Inventories;



import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.InvUtil.CustomInventory;
import  me.imu.imusenchants.ImusEnchants;

public class TestINv extends CustomInventory
{
	public TestINv()
	{
		super(ImusEnchants.Instance, "testing", 6*9);
	}

	@Override
	public INVENTORY_AREA setInventoryLock()
	{
		return INVENTORY_AREA.UPPER_INV;
	}

	@Override
	public void onAwake()
	{

	}

	@Override
	public void onOpen()
	{
		super.onOpen();
		
		initButtons();
	}
	
	private int _pageID = 0;
	private final int _maxPages = 2;
	
	private void initButtons()
	{
		Button button;
		
		button = new Button(0, new ItemStack(Material.STONE), inventoryClickEvent -> 
		{
			switchPage();
	    });
		addButton(button);
		
		getButtonHandler().takeSnapshot("0");
		getButtonHandler().clearButtons();
		
		button = new Button(1, new ItemStack(Material.STONE), inventoryClickEvent -> 
		{
			switchPage();
	    });
		addButton(button);
		
		getButtonHandler().takeSnapshot("1");
		getButtonHandler().clearButtons();
		
		
		button = new Button(2, new ItemStack(Material.STONE), inventoryClickEvent -> 
		{
			switchPage();
	    });
		addButton(button);
		
		getButtonHandler().takeSnapshot("2");
		getButtonHandler().clearButtons();
		
		getButtonHandler().restoreSnapshot("0");
	}

	private void switchPage()
	{
		incrementPage();
		getButtonHandler().restoreSnapshot(String.valueOf(_pageID));
	}
	
	private void incrementPage()
	{
	    _pageID++;
	    if (_pageID > _maxPages) {
	        _pageID = 0;
	    }
	}

}
