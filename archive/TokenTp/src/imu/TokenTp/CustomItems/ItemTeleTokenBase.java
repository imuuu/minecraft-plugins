package imu.TokenTp.CustomItems;

import org.bukkit.ChatColor;

import imu.TokenTp.Enums.TeleTokenType;
import imu.TokenTp.main.Main;

public class ItemTeleTokenBase extends ItemTeleToken
{

	public ItemTeleTokenBase(Main main) 
	{
		super(main);
		setThings();
		setDisplayName(ChatColor.DARK_PURPLE +" "+ ChatColor.BOLD + "Teletoken"+ ChatColor.WHITE +""+ ChatColor.BOLD+ " BASE");
	}
	
	void setThings()
	{
		setDesc("Combine with card and get Token!");
		setAddress("No destination",null);
		setTeleTokenType(TeleTokenType.BASE);
	}

}
