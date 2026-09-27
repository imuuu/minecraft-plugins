package imu.TokenTp.CustomItems;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import imu.TokenTp.Enums.TeleTokenType;
import imu.TokenTp.main.Main;

public class ItemTeleTokenToken  extends ItemTeleToken
{
	public ItemTeleTokenToken(Main main, String addressName, Location loc) 
	{
		super(main);
		_addressName = addressName;
		_loc = loc;
		setThings();
	}
	public ItemTeleTokenToken(Main main) 
	{
		super(main);
	}
	
	void setThings()
	{
		setTokenDesc();
		setAddress(_addressName,_loc);
		setTeleTokenType(TeleTokenType.TOKEN);
		
	}
	
	public void setTokenDesc()
	{
		setDesc("You are able to teleport with this token! Press M2!");
		setDisplayName(ChatColor.AQUA +" "+ ChatColor.MAGIC+ "#" +ChatColor.DARK_PURPLE +" "+ ChatColor.BOLD + "Teletoken"+ ChatColor.AQUA +" "+ ChatColor.MAGIC+ "#");
	}
}
