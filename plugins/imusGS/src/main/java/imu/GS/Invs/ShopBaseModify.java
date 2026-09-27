package imu.GS.Invs;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import imu.GS.ENUMs.ModDataShop;
import imu.GS.Interfaces.IModData;
import imu.GS.Main.Main;
import imu.GS.Prompts.ConvShopMod;
import imu.GS.ShopUtl.Shop;
import imu.GS.ShopUtl.ShopModData;
import imu.GS.ShopUtl.ShopNormal;
import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;


public class ShopBaseModify extends CustomInvLayout
{
	private ShopNormal _shopBase;
	private ShopModData _shopModData;
	private Main _main;
	
	BukkitTask _runnable;
	
	public ShopBaseModify(Plugin main, Player player, ShopNormal shopBase) {
		super(main, player, "&6Modifying Shop: "+shopBase.GetDisplayName(), 9*3);
		_shopBase = shopBase;
		_main = (Main)main;		
		SetModData(new ShopModData().ReadShop(shopBase));
		
		
	}

	enum BUTTON implements IButton
	{
		NONE,
		BACK,
		CONFIRM,
		SHOP_NAME,
		//SHOP_DISPLAYNAME,
		SHOP_SELL_MUL,
		SHOP_BUY_MUL,
		SHOP_EXPIRE_PERCENT,
		SHOP_EXPIRE_COOLDOWN,
		SHOP_LOCKED,
		SHOP_ABSOLUTE_POS,
		SHOP_REMOVE,
		CANCEL_REMOVE,
		CUSTOMERS_CAN_ONLY_BUY
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
	
	public void SetModData(IModData shopModData)
	{
		_shopModData = (ShopModData)shopModData;		
	}
	
	@Override
	public void setupButtons() 
	{
		for(int i = 0; i < _size; ++i){_inv.setItem(i, Metods.setDisplayName(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), " "));}
		
		setupButton(BUTTON.BACK, Material.RED_STAINED_GLASS_PANE, "&b<= &cBACK", _size-9);
		setupButton(BUTTON.CONFIRM, Material.GREEN_STAINED_GLASS_PANE, "&bCONFIRM", _size-1);
		
		String m1m2 = Metods.msgC("&bM1: &aSet &bM2: &cDefault");
		String setTo = Metods.msgC("&6Set To &9");
		
		ItemStack stack = new ItemStack(Material.PAPER);
		Metods.setDisplayName(stack, "&6Rename shop name");
		_metods.addLore(stack, setTo+"&r" +_shopModData.GetValueStr(ModDataShop.NAME, "&2", "", "NONE"), false);_metods.addLore(stack, m1m2, false);	
		_inv.setItem(0, SetButton(stack, BUTTON.SHOP_NAME));
		
		stack = new ItemStack(Material.GOLD_INGOT);
		Metods.setDisplayName(stack, "&6Sell Multiplier");
		_metods.addLore(stack, setTo +_shopModData.GetValueStr(ModDataShop.SELL_MUL, "&2", "", "NONE"), false);_metods.addLore(stack, m1m2, false);	
		_inv.setItem(2, SetButton(stack, BUTTON.SHOP_SELL_MUL));
		
		stack = new ItemStack(Material.IRON_INGOT);
		Metods.setDisplayName(stack, "&6Buy Multiplier");
		_metods.addLore(stack, setTo +_shopModData.GetValueStr(ModDataShop.BUY_MUL, "&2", "", "NONE"), false);_metods.addLore(stack, m1m2, false);	
		_inv.setItem(4, SetButton(stack, BUTTON.SHOP_BUY_MUL));
		
		stack = new ItemStack(Material.STICKY_PISTON);
		Metods.setDisplayName(stack, "&6Absolute position");
		_metods.addLore(stack, setTo +_shopModData.GetValueStr(ModDataShop.ABSOLUTE_POS, "&2", "", "NONE"), false);_metods.addLore(stack, m1m2, false);	
		_inv.setItem(6, SetButton(stack, BUTTON.SHOP_ABSOLUTE_POS));
		
		stack = new ItemStack(Material.TRIPWIRE_HOOK);
		Metods.setDisplayName(stack, "&6Lock");
		_metods.addLore(stack, setTo +_shopModData.GetValueStr(ModDataShop.LOCKED, "&2", "", "NONE"), false);_metods.addLore(stack, m1m2, false);		
		_inv.setItem(8, SetButton(stack, BUTTON.SHOP_LOCKED));
		
		stack = new ItemStack(Material.COMPASS);
		Metods.setDisplayName(stack, "&6Expire percent");
		_metods.addLore(stack, setTo +_shopModData.GetValueStr(ModDataShop.EXPIRE_PERCENT, "&2", "", "NONE"), false);_metods.addLore(stack, m1m2, false);	
		_inv.setItem(10, SetButton(stack, BUTTON.SHOP_EXPIRE_PERCENT));
		
		stack = new ItemStack(Material.CLOCK);
		Metods.setDisplayName(stack, "&6Expire Cooldown");
		_metods.addLore(stack, setTo +_shopModData.GetValueStr(ModDataShop.EXPIRE_COOLDOWN, "&2", "", "NONE"), false);_metods.addLore(stack, m1m2, false);	
		_inv.setItem(12, SetButton(stack, BUTTON.SHOP_EXPIRE_COOLDOWN));
		
		stack = new ItemStack(Material.IRON_BARS);
		Metods.setDisplayName(stack, "&6Block Customer Sell");
		_metods.addLore(stack, setTo +_shopModData.GetValueStr(ModDataShop.CUSTOMERS_CAN_ONLY_BUY, "&2", "", "NONE"), false);_metods.addLore(stack, m1m2, false);	
		_inv.setItem(14, SetButton(stack, BUTTON.CUSTOMERS_CAN_ONLY_BUY));
		
		stack = new ItemStack(Material.BARRIER);
		Metods.setDisplayName(stack, "&l&6Remove This Shop");
		_metods.addLore(stack, "&eConfirmation Needed!", false);
		_metods.addLore(stack, setTo +_shopModData.GetValueStr(ModDataShop.REMOVE_SHOP, "&2", "", "NONE"), false);_metods.addLore(stack, m1m2, false);	
		_inv.setItem(_size-7, SetButton(stack, BUTTON.SHOP_REMOVE));
	}

	
	void Back()
	{	
		_player.closeInventory();
		new ShopModINV(_main, _player, _shopBase).openThis();
	}
	
	void Confirm()
	{		
		Shop s = _main.get_shopManager().GetShop(_shopBase.GetUUID());
		
		if(s == null)
		{
			_player.sendMessage(ChatColor.RED + "Couldn't find shop named as "+_shopBase.GetName());
			return;
		}
		
		if(!(s instanceof ShopNormal)) return;
		
		ShopNormal shop = (ShopNormal)s;
		
		if(_shopModData._removeShop)
		{
			_main.get_shopManager().RemoveShop(shop.GetUUID());
			_player.sendMessage(Metods.msgC("&9Shop named "+shop.GetName()+" &9has been &cremoved"));
			_player.closeInventory();
			_main.get_shopManager().UpdateTabCompliters();
			return;
		}
		
		shop.set_sellM(_shopModData._sellMultiplier);
		shop.set_buyM(_shopModData._buyMultiplier);
		shop.set_expire_percent(_shopModData._expire_percent);
		shop.set_expire_cooldown_m(_shopModData._expire_cooldown_minutes);
		shop.SetLocked(_shopModData._lock);
		shop.SetAbsolutePosBool(_shopModData._absoluteItemPosition);
		shop.SetCustomersCanOnlyBuy(_shopModData._customersCanOnlyBuy);
		shop.RefreshPrices();
		if(!shop.GetDisplayName().equalsIgnoreCase(_shopModData._displayName)) 
		{
			String oldName = shop.GetName();
			_player.sendMessage(Metods.msgC("&2 Shop name has been changed from "+shop.GetDisplayName()+ "&2 to "+_shopModData._displayName));
			_player.sendMessage(Metods.msgC("&e Shop data has been saved to database!"));			
			shop.SetName(_shopModData._displayName);
			if(_main.GetDenizenSCreator().IsFileExist())
			{
				_main.GetDenizenSCreator().RenameShop(oldName, shop.GetName());
				_player.sendMessage(Metods.msgC("&9Remember &2reload &9DenizenScripts! =>  &b/ex reload!"));
			}
			_main.get_shopManager().AddShop(shop);
			shop.SaveDataAsync();
			
		}
		
		_player.closeInventory();
		new ShopModINV(_main, _player, shop).openThis();
	}
	
	
	@Override
	public void openThis() 
	{
		super.openThis();
		_main.RegisterInv(this);
		setupButtons();
	}
	@Override
	public void invClosed(InventoryCloseEvent e) {
		_main.UnregisterInv(this);
		if(_runnable != null) _runnable.cancel();
		
	}

	@Override
	public void onClickInsideInv(InventoryClickEvent e) 
	{
		BUTTON button = GetButtonPress(e);
		
		ConversationFactory cf = null;
		String question = null;
		Conversation conv = null;
		ModDataShop value = null;
		
		if(button != BUTTON.NONE) cf = new ConversationFactory(_main);
		
		switch(button)
		{
		case BACK:
			Back();
			return;
		case CONFIRM:
			Confirm();
			return;
		case NONE:
			break;
		case SHOP_NAME:
			question = Metods.msgC("&6Give shop's new name. You are able to use colors with color code( & )");
			value = ModDataShop.NAME;
			break;
		case SHOP_BUY_MUL:
			question = Metods.msgC("&6Give shop &9buy &6multiplier");
			value = ModDataShop.BUY_MUL;
			break;
		case SHOP_SELL_MUL:
			question = Metods.msgC("&6Give shop &aSell &6multiplier");
			value = ModDataShop.SELL_MUL;
			break;
		case SHOP_EXPIRE_COOLDOWN:
			question = Metods.msgC("&6Give Expire Cooldown in &aminutes");
			value = ModDataShop.EXPIRE_COOLDOWN;
			break;
		case SHOP_EXPIRE_PERCENT:
			question = Metods.msgC("&6Give Expire percent");
			value = ModDataShop.EXPIRE_PERCENT;
			break;
		case SHOP_LOCKED:
			_shopModData.SetAndCheck(ModDataShop.LOCKED, "");
			setupButtons();
			break;		
		case SHOP_ABSOLUTE_POS:
			_shopModData.SetAndCheck(ModDataShop.ABSOLUTE_POS, "");
			setupButtons();
			break;
		case SHOP_REMOVE:
			_shopModData.SetAndCheck(ModDataShop.REMOVE_SHOP, "");
			if(_shopModData._removeShop) {GenerateWarning(); return;}
			setupButtons();
			break;
		case CUSTOMERS_CAN_ONLY_BUY:
			_shopModData.SetAndCheck(ModDataShop.CUSTOMERS_CAN_ONLY_BUY, "");
			setupButtons();
			break;
		case CANCEL_REMOVE:
			_shopModData._removeShop = false;
			setupButtons();
			break;
		
		}
		
		if(value != null) 
		{
			conv = cf.withFirstPrompt(new ConvShopMod(value,this,_shopModData,question)).withLocalEcho(true).buildConversation(_player);
			_player.closeInventory();
			conv.begin();
			return;
		}
	}

	void GenerateWarning()
	{
		if(_runnable != null) _runnable.cancel();
		
		for(int i=0; i < _size; i++) {_inv.setItem(i, null);};
		ItemStack _warning = new ItemStack(Material.BARRIER);
		Metods.setDisplayName(_warning, "&l&cWARNING!");
		_metods.addLore(_warning, "&eAll items from shop will be &clost!",false);
		_metods.addLore(_warning, "&eYou are try to remove the shop!",false);
		
		ImusAPI._metods.AddGlow(_warning);
		_runnable = new BukkitRunnable()		 
		{
			int rolls = _size-1;
			
			int i = 0;
			
			@Override
			public void run() 
			{
				_inv.setItem(i++, _warning);
				if(rolls-- <= 0) 
				{
					this.cancel();
					setupButton(BUTTON.CONFIRM, Material.GREEN_STAINED_GLASS_PANE, "&bCONFIRM", _size-1);
					setupButton(BUTTON.CONFIRM, Material.GREEN_STAINED_GLASS_PANE, "&bCONFIRM", _size-10);
					setupButton(BUTTON.CONFIRM, Material.GREEN_STAINED_GLASS_PANE, "&bCONFIRM", _size-19);
					setupButton(BUTTON.CANCEL_REMOVE, Material.RED_STAINED_GLASS_PANE, "&bCANCEL", 0);
					setupButton(BUTTON.CANCEL_REMOVE, Material.RED_STAINED_GLASS_PANE, "&bCANCEL", 9);
					setupButton(BUTTON.CANCEL_REMOVE, Material.RED_STAINED_GLASS_PANE, "&bCANCEL", 18);
				}
			}
		}.runTaskTimerAsynchronously(_main, 0, 1);
	}


}
