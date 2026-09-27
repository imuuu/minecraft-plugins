package imu.iMiniGames.Other;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class ArenaKit 
{
	String _kitName;
	ItemStack[] _kitInv;
	
	public ArenaKit(String name, ItemStack[] kitInv) 
	{
		_kitName = name;
		_kitInv = kitInv;
	}
	public String get_kitName() {
		return _kitName;
	}
	public void set_kitName(String _kitName) {
		this._kitName = _kitName;
	}
	public ItemStack[] get_kitInv() {
		return _kitInv;
	}
	public void set_kitInv(ItemStack[] _kitInv) {
		this._kitInv = _kitInv;
	}
	public String get_kitNameWithColor()
	{
		return ChatColor.translateAlternateColorCodes('&', _kitName);
	}
	
}
