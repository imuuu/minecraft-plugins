package imu.GS.Invs;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.base.Strings;

import imu.GS.ENUMs.EnchantResrictionOptions;
import imu.GS.ENUMs.ModDataItemGenValues;
import imu.GS.ENUMs.ModDataShopStockable;
import imu.GS.Interfaces.IModData;
import imu.GS.Interfaces.IModDataInv;
import imu.GS.Main.Main;
import imu.GS.Prompts.ConvModData;
import imu.GS.ShopUtl.ItemGenModData;
import imu.GS.ShopUtl.ShopNormal;
import imu.GS.ShopUtl.ShopItemModData;
import imu.GS.ShopUtl.ItemPrice.PriceOwn;
import imu.GS.ShopUtl.ShopItems.ShopItemStockable;
import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;

public class ShopItemGeneratorInv extends CustomInvLayout implements IModDataInv
{
	ShopNormal _shopBase;
	ItemGenModData _modData;
	Main _main;
	ArrayList<ItemStack> _modButtons = new ArrayList<>();
	final int _maxGenAmount = 27;
	ArrayList<Material> _blockedMaterials = new ArrayList<>();
	
	GenItem[] _genItems;
	public ShopItemGeneratorInv(Plugin main, Player player, ShopNormal shopBase) 
	{
		super(main, player, "&9=== Item Generator ", 6*9);
		_main = (Main)main;
		_shopBase = shopBase;
		_genItems = new GenItem[27];
		_modData = new ItemGenModData();
		_blockedMaterials.add(Material.AIR);
		_blockedMaterials.add(Material.DEBUG_STICK);
	}
	
	public class GenItem
	{
		public GenItem(ItemStack stack)
		{
			_real = stack.clone();
		}
		ItemStack _real;
		ItemStack _clone;
		public ShopItemModData _modDataShopItem = new ShopItemModData();
		public boolean _selected = false;
		
		ItemStack GetToolTipItem()
		{
			_clone = _real.clone();
			String[] lores = new String[]
					{						
							"&e================",
							"&bM3 &3=> &eCopy to Inv",
							"&5LOCKED : &2"+ (_selected ? "&aTRUE" : "&cFALSE"),
							"&9Price : &2"+ _modDataShopItem.GetValueStr(ModDataShopStockable.CUSTOM_PRICE, "", "", "Material price"),
							"&9Stack Amount: &2"+ _modDataShopItem.GetValueStr(ModDataShopStockable.MAX_AMOUNT, "", "", "NONE"),
							"&9Fill Amount: &2"+ _modDataShopItem.GetValueStr(ModDataShopStockable.FILL_AMOUNT, "", "", "NONE"),
							"&9Fill Time: &2"+ _modDataShopItem.GetValueStr(ModDataShopStockable.FILL_DELAY, "", "", "NONE"),
							"&e================",
					};
			ImusAPI._metods.addLore(_clone, lores);
			SetButton(_clone, BUTTON.GEN_ITEM);
			return _clone;
		}
		
	}
	
	enum BUTTON implements IButton
	{
		NONE,
		BACK,
		CONFIRM,
		GENERATE_ITEMS,
		GEN_ITEM,
		GEN_BUTTON,
		SELECT_ALL_NONE,
	}
	BUTTON GetBUTTON(ItemStack stack)
	{
		if(stack == null) return BUTTON.NONE;
		String bName = getButtonName(stack);		
		if(Strings.isNullOrEmpty(bName)) return BUTTON.NONE;
		return BUTTON.valueOf(bName);
	}
	
	ModDataItemGenValues GetButtonModData(ItemStack stack)
	{
		String str = _metods.getPersistenData(stack, "pd_modDataV", PersistentDataType.STRING);
		if(stack == null) return null;			
		if(Strings.isNullOrEmpty(str)) return null;
		return ModDataItemGenValues.valueOf(str);
	}
	
	ItemStack SetButtonModData(ItemStack stack, ModDataItemGenValues value)
	{
		_metods.setPersistenData(stack, "pd_modDataV", PersistentDataType.STRING, value.toString());
		return stack;
	}
	
	@Override
	public void SetModData(IModData modData) {
		_modData =(ItemGenModData) modData;
	}
	
	@Override
	public void openThis() 
	{
		super.openThis();
		setupButtons();
		SetValueButtons();
		_main.RegisterInv(this);
	}
	@Override
	public void setupButtons() 
	{
		for(int i = _size-1; i > _size-28; i--) {setupButton(BUTTON.NONE, Material.BLACK_STAINED_GLASS_PANE, " ", i);}
		setupButton(BUTTON.BACK,Material.RED_STAINED_GLASS_PANE, "&c<== BACK", _size-9);
		setupButton(BUTTON.CONFIRM,Material.GREEN_STAINED_GLASS_PANE, "&9CONFIRM and add to shop", _size-1);
		
		
		
		setupButton(BUTTON.SELECT_ALL_NONE, Material.PAPER, "&9Select &bM1: &aALL  &bM2: &cNONE", _size-3);
		
		
	}
	
//	void SetSlotID(ItemStack stack, int slot)
//	{
//		_metods.setPersistenData(stack, "pd_slot", PersistentDataType.INTEGER, slot);
//	}
//	
//	Integer GetSlotID(ItemStack stack)
//	{
//		return _metods.getPersistenData(stack, "pd_slot", PersistentDataType.INTEGER);
//	}
	
	void SetValueButtons()
	{
		new BukkitRunnable() 
		{
			
			@Override
			public void run() 
			{
				_modButtons.clear();
				final String setTo = "&9Set to: &a";
				final String falseStr = "&cNONE";
				final String m1m2 = Metods.msgC("&bM1: &aSet &bM2: &cReset");
				final String m1m2Increase = Metods.msgC("&9Increase by &bM1: &a+1  &bSM1: &a+5 &bM2: &c-1 &bSM2: &c-5");
				//final String m1false = Metods.msgC("&9Chance value with &bM1 between &aTrue &9and &cFalse");
				ModDataItemGenValues value;
				
				ItemStack stack = new ItemStack(Material.GOLD_INGOT);
				Metods.setDisplayName(stack, "&6Set Price Range"); value = ModDataItemGenValues.PRICE_RANGE;
				_metods.SetLores(stack, new String[] {m1m2,setTo + _modData.GetValueStr(value, "", "", falseStr+ "&b => Use material price")},false);
				SetButtonModData(stack, value);
				_modButtons.add(stack);
				
				stack = new ItemStack(Material.NETHERITE_SCRAP);
				Metods.setDisplayName(stack, "&6Set Stack Amount Range"); value = ModDataItemGenValues.STACK_AMOUNT_RANGE;
				_metods.SetLores(stack, new String[] {m1m2,setTo + _modData.GetValueStr(value, "", "", falseStr)},false);
				SetButtonModData(stack, value);
				_modButtons.add(stack);
				_modButtons.add(Metods.setDisplayName(new ItemStack(Material.BLACK_STAINED_GLASS_PANE),  " "));
				
				
				stack = new ItemStack(Material.ENCHANTING_TABLE);
				Metods.setDisplayName(stack, "&6Enchants");value = ModDataItemGenValues.ENCHANTS;
				String[] lores = new String[] 
						{
								m1m2Increase,
								"&5==== &bM3 &9Change Selector Line &5====",
								" ",
								(_modData._selector_ench == 0 ? "&6&l>" : "&6") +"Enchants amount: &2"+_modData.GetValueStr(value, "", "", falseStr),
								(_modData._selector_ench == 1 ? "&2&l>" : "&2") +"Max level: &3"+_modData.GetValueStr(ModDataItemGenValues.ENCH_LEVEL_MAX, "", "", "&9Normal &7(can't be lower than min)"),
								(_modData._selector_ench == 2 ? "&c&l>" : "&c") +"Min level: &3"+_modData.GetValueStr(ModDataItemGenValues.ENCH_LEVEL_MIN, "", "", "&9Normal &7(can't be higher than max)"),					
								(_modData._selector_ench == 3 ? "&4&l>" : "&4") +"Restrictions: &3"+_modData.GetValueStr(ModDataItemGenValues.ENCH_RESTRICT, "", "", ""),
								
						};
				_metods.SetLores(stack, lores,false);
				SetButtonModData(stack, value);
				_modButtons.add(stack);
				_modButtons.add(Metods.setDisplayName(new ItemStack(Material.BLACK_STAINED_GLASS_PANE),  " "));
				
				stack = new ItemStack(Material.REDSTONE);
				Metods.setDisplayName(stack, "&6Give tag for search");value = ModDataItemGenValues.TAGS;
				_metods.SetLores(stack, new String[] {m1m2,setTo + _modData.GetValueStr(value, "", "", falseStr)},false);
				SetButtonModData(stack, value);
				_modButtons.add(stack);
				_modButtons.add(Metods.setDisplayName(new ItemStack(Material.BLACK_STAINED_GLASS_PANE),  " "));
				
				stack = new ItemStack(Material.WATER_BUCKET);
				Metods.setDisplayName(stack, "&6Set fill amount range");value = ModDataItemGenValues.FILL_AMOUNT_RANGE;
				_metods.SetLores(stack, new String[] {m1m2,setTo + _modData.GetValueStr(value, "", "", falseStr)},false);
				SetButtonModData(stack, value);
				_modButtons.add(stack);
				
				stack = new ItemStack(Material.CLOCK);
				Metods.setDisplayName(stack, "&6Set fill time range");value = ModDataItemGenValues.FILL_TIME_RANGE;
				_metods.SetLores(stack, new String[] {m1m2,setTo + _modData.GetValueStr(value, "", "", falseStr)},false);
				SetButtonModData(stack, value);
				_modButtons.add(stack);
				
				
				stack = new ItemStack(Material.BEACON);
				Metods.setDisplayName(stack, "&bM3 &6=> &5Generate Items");
				_metods.SetLores(stack, new String[] {
						"&bM1: &a+1  &bSM1: &a+5",
						"&bM2: &c-1 &bSM2: &c-5",
						" ",
						"&6Generation Amount: &2"+_modData.GetValueStr(ModDataItemGenValues.GENERATION_AMOUNT, "", "", ""),
						
				}, false);
				SetButton(stack, BUTTON.GENERATE_ITEMS);
				_inv.setItem(_size-5, stack);
				
				
				int offSet = _size - 19;
				for(ItemStack s : _modButtons)
				{			
					int slot = offSet+=1;
					//SetSlotID(stack, slot);
					_inv.setItem(slot, SetButton(s, BUTTON.GEN_BUTTON));
				}
			}
		}.runTaskAsynchronously(_main);
		
	}
	
	@Override
	public void invClosed(InventoryCloseEvent e) 
	{
		_main.UnregisterInv(this);
	}
	
	int GetEnchLevel(Enchantment ench)
	{
		int level;
 		if(_modData._ench_level_min > 0 || _modData._ench_level_max > 0)
 		{
 			
 			level = ThreadLocalRandom.current().nextInt(_modData._ench_level_min > 0 ? _modData._ench_level_min : ench.getStartLevel(), _modData._ench_level_max > 0 ? _modData._ench_level_max+1 : ench.getMaxLevel());
 		}else
 		{
 			level = (ench.getStartLevel() == ench.getMaxLevel()) ? ench.getStartLevel() : ThreadLocalRandom.current().nextInt(ench.getStartLevel(),ench.getMaxLevel()+1);
 		}
		return level;
	}
	void EnchantItem(ItemStack stack)
	{
		int failSafe = 0;
		for(int i = 0; i < _modData._enchants; i++)
		{
			failSafe++;
			if(failSafe > 200) 
			{
				//System.out.println("fail safe!");
				return;
			}
			
			Enchantment ench = Enchantment.values()[ThreadLocalRandom.current().nextInt(Enchantment.values().length)];
			
			int level = GetEnchLevel(ench);
			//System.out.println("level: "+level);
			
			if(_modData.GetEnchRestrictionName() == EnchantResrictionOptions.None) // None
			{
				stack.addUnsafeEnchantment(ench, level);
				continue;
			}
			
			if(ench.canEnchantItem(stack))
			{
				if(_modData.GetEnchRestrictionName() == EnchantResrictionOptions.All_but_Ignore_conflicts)
				{
					if(_modData._ench_level_min > 0 || _modData._ench_level_max > 0)
			 		{
						stack.addUnsafeEnchantment(ench, level);
			 		}
					else
			 		{
						stack.addEnchantment(ench, level);
			 		}
					
				}
				else // all
				{
					
					boolean conflic = false;
					for(Enchantment itemEnch : stack.getEnchantments().keySet())
					{
						if(ench.conflictsWith(itemEnch))
						{
							//System.out.println("there is conflict: "+ench.getName() + " : "+itemEnch.getName());
							conflic = true;
							continue;
						}
						
					}
					if(!conflic) 
					{
						if(_modData._ench_level_min > 0 || _modData._ench_level_max > 0)
				 		{
							stack.addUnsafeEnchantment(ench, level);
				 		}
						else
				 		{
							stack.addEnchantment(ench, level);
				 		}
						
					}else
					{
						--i;
					}					
				}
	
			}
			else
			{
				--i;
			}
		}
	}
	
	GenItem NewGenItem(ItemStack stack)
	{
		GenItem gen = new GenItem(stack);
		if(_modData._fillAmount_min != -1 && _modData._fillAmount_max != -1) gen._modDataShopItem._fillAmount = ThreadLocalRandom.current().nextInt(_modData._fillAmount_min,_modData._fillAmount_max+1);
		if(_modData._fillTime_min != -1 && _modData._fillTime_max != -1) gen._modDataShopItem._fillDelayMinutes = ThreadLocalRandom.current().nextInt(_modData._fillTime_min,_modData._fillTime_max+1);
		if(_modData._stack_amount_min != -1 && _modData._stack_amount_max != -1) gen._modDataShopItem._maxAmount = ThreadLocalRandom.current().nextInt(_modData._stack_amount_min,_modData._stack_amount_max+1);
		if(_modData._priceMin != -1 && _modData._priceMax != -1) 
		{
			gen._modDataShopItem._itemPrice =  new PriceOwn().SetPrice(ThreadLocalRandom.current().nextDouble(_modData._priceMin,_modData._priceMax+1.0));
		}
		return gen;
	}
	void GenerateItem(int slot)
	{
		if(_genItems[slot] != null && _genItems[slot]._selected) {_inv.setItem(slot, _genItems[slot].GetToolTipItem());return;}
		
		int randInt = ThreadLocalRandom.current().nextInt(Material.values().length);
		Material mat = Material.values()[randInt];
		if(_blockedMaterials.contains(mat)){GenerateItem(slot); return;}
		
		ItemStack stack = new ItemStack(mat);
		if(ImusAPI._metods.isTool(stack) || ImusAPI._metods.isArmor(stack))
		{
			//System.out.println("stack: "+stack.getType() + " is tool");
			EnchantItem(stack);
		}
		
		
		_genItems[slot] = NewGenItem(stack);
		
		_inv.setItem(slot, _genItems[slot].GetToolTipItem());
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if(_inv.getItem(slot) == null || _inv.getItem(slot).getType() == Material.AIR)
				{
					GenerateItem(slot);
				}
			}
		}.runTaskLaterAsynchronously(_main, 1);
	}
	
	void GenerateItems()
	{	
		new BukkitRunnable() 
		{
			
			@Override
			public void run() 
			{
				for(int i = 0; i < 27; i++)
				{
					if(_genItems[i] != null && !_genItems[i]._selected) _genItems[i] = null;
					if(_modData._generationAmount > i) 
					{
						GenerateItem(i);
						continue;
					}					
					_inv.setItem(i, Metods.setDisplayName(new ItemStack(Material.BLUE_STAINED_GLASS_PANE), " "));					
				}
			}
		}.runTaskAsynchronously(_main);
		
		
	}
	
	void SetAllSelected(boolean value)
	{
		for(int i = 0 ; i < _genItems.length; i++) 
		{
			if(_genItems[i] == null) continue;
			_genItems[i]._selected = value;
			_inv.setItem(i, _genItems[i].GetToolTipItem());
		}
	}
	
	int GetFastAmount(InventoryClickEvent e)
	{
		int mult = 0;
		if(e.getClick().isLeftClick()) mult = 1;
		if(e.getClick().isRightClick()) mult = -1;
		if(mult == 0) return 0;
		if(e.getClick().isShiftClick()) mult *= 5;
		return mult;
	}
	void ClickedModData(InventoryClickEvent e,ModDataItemGenValues value)
	{
		final String multiChose = "&8(&6Seperate values with &9&oSPACE&8)";
		final String minMax = "&cmin &2max";
		final String frontColor = "&3";
		int amount = 0;
		ClickType cType = e.getClick();
		switch (value) 
		{
		case ENCHANTS:
			if(cType == ClickType.MIDDLE)
			{
				_modData.ChangeEnchSelector();
				break;
			}
			if(_modData._selector_ench == 0)
			{
				//if(cType.isRightClick()) {_modData._enchants = 0;break;}
				_modData._enchants += GetFastAmount(e);
				if(_modData._enchants < 0) _modData._enchants = 0;
				break;
			}
			if(_modData._selector_ench == 1)
			{
				_modData.SetAndCheck(ModDataItemGenValues.ENCH_LEVEL_MAX, _modData._ench_level_max + GetFastAmount(e)+"");
				if(_modData._ench_level_max < -1) _modData._ench_level_max = -1;
				break;
			}
			if(_modData._selector_ench == 2)
			{
				_modData.SetAndCheck(ModDataItemGenValues.ENCH_LEVEL_MIN, _modData._ench_level_min + GetFastAmount(e)+"");
				if(_modData._ench_level_min < -1) _modData._ench_level_min = -1;
				break;
			}
			
			if(_modData._selector_ench == 3)
			{
				_modData.SetAndCheck(ModDataItemGenValues.ENCH_RESTRICT, " ");
				break;
			}
					
			break;
//		case ENCH_RESTRICT:
//			if(cType.isRightClick()) {_modData._ench_restrict = 0;break;}
//			_modData.SetAndCheck(ModDataItemGenValues.ENCH_RESTRICT, " ");
//			break;
		case FILL_AMOUNT_RANGE:
			if(cType.isRightClick()) {_modData._fillAmount_min = -1; _modData._fillAmount_max = -1;break;}
			_metods.ConversationWithPlayer(_player, new ConvModData(ModDataItemGenValues.FILL_AMOUNT_RANGE, this ,_modData, frontColor+"&6Give fill amount range: "+minMax+" "+multiChose));
			_player.closeInventory();
			return;
		case FILL_TIME_RANGE:
			if(cType.isRightClick()) {_modData._fillTime_min = -1; _modData._fillTime_max = -1;break;}
			_metods.ConversationWithPlayer(_player, new ConvModData(ModDataItemGenValues.FILL_TIME_RANGE, this ,_modData, frontColor+"&6Give fill time range in minutes: "+minMax+" "+multiChose));
			_player.closeInventory();
			return;
		case GENERATION_AMOUNT:
			amount = _modData._generationAmount;			
			amount += GetFastAmount(e);
			if(amount < 0) amount = 0;
			if(amount > 27) amount = _maxGenAmount;
			_modData.SetAndCheck(ModDataItemGenValues.GENERATION_AMOUNT, ""+amount);
			break;
		case PRICE_RANGE:
			if(cType.isRightClick()) {_modData._priceMin = -1; _modData._priceMax = -1;break;}
			_metods.ConversationWithPlayer(_player, new ConvModData(ModDataItemGenValues.PRICE_RANGE, this ,_modData, frontColor+"&6Give price range: "+minMax+" "+multiChose));
			_player.closeInventory();
			return;
		case SEARCH_OPTION:
			break;
		case STACK_AMOUNT_RANGE:
			if(cType.isRightClick()) {_modData._stack_amount_min = -1; _modData._stack_amount_max = -1;break;}
			_metods.ConversationWithPlayer(_player, new ConvModData(ModDataItemGenValues.STACK_AMOUNT_RANGE, this ,_modData, frontColor+"&6Give stack amount range.. "+minMax+" "+multiChose));
			_player.closeInventory();
			return;
		case TAGS:
			if(cType.isRightClick()) {_modData.ClearSearchTags();break;}
			_metods.ConversationWithPlayer(_player, new ConvModData(ModDataItemGenValues.TAGS, this ,_modData, frontColor+"&6Give a tag:"));
			_player.closeInventory();
			return;
		default:
			break;
		
		}
		
		SetValueButtons();
	}
	
	void Confirm()
	{
		for(int i = 0; i < _genItems.length; i++)
		{
			if(_genItems[i] == null || !_genItems[i]._selected) continue;
			ItemStack stack = _genItems[i]._real.clone();
			ShopItemStockable stockable = new ShopItemStockable(_main,_shopBase, stack, 1);
			stockable.SetModData(_genItems[i]._modDataShopItem);
			_shopBase.AddNewItem(stockable, false);
		}
		Back();
	}
	
	void Back()
	{
		_player.closeInventory();
		new ShopModINV(_main, _player, _shopBase).openThis();
	}
	@Override
	public void onClickInsideInv(InventoryClickEvent e) 
	{
		ItemStack stack = e.getCurrentItem();
		BUTTON button = GetBUTTON(stack);
		
		switch (button) {
		case NONE:
			break;
		case GEN_BUTTON:
			ModDataItemGenValues value = GetButtonModData(stack);
			if(value != null) ClickedModData(e,value);
			break;
		case BACK:
			Back();
			break;
		case CONFIRM:
			Confirm();
			break;
		case GENERATE_ITEMS:
			ClickedModData(e,ModDataItemGenValues.GENERATION_AMOUNT);
			if(e.getClick() == ClickType.MIDDLE) GenerateItems();			
			break;
		case SELECT_ALL_NONE:
			if(e.getClick().isLeftClick()) SetAllSelected(true);
			if(e.getClick().isRightClick()) SetAllSelected(false);
			break;
		case GEN_ITEM:
			if(e.getClick() == ClickType.MIDDLE) { ImusAPI._metods.InventoryAddItemOrDrop(_genItems[e.getSlot()]._real, _player, 1);return;}
			int slot = e.getSlot();
			if(_genItems[slot] != null) _genItems[slot]._selected = _genItems[slot]._selected ? false : true;
			_inv.setItem(slot, _genItems[slot].GetToolTipItem());
			break;
		default:
			break;
		}
		
	}

	

	

}
