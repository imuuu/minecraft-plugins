package imu.HomeTele.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import imu.HomeTele.Commands.Commands;

public class EventClass implements Listener
{
	private Commands _cmd;
	public EventClass(Commands cmd) 
	{
		_cmd = cmd;
	}

	@EventHandler
	public void onJoin(PlayerLoginEvent e)
	{
		_cmd.setHomeToHash(e.getPlayer());
		_cmd.updateNameConfig(e.getPlayer());
	}
	
	@EventHandler
	public void onLeave(PlayerQuitEvent e)
	{
		_cmd.playerHomes.remove(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e)
	{
		if(e.getEntity() instanceof Player)
		{
			Player player = (Player) e.getEntity();
			if(_cmd.playerChecks.containsKey(player))
			{
				_cmd.cancelTeleport(player);
			}
				
		}
	}
	
}
