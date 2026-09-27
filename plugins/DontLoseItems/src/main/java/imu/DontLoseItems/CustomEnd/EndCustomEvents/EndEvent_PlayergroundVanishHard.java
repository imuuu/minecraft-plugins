package imu.DontLoseItems.CustomEnd.EndCustomEvents;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import imu.DontLoseItems.CustomEnd.EndEvents;
import imu.iAPI.Other.Cooldowns;

public class EndEvent_PlayergroundVanishHard extends EndEvent
{
	private HashMap<UUID, Location> _playerLocs = new HashMap<>();
	private final double _distance = 0.3;
	private final int _radius = 4;
	private final Vector _offset = new Vector(0, -1, 0);
	
	private final Material[] _mats = 
		{
				Material.BLACK_TERRACOTTA,
				Material.GRAY_TERRACOTTA,
				Material.WHITE_TERRACOTTA,
				Material.AIR,
			};
	
 	public EndEvent_PlayergroundVanishHard()
	{
		super("Harder blocks disapier underneat ", 80);
		ChestLootAmount = 3;
	}

	@Override
	public void OnEventStart()
	{
		ChestLootAmount = 3;
		_playerLocs.clear();
		
	}

	@Override
	public void OnEventEnd()
	{
		AddChestLootBaseToAll(ChestLootAmount);
		_playerLocs.clear();
	}

	@Override
	public void OnPlayerLeftMiddleOfEvent(Player player)
	{
		
	}

	@Override
	public void OnPlayerJoinMiddleOfEvent(Player player)
	{
		
	}
	private double GetDistance(Player p)
	{
		
		if(!_playerLocs.containsKey(p.getUniqueId())) 
		{
			_playerLocs.put(p.getUniqueId(), p.getLocation());
			return 1000;
		}
		Location loc = _playerLocs.get(p.getUniqueId());
		
		if(loc.getWorld() != p.getLocation().getWorld()) return 0;
		
		double distance = loc.distance(p.getLocation());
		
		return distance;
	}
	@Override
	public void OnOneTickLoop()
	{
		for(Player p : GetPlayers())
		{

			if( GetDistance(p) > _distance) 
			{
				_playerLocs.put(p.getUniqueId(), p.getLocation());
				OnRemoveGround(p);
			}
		}
	}
	
	private Cooldowns _cd = new Cooldowns();
	private void OnRemoveGround(Player p)
	{

		EndEvents.Instance.CreateMaterialSphere(null, p.getLocation().add(_offset), _mats, 10, p.getLocation().getY()-1, _radius);
		
		final String id = p.getUniqueId() + "water_search";
		if(!_cd.isCooldownReady(id))
		{
			return;
		}
		
		_cd.setCooldownInSeconds(id, 10);
		
		EndEvents.Instance.replaceWaterSourcesAsync(p.getLocation(), 300);
	}

	@Override
	public String GetEventName()
	{
		
		return GetName();
	}

	@Override
	public String GetRewardInfo()
	{
		
		return "Chestloot roll + "+ChestLootAmount;
	}

	@Override
	public String GetDescription()
	{
		
		return "Harder blocks disapier";
	}
	
	

}
