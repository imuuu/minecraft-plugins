package imu.GS.Managers;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import imu.GS.Main.Main;
import imu.GS.ShopUtl.ItemPrice.ItemPrice;
import imu.GS.ShopUtl.ShopItems.ShopItemUnique;
import imu.iAPI.Other.Metods;

public final class UniqueManager 
{
	Main _main;
	ShopManager _shopManager;
	ShopManagerSQL _shopManagerSQL;
	//HashMap<UUID, ShopItemUnique> _uniques;
	HashMap<ItemStack, ShopItemUnique> _uniques;
	//public final String _pd_uniqueUUID = "gs.uniqueUUID";
	
	public UniqueManager(Main main, ShopManager shopManager, ShopManagerSQL shopManagerSQL)
	{
		_main = main;
		_shopManager = shopManager;
		_shopManagerSQL = shopManagerSQL;
		_uniques = new HashMap<>();
	}
	
	public HashMap<ItemStack, ShopItemUnique> GetUniques()
	{
		return _uniques;
	}
	
	public void AddUniqueItem(ShopItemUnique siu, boolean saveDatabase)
	{
		//PutPDuuid(siu.GetRealItem(), siu.GetUUID());
		
		_uniques.put(GetTestStack(siu.GetRealItem()), siu);

		
		if(saveDatabase) _shopManagerSQL.SaveUniqueItemAsync(siu);
	}
	ItemStack GetTestStack(ItemStack stack)
	{
		ItemStack testItem = stack.clone();
		Metods._ins.setDamage(testItem, 0);
		return testItem;
		
	}
	public ItemPrice GetPriceItem(ItemStack stack)
	{
		return _uniques.get(GetTestStack(stack)).GetItemPrice();
	}
	
//	public ItemPrice GetPriceItem(ItemStack stack)
//	{
//		return GetUnique(stack).GetItemPrice();
//	}
	
//	public void PutPDuuid(ItemStack stack, UUID uuid)
//	{
//		ImusAPI._metods.setPersistenData(stack, _pd_uniqueUUID, PersistentDataType.STRING, uuid.toString());
//	}
//	
//	public UUID GetUniqueUUID(ItemStack stack)
//	{
//		return UUID.fromString(ImusAPI._metods.getPersistenData(stack, _pd_uniqueUUID, PersistentDataType.STRING));
//	}
	
	public UUID GetUniqueUUID(ItemStack stack)
	{
		return _uniques.get(GetTestStack(stack)).GetUUID();
	}
//	
//	public void RemovePDuuid(ItemStack stack)
//	{
//		ImusAPI._metods.removePersistenData(stack, _pd_uniqueUUID);
//	}
//	
//	public ShopItemUnique GetUnique(ItemStack stack)
//	{
//		return _uniques.get(UUID.fromString(ImusAPI._metods.getPersistenData(stack, _pd_uniqueUUID, PersistentDataType.STRING)));
//	}
	
//	public void RemoveShopItemUnique(ShopItemBase sib)
//	{
//		_uniques.remove(sib.GetUUID());
//		RemovePDuuid(sib.GetRealItem());
//		_main.get_shopManager().GetShopManagerSQL().DeleteUniqueItemAsync(sib);
//	}
	
	public void RemoveUnique(ItemStack stack)
	{
		_main.get_shopManager().GetShopManagerSQL().DeleteUniqueItemAsync(_uniques.get(GetTestStack(stack)));
		_uniques.remove(GetTestStack(stack));
	}
	
//	public boolean IsUnique(ItemStack stack)
//	{
//		return ImusAPI._metods.getPersistenData(stack, _pd_uniqueUUID, PersistentDataType.STRING) == null ? false : true;
//	}
	
	public boolean IsUnique(ItemStack stack)
	{
		return _uniques.containsKey(GetTestStack(stack));
	}
	
//	public boolean IsSavedUnique(ItemStack stack)
//	{
//		String uuidSTR = ImusAPI._metods.getPersistenData(stack, _pd_uniqueUUID, PersistentDataType.STRING);
//		if(uuidSTR == null)
//			return false;
//		
//		UUID uuid = UUID.fromString(uuidSTR);
//		return _uniques.containsKey(uuid);
//	}
	
}
