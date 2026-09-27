package imu.AccountBoundItems.Other;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class ItemPersistentDataMethods extends ItemMetods
{
	/**
	 * 
	 * adds persistendata to item as uuid
	 * @return
	 */
	public void setUUIDData(ItemStack stack, String uuid)
	{
		setPersistenData(stack, main.keyNames.get("uuid"), PersistentDataType.STRING, uuid);
	}
	
	/**
	 * 
	 * adds persistendata to item as Name 
	 * @return
	 */
	public void setNameData(ItemStack stack, String name)
	{
		setPersistenData(stack, main.keyNames.get("name"), PersistentDataType.STRING, name);
	}
	
	public void setBoundData(ItemStack stack, Integer bound)
	{
		setPersistenData(stack, main.keyNames.get("bound"),PersistentDataType.INTEGER, bound);
	}
	
	Integer getBoundData(ItemStack stack)
	{
		return getPersistenData(stack,  main.keyNames.get("bound"), PersistentDataType.INTEGER);
	}
	
	/**
	 * 
	 * adds persistendata to item as price
	 * @return
	 */
	public void setPriceData(ItemStack stack, double price, boolean override)
	{
		if(override)
		{
			setPersistenData(stack, main.keyNames.get("price"), PersistentDataType.DOUBLE, price);
			setPersistenData(stack, main.keyNames.get("overrideprice"), PersistentDataType.INTEGER, 1);
		}
		else
		{
			setPersistenData(stack, main.keyNames.get("price"), PersistentDataType.DOUBLE, price);
			setPersistenData(stack, main.keyNames.get("overrideprice"), PersistentDataType.INTEGER, 0);
		}
		
	}
	
	public boolean isOverridePrice(ItemStack stack)
	{
		Integer isOv = getPersistenData(stack, main.keyNames.get("overrideprice"),PersistentDataType.INTEGER);
		if(isOv != null && isOv > 0)
		{
			return true;
		}
		return false;
		
	}
	
	/**
	 * 
	 * get item price if there is, returns null if not
	 * @return
	 */
	public Double getPriceData(ItemStack stack)
	{
		return getPersistenData(stack, main.keyNames.get("price"), PersistentDataType.DOUBLE);
	}
	
	
	
	/**
	 * 
	 * add item tag as broken. broken = 1, not broken = 0
	 * @return
	 */
	public void setBrokenData(ItemStack stack, int isBroken)
	{
		setPersistenData(stack, main.keyNames.get("broken"), PersistentDataType.INTEGER, isBroken);
	}
	
	/**
	 * 
	 * gets if item is broken.. if broken then return 0, if not 1
	 * @return
	 */
	Integer getBrokenData(ItemStack stack)
	{
		return getPersistenData(stack, main.keyNames.get("broken"), PersistentDataType.INTEGER);
	}
	
	/**
	 * 
	 * get items binders name if there is, returns null if not
	 * @return
	 */
	public String getNameData(ItemStack stack)
	{
		return getPersistenData(stack, main.keyNames.get("name"), PersistentDataType.STRING);
	}
	
	/**
	 * 
	 * get items player UUID if there is, returns null if not
	 * @return
	 */
	public String getUUIDData(ItemStack stack)
	{
		return getPersistenData(stack, main.keyNames.get("uuid"), PersistentDataType.STRING);
	}
	
	void setWaitData(ItemStack stack, Integer data)
	{
		setPersistenData(stack, main.keyNames.get("wait"), PersistentDataType.INTEGER, data);
	}
	
	public Integer getWaitData(ItemStack stack)
	{
		return getPersistenData(stack, main.keyNames.get("wait"), PersistentDataType.INTEGER);
	}
	
	void setOnUseWaitData(ItemStack stack,Integer data)
	{
		setPersistenData(stack, main.keyNames.get("onuse"), PersistentDataType.INTEGER, data);
	}
	
	Integer getOnUseWaitData(ItemStack stack)
	{
		return getPersistenData(stack, main.keyNames.get("onuse"), PersistentDataType.INTEGER);
	}
}
