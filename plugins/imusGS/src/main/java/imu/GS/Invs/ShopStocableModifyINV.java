package imu.GS.Invs;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import imu.GS.ENUMs.ModDataShopStockable;
import imu.GS.Interfaces.IModData;
import imu.GS.Interfaces.IModDataInv;
import imu.GS.Main.Main;
import imu.GS.Prompts.ConvModData;
import imu.GS.Prompts.ConvPromptModModifyINV;
import imu.GS.ShopUtl.ShopItemModData;
import imu.GS.ShopUtl.ShopNormal;
import imu.GS.ShopUtl.ShopItems.ShopItemSeller;
import imu.GS.ShopUtl.ShopItems.ShopItemStockable;
import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;

public class ShopStocableModifyINV extends CustomInvLayout implements IModDataInv
{
	ShopItemSeller _sis;
	ItemStack copy_item;
	
	String pd_buttonType = "gs.sModModI.buttonType";
	String pd_buttonAnswer ="gs.sModModI.buttonAnser";

	ShopNormal _shop;
	
	Main _main;
	ShopItemModData _modData;
	boolean _isClosed;
	public ShopStocableModifyINV(Main main, Player player, ShopItemSeller sis, ShopItemModData modData) 
	{
		super(main, player, "&3Modify Stockable Item", 9*3);
		_main = main;
		_sis = sis;
		copy_item = _sis.GetRealItem().clone();
		_shop = sis.GetShop();
	
		SetModData((ShopItemModData)modData.clone());
		//System.out.println("open inv");
	}
		
	enum BUTTON implements IButton
	{
		NONE,
		BACK,
		CONFIRM,
		MAX_AMOUNT,
		SET_PERMISSION,
		SET_FILL_DELAY,
		SET_FILL_AMOUNT,
		SET_PRICE,
		SET_WORLDS,
		SET_CAN_BE_SOLD,
		SET_DISTANCE,
		SET_TAGS,
		REMOVE_INF,
		CLONE_TO_INV,
		SET_TIME_SELL,
		OVERIDE_ALL,
						
	}
	

	@Override
	public void SetModData(IModData modData) 
	{
		_modData = (ShopItemModData)modData;
		makeInv();
	}
	
	void makeInv()
	{
		for(int i = 0; i < _inv.getContents().length; ++i)
		{
			_inv.setItem(i, Metods.setDisplayName(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), " "));
		}
		
		String m1m2 = Metods.msgC("&bM1: &aSet &bM2: &cRemove");
		String setTo = Metods.msgC("&aSet To &1");
		String false_setTo = Metods.msgC("&aSet To &cNONE");
		String lore;
		int id = 0;
		setupButton(BUTTON.BACK, Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "<< BACK", 0);
		setupButton(BUTTON.CONFIRM, Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "<< CONFIRM", 9*2);
		_inv.setItem(9, new ItemStack(copy_item));
		setupButton(BUTTON.REMOVE_INF, Material.LAVA_BUCKET, ChatColor.RED + ""+ChatColor.BOLD + "Remove this items from shop", 8);
		setupButton(BUTTON.CLONE_TO_INV, Material.SLIME_BALL, ChatColor.AQUA + ""+ChatColor.BOLD + "Clone to your inv", _size-1);
		_inv.setItem(_size-10, Metods._ins.AddGlow(setupButton(BUTTON.OVERIDE_ALL, Material.PAPER, ChatColor.AQUA + ""+ChatColor.BOLD + "Overide all items in shop with this data!", _size-10)));
		id = 2;
		lore = _modData.GetValueStr(ModDataShopStockable.MAX_AMOUNT,setTo ,null, false_setTo);
		ItemStack custom_amount = setupButton(BUTTON.MAX_AMOUNT, lore.equalsIgnoreCase(false_setTo) ? Material.GLASS_PANE : Material.BLUE_STAINED_GLASS_PANE, ChatColor.DARK_PURPLE + "Max stock amount", id);		
		ImusAPI._metods.addLore(custom_amount, _modData.GetValueStr(ModDataShopStockable.MAX_AMOUNT,setTo ,null, false_setTo), false);
		ImusAPI._metods.addLore(custom_amount, m1m2, false);
		
		id = 4;
		lore = _modData.GetValueStr(ModDataShopStockable.PERMISSIONS,setTo ,null,  false_setTo);
		ItemStack setPerms = setupButton(BUTTON.SET_PERMISSION, lore.equalsIgnoreCase(false_setTo) ? Material.GLASS_PANE : Material.BLUE_STAINED_GLASS_PANE, ChatColor.DARK_PURPLE + "Set permission", id);
		ImusAPI._metods.addLore(setPerms, lore, false);ImusAPI._metods.addLore(setPerms, m1m2, false);
		
		id = 6;
		lore = _modData.GetValueStr(ModDataShopStockable.FILL_DELAY,setTo ,null,  false_setTo);
		ItemStack setFillDelay = setupButton(BUTTON.SET_FILL_DELAY, lore.equalsIgnoreCase(false_setTo) ? Material.GLASS_PANE : Material.BLUE_STAINED_GLASS_PANE, ChatColor.DARK_PURPLE + "Set Stock FillDelay(min)", id);
		
		ImusAPI._metods.addLore(setFillDelay, lore, false);
		ImusAPI._metods.addLore(setFillDelay, ChatColor.YELLOW + "How often stock will be filled and", true);
		ImusAPI._metods.addLore(setFillDelay, m1m2, false);
		//ImusAPI._metods.addLore(setDelay_Amount, ChatColor.BLUE + "Minimum time: "+ _shop.getStockCheckTime()/60, true);
		
		id = 12;
		lore = _modData.GetValueStr(ModDataShopStockable.FILL_AMOUNT,setTo ,null,  false_setTo);
		ItemStack setFillAmount = setupButton(BUTTON.SET_FILL_AMOUNT, lore.equalsIgnoreCase(false_setTo) ? Material.GLASS_PANE : Material.BLUE_STAINED_GLASS_PANE, ChatColor.DARK_PURPLE + "Set Stock FillAmount", id);
		
		ImusAPI._metods.addLore(setFillAmount, lore , false);
		ImusAPI._metods.addLore(setFillAmount, ChatColor.YELLOW + "How many it fills. Cant go over", true);
		ImusAPI._metods.addLore(setFillAmount, ChatColor.YELLOW + "shop amount(or custom amount)", true);
		ImusAPI._metods.addLore(setFillAmount, m1m2, false);
		//ImusAPI._metods.addLore(setDelay_Amount, ChatColor.BLUE + "Minimum time: "+ _shop.getStockCheckTime()/60, true);
		
		id = 14;
		lore = _modData.GetValueStr(ModDataShopStockable.CUSTOM_PRICE,setTo ,null,  false_setTo);
		ItemStack setCusPrice = setupButton(BUTTON.SET_PRICE, lore.equalsIgnoreCase(false_setTo) ? Material.GLASS_PANE : Material.BLUE_STAINED_GLASS_PANE, ChatColor.DARK_PURPLE + "Set Custom Price", id);
		
		ImusAPI._metods.addLore(setCusPrice, lore, false);
		ImusAPI._metods.addLore(setCusPrice, ChatColor.YELLOW + "Set own price for item in this shop!", true);
		ImusAPI._metods.addLore(setCusPrice, ChatColor.YELLOW + "Be carefull with money explote! Cant be too cheap!", true);
		ImusAPI._metods.addLore(setCusPrice, m1m2, false);
		
		//TODO
//		id = 20;
//		lore = _modData.GetValueStr(ModDataShopStockable.WORLD_NAMES,setTo , null,  false_setTo);
//		ItemStack setWorlds = setupButton(BUTTON.SET_WORLDS, lore.equalsIgnoreCase(false_setTo) ? Material.GLASS_PANE : Material.BLUE_STAINED_GLASS_PANE, ChatColor.DARK_PURPLE + "Set Spesific worlds", id);
//		
//		ImusAPI._metods.addLore(setWorlds, lore, false);
//		ImusAPI._metods.addLore(setWorlds, ChatColor.YELLOW + "Set worlds where you can get this item and", true);
//		ImusAPI._metods.addLore(setWorlds, ChatColor.YELLOW + "Item will be removed(from inv) if entered wrong world", true);
//		ImusAPI._metods.addLore(setWorlds, m1m2, false);
								
		id = 22;
		lore = _modData.GetValueStr(ModDataShopStockable.DISTANCE_LOC, null ,null, false_setTo);
		ItemStack setDistance_item = setupButton(BUTTON.SET_DISTANCE, lore.equalsIgnoreCase(false_setTo) ? Material.GLASS_PANE : Material.BLUE_STAINED_GLASS_PANE,  ChatColor.DARK_PURPLE + "Set Distance radius.", id);		
		
		ImusAPI._metods.addLore(setDistance_item, lore.split("; "));
		//ImusAPI._metods.addLore(setDistance_item, setTo, false);
		ImusAPI._metods.addLore(setDistance_item, ChatColor.YELLOW + "Item will be shown inside given radius", true);
		ImusAPI._metods.addLore(setDistance_item, m1m2, false);
		
		id = 24;
		lore = _modData.GetValueStr(ModDataShopStockable.SELL_TIME_START,setTo ,null,  false_setTo);
		ItemStack setTimeSell = setupButton(BUTTON.SET_TIME_SELL, lore.equalsIgnoreCase(false_setTo) ? Material.GLASS_PANE : Material.BLUE_STAINED_GLASS_PANE,  ChatColor.DARK_PURPLE + "Set sell time", id);
		
		ImusAPI._metods.addLore(setTimeSell, lore , false);
		ImusAPI._metods.addLore(setTimeSell, ChatColor.YELLOW + "Set time when item will be apearing in shop", true);
		ImusAPI._metods.addLore(setTimeSell, m1m2 , false);
		
		id = 10;
		lore = _modData.GetValueStr(ModDataShopStockable.TAGS, setTo ,null,  false_setTo);
		ItemStack tags = setupButton(BUTTON.SET_TAGS, lore.equalsIgnoreCase(false_setTo) ? Material.GLASS_PANE : Material.BLUE_STAINED_GLASS_PANE,  ChatColor.DARK_PURPLE + "Add Tags", id);
		
		ImusAPI._metods.addLore(tags, lore , false);
		ImusAPI._metods.addLore(tags, ChatColor.YELLOW + "Give tags", true);
		ImusAPI._metods.addLore(tags, m1m2 , false);
		
		id = _size-1;
		
		
			
	}
	
	@Override
	public void openThis() 
	{
		super.openThis();
//		if(!HasRegistered())
//			RegisterToEvents();
//		
		_main.RegisterInv(this);
		_isClosed = _shop.HasLocked();
		_shop.SetLocked(true);
	}
	

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onClickInsideInv(InventoryClickEvent e) 
	{
		ItemStack stack = e.getCurrentItem();
		if(stack == null) return;
		String bName = getButtonName(stack);
		
		//if(Strings.isNullOrEmpty(bName)) return;
		if(bName == null || bName == "") return;
		
		BUTTON button = BUTTON.valueOf(bName);
		
		ConversationFactory cf = null;
		String question = null;
		Conversation conv = null;
		//int remove_state = 0;
		if(e.getClick() == ClickType.LEFT)
		{
			cf = new ConversationFactory(_main);
			switch (button) 
			{
			case NONE:
				
				break;
			case BACK:
				_player.closeInventory();
				new ShopModINV(_main, _player, _shop).openThis();
				return;
			case CONFIRM:
				
				((ShopItemStockable)_sis).SetModData(_modData);
				_main.GetTagManager().SaveTagsAsync(_sis);
				_main.GetTagManager().LoadAllShopItemTagsNamesAsync();
				_player.closeInventory();
				new ShopModINV(_main, _player, (ShopNormal)_main.get_shopManager().GetShop(_shop.GetUUID())).openThis();
				return;
			case MAX_AMOUNT:				
				question = ChatColor.DARK_PURPLE + "Give max stock amount?";
				conv = cf.withFirstPrompt(new ConvPromptModModifyINV(_player, ModDataShopStockable.MAX_AMOUNT, this, _modData, question)).withLocalEcho(true).buildConversation(_player);
				break;
			case SET_PERMISSION:
				question = ChatColor.DARK_PURPLE + "Give Permission name?";
				conv = cf.withFirstPrompt(new ConvPromptModModifyINV(_player, ModDataShopStockable.PERMISSIONS, this, _modData, question)).withLocalEcho(true).buildConversation(_player);
				break;
			case SET_FILL_DELAY:
				question = ChatColor.DARK_PURPLE + "Give Fill Delay";
				conv = cf.withFirstPrompt(new ConvPromptModModifyINV(_player, ModDataShopStockable.FILL_DELAY, this, _modData, question)).withLocalEcho(true).buildConversation(_player);
				break;
			case SET_FILL_AMOUNT:
				question = ChatColor.DARK_PURPLE + "Give Fill Amount";
				conv = cf.withFirstPrompt(new ConvPromptModModifyINV(_player, ModDataShopStockable.FILL_AMOUNT, this, _modData, question)).withLocalEcho(true).buildConversation(_player);
				break;
			case SET_PRICE:				
				//question = ChatColor.DARK_PURPLE + "Give Own Price?";
				//conv = cf.withFirstPrompt(new ConvPromptModModifyINV(_player, ITEM_MOD_DATA.OWN_PRICE, this, _modData, question)).withLocalEcho(true).buildConversation(_player);
				new ChangePriceINV(_main, _player, this,_sis, _modData).openThis();
				return;
			case SET_WORLDS:
				question = ChatColor.DARK_PURPLE + "Give World name?(ex: world world_nether this(takes world where you are) )";
				conv = cf.withFirstPrompt(new ConvPromptModModifyINV(_player, ModDataShopStockable.WORLD_NAMES, this, _modData, question)).withLocalEcho(true).buildConversation(_player);
				break;
			case SET_DISTANCE:
				question = ChatColor.DARK_PURPLE + "Give Distance and location?{distance worldName x y z}(ex: 50 world 1 55 23 OR 50 this(this= ur loc))";
				conv = cf.withFirstPrompt(new ConvPromptModModifyINV(_player, ModDataShopStockable.DISTANCE_LOC, this, _modData, question)).withLocalEcho(true).buildConversation(_player);
				break;
			case REMOVE_INF:
				_shop.RemoveItem(_sis.GetPage(), _sis.GetSlot());
				_player.closeInventory();
				new ShopModINV(_main, _player, _shop).openThis();
				return;
			case CLONE_TO_INV:
				ImusAPI._metods.InventoryAddItemOrDrop(_sis.GetRealItem().clone(), _player);
				return;
			case SET_TIME_SELL:
				question = ChatColor.DARK_PURPLE + "Give startTime and endTime  Seperate by space(ex: 0 13000(whole daytime)(min:0 max:24000)";
				conv = cf.withFirstPrompt(new ConvPromptModModifyINV(_player, ModDataShopStockable.SELL_TIME_START, this, _modData, question)).withLocalEcho(true).buildConversation(_player);
				break;
			case SET_TAGS:
				ImusAPI._metods.ConversationWithPlayer(_player, new ConvModData(ModDataShopStockable.TAGS, this, _modData, "&eGive tag"));
				break;
			case OVERIDE_ALL:
				OverideAll();
				return;
			
			}
			if(conv != null) conv.begin();			
			_player.closeInventory();
		}
		else if(e.getClick() == ClickType.RIGHT)
		{
			switch (button) 
			{
			case MAX_AMOUNT:
				_modData._maxAmount = -1;
				break;
			case SET_PERMISSION:
				_modData.ClearPermissions();
				break;
			case SET_FILL_DELAY:
				_modData._fillDelayMinutes = -1;
				break;
			case SET_FILL_AMOUNT:
				_modData._fillAmount = -1;
				break;
			case SET_PRICE:
				_modData._itemPrice = null;
				break;
			case SET_WORLDS:
				_modData.ClearWorldNames();
				break;
			case SET_DISTANCE:
				_modData.ClearLocations();
				break;
			case REMOVE_INF:
//				if(_answers[slot].equalsIgnoreCase("sure"))
//				{
//					_shop.removeItemFromShopNEW(orginal_item,true);
//					_main.getShopModManager().openModShopInv(_player, _shop);
//				}
				break;
			case SET_TIME_SELL:
				_modData._sellTimeStart = -1;
				_modData._sellTimeEnd = -1;
				break;
			case BACK:
				break;
			case CONFIRM:
				break;
			case NONE:
				break;
			case SET_CAN_BE_SOLD:
				break;
			case CLONE_TO_INV:
				break;
			case SET_TAGS:
				_modData.ClearTags();
				break;
			
			}
		}
		makeInv();
		
	}
	
	void OverideAll()
	{
		int count = 0;
		for(ShopItemSeller[] siss : _shop.get_items())
		{
			for(ShopItemSeller sis : siss)
			{
				if(!(sis instanceof ShopItemStockable)) continue;
				
				((ShopItemStockable)sis).SetModData(_modData);
				count++;
				
			}
		}
		
		_player.sendMessage(Metods.msgC("&3Data has been changed total of &2"+count+" &3items"));
	}

	@Override
	public void invClosed(InventoryCloseEvent e) 
	{
		_shop.SetLocked(_isClosed);
		_main.UnregisterInv(this);
	}

	

	@Override
	public void setupButtons() {
		
	}

	
	
	
	
}
