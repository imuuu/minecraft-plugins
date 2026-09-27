package imu.DontLoseItems.Events;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import imu.DontLoseItems.Enums.DIFFICULT;
import imu.DontLoseItems.Enums.ITEM_RARITY;
import imu.DontLoseItems.Managers.Manager_Difficult;
import imu.DontLoseItems.Managers.Manager_HellArmor;
import imu.DontLoseItems.main.DontLoseItems;
import imu.DontLoseItems.other.PlayerFear;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.ConfigMaker;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ImusUtilities;


public class NetherEvents implements Listener
{
	public static NetherEvents Instance;
	private Random _rand;
	private Cooldowns _cds;
	private World _nether;
	
	private boolean _enableMagmaCube = true;
	private boolean _enableBlazeFireBallSpread = true;
	private boolean _enableGhastFireBall = true;
	private boolean _enableWaterBottleBuff = true;
	private boolean _enableShieldBlockArrow = true;
	private boolean _enableWitherSpeed = true;
	private boolean _enableHoglinSpeed = true;
	private boolean _enableSpeedZombie = true;
	private boolean _enableDoubleFireDamage = true;
	private boolean _enableDisableTotemNether = true;
	
	private boolean _enableOnlySmallMagmaCubeLava = true;
	
	private long _ghastMaterialDelayTicks = 9;
	
	private final int _maxWaterBottleUses = 2;
	private int _chanceEntityHaveShield = 15;
	private final int _radiusOfGhastBall = 7;
	private final int _radiusOfBlazeBall = 2;
	private final boolean _blazeFireStartONfeetOnHit = true;
	
	private final double _fearIncrease = 15; // 14
	private final double _fearDecrease = 10;
	private final int _fearDecreaseLightLevel = 12; //top of torch is 12
	private final int _fearIncreaseByHit = 5; //top of torch is 12
	
	private final double _fearDamageDelay = 8;
	private final double _fearIncreaseDelay = 25;
	private final double _fearDecreaseDelay = 0.8;
	
	private final double _torchCheckDelay = 10;
	private final long _asyncLoopDelay = 2L;
	
	//speed zombie settings
	private final double _fearIncreaseBySpeedZombie = 15;
	private final double _speedZombieDropChance = 36;
	private final int _speedZombieRollChances = 5;
	private final double _speedZombieChanceToSpawn = 5;
	
	//pigling
	private final boolean _enablePiglinArrowKnockBack = true;
	private HashMap<UUID, PlayerFear> _playerFear;	
	
	@SuppressWarnings("unused")
	private BukkitTask _asyncTask;
	
	
	private final Material[] _ghastBallMaterials = new Material[]
	{
		Material.BLACK_TERRACOTTA,
		Material.GRAY_TERRACOTTA,
		Material.PURPLE_TERRACOTTA,
		//Material.RED_TERRACOTTA,
		Material.ORANGE_TERRACOTTA,
		Material.YELLOW_TERRACOTTA,
		Material.WHITE_TERRACOTTA,
		Material.AIR,
	};
	
	private Set<Location> _mutationBlock;
	
	private HashSet<EntityType> _validEquipEnteties;
	
	private HashMap<Location, Long> _placedTorches;
	
	
	
	private final long TORCH_TIME = 23000;
	public NetherEvents()
	{
		Instance = this;
		_rand = new Random();
		_playerFear = new HashMap<>();
		_placedTorches = new HashMap<>();

		_cds = new Cooldowns();
		// GetSettings();
		_mutationBlock = Collections.synchronizedSet(new HashSet<>());
		_nether = Bukkit.getWorld("world_nether");
		
		GetSettings();
		
		InitEntityTypes();
		RunnableAsync();
	}
	public void OnDisabled()
	{
		TorchCheckAsync(true);
	}
	private void InitEntityTypes()
	{
		_validEquipEnteties = new HashSet<>();
		_validEquipEnteties.add(EntityType.WITHER_SKELETON);
		_validEquipEnteties.add(EntityType.PIGLIN);
		_validEquipEnteties.add(EntityType.ZOMBIFIED_PIGLIN);
		_validEquipEnteties.add(EntityType.PIGLIN_BRUTE);
		_validEquipEnteties.add(EntityType.SKELETON);
	}
	
	private DIFFICULT getPlayerDifficulty(Player player)
	{
		return getPlayerDifficulty(player.getUniqueId());
	}
	
	private DIFFICULT getPlayerDifficulty(UUID uuid)
	{
		return Manager_Difficult.Instance.getPlayerSettings(uuid).NetherDifficulty;
	}
	
	void RunnableAsync()
	{
		
		_asyncTask = new BukkitRunnable() 
		{			
			
			@Override
			public void run() 
			{
				for(Player player : Bukkit.getServer().getOnlinePlayers())
				{
					if(!IsNether(player)) continue;
					
					if(player.getGameMode() != GameMode.SURVIVAL) 
					{
						GetFear(player).SetFear(0);
						continue;
					}
					
					if(getPlayerDifficulty(player) == DIFFICULT.NO_FEAR)
					{
						continue;
					}
					
					Block block = player.getLocation().getBlock();
					
					if(block != null && block.getLightLevel() >= _fearDecreaseLightLevel)
					{
						if(_cds.isCooldownReady("fearDecrease")) 
						{
							_cds.setCooldownInSeconds("fearDecrease", _fearDecreaseDelay);
							
							if(GetFear(player).GetFearLevel() > 0) AddFearToPlayer(player, -_fearDecrease);
							
						}	
					}
					
					if(_cds.isCooldownReady("fearIncrease"))
					{
						AddFearToPlayer(player, _fearIncrease - GetPlayerFearReduceAmount(player));
					}
					
					if(_cds.isCooldownReady("torchCheck"))
					{
						CheckTotem(player);
					}
					
					if(!_cds.isCooldownReady("fearDamage")) continue;
					
					
					PlayerFear fear = GetFear(player);
					
					new BukkitRunnable() 
					{
			            @Override
			            public void run() 
			            {
			            	fear.TriggerFear(player);
						   
			            }
			        }.runTask(DontLoseItems.Instance); 
					
				}
				
				if(_cds.isCooldownReady("fearIncrease"))
				{
					_cds.setCooldownInSeconds("fearIncrease", _fearIncreaseDelay);
				
				}
				
				if(_cds.isCooldownReady("fearDamage"))
				{
					_cds.setCooldownInSeconds("fearDamage", _fearDamageDelay);
				}
				
				if(_cds.isCooldownReady("torchCheck"))
				{
					_cds.setCooldownInSeconds("torchCheck", _torchCheckDelay);
					
					TorchCheckAsync(false);
				}

				
			}
		}.runTaskTimerAsynchronously(DontLoseItems.Instance, 0, _asyncLoopDelay);	
	}
	
	private void CheckTotem(Player player)
	{
		if(!_enableDisableTotemNether) return;
		
		if(player.getGameMode() != GameMode.SURVIVAL) return;
		
		ItemStack stack = player.getInventory().getItemInOffHand();
		
		
		if(stack == null || stack.getType() != Material.TOTEM_OF_UNDYING ) return;
		
		new BukkitRunnable() {
			
			@Override
			public void run()
			{
				ItemStack newStack = stack.clone();
				stack.setAmount(0);
				
				Metods._ins.InventoryAddItemOrDrop(newStack, player);
				
				player.sendMessage("");
				player.sendMessage(ChatColor.BLUE+ "Totem of Undying scares nethers darkness!");
				player.sendMessage("");
				player.sendMessage(ChatColor.BLUE+ "So it was feeling a little down, "
						+ "so it went to see a spiritual advisor. "
						+ "It's hoping to find some inner peace and strength!");
			}
		}.runTask(DontLoseItems.Instance);
		
	}
	
	private void TorchCheckAsync(boolean force)
	{
		LinkedList<Location> torches = new LinkedList<>();
		
		long currentTime = System.currentTimeMillis();
		for(var torch : _placedTorches.entrySet())
		{
			if(currentTime - torch.getValue() > TORCH_TIME || force)
			{
				Location loc = torch.getKey();
				torches.add(loc);
			}

		}
		for(Location loc : torches)
		{
			_placedTorches.remove(loc);
		}
		
		if(force)
		{
			for(Location loc : torches)
			{
				if(loc.getBlock().getType() != Material.TORCH) continue;
				
				loc.getBlock().breakNaturally();
			}
			torches.clear();
			return;
		}
		new BukkitRunnable() 
		{
			@Override
			public void run()
			{
				for(Location loc : torches)
				{
					if(loc.getBlock().getType() != Material.TORCH) continue;
					
					loc.getBlock().breakNaturally();
				}
				torches.clear();
			}
		}.runTask(DontLoseItems.Instance);
	}
	
	private PlayerFear GetFear(Player player)
	{
		if(!_playerFear.containsKey(player.getUniqueId())) 
			_playerFear.put(player.getUniqueId(), new PlayerFear(player));
		
		return _playerFear.get(player.getUniqueId());
	}
	
	private double GetPlayerFearReduceAmount(Player player)
	{
		PlayerInventory inv = player.getInventory();
		double helmet = Manager_HellArmor.Instance.GetFearReduceAmount(inv.getHelmet());
		double chest = Manager_HellArmor.Instance.GetFearReduceAmount(inv.getChestplate());
		double legg = Manager_HellArmor.Instance.GetFearReduceAmount(inv.getLeggings());
		double feet = Manager_HellArmor.Instance.GetFearReduceAmount(inv.getBoots());
		
		return helmet + chest + legg + feet;
		
	}
	
	private void AddFearToPlayer(Player player, double amount)
	{
		if(getPlayerDifficulty(player) == DIFFICULT.NO_FEAR)
		{
			return;
		}
		
		//System.out.println("fear: "+amount);
		PlayerFear fear = GetFear(player);
		fear.SetPlayer(player);
		
		double fearIncrease = amount;
		
		BossBar bossBar = fear.GetBossBar() == null ? Bukkit.createBossBar("Fear Level", BarColor.PURPLE, BarStyle.SEGMENTED_10, BarFlag.CREATE_FOG) : fear.GetBossBar();
        bossBar.addPlayer(player);
		bossBar.setProgress(fear.GetFearLevel() * 0.01);
        
		bossBar.setTitle(ChatColor.BLACK + "| "+ChatColor.DARK_PURPLE+"Fear"+ChatColor.BLACK+" |");//: " +ChatColor.BLUE +(int) fear.GetFearLevel());// + "%");
		fear.SetBossBar(bossBar);
		
		new BukkitRunnable() 
		{
            @Override
            public void run() 
            {
            	fear.AddFearLevel(fearIncrease);
            	bossBar.setProgress(fear.GetFearLevel() * 0.01);
            }
        }.runTaskLater(DontLoseItems.Instance, 20L); 
        
		new BukkitRunnable() 
		{
            @Override
            public void run() 
            {
                bossBar.removeAll();
            }
        }.runTaskLater(DontLoseItems.Instance, 200L); 
        //player.sendMessage(ChatColor.DARK_PURPLE+ " Fear has increased by "+fearIncrease + " total: "+fear.GetFearLevel());
	}
	


	@SuppressWarnings("unused")
	private boolean IsNether(World world)
	{
		if(world == null) return false;
		
		return world == _nether;
	}
	private boolean IsNether(Entity entity)
	{
		if(entity == null) return false;
		
		return entity.getWorld() == _nether;
	}
	@SuppressWarnings("unused")
	private boolean IsNether(Block block)
	{
		if(block == null) return false;
		
		return block.getWorld() == _nether;
	}
	@SuppressWarnings("unused")
	private boolean IsNether(Location loc)
	{
		if(loc == null) return false;
		
		return loc.getWorld() == _nether;
	}
	
	
//	@EventHandler(priority = EventPriority.HIGHEST)
//	  public void onBlockPopulate(BlockPopulator e) 
//	{
//		
//	    if (event.getBlock().getType() == Material.ANCIENT_DEBRIS) {
//	      event.setCancelled(true);
//	    }
//	  }
	
	@EventHandler
	public void CancelPortals(PlayerPortalEvent e)
	{	
		if(e.getTo().getWorld().getName().matches("world_nether") )
		{
			AddFearToPlayer(e.getPlayer(), -100);
			return;
		}
		
		
		
	}
	
	@EventHandler
	public void OnBlockPlace(BlockPlaceEvent e)
	{
		
		if(!IsNether(e.getPlayer())) return;
		
		if(e.getBlock().getType() != Material.TORCH) return;
		
		if(e.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
		
		if(getPlayerDifficulty(e.getPlayer()) == DIFFICULT.NO_FEAR) return;
		
		if(Manager_HellArmor.Instance.IsHellTorch(e.getPlayer().getInventory().getItemInMainHand())) 
		{
			return;
		}
		
		if(_placedTorches.containsKey(e.getBlock().getLocation()))  return;
		
		if(_rand.nextInt(100) < 5)
		{
			e.getPlayer().sendMessage(ChatColor.BLUE + "Seemss.. These torches can't stand"+ChatColor.DARK_GRAY+" darkness "+ChatColor.BLUE+" very long..");
		}
		
		_placedTorches.put(e.getBlock().getLocation(), System.currentTimeMillis());
	}

	private void EquipItemWithChance(LivingEntity entity, ItemStack item, int chance, boolean mainHand)
	{
		if (_rand.nextInt(100) >= _chanceEntityHaveShield) return;
		
		if(mainHand) entity.getEquipment().setItemInMainHand(item,false);
		else entity.getEquipment().setItemInOffHand(item,false);
		
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void OnEntitySpawn(CreatureSpawnEvent e) 
	{
		if(!IsNether(e.getEntity())) return;
		
		if(e.isCancelled()) return;
		
		//System.out.println("Entity type: "+e.getEntityType());
		
		if(e.getSpawnReason() != SpawnReason.NATURAL && e.getSpawnReason() != SpawnReason.SPAWNER_EGG) return;
		
		if(!(e.getEntity() instanceof LivingEntity)) return;
		
		if(!(e.getEntity() instanceof Mob)) return;
		
		LivingEntity entity = (LivingEntity)e.getEntity();
		EntityType entityType = e.getEntityType();
	    
		ItemStack mainHand = entity.getEquipment().getItemInMainHand();
		//ItemStack offHand = entity.getEquipment().getItemInOffHand();

		if((_enableSpeedZombie && _rand.nextInt(100) < _speedZombieChanceToSpawn))
		{
			
			e.setCancelled(true);
	        Zombie zombie = (Zombie)e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.ZOMBIE);
	        
	        zombie.setAge(1);
	        zombie.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false));
	        zombie.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 4, false, false));
	          
	        ItemStack helmet = new ItemStack(Material.IRON_HELMET);;
	        helmet.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 2);
	        //helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
	        entity.getEquipment().setHelmet(helmet,false);
	        
	        ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);;
	        chestplate.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 4);
	        //chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
	        zombie.getEquipment().setChestplate(chestplate, false);
	        
	        ItemStack legg = new ItemStack(Material.DIAMOND_LEGGINGS);;
	        legg.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 2);
	        legg.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
	        zombie.getEquipment().setLeggings(legg, false);
	        
	        
	        ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);
	        boots.addEnchantment(Enchantment.SOUL_SPEED, 3);
	        boots.addEnchantment(Enchantment.PROTECTION_PROJECTILE, 2);
	        //boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
	        zombie.getEquipment().setBoots(boots,false);
	        
	        Metods._ins.setPersistenData(zombie, "SPEED_ZOMBIE", PersistentDataType.INTEGER, 1);
	        
	        
	        return;
		}
		
		
		if (_enableWitherSpeed && entityType == EntityType.WITHER_SKELETON) 
	    {
	        e.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
	    }
		
		if (_enableHoglinSpeed && entityType == EntityType.HOGLIN) 
	    {
	        e.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
	    }
	    
	    if(!_enableShieldBlockArrow) return;
	    
	    if(!_validEquipEnteties.contains(entityType)) return;
	    
	    boolean isPiglin = entityType == EntityType.PIGLIN || entityType == EntityType.WITHER_SKELETON;
	    int multiplier = isPiglin ? 4 : 1;
	    
	    if(isPiglin || entityType == EntityType.ZOMBIFIED_PIGLIN)
	    {
	    	EquipItemWithChance(e.getEntity(), new ItemStack(Material.DIAMOND_SWORD), 5 * multiplier, true);
	    }
	    int chance = _chanceEntityHaveShield * multiplier;
	    //System.out.println("Its piglin: "+isPiglin+ " chance: "+chance);
	    EquipItemWithChance(e.getEntity(), new ItemStack(Material.SHIELD), chance, false);
	    
	    
	    if(mainHand != null && mainHand.getType() == Material.CROSSBOW)
	    {

	    	ItemStack stack = new ItemStack(Material.DIAMOND_BOOTS);
		
			ItemMeta meta = stack.getItemMeta();
	 
		
			meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, new AttributeModifier(UUID.randomUUID(), "generic.movementSpeed", 0.02,AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));
			meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), "generic.attackSpeed", 10f, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.FEET));
			//meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.armor", rarityItem.Values[2], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD));
			//meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(), "generic.armor_toughness", rarityItem.Values[3], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HEAD));
			//meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.randomUUID(), "generic.armor_toughness", rarityItem.Values[4], AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST));

			stack.setItemMeta(meta);

			entity.getEquipment().setBoots(stack);
	    }
	}
	
	@EventHandler
	public void OnEntityDamageByEntity(EntityDamageByEntityEvent e) 
	{
		if(!IsNether(e.getEntity())) return;
		
		if(e.isCancelled()) return;
		
		if (!(e.getEntity() instanceof Player)) {
	        return;
	    }

	    Player player = (Player) e.getEntity();
	    DIFFICULT difficulty = getPlayerDifficulty(player);
	    
	    if (e.getDamager() instanceof Arrow)
		{
	    	if(!_enablePiglinArrowKnockBack) return;
	    	
	    	if(difficulty == DIFFICULT.NO_FEAR) return;
	    	
			Arrow arrow = (Arrow) e.getDamager();

		    if (!(arrow.getShooter() instanceof Piglin)) {
		        return;
		    }
		    
		    if(player.isBlocking()) return;
		    
		    // Get the direction the arrow was traveling
		    Vector arrowDirection = arrow.getVelocity().normalize();

		    player.setVelocity(arrowDirection.multiply(5));

		    return;
	    }

	    
		if(!_enableSpeedZombie) return;
		
	    if (e.getDamager().getType() != EntityType.ZOMBIE) return;
	    
	    //if(!(e.getEntity() instanceof Player)) return;
	    	    
	    if(Metods._ins.getPersistenData(e.getDamager(), "SPEED_ZOMBIE", PersistentDataType.INTEGER) == null) return;
	    
	    if(difficulty == DIFFICULT.NO_FEAR) 
	    {
	    	e.getDamager().remove();
	    	e.setCancelled(true);
	    	return;
	    }
	    AddFearToPlayer(((Player)e.getEntity()), _fearIncreaseBySpeedZombie);
	    
	  }
	
	public void SendArrowBack(LivingEntity shooter, Arrow arrow, double velocityMultiplier ,double damageMultiplier)
	{
		System.out.println("damage: "+arrow.getDamage());
        arrow.setDamage(arrow.getDamage() * 2);
        Vector velocity = arrow.getVelocity();
        arrow.setBounce(false);
        arrow.setShooter(shooter);
//        arrow.setGlowing(true);
//        arrow.setColor(Color.GREEN);
//        arrow.setPierceLevel(10);
//        arrow.setGravity(false);
        arrow.setVelocity(velocity.multiply(velocityMultiplier));
        
        System.out.println("damage: "+arrow.getDamage());
	}
	
	public void SendArrowBackV2(LivingEntity reflecterEntity, Entity target,Arrow arrow, double velocityMultiplier ,double damageMultiplier, boolean addGravity)
	{
        arrow.setDamage(arrow.getDamage() * 2);
        //Vector velocity = arrow.getVelocity();
        arrow.setBounce(false);
        arrow.setShooter((LivingEntity)target);
        arrow.setGlowing(true);
        arrow.setTicksLived(20 * 55);
        
        arrow.setColor(Color.GREEN);
        arrow.setPierceLevel(10);
        arrow.setGravity(addGravity);
        
        Vector reflecter = reflecterEntity.getLocation().toVector();
        reflecter.add(new Vector(0, 0.3, 0));
        Vector direction = reflecter.subtract(target.getLocation().toVector());
        direction = direction.normalize();
        
        arrow.setVelocity(direction.multiply(velocityMultiplier));
        //System.out.println("damageV2: "+arrow.getDamage()+ " vector: "+arrow.getVelocity());
	}
	@EventHandler
	public void OnBlockingArrow(EntityDamageByEntityEvent e) 
	{
		if(!_enableShieldBlockArrow) return;
		
		if(e.isCancelled()) return;
		
		if(!(e.getEntity() instanceof LivingEntity)) return;
		
		LivingEntity entity = (LivingEntity)e.getEntity();
		
//		if(e.getEntity() instanceof Player) //MOVED
//		{
//			 //System.out.println("sending back");
//			 
//			 if (e.getDamager().getType() != EntityType.ARROW) return;
//			 
//			 if(!((Player)entity).isBlocking()) return;
//			 
//			 ItemStack shield = entity.getEquipment().getItemInOffHand();
//			 if(!Manager_HellArmor.Instance.IsHellReflectShield(shield)) return;
//			 //System.out.println("==> sending back");
//			 Arrow arrow = (Arrow) e.getDamager();
//			 //SendArrowBackV2(entity, (Entity)arrow.getShooter(), arrow, 10, 2, true);
//			 ITEM_RARITY rarity = Manager_HellArmor.Instance.GetRarity(shield);
//			 Manager_HellArmor.Instance.OnHellShieldReflect(entity, (Entity)arrow.getShooter(), arrow,rarity);
//			 
//			 int durMulti = 6 - rarity.GetIndex();
//			 Metods._ins.giveDamage(shield, Manager_HellArmor.Instance.ReflectShieldDurabilityLost * durMulti, true);
//			 e.setCancelled(true);
//			 
//			 return;
//		}
		
		if(!IsNether(e.getEntity())) return;
		
		if(!_validEquipEnteties.contains(e.getEntityType())) return;
		
	    if (e.getDamager().getType() != EntityType.ARROW) return;
	    
        if (entity.getEquipment().getItemInOffHand().getType() != Material.SHIELD) return;
        
        

        Arrow arrow = (Arrow) e.getDamager();
        
        if (arrow.getShooter() instanceof Player) 
        {
            Player shooter = (Player) arrow.getShooter();
            
            if(getPlayerDifficulty(shooter) == DIFFICULT.NO_FEAR) return;
            
        }
        SendArrowBack(entity, arrow, 10,2);
        e.setCancelled(true);
	}
	
	 @EventHandler
	  public void OnHoglinAttack(EntityDamageByEntityEvent e) 
	 {
	    if (e.getEntity() instanceof Player && e.getDamager().getType() == EntityType.HOGLIN) 
	    {
	    	Player player = (Player) e.getEntity();
	    	if(getPlayerDifficulty(player) == DIFFICULT.NO_FEAR)
	    	{
	    		return;
	    	}
	    	
	    	Hoglin hoglin = (Hoglin) e.getDamager();
	    	
	        Vector direction = player.getLocation().toVector().subtract(hoglin.getLocation().toVector());
	        direction.setY(8);
	        direction.normalize();
	        
	        
	        
	        player.setVelocity(direction);
	    }
	  }
	
	@EventHandler
	public void OnEntityDamage(EntityDamageEvent e)
	{
		if(!IsNether(e.getEntity())) return;
		
		if(e.isCancelled()) return;
		
		Entity entity = e.getEntity();
		
		if(!(entity instanceof Player)) return;
		
		Player player = (Player) entity;
		
		if(getPlayerDifficulty(player) == DIFFICULT.NO_FEAR)
		{
			return;
		}
          
		if(_fearIncreaseByHit > 0)
		{
			if (e.getCause() != EntityDamageEvent.DamageCause.POISON 
			&&  e.getCause() != EntityDamageEvent.DamageCause.FIRE 
			&&  e.getCause() != EntityDamageEvent.DamageCause.FIRE_TICK 
			&&  e.getCause() != EntityDamageEvent.DamageCause.LAVA)
			{
				AddFearToPlayer((Player)e.getEntity(), _fearIncreaseByHit);
			}
			
		}
		
		if(!_enableDoubleFireDamage) return;
		

		DamageCause cause = e.getCause();
		
		if(cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK)
		{
			e.setDamage(e.getDamage() * 2);
		}

	}
	
	@EventHandler
	public void OnProtectileHit(ProjectileHitEvent e)
	{
		if (!(e.getEntity() instanceof Fireball))
		{
			return;
		}
		
		if(!IsNether(e.getEntity())) return;
		
		if(e.isCancelled()) return;
		
		Block block = e.getHitBlock();
		
		boolean isSearchedBlock = false;
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
			 
			 if(block == null || block.getType() == Material.AIR)  return;
			
			 isSearchedBlock = true;
		}
		
		Location hitLoc = block.getLocation();
		
		Fireball fireball = (Fireball)e.getEntity();
		
		if(fireball.getShooter() instanceof Ghast && _enableGhastFireBall)
		{
			OnGhastExplotion(fireball,hitLoc, _radiusOfGhastBall);
			
			return;
		}
		
		if(fireball.getShooter() instanceof Blaze && _enableBlazeFireBallSpread )// && !isSearchedBlock) //if search block then it takes if hit the player
		{
			if(!_blazeFireStartONfeetOnHit) { if(!isSearchedBlock) {return;}}
			
			OnBlazeExplotion(hitLoc);
			
			return;
		}

	}
	
	public void OnGhastExplotion(Entity entity, Location hitLoc, int radius)
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
		    		
		    		_mutationBlock.add(loc);
		    		
					blocks.add(loc.getBlock());
				}
				
				
	    		
				new BukkitRunnable() 
				{
					
					@Override
					public void run()
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
						
						//System.out.println("blocks: "+blocks.size()+ " mats: "+_ghastBallMaterials.length);
						
			    		//Runs in main thread
						ChangeBlockType(blocks, _ghastBallMaterials, _ghastMaterialDelayTicks, 0);
					}
				}.runTask(DontLoseItems.Instance);
				
			}
		}.runTaskAsynchronously(DontLoseItems.Instance);
		
	}
	
	private void OnBlazeExplotion(Location hitLoc)
	{	
		for(Location loc : ImusUtilities.CreateSphere(hitLoc, _radiusOfBlazeBall, ImusAPI.AirHashSet, null))
		{
			//if(loc.getBlock() == null || loc.getBlock().getType() == Material.AIR) continue;
			
			Block firePos = loc.add(0, 1, 0).getBlock();
			
			if(firePos == null || firePos.getType() != Material.AIR) continue;
			
			firePos.setType(Material.FIRE);
		}
	}
	
	@EventHandler
	public void OnEntityDeath(EntityDeathEvent e)
	{
		Entity entity = e.getEntity();

		// if(!entity.getWorld().getName().matches("world_nether")) return;
		if(!IsNether(entity)) return;
		
		if(entity instanceof Zombie && _enableSpeedZombie)
		{	        
	        if(Metods._ins.getPersistenData(entity, "SPEED_ZOMBIE", PersistentDataType.INTEGER) == null) return;
	        
	        if(_rand.nextInt(100) >= _speedZombieDropChance) return;
	        
	        System.out.println("Loot spawned by killed zombie");
	        e.getDrops().clear();

	        LinkedList<ItemStack> drops = ChestLootEvents.Instance.GenerateNetherLoot(1, _speedZombieRollChances);
	        
	        for(ItemStack stack : drops)
	        {
	        	 entity.getWorld().dropItemNaturally(entity.getLocation(), stack);
	        }
	       
		}
		
		if (entity instanceof MagmaCube && _enableMagmaCube)
		{
			MagmaCube mCube = (MagmaCube) entity;

			//System.out.println("size: " + mCube.getSize());

			if (mCube.getSize() > 1 && _enableOnlySmallMagmaCubeLava)
				return;

			Location loc = mCube.getLocation();

			Block block = loc.getBlock();
			
			block.setType(Material.LAVA);

		}
	}
	
	@EventHandler
	public void onPlayerDrinkWaterBottle(PlayerItemConsumeEvent e) 
	{
		if(!_enableWaterBottleBuff) return;
		
	    Player player = e.getPlayer();
	    
	    if(!IsNether(player)) return;
	    
	    ItemStack stack = e.getItem();
	    
	    if (stack.getType() != Material.POTION)  return;
	    
	    PotionMeta potionMeta = (PotionMeta) stack.getItemMeta();
	    PotionData potionData = potionMeta.getBasePotionData();
	    
	    if (potionData.getType() != PotionType.WATER)  return;
	      
	    player.setFireTicks(0);
        PotionEffect fireResistance = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 200, 0);
        PotionEffect existingEffect = player.getPotionEffect(PotionEffectType.FIRE_RESISTANCE);

        if (existingEffect != null) 
        {
          fireResistance = new PotionEffect(PotionEffectType.FIRE_RESISTANCE,
          existingEffect.getDuration() + fireResistance.getDuration(),
          fireResistance.getAmplifier());
        }
        player.addPotionEffect(fireResistance);
        
        int uses = _maxWaterBottleUses;
        
        Integer us = Metods._ins.getPersistenData(stack, "water_bottle_uses", PersistentDataType.INTEGER);
        
        if(us != null) uses = us;
        
        uses --;
        
        stack = player.getInventory().getItemInMainHand();
        
        Metods.setDisplayName(stack, ChatColor.BLUE+"Water Bottle "+ChatColor.DARK_AQUA+uses +ChatColor.BLUE+" / "+ChatColor.DARK_PURPLE+_maxWaterBottleUses);
        
        Metods._ins.setPersistenData(stack, "water_bottle_uses", PersistentDataType.INTEGER, uses);
        
        if(uses <= 0) 
        {
        	//stack.setType(Material.POTION);
        	Metods._ins.removePersistenData(stack, "water_bottle_uses");
        	return;
        }

        e.setCancelled(true);
        
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
	    		
	    		if(mat.getBlastResistance() >= 1200) continue;
	    		
	    		if(mat.isInteractable())
	    		{
	    			block.breakNaturally();
	    			continue;
	    		}

	    		block.setType(mat_list[index]);
	    		
	    		
	    	}
	    	
	        
	    }, delay);
	}
	void GetSettings()
	{
		final String netherSettings = "NetherSettings";
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
		
		String enableMagmaCube = netherSettings+".enableMagmaCubeLava";
		if(!config.contains(enableMagmaCube+".")) config.set(enableMagmaCube, _enableMagmaCube);
		
		String enableBlazeFireBallSpread = netherSettings + ".enableBlazeFireBallSpread";
		if(!config.contains(enableBlazeFireBallSpread)) config.set(enableBlazeFireBallSpread, _enableBlazeFireBallSpread);

		String enableGhastFireBall = netherSettings + ".enableGhastFireBall";
		if(!config.contains(enableGhastFireBall)) config.set(enableGhastFireBall, _enableGhastFireBall);

		String enableWaterBottleBuff = netherSettings + ".enableWaterBottleBuff";
		if(!config.contains(enableWaterBottleBuff)) config.set(enableWaterBottleBuff, _enableWaterBottleBuff);

		String enableShieldBlockArrow = netherSettings + ".enableShieldBlockArrow";
		if(!config.contains(enableShieldBlockArrow)) config.set(enableShieldBlockArrow, _enableShieldBlockArrow);

		String enableWitherSpeed = netherSettings + ".enableWitherSpeed";
		if(!config.contains(enableWitherSpeed)) config.set(enableWitherSpeed, _enableWitherSpeed);

		String enableHoglinSpeed = netherSettings + ".enableHoglinSpeed";
		if(!config.contains(enableHoglinSpeed)) config.set(enableHoglinSpeed, _enableHoglinSpeed);

		String enableDoubleFireDamage = netherSettings + ".enableDoubleFireDamage";
		if(!config.contains(enableDoubleFireDamage)) config.set(enableDoubleFireDamage, _enableDoubleFireDamage);

		String enableOnlySmallMagmaCubeLava = netherSettings + ".enableOnlySmallMagmaCubeLava";
		if(!config.contains(enableOnlySmallMagmaCubeLava)) config.set(enableOnlySmallMagmaCubeLava, _enableOnlySmallMagmaCubeLava);
		
		cm.saveConfig();
		
		_enableMagmaCube = config.getBoolean(enableMagmaCube);
		_enableBlazeFireBallSpread = config.getBoolean(enableBlazeFireBallSpread);
		_enableGhastFireBall = config.getBoolean(enableGhastFireBall);
		_enableWaterBottleBuff = config.getBoolean(enableWaterBottleBuff);
		_enableShieldBlockArrow = config.getBoolean(enableShieldBlockArrow);
		_enableWitherSpeed = config.getBoolean(enableWitherSpeed);
		_enableHoglinSpeed = config.getBoolean(enableHoglinSpeed);
		_enableDoubleFireDamage = config.getBoolean(enableDoubleFireDamage);
		_enableOnlySmallMagmaCubeLava = config.getBoolean(enableOnlySmallMagmaCubeLava);

		
		

	}

}
