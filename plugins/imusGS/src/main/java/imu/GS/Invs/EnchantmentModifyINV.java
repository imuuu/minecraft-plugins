package imu.GS.Invs;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import imu.GS.ENUMs.GearValues;
import imu.GS.Main.Main;
import imu.GS.Managers.ShopEnchantManager;
import imu.GS.Managers.ShopEnchantManager.ENCHANT_OTHER_VALUES;
import imu.GS.Other.EnchantINFO;
import imu.iAPI.Convs.ConvModData;
import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Interfaces.IModDataInv;
import imu.iAPI.Interfaces.IModDataValue;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;

public class EnchantmentModifyINV extends CustomInvLayout implements IModDataInv
{
	private ShopEnchantManager _eManager;
	private Main _main;
	
	private final int ENCH_MAX_SELECTOR = ENCH_SEL_NAME.values().length-1;
	private final int GEAR_MAX_SELECTOR = GearValues.values().length-1;
	
	private int[] _selector_array;

	private int _clickedEnch;
	Cooldowns _cds = new Cooldowns();
	
	private EnchantINFO _enchantInfoOverideALL = new EnchantINFO(null, 0, 0, 0);
	private final int _overide_all_slot = _size-2;
	
	public EnchantmentModifyINV(Plugin main, Player player) 
	{
		super(main, player, "&5===>Modifying Enchantment", 6 * 9);
		_main = (Main)main;
		_eManager = _main.GetShopEnchantManager();
		
		_selector_array = new int[6 * 9];

	}

	@Override
	public boolean SetModData(IModDataValue value, String anwser) 
	{
		BUTTON button = (BUTTON) value;
		if(button == BUTTON.ENCHANT)
		{
			LinkedList<EnchantINFO> infos = new LinkedList<EnchantINFO>();
			//EnchantINFO eInfo;
			if(_clickedEnch != _overide_all_slot)
			{
				infos.add(_eManager.GetInfo(Enchantment.values()[_clickedEnch]));
			}
			else
			{
				for(int i = 0; i < Enchantment.values().length; ++i)
				{
					infos.add(_eManager.GetInfo(Enchantment.values()[i]));
				}
			}
			ENCH_SEL_NAME selector = ENCH_SEL_NAME.values()[_selector_array[_clickedEnch]];
			
			if(!Metods._ins.isDigit(anwser)) return false;
			double number = Double.parseDouble(anwser);
			
			for(EnchantINFO eInfo : infos)
			{
				switch (selector) 
				{
				case MAX_LEVEL:
					eInfo.SetMaxLevel((int) Math.round(number));
					break;
				case MAX_PRICE:
					if(!eInfo.SetPrice(eInfo.GetMinPrice(),eInfo.GetMaxPrice().SetPrice(number))) return false;
					break;
				case MIN_PRICE:
					if(!eInfo.SetPrice(eInfo.GetMinPrice().SetPrice(number), eInfo.GetMaxPrice())) return false;
					break;
				case RAW_MULTIPLIER:
					eInfo.Set_rawMultiplier(number);
					break;
			}
			
				
			
		}
			String enchName = infos.size() > 1 ? "&5All" : infos.get(0).GetName().toUpperCase();
			_player.sendMessage(Metods.msgC("&b"+enchName+"&3 data has been saved"));
			_eManager.SaveEnchantInfoAsync(infos);			
			openThis();
			return true;
		}
		
		if(button == BUTTON.GEAR_MODIFY)
		{
			GearValues selector = GearValues.values()[_selector_array[_clickedEnch]];
			
			if(!Metods._ins.isDigit(anwser)) return false;
			double number = Double.parseDouble(anwser);
			
			_eManager.SaveEnchantGearMultiAsync(selector, number);
			_eManager.SetGearValue(selector, number);
			_player.sendMessage(Metods.msgC("&b"+selector+"&3 data has been saved"));
			openThis();
			return true;
		}
		
		if(button == BUTTON.OTHER_VALUE)
		{
			if(!Metods._ins.isDigit(anwser)) return false;
			double number = Double.parseDouble(anwser) / 100.0;
			_eManager.Set_enchant_count_percent(number);
			_eManager.SaveEnchantOtherValuesAsync(ENCHANT_OTHER_VALUES.ENCHANT_COUNT_PERCENT, number);
			_player.sendMessage(Metods.msgC("&bEnchant Count Buff Percent&3 data has been saved"));
			openThis();
			return true;
		}
		
		
		return true;
	}
	
	@Override
	public void SetModDataFAILED(IModDataValue value, String qustion, String anwser) 
	{
		BUTTON button = (BUTTON) value;
		String failedMsg = "";
		if(button == BUTTON.ENCHANT)
		{
			ENCH_SEL_NAME selector = ENCH_SEL_NAME.values()[_selector_array[_clickedEnch]];
			
			switch (selector) 
			{
			case MAX_LEVEL:		
			case RAW_MULTIPLIER:
				failedMsg = "&cNeed to be number!";
				break;
			case MAX_PRICE:
			case MIN_PRICE:
				if(_clickedEnch == _overide_all_slot)
				{
					failedMsg = "&eSome of enchants had this issue: &2Max &e> &cmin!";
					break;
				}
				failedMsg = "&2Max &e> &cmin!";
				break;
			}
		}
		
		if(button == BUTTON.GEAR_MODIFY)
		{
			failedMsg = "&cNeed to be number!";
		}
		
		
		_player.sendMessage(Metods.msgC("&cInvalid syntax! "+failedMsg));
		Metods._ins.ConversationWithPlayer(_player, new ConvModData(value,this,anwser));
	}


	@Override
	public void openThis() 
	{
		super.openThis();
		setupButtons();	
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
	
	enum BUTTON implements IButton, IModDataValue
	{
		NONE,
		ENCHANT,
		ALL_ENCHANT,
		GEAR_MODIFY,
		OTHER_VALUE
	}
	
//	enum ENCH implements IModDataValue
//	{
//		MOD_ENCHANT,
//		MOD_GEAR
//	}
//	
	@Override
	public void invClosed(InventoryCloseEvent e) 
	{

	}

	@Override
	public void onClickInsideInv(InventoryClickEvent e) 
	{
		if(!_cds.isCooldownReady("click")) return;
		_cds.setCooldownInSeconds("click", 0.1);
		BUTTON button = GetButtonPress(e);
		Integer slot = GetSLOT(e.getCurrentItem());
		if(slot == null) return;
		
		switch (button) 
		{
		case ENCHANT:
			
			if(e.getClick() == ClickType.RIGHT) 
			{
				MoveSelector_Ench(e.getCurrentItem());
				if(slot == _overide_all_slot)
				{
					RefresItem_OverridedALL();
					return;
				}
				
				RefresItem_Ench(slot);
				return;
			}
			if(e.getClick() == ClickType.MIDDLE)
			{
				_selector_array[slot]=ENCH_SEL_NAME.values().length-1;
			}
			_clickedEnch = slot;
			Modify_Ench(ENCH_SEL_NAME.values()[_selector_array[slot]]);
			break;
		case NONE:
			return;
		case GEAR_MODIFY:
			if(e.getClick() == ClickType.RIGHT) 
			{
				MoveSelector_Gear(e.getCurrentItem());
				RefresItem_Gear();
				return;
			}
			
			_clickedEnch = slot;
			Modify_Gear(GearValues.values()[_selector_array[slot]]);
			break;
		case OTHER_VALUE:
			_clickedEnch = slot;
			Modify_Other();
			break;
		default:
			break;
	
		}
		
		//setupButtons();
	}
	
	enum ENCH_SEL_NAME
	{		
		MAX_PRICE,
		MIN_PRICE,
		RAW_MULTIPLIER,
		MAX_LEVEL,		
	}
	
	void Modify_Ench(ENCH_SEL_NAME selector)
	{
		String str = "";
		switch (selector) 
		{
			case MAX_LEVEL:
				str = "&3Give &2max &bLevel &7(-1 sets to &r&ldefault&7)";
			break;
			case MAX_PRICE:
				str = "&3Give &2max &4Price";
			break;
			case MIN_PRICE:
				str = "&3Give &cmin &4Price";
			break;
		case RAW_MULTIPLIER:
			str = "&3Give Raw multiplier";
			break;

		}
		Metods._ins.ConversationWithPlayer(_player, new ConvModData(BUTTON.ENCHANT,this,str));
		_player.closeInventory();
	}
	void Modify_Other()
	{
		String str = "&9Give &bEnchant Count Buff &5Percent &9value";
		Metods._ins.ConversationWithPlayer(_player, new ConvModData(BUTTON.OTHER_VALUE,this,str));
		_player.closeInventory();
	}
	void Modify_Gear(GearValues selector)
	{
		
		String str = "&9Give &b"+selector+" &5multiplier &9value";
		Metods._ins.ConversationWithPlayer(_player, new ConvModData(BUTTON.GEAR_MODIFY,this,str));
		_player.closeInventory();
	}
	int GetSelector(ItemStack stack)
	{
		Integer slot = GetSLOT(stack);
		if(slot == null) slot = 0;
		return _selector_array[slot];
	}
	
	void MoveSelector_Ench(ItemStack stack)
	{
		Integer slot = GetSLOT(stack);
		_selector_array[slot]++;
		if(_selector_array[slot] < 0) _selector_array[slot] = ENCH_MAX_SELECTOR;
		if(_selector_array[slot] > ENCH_MAX_SELECTOR) _selector_array[slot] = 0;
		
		if(ENCH_SEL_NAME.values()[_selector_array[slot]] == ENCH_SEL_NAME.MAX_LEVEL)
		{
			_selector_array[slot] = 0;
		}
	}
	
	void MoveSelector_Gear(ItemStack stack)
	{
		Integer slot = GetSLOT(stack);
		_selector_array[slot]++;
		if(_selector_array[slot] < 0) _selector_array[slot] = GEAR_MAX_SELECTOR;
		if(_selector_array[slot] > GEAR_MAX_SELECTOR) _selector_array[slot] = 0;

	}
	
	void RefresItem_OtherValues()
	{
		ItemStack stack = SetButton(new ItemStack(Material.ANVIL), BUTTON.OTHER_VALUE);
		Metods.setDisplayName(stack, "&6Other Multipliers");
		Metods._ins.hideAttributes(stack);
		String[] lores = new String[6];
		double percent = Metods.Round(_main.GetShopEnchantManager().Get_enchant_count_percent() * 100.0);
		lores[0] ="&6M1 &2Modify &6value   &e&   &6M2 &bMove &6Selector";
		lores[1] =" ";
		lores[2] ="&5Each Enchant increases &2"+percent+"&6% &5to output";
		lores[3] ="&5This effects negative values too";
		lores[4] =" ";
		lores[5] = "&9 Ench Count Buff Percent&e :&2 "+ percent+" &5%";
		Metods._ins.SetLores(stack, MakeItFat(lores,_size-4,5), false);
		SetITEM(_size-4,stack);			
	}
	
	void RefresItem_Gear()
	{
		ItemStack stack = SetButton(new ItemStack(Material.NETHERITE_CHESTPLATE), BUTTON.GEAR_MODIFY);
		Metods.setDisplayName(stack, "&6Gear Multipliers");
		Metods._ins.hideAttributes(stack);
		String[] lores = new String[GearValues.values().length+2];
		lores[0] ="&6M1 &2Modify &6value   &e&   &6M2 &bMove &6Selector";
		lores[1] =" ";
		
		for(int i = 0; i < GearValues.values().length; i++)
		{
			GearValues value = GearValues.values()[i];
			lores[i+2] = "&9 "+value+ " &e :&2 "+ Metods.Round(_main.GetShopEnchantManager().GetMultiplierForGear(value));
		}
		Metods._ins.SetLores(stack, MakeItFat(lores,_size-6,2), false);
		SetITEM(_size-6,stack);			
	}
	String[] MakeItFat(String[] lores, int slot,int offset)
	{
		for(int l = 0; l < lores.length; l++)
		{						
			if((_selector_array[slot]+offset) != (l+offset)) continue;
			String[] ar = lores[(l+offset)].split(" ");
			for(int ll= 0; ll < ar.length; ll++)
			{
				ar[ll] = "&l"+ar[ll];
			}
			
			//lores[(l+offset)] = "&b&l==> "+lores[(l+offset)];
			lores[(l+offset)] = "&b&l==> "+Metods._ins.CombineArrayToOneString(ar, " ");
			break;
		}
		return lores;
	}
	
	void RefresItem_OverridedALL()
	{
		ItemStack stack = SetButton(new ItemStack(Material.PAPER), BUTTON.ENCHANT);
		Metods.setDisplayName(stack, "&5Overide ALL");
		RefresItem_Ench(stack, _enchantInfoOverideALL,_overide_all_slot);
	}
	
	void RefresItem_Ench(int slot)
	{
		EnchantINFO eInfo = _eManager.GetInfo(Enchantment.values()[slot]);
		ItemStack stack = SetButton(new ItemStack(Material.ENCHANTED_BOOK), BUTTON.ENCHANT);
		Metods.setDisplayName(stack, "&5"+Enchantment.values()[slot].getKey().getKey().toUpperCase());
		RefresItem_Ench(stack, eInfo, slot);
	}
	
	void RefresItem_Ench(ItemStack stack, EnchantINFO eInfo, int slot)
	{					
		String[] lores = new String[]
				{
						"&6M1 &2Modify &6value   &e&   &6M2 &bMove &6Selector",
						" ",					
						"&6 Max Price:&2 " + eInfo.GetMaxPrice().GetPrice(),
						"&6 Min Price:&2 " + eInfo.GetMinPrice().GetPrice(),
						"&6 Raw Mult:&2 " + eInfo.Get_rawMultiplier(),
						"&7(&e Modify by M3&7)&6 Max Level:&2 " + eInfo.GetMaxLevel(),
				};

		Metods._ins.SetLores(stack, MakeItFat(lores,slot,2), false);
		SetITEM(slot, stack);
	}
	
	@Override
	public void setupButtons() {
		
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				RefresItem_Gear();
				int i = 0;
				for(; i < Enchantment.values().length; ++i)
				{
					RefresItem_Ench(i);
				}
				for(;i < _size-9; ++i)
				{
					setupButton(BUTTON.NONE, Material.BLACK_STAINED_GLASS_PANE, " ", i);
				}
				
				for(;i < _size; ++i)
				{
					setupButton(BUTTON.NONE, Material.ORANGE_STAINED_GLASS_PANE, " ", i);
				}
				
				RefresItem_Gear();
				RefresItem_OtherValues();
				RefresItem_OverridedALL();
				
			}
		}.runTaskAsynchronously(_main);
		
	}

	
	

	


	

}
