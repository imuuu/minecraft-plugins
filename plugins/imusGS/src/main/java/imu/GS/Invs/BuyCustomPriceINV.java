package imu.GS.Invs;


import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import imu.GS.Main.Main;
import imu.GS.Other.CustomPriceData;
import imu.GS.Other.LogData;
import imu.GS.ShopUtl.Shop;
import imu.GS.ShopUtl.ShopItemBase;
import imu.GS.ShopUtl.ShopNormal;
import imu.GS.ShopUtl.Customer.ShopItemCustomer;
import imu.GS.ShopUtl.ItemPrice.PriceCustom;
import imu.GS.ShopUtl.ShopItems.ShopItemSeller;
import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;

public class BuyCustomPriceINV extends CustomerInv
{
	ShopNormal _shopBase;
	ShopItemSeller _sis;
	Main _main;
	
	int _selected_amount = 1;
	int _last_selected_amount = 1;
	
	BukkitTask runnable;
	int _maxAmount = 0;
	int _buyAmounts[] = new int[7];
	
	private final ItemStack[] normal_stacks = new ItemStack[]{
			new ItemStack(Material.IRON_BLOCK),
			new ItemStack(Material.COPPER_BLOCK),
			new ItemStack(Material.GOLD_BLOCK),
			new ItemStack(Material.DIAMOND_BLOCK),
			new ItemStack(Material.EMERALD_BLOCK),
			new ItemStack(Material.NETHERITE_BLOCK),
			new ItemStack(Material.CRYING_OBSIDIAN)};
	
	private final ItemStack[] larger_stacks = new ItemStack[]{
			new ItemStack(Material.WHITE_WOOL),
			new ItemStack(Material.YELLOW_WOOL),
			new ItemStack(Material.GREEN_WOOL),
			new ItemStack(Material.BLUE_WOOL),
			new ItemStack(Material.PURPLE_WOOL),
			new ItemStack(Material.BROWN_WOOL),
			new ItemStack(Material.BLACK_WOOL)};
	
	LinkedList<LogData> logs = new LinkedList<>();
	
	public BuyCustomPriceINV(Plugin main, Player player, ShopNormal shopBase, ShopItemSeller sis) {
		super(main, player, "Buying "+ImusAPI._metods.GetItemDisplayName(sis.GetDisplayItem()), 6*9);
		_main=(Main)main;
		_shopBase = shopBase;
		_sis = sis;
		_sis.RegisterSlot(_inv, this, -1, _size-5, true);
		_main.RegisterInv(this);
		_maxAmount = _sis.Get_amount();
		_selected_amount = ((PriceCustom)_sis.GetItemPrice()).GetMinimumStackAmount();
		
		MenuToolTip();
		setupButtons();
		SetShopSlot(_sis, 0, _size-5);
		CheckSelectedAmount();
		RefreshBuyStacks();
	}
	
	enum BUTTON implements IButton
	{
		NONE,
		BACK,
		CONFIRM,
		CHANGE_AMOUNT;
	}

	@Override
	public void invClosed(InventoryCloseEvent e) 
	{
		_sis.UnRegisterSlot(_inv);
		_main.UnregisterInv(this);
		if(runnable != null)
		{
			runnable.cancel();
		}
		_main.get_shopManager().SendLogs(_player, logs);
		
	}
	
	final void MenuToolTip()
	{
		for(int i = _size-9*3; i < _size; i++) {setupButton(BUTTON.NONE, Material.BLACK_STAINED_GLASS_PANE," ", i);}
		setupButton(BUTTON.BACK, Material.RED_STAINED_GLASS_PANE,"&c<= BACK", _size-9);
		ItemStack stack = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
		Metods.setDisplayName(stack, "&9BUY");
		_metods.addLore(stack, new String[] { "&bM1: &aBuying &9and going &cback &9to Shop menu!","&bM2: &aBuying &9and staying &6this &9menu"});
		_inv.setItem(_size-1, SetButton(stack, BUTTON.CONFIRM));
		
		
		setupButton(BUTTON.NONE, Material.PURPLE_STAINED_GLASS_PANE, "&eYou are buying this &b==>", _size-6);
		setupButton(BUTTON.NONE, Material.PURPLE_STAINED_GLASS_PANE, "&b<== &eYou are buying this", _size-4);
	}
	
	@Override
	public void setupButtons() 
	{
		for(int i = _size-9*3; i < _size-9; i++) {setupButton(BUTTON.NONE, Material.BLACK_STAINED_GLASS_PANE," ", i);}
		
		ItemStack[] stacks;
		int minimumAmount = ((PriceCustom)_sis.GetItemPrice()).GetMinimumStackAmount();
		if( minimumAmount <= 1)
		{
			_buyAmounts = new int[]{1, 4, 8, 16, 32, 64, _sis.Get_amount()};
			stacks = normal_stacks;
		}
		else
		{
			_buyAmounts = new int[]{1 * minimumAmount, 2* minimumAmount, 3* minimumAmount, 4* minimumAmount, 5* minimumAmount, 6* minimumAmount, _sis.Get_amount()};
			stacks = larger_stacks; 
		}
		
		
		
		for(int i = 0; i < stacks.length; i++)
		{
			ItemStack clone =  stacks[i].clone();
			if(_sis.Get_amount() < _buyAmounts[i] )
			{
				SetUnableBuyButton(_size-26+i, _buyAmounts[i], "&c&lNot Enough in Stock! &8(&7In Stock:&3 "+_sis.Get_amount()+"&8)");
				continue;
			}

			
			if(!CheckIfPlayerHasItems(_buyAmounts[i], false))
			{
				//SetUnableBuyButton(_size-26+i, _buyAmounts[i], "&c&lYou dont have enough items");
				ImusAPI._metods.addLore(clone, "&c&lYou dont have enough items!", false);
				ImusAPI._metods.AddGlow(clone);
			}
				
			Metods.setDisplayName(clone, "&9BUY&6 "+_buyAmounts[i]);
			ImusAPI._metods.setPersistenData(clone, "buyAmount", PersistentDataType.INTEGER, _buyAmounts[i]);
			_inv.setItem(_size-26+i, SetButton(clone, BUTTON.CHANGE_AMOUNT));
		}

		
	}
	
	@Override
	public void SetShopSlot(ShopItemSeller sis, int page, int slot) 
	{
		_sis = sis;
		_maxAmount = sis.Get_amount();
		ItemStack displayItem =  _sis.GetRealItem().clone();
		SetButton(displayItem, BUTTON.NONE);
		ImusAPI._metods.addLore(displayItem, "&9Minimum amount to buy: &a"+((PriceCustom)_sis.GetItemPrice()).GetMinimumStackAmount(), false);
		ImusAPI._metods.addLore(displayItem, "&9Stock Amount: &a"+_sis.Get_amount(), false);
		
		_inv.setItem(slot, displayItem);
		
		setupButtons();
		
	}
	
	void SetUnableBuyButton(int slot, int amount, String reason)
	{
		ItemStack unable = Metods.setDisplayName(new ItemStack(Material.BARRIER), "&cUnable to Buy: &4"+amount);
		ImusAPI._metods.addLore(unable, reason, false);
		_inv.setItem(slot, SetButton(unable, BUTTON.NONE));
	}
	
	void RefreshBuyStacks()
	{
		if(runnable != null)
		{
			runnable.cancel();
		}
		
		runnable = new BukkitRunnable() 
		{
			
			@Override
			public void run() 
			{
				PriceCustom pc = (PriceCustom)_sis.GetItemPrice();
				CustomPriceData[] data = pc.GetItems();
				
				ItemStack stack;
				int i = 0;
				for(; i < data.length; ++i)
				{
					if(i >= 27) break;
					
					int inv_count = 0;					
					stack = data[i]._stack.clone();
					
					for(ItemStack invStack : _player.getInventory().getContents()) 
					{
						if(invStack == null) continue; 
						if(invStack.isSimilar(stack)) inv_count+= invStack.getAmount();
					}
					int amount = data[i]._amount * _selected_amount / pc.GetMinimumStackAmount();
					
					String color = inv_count >= amount ? "&a" : "&c";
					ImusAPI._metods.addLore(stack, "&eYou have: "+ color + inv_count, false);
					ImusAPI._metods.addLore(stack, "&9Cost you:&6 "+ amount + " &e(&8"+data[i]._amount+" &3x &8"+_selected_amount/pc.GetMinimumStackAmount()+"&e)", false);
					
					_inv.setItem(i, stack);		
				}
				if(((PriceCustom)_sis.GetItemPrice()).GetPrice() > 0)
				{
					stack = new ItemStack(Material.PAPER);
					Metods.setDisplayName(stack, "&6Extra values");
					double player_balance = _main.get_econ().getBalance(_player);
					double itemPrice = ((PriceCustom)_sis.GetItemPrice()).GetPrice();
					_metods.addLore(stack, new String[] {"&9Money&6: &a"+Metods.Round(itemPrice)+ "&2$","&eYour have: "+(player_balance > itemPrice ? "&a":"&c")+Metods.Round(player_balance)+" &2$"});
					_inv.setItem(i, _metods.AddGlow(stack));
				}
			}
		}.runTaskAsynchronously(_main);	
	}
	
	
	
	boolean CheckIfPlayerHasItems(int selAmount, boolean removeItemsFromPlayerrInv)
	{
		//double moneyNeeded = ((PriceCustom)_sis.GetItemPrice()).GetPrice();
		
		//if(moneyNeeded < money)
		
		
		if(!_shopBase.BuyConfirmation(_player, _sis,_sis.GetItemPrice().GetCustomerPrice(1) ,1, false))
		{			
			return false;
		}

		PriceCustom pc = (PriceCustom)_sis.GetItemPrice();
		CustomPriceData[] items = pc.GetItems();
		CustomPriceData[] datas = new CustomPriceData[items.length];
		for(int i = 0; i < items.length; i++) {datas[i] = new CustomPriceData(items[i]._stack.clone(), items[i]._amount * selAmount / pc.GetMinimumStackAmount());}
		//ArrayList<ItemStack> ref_stacks = new ArrayList<>();
		ItemStack[] newStacks = new ItemStack[_player.getInventory().getContents().length];
		
		int i = 0;
		for(ItemStack stack : _player.getInventory().getContents())
		{
			ItemStack copy = null;
			if(stack != null)
			{
				copy = stack.clone();
				for(CustomPriceData data : datas)
				{
					if(data._stack.isSimilar(copy) && data._amount > 0) 
					{			
						data._amount -= copy.getAmount();
						if(data._amount < 0)
						{
							copy.setAmount(Math.abs(data._amount));
						}
						else
						{					
							copy.setAmount(0);
						}						
					}
				}
			}
			
			
			newStacks[i++] = copy;
		}
		
		for(CustomPriceData data : datas) { if(data._amount > 0) return false; }
		
		//System.out.println("setting data!");
		if(removeItemsFromPlayerrInv)
			_player.getInventory().setContents(newStacks);
		

		_shopBase.BuyConfirmation(_player, _sis, _sis.GetItemPrice().GetCustomerPrice(1),1, removeItemsFromPlayerrInv);	
		return true;
	}
	
	void CheckSelectedAmount()
	{
		for(int i = _size-18; i < _size-9; ++i) {_inv.setItem(i, Metods.setDisplayName(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), " "));}
		int slot = -1;
		int offSet = _size-26+9;

		
		if(_buyAmounts[0] == _selected_amount) slot = offSet;
		if(_buyAmounts[1] == _selected_amount) slot = offSet+1;
		if(_buyAmounts[2] == _selected_amount) slot = offSet+2;
		if(_buyAmounts[3] == _selected_amount) slot = offSet+3;
		if(_buyAmounts[4] == _selected_amount) slot = offSet+4;
		if(_buyAmounts[5] == _selected_amount) slot = offSet+5;
		
		if(_selected_amount == _maxAmount && slot == -1 ){slot = offSet+6;}
		
		if(slot == -1) return;
		
		_inv.setItem(slot, Metods.setDisplayName(new ItemStack(Material.REDSTONE_TORCH), "&a^SELECTED^") );
		
	}
	BUTTON GetButtonPress(InventoryClickEvent e)
	{
		ItemStack stack = e.getCurrentItem();
		if(stack == null)
			return BUTTON.NONE;
		
		String buttonName = getButtonName(e.getCurrentItem());
		if(buttonName == null)
			return BUTTON.NONE;
		
		return BUTTON.valueOf(buttonName);
	}
	
	void Back()
	{
		Shop shop = _main.get_shopManager().GetShop(_shopBase.GetName());
		_player.closeInventory();
		shop.Open(_player);	
	}
	
	@Override
	public void onClickInsideInv(InventoryClickEvent e) 
	{
		BUTTON button = GetButtonPress(e);
		
		switch (button) 
		{
		case NONE:
			return;
		case BACK:
			Back();
			break;
		case CHANGE_AMOUNT:
			_last_selected_amount = _selected_amount;
			_selected_amount = ImusAPI._metods.getPersistenData(e.getCurrentItem(), "buyAmount", PersistentDataType.INTEGER);
			if(_last_selected_amount == _selected_amount) return;
			CheckSelectedAmount();
			RefreshBuyStacks();
			break;
		case CONFIRM:
			
			ShopItemBase sib = _shopBase.GetItem(_sis.GetPage(), _sis.GetSlot());

			int amount = _selected_amount;// * ((PriceCustom)sib.GetItemPrice()).GetMinimumStackAmount();
			if(sib.Get_amount() < amount)
			{
				amount = sib.Get_amount();				
			}
			
			if(amount < ((PriceCustom)sib.GetItemPrice()).GetMinimumStackAmount()) return;
			
			
			if(amount != _selected_amount)
			{
				_player.sendMessage(Metods.msgC("&cYou were able to buy only &2"+amount+" &cbecause there weren't enough items in stock!"));
				return;
			}
			
			
			if(!CheckIfPlayerHasItems(amount, true)) return;
			
			
			ImusAPI._metods.InventoryAddItemOrDrop(sib.GetRealItem().clone(), _player, amount);
			
			
			sib.AddAmount(amount*-1);
			sib.UpdateItem();
			
			ShopItemCustomer sic = new ShopItemCustomer(_main, _shopBase, _player, sib.GetRealItem().clone(), amount);
			_main.get_shopManager().LogRegisterPurchace(logs, new LogData(sic, sic.GetItemPrice().GetCustomerPrice(1), amount));
			
			if(e.getClick() == ClickType.LEFT) {Back();return;};
			
			
			CheckSelectedAmount();
			RefreshBuyStacks();
			break;

		}
		
	}
	
	
	

}
