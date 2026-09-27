package imu.GS.Managers;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import imu.GS.Main.Main;
import imu.GS.Other.LogData;
import imu.GS.ShopUtl.Shop;
import imu.GS.ShopUtl.ShopItemBase;
import imu.GS.ShopUtl.ShopNormal;
import imu.GS.ShopUtl.ItemPrice.PriceCustom;
import imu.GS.ShopUtl.ShopItems.ShopItemSeller;
import imu.GS.ShopUtl.ShopItems.ShopItemStockable;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;
import imu.iAPI.Other.Tuple;

public class ShopManager 
{
	private Main _main;
	
	private ArrayList<Shop> _shops;
	
	
	private BukkitTask RunnableAsyncTask;
	private ShopManagerSQL _shopManagerSQL;
	private UniqueManager _uniqueManager;
	
	//private HashMap<UUID, CustomInvLayout> _opened_invs = new HashMap<>();
	private HashMap<UUID, ArrayList<Tuple<String, PriceCustom>>> _savedPriceCustoms = new HashMap<>();
	
	private int _shopCheckTime_s = 20;
	
	public final String pd_page="gs.page";
	public final String pd_slot="gs.slot";
	public double _durability_penalty = 0.1; // 0.0 => 0%
	@SuppressWarnings("unused")
	private Cooldowns _cds;
	public ShopManager(Main main)
	{
		_main = main;
		_cds = new Cooldowns();
		_shops = new ArrayList<>();
		_shopManagerSQL = new ShopManagerSQL(_main, this);
		_uniqueManager = new UniqueManager(_main, this, _shopManagerSQL);
		
	}
	
	public void Init()
	{	
		new BukkitRunnable() 
		{			
			@Override
			public void run() 
			{
				_shopManagerSQL.LoadTables();
				_main.GetMaterialManager().CreateTables();
				_main.GetMaterialManager().LoadMaterialPrices();
				_main.GetMaterialManager().LoadMaterialOverflows();
				_shopManagerSQL.LoadUniques();
				_shopManagerSQL.LoadShops();	
				_shopManagerSQL.LoadShopItems();
				_main.GetShopEnchantManager().INIT();
				_main.GetTagManager().LoadMaterialTags();				
				_main.GetTagManager().LoadAllShopItemTagsNamesAsync();
				
			}
		}.runTaskAsynchronously(_main);
		
		
		RunnableAsync();
	}
	
	
	
	public double GetDurabilityReduction(ItemStack stack)
	{
		double durProsent = Metods._ins.getDurabilityProsent(stack);
		if(durProsent < 1.0)
		{
			durProsent = durProsent - _durability_penalty;
			
		}
		if(durProsent < 0) durProsent = 0;
		if(durProsent > 1.0) durProsent = 1;
		
		return durProsent;
	}
	
	
	
	public ShopManagerSQL GetShopManagerSQL()
	{
		return _shopManagerSQL;
	}
	
	public Integer GetSISSlot(ItemStack stack)
	{
		if(stack == null) return null;
		return ImusAPI._metods.getPersistenData(stack, pd_slot, PersistentDataType.INTEGER);
	}
	
	public Integer GetSISPage(ItemStack stack)
	{
		if(stack == null) return null;
		return ImusAPI._metods.getPersistenData(stack, pd_page, PersistentDataType.INTEGER);
	}
	
	public UniqueManager GetUniqueManager()
	{
		return _uniqueManager;
	}
	
//	public void RegisterOpenedInv(Player player, CustomInvLayout inv)
//	{
//		_opened_invs.put(player.getUniqueId(), inv);
//	}
//	
//	public void UnRegisterOpenedInv(Player player)
//	{
//		_opened_invs.remove(player.getUniqueId());
//	}
	
	public void SavePriceCustom(UUID playerUUID, String name,PriceCustom pc)
	{
		if(!_savedPriceCustoms.containsKey(playerUUID)) _savedPriceCustoms.put(playerUUID, new ArrayList<>());
		_savedPriceCustoms.get(playerUUID).add(new Tuple<String, PriceCustom>(name,pc));
	}
	
	public ArrayList<Tuple<String,PriceCustom>> GetSavedPlayerPriceCustoms(UUID playerUUID)
	{
		return (_savedPriceCustoms.containsKey(playerUUID) ? _savedPriceCustoms.get(playerUUID) : new ArrayList<Tuple<String,PriceCustom>>());
	}
	
	public ArrayList<Shop> GetShops()
	{
		return _shops;
	}
	
	public ArrayList<ShopNormal> GetNormalShops()
	{
		ArrayList<ShopNormal> normalShops = new ArrayList<ShopNormal>();
		
		for (Shop shop : _shops)
		{
			if(shop instanceof ShopNormal) normalShops.add((ShopNormal)shop);
		}
		
		return normalShops;
	}
	
	public void LogRegisterPurchace(List<LogData> logDatas, LogData data)
	{
		for(LogData logi : logDatas)
		{
			ShopItemBase sib = logi.Get_shopitem();
			if(!sib.getClass().equals(data.Get_shopitem().getClass())) continue;
			
			if(!sib.IsSameKind(data.Get_shopitem())) continue;
			
			if(data.Get_price() != logi.Get_price()) continue;
			
			logi.AddAmount(data.Get_amount());
			return;
		}
		
		logDatas.add(data);
	}
	
	public void SendLogs(Player player,List<LogData> logDatas)
	{
		_main.get_shopManager().GetShopManagerSQL().LogPurchaseAsync(player, logDatas,0);
	}
	
	void RunnableAsync()
	{
		//Instant timeStamp = Instant.now();
//		Instant oneHourAgo = timeStamp.minus(Duration.ofMinutes(1));
//		if(timeStamp.isBefore(oneHourAgo))
//		{
//			System.out.println("One minute has passed");
//			timeStamp.i
//		}
		
		
		RunnableAsyncTask = new BukkitRunnable() 
		{			
			
			@Override
			public void run() 
			{
				//System.out.println("Check expires");
				for(Shop s : _shops)
				{
					if(!(s instanceof ShopNormal)) continue;
					
					ShopNormal shop = (ShopNormal)s;
					
					if(shop.HasExpired()) // && 
					{						
						//System.out.println("Check shop");
						shop.SetLockToInteract(true);
						CheckShopItems(shop,true);				
						shop.SetNewExpire();
						
						shop.ArrangeShopItems(shop.HasCustomers() ? false : true, false); // => interact fill be false
												
						//shop.SetLockToInteract(false);
					}
					else
					{
						shop.SetLockToInteract(true);
						CheckShopItems(shop,false);
						shop.SetLockToInteract(false);
					}
				}
				
//				if(_cds.isCooldownReady("SQL_CONNECTION_CHECK"))
//				{
//					_cds.setCooldownInSeconds("SQL_CONNECTION_CHECK", 60*60);
//					
//					if(_shopManagerSQL.CheckConnection())
//					{
//						Bukkit.getLogger().info("[imusGS] Checking SQL connection and its TRUE");
//					}else
//					{
//						Bukkit.getLogger().info("[imusGS] Checking SQL connection and its FALSE");
//					}
//				}
				
			}
		}.runTaskTimerAsynchronously(_main, 20 * _shopCheckTime_s, 20 * _shopCheckTime_s);
		
		
	}
	void CheckShopItems(ShopNormal sBase, boolean checkExpires)
	{
		for(ShopItemSeller[] siss : sBase.get_items())
		{
			for(ShopItemSeller sis : siss)
			{
				
				if(sis == null)
					continue;
				if(sis instanceof ShopItemStockable)
				{
					if(((ShopItemStockable)sis).AbleToFill())
					{
						((ShopItemStockable)sis).Fill();
						((ShopItemStockable)sis).RefreshFillCD();
					}
					continue;
				}
				
				if(!checkExpires) continue;
				
				int amount = sis.Get_amount();
				
				int newAmount = (int)Math.floor(amount * (1.0 - sBase.get_expire_percent()));
				if(newAmount < 0)
					newAmount = 0;

				sis.Set_amount(newAmount);
			}
		}
	}

	
	
	
//	public boolean OpenShop(Player p,String name)
//	{
//		Shop shop = GetShop(name);
//		if(shop != null)
//		{			
//			if(shop.HasLocked())
//			{
//				p.sendMessage(Metods.msgC("&2Shop has closed. Please come laiter"));
//				return false;
//			}
//			shop.AddNewCustomer(p);
//			return true;
//		}
//		return false;
//	}
	
	public void onDisabled()
	{	
		closeShopInvs();
		if(RunnableAsyncTask != null)
			RunnableAsyncTask.cancel();
	}
	
	void closeShopInvs()
	{
		for(Shop shop : _shops)
		{
			shop.RemoveCustomerALL();
		}
		
	}
	
	public void CreateNewShop(String name)
	{
		ShopNormal shop = new ShopNormal(_main, UUID.randomUUID(), name, 1);
		AddShop(shop);
	}
	
	public void AddShop(ShopNormal shopBase)
	{
		_shops.add(shopBase);
		//System.out.println("Shop added: "+shopBase+ " size shops: "+_shops.size());
		
		SaveShop(shopBase.GetUUID(), true);
		UpdateTabCompliters();
	}
	
	public void RemoveShop(UUID uuid)
	{
		Shop s = GetShop(uuid);
		if(s == null)
			return;
		
		s.RemoveCustomerALL();
		_shops.remove(s);

		_shopManagerSQL.DeleteShopAsync((ShopNormal)s);
	}
	
	public Shop GetShop(UUID uuid)
	{
		for(Shop s : _shops)
		{
			if(s.GetUUID().equals(uuid)) return s;
		}
		return null;
	}
	
	public Shop GetShop(String name)
	{
		String searchName = ImusAPI._metods.StripColor(name);

		for(Shop s : _shops)
		{
			if(s.GetName().toLowerCase().contains(searchName.toLowerCase()))
			{
				//System.out.println("Shop found!");
				return s;
			}
				
		}
		//System.out.println("Shop NOT found by name!"+name);
		return null;
	}
	
	public void SaveShop(UUID uuid, boolean dontsavedatabase)
	{
//		if(_main.GetSQL() == null || dontsavedatabase)
//		{
//			System.out.println("Couldnt find SQL => didnt save data");
//			return;
//			
//		}
			
		
		Shop sBase = GetShop(uuid);
		
		if(sBase == null)
		{
			System.out.println("Coudnt foind shop from list");
			return;
		}
			
		
		_shopManagerSQL.SaveShopAsync((ShopNormal)sBase);
	}
	

	
	
	public void UpdateTabCompliters()
	{
		
		String[] shopNames = new String[_shops.size()];
		for(int i = 0; i < _shops.size(); ++i)
		{
			shopNames[i] = _shops.get(i).GetName();
		}
		
		_main.UpdateShopNames(shopNames);
	}
	
	
}
