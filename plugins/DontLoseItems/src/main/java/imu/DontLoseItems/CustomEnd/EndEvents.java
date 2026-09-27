package imu.DontLoseItems.CustomEnd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import imu.DontLoseItems.Events.NetherEvents;
import imu.DontLoseItems.main.DontLoseItems;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.ConfigMaker;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ImusUtilities;


public class EndEvents implements Listener
{
	public static EndEvents Instance;

	public UnstableEnd UnstableEnd;	
	
	@SuppressWarnings("unused")
	private BukkitTask _asyncTask;
	@SuppressWarnings("unused")
	private BukkitTask _task;
	
	private HashMap<INC_ID, UnstableIncrease> _increases;
	private HashMap<UUID, UnstableEnd_Player> _players;
	
	private HashSet<Material> _validBlocks;
	public HashSet<Material> EndBlocks;
	
	private final int _shulkerDropChance = 10;
	private final double _lootingBonusPerLevel = 4;
	
	private final int _distanceToUnstable = 1000;
	//private Location _locZero;
	@SuppressWarnings("unused")
	private World _end;
	
	private final boolean _enableGhastFireBall = true;
	private final int _ghastFireBall_radius = 3;
	
	private Set<Location> _mutationBlock;
	
	private Cooldowns _cd = new Cooldowns();
	
	private int _playerReduceAmount = 0;
	private double _entityUnstableDamage = 0;
	private final int _entityUnstableDamageCD = 1;
	
	public EndEvents()
	{
		Instance = this;
		UnstableEnd = new UnstableEnd();
		_players = new HashMap<>();
		_mutationBlock = Collections.synchronizedSet(new HashSet<>());
		InitIncrease();
		//GetSettings();
		_end = GetEnd();
		
		RunnableAsync();
		Runnable();
		InitValidBlocks();
		
	}
	private World GetEnd()
	{
		for(World w : Bukkit.getWorlds()) {if(w.getEnvironment() == Environment.THE_END) return w;}
		return null;
	}
	public void InitValidBlocks()
	{
		_validBlocks = new HashSet<>();
		_validBlocks.add(Material.END_STONE);
		_validBlocks.add(Material.END_STONE_BRICK_SLAB);
		_validBlocks.add(Material.END_STONE_BRICK_WALL);
		_validBlocks.add(Material.END_STONE_BRICKS);
		_validBlocks.add(Material.END_ROD);
		
		EndBlocks = new HashSet<>();
		EndBlocks.add(Material.PURPUR_BLOCK);
		EndBlocks.add(Material.END_STONE);
		EndBlocks.add(Material.END_STONE_BRICKS);
		EndBlocks.add(Material.END_PORTAL_FRAME);
		EndBlocks.add(Material.END_ROD);
		EndBlocks.add(Material.DRAGON_EGG);
		EndBlocks.add(Material.CHORUS_PLANT);
		EndBlocks.add(Material.CHORUS_FLOWER);
	}
	public enum INC_ID
	{
		PLACING_OTHER_BLOCKS,
		BREAKING_OTHER_BLOCKS,
		ON_ENTITY_DAMAGE, 
		ON_ENTITY_DEATH,
		ON_OPEN_CHEST,
		ON_USE_FIREWORK
	}
	public void InitIncrease()
	{
		_increases = new HashMap<>();
		
		_increases.put(INC_ID.BREAKING_OTHER_BLOCKS, new  UnstableIncrease(8));
		_increases.put(INC_ID.PLACING_OTHER_BLOCKS, new  UnstableIncrease(15)); //
		
		//remember _entityUnstableDamageCD = 1 which means maximum damage is ON_ENTITY_DAMAGE on 1 second
		_increases.put(INC_ID.ON_ENTITY_DAMAGE, new  UnstableIncrease(5));
		_increases.put(INC_ID.ON_ENTITY_DEATH, new  UnstableIncrease(8));
		_increases.put(INC_ID.ON_OPEN_CHEST, new  UnstableIncrease(100));
		_increases.put(INC_ID.ON_USE_FIREWORK, new  UnstableIncrease(30));
	}
	public UnstableIncrease GetIncrease(INC_ID id)
	{
		if(!_increases.containsKey(id)) return new UnstableIncrease(0);
		
		return _increases.get(id);
	}
	public void OnDisabled()
	{
		UnstableEnd.OnDisabled();
	}
		
	private void Runnable()
	{
		
		_task = new BukkitRunnable() 
		{			

			@Override
			public void run() 
			{		
				UnstableEnd.OnLoop();
			}
		}.runTaskTimer(DontLoseItems.Instance, 0, 1);	
	}
	private void RunnableAsync()
	{
		
		_asyncTask = new BukkitRunnable() 
		{			
			//int counter = 0;
			
			@Override
			public void run() 
			{
				
				LinkedList<Player> includedPlayers = new LinkedList<>();
				for(Player player : Bukkit.getOnlinePlayers())
				{
					if(IsPlayerUnstableArea(player)) 
					{
						includedPlayers.add(player);
					}
				}
				//counter++;
				
				if(includedPlayers.size() == 0) return;
				
				new BukkitRunnable() 
				{
					
					@Override
					public void run()
					{
						for(Player player : includedPlayers)
						{
							AddUnstablePlayer(player);
						}
						
						includedPlayers.clear();
					}
				}.runTask(DontLoseItems.Instance);
				
			}
		}.runTaskTimerAsynchronously(DontLoseItems.Instance, 0, 20*5);	
	}
	
	public void AddUnstablePlayer(Player player)
	{
		_players.put(player.getUniqueId(), new UnstableEnd_Player(player));
	}
	
	public UnstableEnd_Player GetPlayer(Player player)
	{
		if(!_players.containsKey(player.getUniqueId())) AddUnstablePlayer(player);
		
		return _players.get(player.getUniqueId());
	}
	
	public void RemovePlayer(Player player)
	{
		if(_players.containsKey(player.getUniqueId())) _players.remove(player.getUniqueId());
	}
	public boolean IsPlayerUnstableArea(Player player)
	{
		return IsPlayerUnstableArea((Entity)player);
	}
	
	public boolean IsPlayerUnstableArea(Chunk chunk)
	{
		Location loc = chunk.getBlock(7, 0, 7).getLocation();
		
		return IsPlayerUnstableArea(loc);

	}
	
	public boolean IsPlayerUnstableArea(Entity entity)
	{
		
		if(!DontLoseItems.IsEnd(entity)) return false;
		
		if(entity.getLocation().distance(entity.getLocation().zero()) < _distanceToUnstable) return false;
		
		
		return true;
	}
	
	public boolean IsPlayerUnstableArea(Location loc)
	{
		loc = loc.clone();
		if(!DontLoseItems.IsEnd(loc)) return false;

		if(loc.distance(loc.clone().zero()) < _distanceToUnstable) return false;
		
		return true;
	}
	private void UpdateUnstapleVoid(INC_ID id)
	{
		UpdateUnstapleVoid(GetIncrease(id));
	}
	
	
	private int GetPlayerReduceAmount()
	{
		if(!_cd.isCooldownReady("_playerReduceAmount")) return _playerReduceAmount;
		
		_cd.setCooldownInSeconds("_playerReduceAmount", 5);
		
		Iterator<UnstableEnd_Player> it = _players.values().iterator();
        int _playerReduceAmount = 0;
		while (it.hasNext())
		{
			
			UnstableEnd_Player uPlayer = it.next();
			Player player = uPlayer.Player;
			if(player == null || !IsPlayerUnstableArea(player))
			{
				it.remove();
				continue;
			}
			
			if(player.getGameMode() == GameMode.SURVIVAL)
			{
				_playerReduceAmount++;
			}

		}
		
		if(_playerReduceAmount <= 0) _playerReduceAmount = 1;
		
		return _playerReduceAmount;
	}
	
	private double GetUnstableIncreaseWithPlayers(UnstableIncrease increase)
	{
		if(_players.size() > 1) return increase.Amount / GetPlayerReduceAmount();
		
		return increase.Amount;
	}
	
	private void UpdateUnstapleVoid(UnstableIncrease increase)
	{		
		if(increase == null) return;
		
		BossBar bossBar = UnstableEnd.BOSS_BAR;
        bossBar.removeAll();
        Iterator<UnstableEnd_Player> it = _players.values().iterator();
        int reduceAmount = 0;
		while (it.hasNext())
		{
			
			UnstableEnd_Player uPlayer = it.next();
			Player player = uPlayer.Player;
			if(player == null || !IsPlayerUnstableArea(player))
			{
				it.remove();
				continue;
			}
			
			if(player.getGameMode() == GameMode.SURVIVAL)
			{
				reduceAmount++;
			}
			
			bossBar.addPlayer(player);
		}
		
		if(reduceAmount <= 0) reduceAmount = 1; 
		
		if(_players.size() > 1)
		{
			UnstableEnd.AddState(increase.Amount / reduceAmount);
		}
		else 
		{ UnstableEnd.AddState(increase.Amount); }
		
		UnstableEnd.RefreshPorgress();
		
		if(!UnstableEnd.IsMax()) return;
		
		UnstableEnd.OnTrigger();    
	}
	

	@EventHandler
	public void OnBlockPlace(BlockPlaceEvent e)
	{
		if(e.isCancelled()) return;
		
		if(!IsPlayerUnstableArea(e.getPlayer())) return;
			
		//if(e.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
				
		if(_validBlocks.contains(e.getBlockPlaced().getType())) return;
		
		UpdateUnstapleVoid(INC_ID.PLACING_OTHER_BLOCKS);

	}
	

	@EventHandler
	public void OnBlockPlace(BlockBreakEvent e)
	{
		if(e.isCancelled()) return;
		
		if(!IsPlayerUnstableArea(e.getPlayer())) return;

		if(_validBlocks.contains(e.getBlock().getType())) return;
		
		UpdateUnstapleVoid(INC_ID.BREAKING_OTHER_BLOCKS);

	}

	
	@EventHandler
	public void OnInventoryOpen(LootGenerateEvent e)
	{

		if (!(DontLoseItems.IsEnd(e.getWorld())))
			return;

		if (e.isCancelled())
			return;
		
		UpdateUnstapleVoid(INC_ID.ON_OPEN_CHEST);
	}
	 
	
	@EventHandler
	public void OnEntityDamage(EntityDamageEvent e)
	{
		if(!IsPlayerUnstableArea(e.getEntity())) return;
		
		if(!_cd.isCooldownReady("_entityUnstableDamage")) return;
		
		_entityUnstableDamage += GetUnstableIncreaseWithPlayers(GetIncrease(INC_ID.ON_ENTITY_DAMAGE));
		
		if(_entityUnstableDamage >= GetIncrease(INC_ID.ON_ENTITY_DAMAGE).Amount) 
		{
			_cd.setCooldownInSeconds("_entityUnstableDamage", _entityUnstableDamageCD);
			_entityUnstableDamage = 0;
		}
		
		UpdateUnstapleVoid(INC_ID.ON_ENTITY_DAMAGE);
		
	}

	
	@EventHandler
	public void OnEntityDeath(EntityDeathEvent e)
	{
		if(!IsPlayerUnstableArea(e.getEntity())) return;
		
		UpdateUnstapleVoid(INC_ID.ON_ENTITY_DEATH);
		
		if(e.getEntityType() == EntityType.SHULKER)
		{
			int looting = 0;
			if(e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)
			{
				EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent)e.getEntity().getLastDamageCause();

				if(edbe.getDamager() instanceof Player)
				{
					Player player = (Player)edbe.getDamager();
					ItemStack stack = player.getInventory().getItemInMainHand();
					looting = Metods._ins.GetItemStackEnchantCount(stack, Enchantment.LOOT_BONUS_MOBS);
				}
				
			}

			if(ThreadLocalRandom.current().nextInt(100) >= _shulkerDropChance+(_lootingBonusPerLevel * looting)) 
			{
				e.getDrops().clear();
				return;
			}
			
			for(var i : e.getDrops())
			{
				i.setAmount(1);
			}
		}
	}
		
	@EventHandler
	public void OnProtectileHit(ProjectileHitEvent e)
	{
		if (!(e.getEntity() instanceof Fireball))
		{
			return;
		}
		
		if(!IsPlayerUnstableArea(e.getEntity())) return;
		
		if(e.isCancelled()) return;
		
		Block block = e.getHitBlock();
		

		if(block == null)
		{
			if(e.getHitEntity() == null || !_enableGhastFireBall) return; //here is chast only thing!
			
			Location testLoc = e.getHitEntity().getLocation().clone();
			
			 for(int i = 0; i < 3; ++i)
			 {
				 block =  testLoc.add(0, -1, 0).getBlock();
				 
				 if(block != null && block.getType() != Material.AIR)
					 break;
			 }
			 
			 if(block == null || block.getType().isAir())  return;

		}
		
		Location hitLoc = block.getLocation();
		
		Fireball fireball = (Fireball)e.getEntity();
		
		if(fireball.getShooter() instanceof Ghast && _enableGhastFireBall)
		{
			NetherEvents.Instance.OnGhastExplotion(fireball, hitLoc, _ghastFireBall_radius);
			
			return;
		}
		
		
	}
	
	@EventHandler
	public void OnPlayerMove(PlayerMoveEvent event) 
	{
		if(!UnstableEnd.IsEventRuning()) return;
		
		if(!IsPlayerUnstableArea(event.getPlayer())) return;
		
	    Player player = event.getPlayer();

	    if (player.getGameMode() == GameMode.SURVIVAL && player.isGliding()) 
	    {
	       
	        player.setGliding(false);
	        player.sendMessage(Metods.msgC("&9Elytra &3flying &cis not allowed during &5EVENTS!"));
	    }
	}

	
	@EventHandler
	public void OnPlayerInteractEvent(PlayerInteractEvent e) 
	{
		
	    if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) 
	    {
	        return;
	    }
	    
	    if(!IsPlayerUnstableArea(e.getPlayer())) return;
	    
	    if(!e.getPlayer().isGliding()) return;
	    
	    if (e.getItem() != null && e.getItem().getType() == Material.FIREWORK_ROCKET) 
	    {
	        UpdateUnstapleVoid(INC_ID.ON_USE_FIREWORK);
	    }
	}
	
	public void CreateUnstableFallingBlocks(Location loc, double pullforce, int lastTicks, int range, boolean useEndblocks, boolean removeBlocks)
	{

		new BukkitRunnable() 
		{		
			@Override
			public void run()
			{
				LinkedList<Location> locs = 
						ImusUtilities.CreateSphere(loc, range, ImusAPI.AirHashSet, null);
				
				if(locs == null || locs.size() == 0) return; 
				
				ArrayList<Block> blocks = new ArrayList<>();
				
				for(Location loc : locs)
				{
					Block b = loc.getWorld().getHighestBlockAt(loc.getBlockX(), loc.getBlockZ());
					
					if(b.getType().isInteractable()) continue;
					
					if(b.getType().getBlastResistance() > 1200) continue;
					
					if(useEndblocks && !EndBlocks.contains(b.getType())) continue;
					
					blocks.add(b);
				}
				
				if(blocks.isEmpty())
				{
					return;
				}
				Integer[] shuffleArray = new Integer[blocks.size()];
				for(int i = 0; i < blocks.size(); i++)
				{
					shuffleArray[i] = i;
				}
				shuffleArray = ImusUtilities.ShuffleArray(shuffleArray);
				
				StartAnimation(loc,blocks, shuffleArray,pullforce,lastTicks,removeBlocks);
				
			}
		}.runTaskAsynchronously(DontLoseItems.Instance);
		
	}
	
	private void StartAnimation(Location loc, ArrayList<Block> blocks, Integer[] indexOrder,double pullforce,int totalTicks, boolean removeBlocks)
	{

		new BukkitRunnable() {
			
			private int counter = 0;
			private LinkedList<FallingBlock> _fallingBlocks = new LinkedList<>();
			int index = 0;
			@Override
			public void run()
			{
				if(counter > totalTicks) 
				{

					for( FallingBlock fb : _fallingBlocks)
					{
						fb.setGravity(true);
					}
					_fallingBlocks.clear();
					cancel();
					return;
				}
				
				//Block b = blocks.get(ThreadLocalRandom.current().nextInt(blocks.size()));
				
				Block b = blocks.get(indexOrder[index]);
				
				if(++index >= blocks.size()) index = 0;
				
				if(b == null || b.getType().isAir())
				{
					counter++;
					return;
				}
				
				
				FallingBlock fallingBlock = loc.getWorld().spawnFallingBlock(b.getLocation(), b.getBlockData());
				
				b.setType(Material.AIR);
				
				Vector vel = fallingBlock.getLocation().toVector().subtract(loc.toVector()).multiply(pullforce).normalize();
				
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
	
	public void CreateMaterialSphere(Entity entity, Location hitLoc, Material[] mat_list, long delay, double maxY, int radius)
	{
		new BukkitRunnable() {
			
			@Override
			public void run()
			{
				LinkedList<Block> blocks = new LinkedList<>();
				//List<Block> blocks = tnt.GetBlocks(e.getLocation());
	    		
				for(Location loc : ImusUtilities.CreateSphere(hitLoc, radius, ImusAPI.AirHashSet ,null))
				{
		    		if(_mutationBlock.contains(loc)) continue;
		    		
		    		if(loc.getY() > maxY) continue;
		    		
		    		_mutationBlock.add(loc);
		    		
					blocks.add(loc.getBlock());
				}
				
				
	    		
				new BukkitRunnable() 
				{
					
					@Override
					public void run()
					{
						if(entity != null)
						{
							EntityExplodeEvent explodeEvent = new EntityExplodeEvent(entity, hitLoc, blocks, 0);
							Bukkit.getServer().getPluginManager().callEvent(explodeEvent);
				    		
				    		if(explodeEvent.isCancelled()) 
				    		{
				    			for(Block b : blocks)
				    	    	{
				    	    		_mutationBlock.remove(b.getLocation());
				    	    	}
				    			return;
				    		}
						}
						
						ChangeBlockType(blocks, mat_list, delay, 0);
					}
				}.runTask(DontLoseItems.Instance);
				
			}
		}.runTaskAsynchronously(DontLoseItems.Instance);
		
	}
	
	private void ChangeBlockType(Iterable<Block> list, Material[] mat_list, long delay, int index) {
	    
		Bukkit.getScheduler().runTaskLater(DontLoseItems.Instance, () -> 
	    {
	    	boolean remove = false;
	    	
	    	
	    	int newIndex  = index + 1;
	    	
	    	if(newIndex < mat_list.length)
	    	{
	    		ChangeBlockType(list, mat_list, delay, newIndex);
	    	}
	    	else
	    	{
	    		remove = true;
	    	}
	    	
	    	for(Block block : list)
	    	{
	    		if(remove) _mutationBlock.remove(block.getLocation());
	    		
	    		Material mat = block.getType();
	    		if(block == null || mat == Material.AIR) continue;
	    		
	    		if(mat.getBlastResistance() >= 2000) continue;
	    		
	    		if(mat.isInteractable())
	    		{
	    			block.breakNaturally();
	    			continue;
	    		}

	    		block.setType(mat_list[index]);
	    		
	    		
	    	}
	    	
	        
	    }, delay);
	}
	
	
	public void replaceWaterSourcesAsync(Location loc, int depth)
	{
		new BukkitRunnable() 
		{
			
			@Override
			public void run()
			{
				HashMap<Location, Material> waterSources = new HashMap<>();
				waterSources = findWaterSources(loc, waterSources, depth);

				List<Location> sources = new ArrayList<>();

				for(Location loc : waterSources.keySet())
				{
					Block block = loc.getBlock();
					if(isSourceBlock(block))
					{
						sources.add(loc);
					}
				}
				
				new BukkitRunnable() {
					
					@Override
					public void run()
					{
						for(Location loc : sources)
						{
							loc.getBlock().setType(Material.AIR);
						}
					}
				}.runTask(DontLoseItems.Instance);
			}
		}.runTaskAsynchronously(DontLoseItems.Instance);
	    
	}
	
	private HashMap<Location, Material> findWaterSources(Location location, HashMap<Location, Material> waterSources, int depth)
	{
		depth--;

		if(depth <= 0)
			return waterSources;

		Block block = location.getBlock();

		// Check if the block is a water block
		if (block.getType() != Material.WATER) {
			return waterSources;
		}

		// Check if this location is already processed
		if (waterSources.containsKey(location)) {
			return waterSources;
		}

		waterSources.put(location, Material.WATER);

		// Recursively check adjacent blocks
		findWaterSources(location.clone().add(1, 0, 0), waterSources, depth); // East
		findWaterSources(location.clone().add(-1, 0, 0), waterSources, depth); // West
		findWaterSources(location.clone().add(0, 1, 0), waterSources, depth); // Up
		findWaterSources(location.clone().add(0, -1, 0), waterSources, depth); // Down
		findWaterSources(location.clone().add(0, 0, 1), waterSources, depth); // South
		findWaterSources(location.clone().add(0, 0, -1), waterSources, depth); // North

		return waterSources;
	}

	private boolean isSourceBlock(Block block) 
	{
		BlockData blockData = block.getBlockData();

		if (blockData instanceof Levelled)
		{
			Levelled levelled = (Levelled) blockData;
			return levelled.getLevel() == 0;
		}

		return false;
	}
	
	void GetSettings()
	{
		final String netherSettings = "EndSettings";
		ConfigMaker cm = new ConfigMaker(DontLoseItems.Instance, netherSettings + ".yml");
		FileConfiguration config = cm.getConfig();

		// String dot_dmg = netherSettings+".durabilityDamageFromDot";
		
		
		if (!config.contains(netherSettings + "."))
		{
			// default values
			DontLoseItems.Instance.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "DontLoseItems : Default config made!");

			cm.saveConfig();
			//return;
		}
		
		

		
		

	}

}
