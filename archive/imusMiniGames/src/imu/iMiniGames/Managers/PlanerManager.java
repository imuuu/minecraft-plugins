package imu.iMiniGames.Managers;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.iAPI.Other.Metods;
import imu.iMiniGames.Main.ImusMiniGames;

public class PlanerManager 
{
	ImusMiniGames _main;
	
	String pd_value = "planer_value";
		
	public PlanerManager(ImusMiniGames main) 
	{
		_main = main;
	}
	
	public void setPDvalue(ItemStack stack,String value)
	{
		Metods._ins.setPersistenData(stack, pd_value, PersistentDataType.STRING, value);
	}
	
	public String getPDvalue(ItemStack stack)
	{
		return Metods._ins.getPersistenData(stack, pd_value, PersistentDataType.STRING);
	}
	
	public void removePDvalue(ItemStack stack)
	{
		Metods._ins.removePersistenData(stack, pd_value);
	}
	
}
