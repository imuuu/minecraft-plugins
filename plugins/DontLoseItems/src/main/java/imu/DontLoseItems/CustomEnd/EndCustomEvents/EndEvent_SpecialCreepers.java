package imu.DontLoseItems.CustomEnd.EndCustomEvents;

import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import imu.DontLoseItems.CustomEnd.EndEvents;
import imu.DontLoseItems.main.DontLoseItems;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Utilities.ImusUtilities;

public class EndEvent_SpecialCreepers extends EndEvent
{
	private LinkedList<Entity> _spawnedEntites = new LinkedList<>();
	private EntityType _spawnType;
	private World _end;
	private final int MAX_ENTITES_IN_CHUNK = 1;

	private EntityType[] _types = 
		{
			EntityType.CREEPER,	
		};
	public EndEvent_SpecialCreepers()
	{
		super("Special Creepers", 80);
		
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
		_spawnType = _types[0];
		for(Entity ent : _end.getLivingEntities())
		{
			if(!EndEvents.Instance.IsPlayerUnstableArea(ent)) continue;
			
			ChanceEntity(ent);
			
		}
	}
	
	@Override
	public void OnEventEnd()
	{
		AddChestLootBaseToAll(ChestLootAmount);
		for(Entity e : _spawnedEntites)
		{
			if(e == null || !e.isValid()) continue;
			
			e.remove();
		}
		_spawnedEntites.clear();
	}
	private void ChanceEntity(Entity ent)
	{
		if(ent.getType() != EntityType.ENDERMAN) return;
		
		if(!EndEvents.Instance.IsPlayerUnstableArea(ent)) return;
		
		Enderman livEnt = (Enderman)ent;
		Location loc = livEnt.getLocation().clone();
		livEnt.remove();
		Entity newEnt =_end.spawnEntity(loc, _spawnType);
		_spawnedEntites.add(newEnt);

	}
	@EventHandler
	public void OnEntitySpawn(EntitySpawnEvent e)
	{
		if(e.isCancelled()) return;
		
		if(e.getEntityType() != EntityType.ENDERMAN) return;

		if(e.getEntity().getLocation().getChunk().getEntities().length > MAX_ENTITES_IN_CHUNK) 
		{
			 e.setCancelled(true);
			 return;
		}
		
		
		ChanceEntity(e.getEntity());

		
	}
	
	@EventHandler
	public void OnBlowUp(EntityExplodeEvent e)
	{

		if(e.getEntityType() != EntityType.CREEPER) return;
		

		Creeper creeper = (Creeper) e.getEntity();
		
		Entity crystal = creeper.getWorld().spawnEntity(creeper.getLocation().add(0,2,0), EntityType.ENDER_CRYSTAL);
		
		new BukkitRunnable() 
		{		
			@Override
			public void run()
			{
				LinkedList<Location> locs = ImusUtilities.CreateSphere(creeper.getLocation(), 20, ImusAPI.AirHashSet, null);
				
				if(locs == null || locs.size() == 0) return; 
				
				LinkedList<Block> blocks = new LinkedList<>();
				for(Location loc : locs)
				{
					Block b = loc.getWorld().getHighestBlockAt(loc.getBlockX(), loc.getBlockZ());
					
					if(b.getType().isInteractable()) continue;
					
					if(b.getType().getBlastResistance() > 1200) continue;
					
					if(!EndEvents.Instance.EndBlocks.contains(b.getType())) continue;
					
					blocks.add(b);
				}
				
				if(blocks.isEmpty()) return;
				
				StartAnimation(creeper, crystal,blocks);
				
			}
		}.runTaskAsynchronously(DontLoseItems.Instance);
		
	}
	
	private void StartAnimation(Entity entity, Entity crystal,LinkedList<Block> blocks)
	{
		new BukkitRunnable() {
			
			private int counter = 0;
			private LinkedList<FallingBlock> _fallingBlocks = new LinkedList<>();
			@Override
			public void run()
			{
				if(counter > 300) 
				{

					TNTPrimed tnt = crystal.getWorld().spawn(crystal.getLocation(), TNTPrimed.class);
					tnt.setFuseTicks(0);
					crystal.remove();
					
					for( FallingBlock fb : _fallingBlocks)
					{
						fb.setGravity(true);
					}
					_fallingBlocks.clear();
					cancel();
					return;
				}
				
				Block b = blocks.get(ThreadLocalRandom.current().nextInt(blocks.size()));
				
				FallingBlock fallingBlock = entity.getWorld().spawnFallingBlock(b.getLocation(), b.getBlockData());
				
				//fallingBlock.setVelocity(fallingBlock.getLocation().toVector().subtract(crystal.getLocation().toVector()).multiply(-10).normalize());
				
				Vector vel = fallingBlock.getLocation().toVector().subtract(crystal.getLocation().toVector()).multiply(-10).normalize();
				
				if (!Double.isFinite(vel.getX()) || !Double.isFinite(vel.getY()) || !Double.isFinite(vel.getZ())) {
					vel = new Vector(0, 1, 0);

				}

				fallingBlock.setVelocity(vel);
				
				fallingBlock.setGravity(false);
				fallingBlock.setDropItem(false);
				fallingBlock.setHurtEntities(true);
				
				_fallingBlocks.add(fallingBlock);
				counter++;
			}
			
		}.runTaskTimer(DontLoseItems.Instance, 0, 1);
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
		return "&6Boom";
	}

	@Override
	public void OnPlayerLeftMiddleOfEvent(Player player)
	{
		
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
