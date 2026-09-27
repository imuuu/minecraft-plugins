package imu.iGeneralStore.ShopUtl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;

import imu.iGeneralStore.ENUMs.ShopType;
import imu.iGeneralStore.Invs.ShopUI;
import imu.iGeneralStore.Invs.ShopUI.BUTTON;
import imu.iGeneralStore.Main.Main;
import imu.iGeneralStore.Other.Cooldowns;
import net.md_5.bungee.api.ChatColor;

public class Shop 
{
	Main _main;
	String _name;
	
	ArrayList<ShopItem> _items = new ArrayList<>();
	
	int shopHolderSize = 27;
	double _sellM = 1.0;
	double _buyM  = 1.0;
	
	double _expire_percent = 0.1f;
	double _expire_cooldown_m = 30;
	String _cd_expire = "expire";
	
	ShopConfigPasser _configPasser;
	Cooldowns _cds;
	
	ShopType _type;
	
	HashMap<UUID, ShopUI> _hCustomers = new HashMap<>();
	
	public Shop(Main main, String name)
	{
		_main = main;
		_name = name;
		_configPasser = new ShopConfigPasser(_main,this);
		_type = ShopType.NORMAL;
		_cds = new Cooldowns();
	}
	
	public void openUI(Player p)
	{
		_hCustomers.put(p.getUniqueId(), new ShopUI(_main, p, this));
	}
	
	public void closeUI(Player p)
	{
		_hCustomers.remove(p.getUniqueId());
	}
	
//	public void refresShopUIslotALLcustomers(int slot)
//	{
//		for(ShopUI ui : _hCustomers.values())
//		{
//			ui.invSetItem(slot, _items.get(slot), BUTTON.SHOP_ITEM);
//		}
//	}
	
	public int getShopHolderSize() {
		return shopHolderSize;
	}

	public void setShopHolderSize(int shopHolderSize) {
		this.shopHolderSize = shopHolderSize;
	}

	public void saveShop(boolean async)
	{
		if(async)
		{
			_configPasser.saveShopAsync();
			return;
		}
		_configPasser.saveShop();
	}
	public void loadShopAsync()
	{
		_configPasser.loadShopAsync();
	}
	public double get_expire_cooldown_m() {
		return _expire_cooldown_m;
	}


	public void set_expire_cooldown_m(double _expire_cooldown_m) {
		this._expire_cooldown_m = _expire_cooldown_m;
	}

	public String getNameWithColor()
	{
		return ChatColor.translateAlternateColorCodes('&', _name);
	}
	
	public String get_name() {
		return _name;
	}
	public void set_name(String _name) {
		this._name = _name;
	}
	public ArrayList<ShopItem> get_items() {
		return _items;
	}
	public void set_items(ArrayList<ShopItem> _items) {
		this._items = _items;
	}
	public double get_sellM() {
		return _sellM;
	}
	public void set_sellM(double _sellM) {
		this._sellM = _sellM;
	}
	public double get_buyM() {
		return _buyM;
	}
	public void set_buyM(double _buyM) {
		this._buyM = _buyM;
	}
	public double get_expire_percent() {
		return _expire_percent;
	}
	public void set_expire_percent(double _expire_percent) {
		this._expire_percent = _expire_percent;
	}
	public ShopType get_type() {
		return _type;
	}
	public void set_type(ShopType _type) {
		this._type = _type;
	}
	
}
