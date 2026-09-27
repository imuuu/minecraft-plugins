package imu.imusTNT.Events;


import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import imu.iAPI.Other.ConfigMaker;
import imu.imusTNT.TNTs.TNT;
import imu.imusTNT.TNTs.TNT_Mananger;
import imu.imusTNT.enums.TNT_TYPE;
import imu.imusTNT.main.ImusTNT;


public class imusTNT_events implements Listener
{
	Plugin _plugin;
	
	
	
	//private final String PD_SPAWNER_TYPE = "tnt_type";

	public imusTNT_events()
	{
		_plugin = ImusTNT.Instance;
		
	}
	
	
	@EventHandler
    public void OnExplode(EntityExplodeEvent e) 
	{
        if (e.getEntityType() == EntityType.TNT) 
        {
        	
        	TNT_TYPE tnt_type = TNT_Mananger.Instance.GetTntType(e.getEntity());
        	
        	if(tnt_type == TNT_TYPE.NONE) return;
        	
        	TNT tnt = TNT_Mananger.Instance.GetTNT(tnt_type);
        	
        	if(TNT_Mananger.Instance.IsExploded(e.getEntity()))
        	{
        		return;
        	}
        	//e.setCancelled(true);
        	e.blockList().clear();

    		Entity entity = e.getEntity();
    		TNT_Mananger.Instance.SetMetadataExplode(entity);
    		List<Block> blocks = tnt.GetBlocks(e.getLocation());
    		EntityExplodeEvent explodeEvent = new EntityExplodeEvent(entity, e.getLocation(), blocks, 10f, e.getExplosionResult());
    		Bukkit.getServer().getPluginManager().callEvent(explodeEvent);
    		
    		if(explodeEvent.isCancelled()) return;
    		
    		tnt.OnExplode(entity,e.getLocation(), explodeEvent.blockList());
    			
    		
    		
        }
    }
	

	@EventHandler
	public void OnBlockPlace(BlockPlaceEvent e)
	{
		if(e.getBlockPlaced().getType() != Material.TNT) return;
		
		if(!TNT_Mananger.Instance.IsCustomTNT(e.getItemInHand())) return;
		
		Block tnt = e.getBlock();
		e.getBlockPlaced();
		
		TNT_Mananger.Instance.SetMetadata(e.getPlayer(),e.getItemInHand(), e.getBlockPlaced());
		
		tnt.getWorld().spawnParticle(Particle.FLAME, tnt.getLocation(), 50, 0.5, 0.5, 0.5, 0.1);


	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e)
	{
		
		if(e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		
		Block block = e.getBlock();
		if(block.getType() != Material.TNT) return;
		
		TNT_TYPE tnt_type = TNT_Mananger.Instance.GetTntType(block);
		
		if(tnt_type == TNT_TYPE.NONE) return;
		
		e.setDropItems(false);
		
		ItemStack stack = TNT_Mananger.Instance.GetStack(tnt_type);
		
		
		block.getWorld().dropItemNaturally(block.getLocation(), stack);
		
	}
	
	@EventHandler
	public void OnUse(PlayerInteractEvent e)
	{
		Block block = e.getClickedBlock();
		
		if( block == null || block.getType() != Material.TNT) return;
		
		if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		
		if(e.getPlayer().getInventory().getItemInMainHand().getType() != Material.FLINT_AND_STEEL) return;
		
		
		TNT_TYPE tnt_type = TNT_Mananger.Instance.GetTntType(block);
		
		if(tnt_type == TNT_TYPE.NONE) return;
		
		block.setType(Material.AIR);
		e.setCancelled(true);
		Entity entity = block.getWorld().spawnEntity(block.getLocation().add(0.5f, 0.1f, 0.5f), EntityType.TNT);
		TNT_Mananger.Instance.SetMetadata(e.getPlayer(), block, entity);
		
		TNT_Mananger.Instance.GetTNT(tnt_type).OnIgnite(e.getPlayer(), entity);
		//block.setType(Material.AIR);
			
	}
	

	void GetSettings()
	{
		ConfigMaker cm = new ConfigMaker(_plugin, "settings.yml");
		FileConfiguration config = cm.getConfig();

		//String dropChance = "settings.spawnerDropChanceSilkTouch";
		
		if (!config.contains("settings."))
		{
			// default values
			_plugin.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "ImusSpawners : Default config made!");
			//config.set(dropChance, _silk_touch_chance);			config.set(spawnerRandomEntityChance, _random_entity_chance);

			cm.saveConfig();
			return;
		}

		//_silk_touch_chance = config.getDouble(dropChance);
		
	}

}
