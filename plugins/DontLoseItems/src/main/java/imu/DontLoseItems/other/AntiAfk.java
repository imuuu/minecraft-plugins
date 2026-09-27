package imu.DontLoseItems.other;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import imu.DontLoseItems.main.DontLoseItems;

public class AntiAfk implements Listener
{
	public class AFK_Player
	{
		public Location LastLoc = null;
		public Long LastMoved = Long.MAX_VALUE;
		public boolean IsWarned = false;
		public AFK_Player(Player player)
		{
			LastLoc = player.getLocation();
			LastMoved = System.currentTimeMillis();
		}
		
		public void Reset(Player player)
		{
			LastLoc = player.getLocation();
			LastMoved = System.currentTimeMillis();
			IsWarned = false;
		}
	}
	private final int MAX_AFK_TIME = 60 * 45; 
	private final int KICK_WARNING_TIME = 60 * 40; 
	private final double MAX_NEEDED_MOVE_DISTANCE = 1;

	private Map<UUID, AFK_Player> _afks = new HashMap<>(); 
	
	public AntiAfk()
	{
		StartAsyncRunnable();
	}
	private void StartAsyncRunnable()
	{
		new BukkitRunnable() {
			
			@Override
			public void run()
			{
				for(Player player : Bukkit.getOnlinePlayers())
				{
					if(player.getGameMode() != GameMode.SURVIVAL)
					{
						RemoveAFK_Player(player);
						continue;
					}
					IsAfk(player);
				}
			}
		}.runTaskTimer(DontLoseItems.Instance, 20, 20);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		_afks.remove(e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void OnPlayerJoin(PlayerJoinEvent e)
	{

		_afks.put(e.getPlayer().getUniqueId(), new AFK_Player(e.getPlayer()));
	}
	
	private void RemoveAFK_Player(Player p)
	{
		if(_afks.containsKey(p.getUniqueId())) _afks.remove(p.getUniqueId());
	}
	private AFK_Player GetAFK_Player(Player p)
	{
		if(!_afks.containsKey(p.getUniqueId())) _afks.put(p.getUniqueId(), new AFK_Player(p));
		
		return _afks.get(p.getUniqueId());
	}
	private void KickPlayer(Player player)
	{
		_afks.remove(player.getUniqueId());
        player.kickPlayer("You have been kicked for being AFK for too long.");
	}
	private double GetDistance(Location loc, Location loc2)
	{
		if(loc == null || loc2 == null) return 999999999;
		
		if(loc.getWorld() != loc2.getWorld() ) return 999999999;
		
		return loc.distance(loc2);
	}
	private void IsAfk(Player player)
	{
		AFK_Player afkPlayer = GetAFK_Player(player);
			
		if (GetDistance(player.getLocation(), afkPlayer.LastLoc) > MAX_NEEDED_MOVE_DISTANCE)
		{
			afkPlayer.Reset(player);
			return;
		} 
		
		long afkTimeMillis = System.currentTimeMillis() - afkPlayer.LastMoved;
	    
	    if (afkTimeMillis  >= MAX_AFK_TIME * 1000)
	    {	
	        KickPlayer(player);
	        return;
	    }
	    
	    if(!afkPlayer.IsWarned  && afkTimeMillis >= KICK_WARNING_TIME * 1000)
	    {
	    	player.sendMessage(ChatColor.YELLOW + "You will be kicked soon if you do not move.");
	    	afkPlayer.IsWarned = true;
	    }
	    
	
	}
}
