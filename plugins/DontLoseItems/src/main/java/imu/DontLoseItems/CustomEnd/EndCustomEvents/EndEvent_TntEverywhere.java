package imu.DontLoseItems.CustomEnd.EndCustomEvents;

import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntitySpawnEvent;

import imu.DontLoseItems.CustomEnd.EndEvents;
import imu.DontLoseItems.main.DontLoseItems;
import imu.iAPI.Utilities.ImusUtilities;

public class EndEvent_TntEverywhere extends EndEvent
{
	private LinkedList<Entity> _spawnedEntites = new LinkedList<>();
	private EntityType _spawnType;
	private World _end;
	private int _ticks = 0;
	private final int _delayTicksToSpawn = 10;
	
	private final int _everyThisTickSpawnToPlayer = 20 * 8;
	private final int _chanceToSpawnToPlayer = 44;
	
	private EntityType[] _types = 
		{
			EntityType.PRIMED_TNT,	
		};
	public EndEvent_TntEverywhere()
	{
		super("Tnt every where!", 40);
		
		for(World w :  DontLoseItems.Instance.getServer().getWorlds() )
		{
			if(w.getEnvironment() == Environment.THE_END)
			{
				_end = w;
				break;
			}
		}
		
		ChestLootAmount = 1;
	}

	@Override
	public void OnEventStart()
	{
		_ticks = 0;
		_spawnType = _types[0];
		RemoveEndermans();
	}
	
	@Override
	public void OnEventEnd()
	{
		_ticks = 0;
		AddChestLootBaseToAll(ChestLootAmount);
		for(Entity e : _spawnedEntites)
		{	
			if(e == null || !e.isValid()) continue;
			
			e.remove();
		}
		_spawnedEntites.clear();
	}
	private void RemoveEndermans()
	{
		for(Entity ent : _end.getLivingEntities())
		{	
			if(!EndEvents.Instance.IsPlayerUnstableArea(ent)) return;
			
			if(ent.getType() != EntityType.ENDERMAN) return;

			Enderman livEnt = (Enderman)ent;

			livEnt.remove();
		}
		

	}

	private void SpawnEntityAround(Player player)
	{
		
		Chunk[] chunks = ImusUtilities.Get9ChunksAround(player.getLocation());

		int player_y = player.getLocation().getBlockY();
		Chunk chunk = chunks[ThreadLocalRandom.current().nextInt(chunks.length)];
		int x = ThreadLocalRandom.current().nextInt(16);
		int z = ThreadLocalRandom.current().nextInt(16);
		
		if(player_y < 0 || player_y > 255) return;
		
		Block b = chunk.getBlock(x, player_y + 15, z);
		
		while(b.getType().isAir() && b.getY() > 0)
		{
			b = b.getRelative(BlockFace.DOWN);
		}
		
		if(b.getType().isAir())
		{
			return;
		}
		
		Location loc = b.getRelative(BlockFace.UP).getLocation();
		
		SpawnTnt(loc);
	}
	
	private void SpawnTnt(Location loc)
	{
		if(loc.getBlock() == null) return;
		
		if(!loc.getBlock().getType().isAir()) return;
		
		Entity newEnt =_end.spawnEntity(loc, _spawnType);
		_spawnedEntites.add(newEnt);
	}
	@Override
	public void OnOneTickLoop()
	{
		_ticks++;
		
		if(_ticks % _everyThisTickSpawnToPlayer == 0 && ThreadLocalRandom.current().nextInt(100) < _chanceToSpawnToPlayer)
		{
			for(Player p : GetPlayers())
			{
				SpawnTnt(p.getLocation().add(0, 1, 0));
			}
		}
		
		if(_ticks % _delayTicksToSpawn != 0) return;
		
		for(Player p : GetPlayers())
		{
			SpawnEntityAround(p);
		}
		
		
	}
	
	@EventHandler
	private void OnEntitySpawn(EntitySpawnEvent e)
	{
		if(e.isCancelled()) return;
		
		if(e.getEntityType() != EntityType.ENDERMAN) return;
		
		if(!EndEvents.Instance.IsPlayerUnstableArea(e.getEntity())) return;
		
		e.setCancelled(true);	
	}

	@Override
	public String GetEventName()
	{
		
		return GetName();
	}

	@Override
	public String GetRewardInfo()
	{
		
		return "Chestloot base by &2+"+ChestLootAmount;
	}

	@Override
	public String GetDescription()
	{
		return "&6Whatch out tnt!";
	}

	@Override
	public void OnPlayerLeftMiddleOfEvent(Player player)
	{
		
	}

	@Override
	public void OnPlayerJoinMiddleOfEvent(Player player)
	{
		
	}

	

}
