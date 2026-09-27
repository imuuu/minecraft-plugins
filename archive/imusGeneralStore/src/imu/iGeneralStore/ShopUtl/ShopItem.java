package imu.iGeneralStore.ShopUtl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import imu.iGeneralStore.ENUMs.ShopItemType;
import imu.iGeneralStore.Main.Main;
import imu.iGeneralStore.Other.ImusAPI;

public class ShopItem 
{
	ItemStack _real_stack;
	ItemStack _display_stack;
	
	ShopItemType _type = ShopItemType.NORMAL;
	
	int _amount = 0;
	Main _main;
	ImusAPI _ia;
	
	String lore_amount_str;
	
	boolean _isEmpty = false;
	Boolean _isArmorTool = null;
	int _slot = -1;
	
	ItemStack emptyDisplay;
	
	public ShopItem(Main main, int slot, ItemStack real, int amount) 
	{
		_main = main;
		_ia = main.get_ia();
		_real_stack = real.clone();
		emptyDisplay = _ia.setDisplayName(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), " ");
		setData(slot,real, amount);
	}
		
	public void setData(int slot,ItemStack real, int amount)
	{
		_display_stack = real.clone();
		_display_stack.setAmount(1);
		
		_amount = amount;
		
		_isArmorTool = null;
		_isEmpty = false;
		_slot = slot;
		
		loadLores();
		toolTip();
	}
	
	public int getSlot()
	{
		return _slot;
	}
	public void setSlot(int slot)
	{
		_slot = slot;
	}
	void loadLores()
	{
		lore_amount_str = _ia.msgC("&6Amount: &a");
	}
	
	void toolTip()
	{
		String[] lores = 
			{
				lore_amount_str + _amount //0
					
			};
		_ia.addLore(_display_stack, lores, false);
	}
	
	public boolean isToolArmor()
	{
		if(_isArmorTool == null)
			_isArmorTool = (_ia.isArmor(_real_stack) || _ia.isTool(_real_stack));
		
		return _isArmorTool;
	}
	
	public ItemStack getDisplayItem()
	{
		return _display_stack;
	}
	public ItemStack getRealItem()
	{
		return _real_stack;
	}
	
	public Boolean isSameKind(ShopItem item)
	{
		return item.getRealItem().isSimilar(_real_stack);
	}
	
	public Integer addAmount(int amount)
	{
		_amount+= amount;
		
		if(_amount <= 0)
		{
			_amount = 0;
			_isEmpty = true;

		}
		else
		{
			_isEmpty = false;
		}	
		_ia.reSetLore(_display_stack, lore_amount_str+_amount, 0);
		return _amount;
	}
	
	public boolean isEmpty()
	{
		return _isEmpty;
	}


	public int get_amount() {
		return _amount;
	}


	public void set_amount(int _amount) {
		this._amount = _amount;
	}
	
	
	
	
}
