package imu.DontLoseItems.CustomEnd.EndCustomEvents;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntitySpawnEvent;

import imu.DontLoseItems.CustomEnd.EndEvents;
import imu.DontLoseItems.main.DontLoseItems;
import imu.iAPI.Utilities.ImusUtilities;

public class EndEvent_RandomEntityTypeEnderman extends EndEvent
{
	private LinkedList<Entity> _spawnedEntites = new LinkedList<>();
	private HashMap<Chunk, Integer> _chunkData = new HashMap<>();
	private EntityType _spawnType;
	private World _end;
	private int _max_in_chunk = 3;
	
	public class SpawnType
	{
		public int MAX_IN_CHUNK = 3;
		public EntityType _type;
		public SpawnType(int amountINChunk, EntityType type)
		{
			MAX_IN_CHUNK = amountINChunk;
			_type = type;
		}
	}
	private SpawnType[] _types = 
		{
			new SpawnType(-1, EntityType.CREEPER),
			new SpawnType(-1, EntityType.ZOMBIE),
			new SpawnType(1, EntityType.PIGLIN_BRUTE),
			new SpawnType(-1, EntityType.WITCH),
			//new SpawnType(-1, EntityType.BAT),
			new SpawnType(-1, EntityType.SHULKER),
			new SpawnType(1, EntityType.RAVAGER),
			new SpawnType(1, EntityType.CAVE_SPIDER),
			new SpawnType(2, EntityType.WITHER_SKELETON),
//			new SpawnType(-1, EntityType.BLAZE),
			new SpawnType(1, EntityType.GHAST),


		};
	
	public EndEvent_RandomEntityTypeEnderman()
	{
		super("Endermen change their type", 60);
		
		for(World w :  DontLoseItems.Instance.getServer().getWorlds() )
		{
			if(w.getEnvironment() == Environment.THE_END)
			{
				_end = w;
				break;
			}
		}
	}

	@Override
	public void OnEventStart()
	{
		_chunkData.clear();
		_spawnedEntites.clear();
		SpawnType spawn = _types[ThreadLocalRandom.current().nextInt(_types.length)];
		_spawnType = spawn._type;
		_max_in_chunk = spawn.MAX_IN_CHUNK == -1 ? _max_in_chunk : spawn.MAX_IN_CHUNK;
		
		for(Entity ent : _end.getLivingEntities())
		{

			ChanceEntity(ent);			
		}
	}
	
	@Override
	public void OnEventEnd()
	{
		for(Entity e : _spawnedEntites)
		{
			
			if(e == null || !e.isValid()) continue;
			
			e.remove();
		}
		_spawnedEntites.clear();
		_chunkData.clear();
	}
	
	private void ChanceEntity(Entity ent)
	{
		if(ent.getType() != EntityType.ENDERMAN) return;
		
		if(!EndEvents.Instance.IsPlayerUnstableArea(ent)) return;
		
		
		
		Enderman livEnt = (Enderman)ent;
		Location loc = livEnt.getLocation().clone();
		livEnt.remove();
		
		if(!_chunkData.containsKey(loc.getChunk())) _chunkData.put(loc.getChunk(), 0);
		
		int data = _chunkData.get(loc.getChunk())+1;
		
		if(data > _max_in_chunk) 
		{
			return;
		}
		
		_chunkData.put(loc.getChunk(), data);
		
		if(_spawnType == EntityType.GHAST) loc.add(0, 5, 0);
		
		
		LivingEntity newEnt = (LivingEntity)_end.spawnEntity(loc, _spawnType);
		if(_spawnType == EntityType.PIGLIN_BRUTE)
		{
			PiglinBrute piglinBrute = (PiglinBrute) newEnt;
			piglinBrute.setConversionTime(Integer.MAX_VALUE);
		}
		_spawnedEntites.add(newEnt);
	}
	@EventHandler
	public void OnEntitySpawn(EntitySpawnEvent e)
	{
		if(e.isCancelled()) return;
		
		if(e.getEntityType() != EntityType.ENDERMAN) return;
		
		ChanceEntity(e.getEntity());
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
		
		return "Chestloot base by +2";
	}

	@Override
	public String GetDescription()
	{
		return "&6Endermen change their form!";
	}

	@Override
	public void OnPlayerLeftMiddleOfEvent(Player player)
	{
		Chunk[] chunks = ImusUtilities.GetChunksAround(player.getLocation(), 4);
		for(Chunk c : chunks)
		{
			for(Entity ent : c.getEntities())
			{
				Iterator<Entity> iterator = _spawnedEntites.iterator();
				while (iterator.hasNext()) 
				{
				    Entity spawnedEnt = iterator.next();
				    if (spawnedEnt == ent) 
				    {
				    	if(spawnedEnt.isValid()) spawnedEnt.remove();
				    	
				    	
				        iterator.remove();
				        
				        break;
				    }
				}
			}
		}
		
	}

	@Override
	public void OnPlayerJoinMiddleOfEvent(Player player)
	{
		
	}

	@Override
	public void OnOneTickLoop()
	{
		
	}

}
