package imu.iMiniGames.Other;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.PlayerDataCard;
import imu.iMiniGames.Handlers.GameCard;

public class PlayerGameDataCard extends PlayerDataCard
{

	public PlayerGameDataCard(Plugin main, Player p, String dataFolderName) 
	{
		super(main, p, dataFolderName);
	}
	
	
	public void setDataToPLAYER(GameCard card, Player player)
	{	
		
		new BukkitRunnable() 
		{
			
			@Override
			public void run() 
			{
				if(player != null)
				{
					player.setHealth(get_health());
					player.setFoodLevel(get_foodLevel());
					changeExp(player, Integer.MAX_VALUE);
					changeExp(player, get_xp());		
					
					player.setGameMode(get_gamemode());
					player.addPotionEffects(get_potionEffects());

					player.setFireTicks(get_fireTick());
					if(player.getAllowFlight() && is_isFlying())
					{
						player.setFlying(is_isFlying());
					}
					
					if(card != null && card instanceof CombatGameCard && ((CombatDataCard) card.getDataCard()).isOwnGearKit)
					{
						
						player.getInventory().setContents(((CombatGameCard)card).checkAndApplyCombatConsumambles(player,get_invContent()));
					}else
					{
						player.getInventory().setContents(get_invContent());
					}
					System.out.println("Minigame info: player: "+player.getName() + " has got all his levels/items/mode back!");
					get_player().teleport(get_location());
				}
				else
				{
					System.out.println("Couldn't find player with uuid PlayerDataCard:setDataToPLAYER");
				}
			}
		}.runTask(ImusAPI._instance);
		
	}
}
