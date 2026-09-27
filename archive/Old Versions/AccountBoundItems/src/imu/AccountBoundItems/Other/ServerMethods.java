package imu.AccountBoundItems.Other;

import java.util.UUID;

import org.bukkit.entity.Player;

import imu.AccountBoundItems.main.Main;
import net.md_5.bungee.api.ChatColor;

public class ServerMethods 
{
	Main main = Main.getInstance();
	
	
	public Player getPlayerOnServer(String nameOrUuid)
	{
		Player player = main.getServer().getPlayer(nameOrUuid);
		
		if(player != null)
		{
			return player;
		}
		try
		{
			UUID uuid = UUID.fromString(nameOrUuid);

			if(uuid != null)
			{			
				player = main.getServer().getPlayer(uuid);
				
				if(player != null)
				{
					return player;
				}
			}
		} catch (Exception e) {
			System.out.println(ChatColor.RED + "player not found by that name/uuid: "+nameOrUuid);
		}
		
		
		return null;
		
	}
	
}
