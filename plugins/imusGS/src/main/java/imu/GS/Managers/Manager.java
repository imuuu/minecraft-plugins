package imu.GS.Managers;

import org.bukkit.Bukkit;

import imu.GS.Main.Main;
import imu.iAPI.Other.Metods;

public abstract class Manager 
{
	protected Main _main;
	public Manager(Main main)
	{
		_main = main;
	}
	public abstract void INIT();
	
	protected void PrintINFO(String metodName, String info)
	{
		Bukkit.getLogger().info(Metods.msgC(_main._pluginNamePrefix+":&6"+getClass().getSimpleName()+":&5"+metodName+": &2" +info));
	}
	protected void PrintERROR(String metodName, String info)
	{
		Bukkit.getLogger().info(Metods.msgC(_main._pluginNamePrefix+":&6"+getClass().getSimpleName()+":&5"+metodName+": &c" +info));
	}

}
