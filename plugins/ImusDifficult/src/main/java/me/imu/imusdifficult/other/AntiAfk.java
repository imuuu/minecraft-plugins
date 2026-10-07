package me.imu.imusdifficult.other;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.imu.imusdifficult.ImusDifficult;

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
	// seconds
	private int MAX_AFK_TIME = 60 * 45;
	private int KICK_WARNING_TIME = 60 * 40;
	private final double MAX_NEEDED_MOVE_DISTANCE = 1;
	private boolean _enabled = true;

	private Map<UUID, AFK_Player> _afks = new HashMap<>();

	public AntiAfk()
	{
		LoadSettings(ImusDifficult.Instance.getConfig());
		StartRunnable();
	}

	// config.yml "anti-afk", re-read when the settings menu changes a value
	public void LoadSettings(FileConfiguration config)
	{
		_enabled = config.getBoolean("anti-afk.enabled", true);
		MAX_AFK_TIME = config.getInt("anti-afk.kick-minutes", 45) * 60;
		KICK_WARNING_TIME = config.getInt("anti-afk.warning-minutes", 40) * 60;
	}

	private void StartRunnable()
	{
		new BukkitRunnable() {

			@Override
			public void run()
			{
				if(!_enabled)
				{
					_afks.clear();
					return;
				}

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
		}.runTaskTimer(ImusDifficult.Instance, 20, 20);
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
