package imu.GS.Invs;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import imu.GS.ENUMs.ModDataShopStockable;
import imu.GS.Main.Main;
import imu.GS.ShopUtl.ShopItemBase;
import imu.GS.ShopUtl.ShopItemModData;
import imu.GS.ShopUtl.ShopNormal;
import imu.GS.ShopUtl.ShopItems.ShopItemStockable;
import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;

public class ShopModINV extends CustomInvLayout
{
	
	private int unique_slots = 0;
	private int current_page = 0;
	
	private ShopNormal _shop;
	private Main _main;

	BukkitTask _runnable = null;
	
	public ShopModINV(Main main, Player player, ShopNormal shop) 
	{
		super(main, player, "Modding: " + shop.GetNameWithColor(), 9*6);
		_main = main;
		unique_slots = _size-9;
		_shop = shop;
		
	}

	
	
	enum BUTTON implements IButton
	{
		NONE,
		GO_LEFT,
		GO_RIGHT,
		SHOP_ITEM,
		SAVE_SHOP_DATA,
		OVERRIDE_ALL,
		MOD_SHOPBASE,
		RANDOM_ITEM_GEN,
		CLEAR_NORMAL_ITEMS,
		

	}
		
	int TotalPages()
	{
		return _shop.get_items().size()-1;
	}
	
	void chanceCurrentPage(int i)
	{
		current_page += i;
		if(current_page < 0)
		{
			current_page = 0;
		}
		if(current_page > TotalPages())
		{
			current_page = TotalPages();
		}
	}
	
	void makeInv()
	{
		ItemStack optionLine = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
		Metods.setDisplayName(optionLine, " ");
		
		
		for(int i = _shop.get_items().get(current_page).length; i < _size; ++i)
		{
			_inv.setItem(i, optionLine);
		}
				
		ItemStack left_button = new ItemStack(Material.BIRCH_SIGN);
		ItemStack right_button = left_button.clone();
		
		ItemStack saveAll_button = new ItemStack(Material.GOLD_INGOT);
		Metods.setDisplayName(saveAll_button, ChatColor.AQUA + "Save shop data to Database!");
		_metods.addLore(saveAll_button, ChatColor.BLUE + "Press this after you have edited some items", true);
		_metods.addLore(saveAll_button, ChatColor.BLUE + "Normally this will be done onDisabled", true);
		_metods.addLore(saveAll_button, ChatColor.BLUE + "If server crashes the onDisable never initialize(=data lost) ", true);
		
//		ItemStack override_button = new ItemStack(Material.PAPER);
//		Metods.setDisplayName(override_button, ChatColor.AQUA +"Override all");
//		_metods.addLore(override_button, ChatColor.BLUE + "Set same data to all", true);
//		_metods.addLore(override_button, ChatColor.BLUE + "if modify is none, it will be removed from all items too", true);
		
		
		ItemStack shopModBase = new ItemStack(Material.BEACON);
		Metods.setDisplayName(shopModBase, ChatColor.AQUA + "Change shop data");
		_metods.addLore(shopModBase, ChatColor.BLUE + "Able to chance ex: name,sellMul..", true);
		SetButton(shopModBase, BUTTON.MOD_SHOPBASE);
		
		ItemStack RemoveCrap = new ItemStack(Material.RED_DYE);
		Metods.setDisplayName(RemoveCrap, ChatColor.AQUA + "Clear shop from normal items");
		_metods.addLore(RemoveCrap, "&9Removes all items except &estockable &9items!", true);
		SetButton(RemoveCrap, BUTTON.CLEAR_NORMAL_ITEMS);
		_inv.setItem(_size-15, RemoveCrap);
		
		Metods.setDisplayName(left_button, ChatColor.AQUA + "<<");
		Metods.setDisplayName(right_button, ChatColor.AQUA + ">>");
		
		SetButton(left_button, BUTTON.GO_LEFT);
		SetButton(right_button, BUTTON.GO_RIGHT);
		SetButton(saveAll_button, BUTTON.SAVE_SHOP_DATA);
		//SetButton(override_button, BUTTON.OVERRIDE_ALL);
		
		_inv.setItem(unique_slots, left_button);
		_inv.setItem(unique_slots+2, saveAll_button);
		_inv.setItem(_size-1, right_button);
		//_inv.setItem(unique_slots+4, override_button);
		_inv.setItem(unique_slots+4, shopModBase);
		
		
		ItemStack stack = new ItemStack(Material.GOLD_ORE);
		Metods.setDisplayName(stack, "&6Generate Items");
		_metods.addLore(stack, "&5Open item generator inventory", false);
		_inv.setItem(_size-3, SetButton(stack, BUTTON.RANDOM_ITEM_GEN));
	}

	@Override
	public void openThis() 
	{
		super.openThis();
		_main.RegisterInv(this);
		_shop._temp_modifying_lock = true;
		makeInv();
		refreshItems();
	}
	
	void Runnable()
	{
		_runnable = new BukkitRunnable() 
		{
			
			@Override
			public void run() 
			{
				_shop._temp_modifying_lock = true;
				
				if(isCancelled())_shop._temp_modifying_lock = false;
			}
		}.runTaskTimerAsynchronously(_main, 0, 20);
	}
	@Override
	public void invClosed(InventoryCloseEvent arg0) 
	{		

		if(_runnable != null) _runnable.cancel();
		
		_shop._temp_modifying_lock = false;
		
	}
	
	BUTTON GetBUTTON(ItemStack stack)
	{
		if(stack == null) return BUTTON.NONE;
		String bName = getButtonName(stack);		
		
		//if(Strings.isNullOrEmpty(bName)) return BUTTON.NONE;
		if(bName == null || bName == "") return BUTTON.NONE;
		
		return BUTTON.valueOf(bName);
	}
	
	@Override
	public void onClickInsideInv(InventoryClickEvent e) 
	{
		ItemStack stack = e.getCurrentItem();

		BUTTON button = GetBUTTON(stack);

		switch (button) 
		{
		case NONE:			
			break;
		case SHOP_ITEM:
			ShopItemStockable item = (ShopItemStockable)_shop.GetItem(current_page, e.getSlot());
			new ShopStocableModifyINV(_main, _player, item, item.GetModData()).openThis();
			break;
		case GO_LEFT:				
			chanceCurrentPage(-1);
			refreshItems();
			return;

		case GO_RIGHT:
			chanceCurrentPage(1);
			refreshItems();
			return;
		case SAVE_SHOP_DATA:
			_main.get_shopManager().SaveShop(_shop.GetUUID(), false);
			_player.closeInventory();
			_player.sendMessage(ChatColor.GOLD + "(Shop) "+_shop.GetNameWithColor()+ " items has been saved!");
			break;
		case OVERRIDE_ALL:
			//_smm.openModShopModifyOVERRIDE_ALL_Inv(_player, stack, _shop, null);
			System.out.println("open override all");
			break;
		case MOD_SHOPBASE:
			_player.closeInventory();
			new ShopBaseModify(_main, _player, _shop).openThis();
			break;
		case RANDOM_ITEM_GEN:
			new ShopItemGeneratorInv(_main, _player, _shop).openThis();
			break;
		case CLEAR_NORMAL_ITEMS:
			ClearCrap();
			break;
		}
	}

	
	private void ClearCrap() 
	{
		
		_shop.ClearCrap();
		_player.closeInventory();
		_player.sendMessage(Metods.msgC("&3Shop &2cleared &3from crap! Inv opens shortly"));
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				new ShopModINV(_main, _player, _shop).openThis();
			}
		}.runTaskLater(_main, 15);
		
	}

	void SetTooltip(ItemStack stack, ShopItemModData _modData)
	{
		String modifyStr = ChatColor.YELLOW +"== Click to modify ==";
		ImusAPI._metods.addLore(stack, modifyStr, false);
		
		String custom_amount, c_permission, c_price ,c_worlds, c_fill_delay, c_fill_amount, c_soldDistance, c_selltime,c_tags;
		String none_color = ChatColor.RED + "";
		String true_color = ChatColor.AQUA + "";
		
		String none_str = none_color + "None";
		
		custom_amount =        _modData._maxAmount != -1 ?     		true_color+ _modData._maxAmount : none_str;
		c_permission =         _modData._permissions != null ? 		true_color+ ImusAPI._metods.CombineArrayToOneString(_modData._permissions.toArray(), "; ") : none_str;
		c_price =              _modData.GetValueStr(ModDataShopStockable.CUSTOM_PRICE, null,null,none_str);
		c_worlds =  	       _modData._worldNames != null ?     	true_color+ ImusAPI._metods.CombineArrayToOneString(_modData._worldNames.toArray(), "; ") : none_str;
		c_fill_delay = 		   _modData._fillDelayMinutes != -1 ? 	true_color+ _modData._fillDelayMinutes : none_str;
		c_fill_amount = 	   _modData._fillAmount != -1 ? 		true_color+ _modData._fillAmount :none_str;
		//c_soldBack =           _shop.getPDCustomCanSoldBack(stack) != null ? none_color+"false" : ChatColor.AQUA + "true";
		String[] disStr = _modData.GetValueStr(ModDataShopStockable.DISTANCE_LOC, null,null,none_str).split("; ");
		c_soldDistance = disStr.length > 1 ? true_color + "More than one" : disStr[0];		
		c_selltime =           _modData.GetValueStr(ModDataShopStockable.SELL_TIME_START, null,null,none_str);
		c_tags 					=_modData._tags != null ? 		true_color+ ImusAPI._metods.CombineArrayToOneString(_modData._tags.toArray(), "; ") : none_str;
		
		String color = ChatColor.BLUE+"";
		String color2 = ChatColor.YELLOW+"";
		
		ImusAPI._metods.addLore(stack, color +"Max stock amount: "+color2+custom_amount, true);
		ImusAPI._metods.addLore(stack, color +"Permission: "+color2+c_permission, true);
		ImusAPI._metods.addLore(stack, color +"Fill Delay: "+color2+c_fill_delay, true);
		ImusAPI._metods.addLore(stack, color +"Fill Amount: "+color2+c_fill_amount, true);
		ImusAPI._metods.addLore(stack, color +"Price: "+color2+c_price, true);
		ImusAPI._metods.addLore(stack, color +"World(s): "+color2+c_worlds, true);
		//ImusAPI._metods.addLore(stack, color +"Can be Sold: "+color2+c_soldBack, true);
		ImusAPI._metods.addLore(stack, color +"Sold Distance&Loc: "+color2+c_soldDistance, true);
		//ImusAPI._metods.addLore(stack, c_soldDistance, _isClosed)
		
		ImusAPI._metods.addLore(stack, color +"Sell time: "+color2+c_selltime, true);
		ImusAPI._metods.addLore(stack, color +"Tags: "+color2+c_tags, true);
		
		
		
	}
	ItemStack removeTooltip(ItemStack stack)
	{
		String modifyStr = ChatColor.YELLOW +"== Click to modify ==";
		return ImusAPI._metods.removeLore(stack, modifyStr);
	}
	
	void refreshItems()
	{

		ItemStack empty = Metods.setDisplayName(new ItemStack( Material.BLACK_STAINED_GLASS_PANE), " ");
		ItemStack copy;
		
		for(int slot = 0; slot < _shop.get_items().get(current_page).length; slot++)
		{
			
			ShopItemBase sib = _shop.GetItem(current_page, slot);
			if( sib == null)
			{
				_inv.setItem(slot, SetButton(empty, BUTTON.NONE));
				continue;
			}
			
			if(!(sib instanceof ShopItemStockable))
			{
				ItemStack regularItem = new ItemStack(sib.GetRealItem().getType());
				ImusAPI._metods.addLore(regularItem, "&cCan't be modified!", false);
				_inv.setItem(slot, SetButton(regularItem, BUTTON.NONE));
				continue;
			}
			
			ShopItemStockable sis = (ShopItemStockable) sib;
			
			copy = sis.GetRealItem().clone();
			SetTooltip(copy, sis.GetModData());
			_inv.setItem(slot, SetButton(copy, BUTTON.SHOP_ITEM));
			
		}
				
	}


	
	@Override
	public void setupButtons() {
		
		
	}
}
