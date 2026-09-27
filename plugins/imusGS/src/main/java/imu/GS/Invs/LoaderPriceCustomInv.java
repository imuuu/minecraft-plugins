package imu.GS.Invs;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Strings;

import imu.GS.Main.Main;
import imu.GS.ShopUtl.ShopItemModData;
import imu.GS.ShopUtl.ItemPrice.PriceCustom;
import imu.GS.ShopUtl.ShopItems.ShopItemStockable;
import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;
import imu.iAPI.Other.Tuple;

public class LoaderPriceCustomInv extends CustomInvLayout
{
	Main _main;
	ArrayList<Tuple<String,PriceCustom>> _priceCustoms;
	
	int _page = 0;
	CustomInvLayout _lastInv;
	ShopItemStockable _sis;
	ShopItemModData _modData;
	PriceCustom _lastPriceCustom;
	public LoaderPriceCustomInv(Plugin main, Player player, CustomInvLayout inv, ShopItemStockable sis, ShopItemModData modData, PriceCustom lastPriceCustom) 
	{
		super(main, player, "&9Load Custom Price ", 4*9);
		_main = (Main)main;
		
		_sis = sis;
		_modData = modData;
		_lastInv = inv;
		_lastPriceCustom = lastPriceCustom;
		_priceCustoms = _main.get_shopManager().GetSavedPlayerPriceCustoms(player.getUniqueId());
	}
	
	
	enum BUTTON implements IButton
	{
		NONE,
		BACK,
		GO_LEFT,
		GO_RIGHT,
		PRICE_CUSTOM_ITEM,
	}
	BUTTON GetBUTTON(ItemStack stack)
	{
		if(stack == null) return BUTTON.NONE;
		String bName = getButtonName(stack);		
		if(Strings.isNullOrEmpty(bName)) return BUTTON.NONE;
		return BUTTON.valueOf(bName);
	}
	
	
	
	@Override
	public void setupButtons() 
	{
		for(int i = _size-1; i > _size-9; i--) {setupButton(BUTTON.NONE, Material.BLACK_STAINED_GLASS_PANE, " ", i);}
		setupButton(BUTTON.BACK,Material.RED_STAINED_GLASS_PANE, "&c<== BACK", _size-9);
		setupButton(BUTTON.GO_LEFT,Material.BIRCH_SIGN, "&9<<", _size-6);
		setupButton(BUTTON.GO_RIGHT,Material.BIRCH_SIGN, "&9>>", _size-4);

	}
	
	void UpdatePage()
	{
		for(int i = 0; i < 27; ++i)
		{
			ItemStack displayItem = null;
			int idx = i +27 * _page;
			if(idx < _priceCustoms.size())
			{
				PriceCustom pc =_priceCustoms.get(idx).GetValue();
				String name = _priceCustoms.get(idx).GetKey();
				displayItem = new ItemStack(Material.CHEST);
				Metods.setDisplayName(displayItem, "&b"+name+" &8(Click to load!)");
				String[] lores = new String[pc.GetItems().length+2];
				lores[0] = "&eMoney: &2"+pc.GetPrice();
				lores[1] = "&9==== Contains items below ====";
				for(int l = 0; l < pc.GetItems().length; l++)
				{
					lores[2+l] = "&e"+pc.GetItems()[l]._amount+" &7"+pc.GetItems()[l]._stack.getType().toString();
				}
				_metods.SetLores(displayItem, lores, false);	
				SetButton(displayItem, BUTTON.PRICE_CUSTOM_ITEM);
				_metods.setPersistenData(displayItem, "idx", PersistentDataType.INTEGER, idx);
			}
			_inv.setItem(i, displayItem);
		}
	}

	@Override
	public void openThis() 
	{
		super.openThis();
		setupButtons();
		UpdatePage();
		_main.RegisterInv(this);
	}
	
	@Override
	public void invClosed(InventoryCloseEvent e) 
	{
		_main.UnregisterInv(this);
	}
	
	void Back()
	{
		_player.closeInventory();
		CreateCustomPriceInv ccp = new CreateCustomPriceInv(_main, _player, (ShopStocableModifyINV)_lastInv, _sis, _modData);
		ccp.openThis();
		ccp.LoadPriceCustom(_lastPriceCustom);
		
	}
	@Override
	public void onClickInsideInv(InventoryClickEvent e) 
	{
		ItemStack stack = e.getCurrentItem();
		BUTTON button = GetBUTTON(stack);
		
		switch (button) {
		case NONE:
			break;
			//CreateCustomPriceInv ccp = new CreateCustomPriceInv(_main, _player, (ShopModModifyINV)_lastInv, _sis, _modData);
		case BACK:
			Back();
			break;
		case GO_LEFT:
			_page = PageChance(_page, -1, _priceCustoms.size(), 27);
			UpdatePage();
			break;
		case GO_RIGHT:
			_page = PageChance(_page, 1, _priceCustoms.size(), 27);
			UpdatePage();
			break;
		case PRICE_CUSTOM_ITEM:
			_player.closeInventory();
			int idx = _metods.getPersistenData(stack, "idx", PersistentDataType.INTEGER);
			CreateCustomPriceInv ccp = new CreateCustomPriceInv(_main, _player, (ShopStocableModifyINV)_lastInv, _sis, _modData);
			ccp.openThis();
			ccp.LoadPriceCustom(_priceCustoms.get(idx).GetValue());
			
			break;
		
		}
		
	}

	

	

}
