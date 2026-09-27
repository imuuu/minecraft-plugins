package imu.TokenTp.CustomItems;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import imu.TokenTp.Enums.TeleTokenType;
import imu.TokenTp.main.Main;

public class ItemTeleTokenCard extends ItemTeleToken
{

	public ItemTeleTokenCard(Main main, String addressName, Location loc) 
	{
		super(main);
		_addressName = addressName;
		_loc = loc;
		setThings();
		setDis();
	}
	
	public ItemTeleTokenCard(Main main) 
	{
		super(main);
		setDis();
	}
	
	void setThings()
	{
		setDesc("Card!");
		setAddress(_addressName,_loc);
		setTeleTokenType(TeleTokenType.CARD);
	}
		
	void setDis()
	{
		setDisplayName(ChatColor.DARK_PURPLE +" "+ ChatColor.BOLD + "Teletoken"+ ChatColor.WHITE +""+ ChatColor.BOLD+ " CARD");
	}
}
