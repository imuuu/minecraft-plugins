package me.imu.imusdifficult.Events;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import me.imu.imusdifficult.ImusDifficult;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;
import org.bukkit.persistence.PersistentDataType;



public class DotEvents implements Listener
{
	
	private int _durabilityDamageArmor = 1;
	private int _arrowFoodLevelReduce = 1;
	private int _foodReduceChance = 65;
	
	private double _howOftenTakesDurLost = 0.2f;
	private Cooldowns _cds;
	private Random _rand;
	public DotEvents()
	{
		_rand = new Random();
		_cds = new Cooldowns();
		LoadSettings(ImusDifficult.Instance.getConfig());
	}

	// Hell leggings + chestplate (ImusHellGear) make the wearer fire proof. Read straight from the
	// item's data so this plugin doesn't need ImusHellGear installed.
	private static boolean HasHellFireProtection(Player player)
	{
		return Metods._ins.getPersistenData(player.getInventory().getLeggings(), "HELL_LEGGINS", PersistentDataType.INTEGER) != null
				&& Metods._ins.getPersistenData(player.getInventory().getChestplate(), "HELL_CHESTPLATE", PersistentDataType.INTEGER) != null;
	}
	

	
	@EventHandler
	public void OnEntityDamage(EntityDamageEvent e)
	{

		
		if(!(e.getEntity() instanceof Player)) return;
		
		if(e.isCancelled()) return;
		
		Player player = (Player) e.getEntity();
		
		DamageCause cause = e.getCause();
		
		
		
		if(cause == DamageCause.POISON)
		{
			Add_DurabilityLost_Armor(player, _durabilityDamageArmor);
		}
		
		if(cause == DamageCause.WITHER)
		{
			Add_DurabilityLost_Armor(player, _durabilityDamageArmor);
		}
		
		if (HasHellFireProtection(player)) return;
		
		if(cause == DamageCause.FIRE)
		{
			Add_DurabilityLost_Armor(player, _durabilityDamageArmor);
		}
		
		if(cause == DamageCause.FIRE_TICK)
		{
			Add_DurabilityLost_Armor(player, _durabilityDamageArmor);
		}
		
		if(cause == DamageCause.LAVA)
		{
			Add_DurabilityLost_Armor(player, _durabilityDamageArmor);
		}

	}
	

	@EventHandler
	public void PlayerQuit(PlayerQuitEvent e)
	{
		_cds.removeCooldown(e.getPlayer().getUniqueId()+"dur");
	}
	@EventHandler
	public void ProjectileLaunch(ProjectileHitEvent e)
	{
		if(!(e.getEntity().getShooter() instanceof Skeleton)) return;
		
		if(!(e.getHitEntity() instanceof Player)) return;
		
		Player player = (Player)e.getHitEntity();
		
		if(player.getGameMode() != GameMode.SURVIVAL ) return;
		
		if(player.isBlocking() ) 
		{
			return;
		}

		int totalLevel = Metods._ins.GetArmorSlotEnchantCount(player, Enchantment.PROJECTILE_PROTECTION);
		int chance = _foodReduceChance -(totalLevel * 3);

		if( _rand.nextInt(100) < chance)
		{
			Add_HungerDamage(player, _arrowFoodLevelReduce);
		}

	}
	
	private void Add_HungerDamage(Player player, int hungerDmg)
	{
		if(player.isBlocking()) return;

		int newHunger = player.getFoodLevel()-hungerDmg;
		
		player.setFoodLevel(newHunger);
		
	}
	private void Add_DurabilityLost_Armor(Player player, int durabilityLost)
	{
		if(durabilityLost <= 0) return;
		
		if(player.getGameMode() != GameMode.SURVIVAL) return;
		
		
		if(!_cds.isCooldownReady(player.getUniqueId()+"dur")) return;
		
		_cds.setCooldownInSeconds(player.getUniqueId()+"dur", _howOftenTakesDurLost);
		
		//System.out.println("dur lost");
		
		ItemStack[] armors = player.getInventory().getArmorContents();
		
		ItemStack helmet = armors[3];
		ItemStack chestplate = armors[2];
		ItemStack leggings = armors[1];
		ItemStack boots = armors[0];
		
		if(ImusAPI._metods.giveDamage(helmet, durabilityLost, true) && helmet.getType() == Material.AIR) player.playSound(player, Sound.ENTITY_ITEM_BREAK, 1, 1);
		if(ImusAPI._metods.giveDamage(chestplate, durabilityLost, true) && chestplate.getType() == Material.AIR) player.playSound(player, Sound.ENTITY_ITEM_BREAK, 1, 1);
		if(ImusAPI._metods.giveDamage(leggings, durabilityLost, true) && leggings.getType() == Material.AIR) player.playSound(player, Sound.ENTITY_ITEM_BREAK, 1, 1);
		if(ImusAPI._metods.giveDamage(boots, durabilityLost, true) && boots.getType() == Material.AIR) player.playSound(player, Sound.ENTITY_ITEM_BREAK, 1, 1);

	}
	
	// config.yml, re-read when the settings menu changes a value
	public void LoadSettings(FileConfiguration config)
	{
		_durabilityDamageArmor = config.getInt("damage-over-time.armor-durability-loss", 1);
		_arrowFoodLevelReduce = config.getInt("skeleton-arrows.food-loss", 1);
		_foodReduceChance = config.getInt("skeleton-arrows.food-loss-chance", 65);
	}
}
