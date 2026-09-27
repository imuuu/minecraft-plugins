package imu.imusTNT.TNTs;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import imu.imusTNT.enums.TNT_TYPE;

public class TNT_Chunk extends TNT
{
	public int X = 16;
	public int Y = 1;
	public int Z = 16;
	
	public double BlastResistanceReshold = 1200; //obsidian 1200,ancient debriss 1200, bedrock 3.6m
	public TNT_Chunk(String displayName, TNT_TYPE type)
	{
		super(displayName, type);
		Init_IgnoreMats();
	}
	
	private void Init_IgnoreMats()
	{
		Add_IgnoreMat(Material.CHEST);
		Add_IgnoreMat(Material.ENDER_CHEST);
		Add_IgnoreMat(Material.DIAMOND_ORE);
		Add_IgnoreMat(Material.DEEPSLATE_DIAMOND_ORE);
		Add_IgnoreMat(Material.DIAMOND_BLOCK);
		Add_IgnoreMat(Material.NETHERITE_BLOCK);
		//Add_IgnoreMat(Material.SHULKER_BOX);
		
	}

	@Override
	public String[] GetLores()
	{
		String[] lores = 
		{
				"&3Removes &call &6blocks in &lchunk &3area of "+X+"x"+Y+"x"+Z,
				"&3Removes &cLava and &9Water",
				"&3Do not break &6inventories&3, &bdiamond&3 or &5debris",
				"&3Broken blocks drop nothing!",
				" ",
				"&l&6Needs Flint and Steel to work!",
				
		};
		return lores;
	}

	@Override
	public void OnExplode(Entity entity, Location loc, List<Block> blocks)
	{
		Player player = TNT_Mananger.Instance.GetTntPlacer(entity);
		for(Block b : blocks)
		{
			//b.setType(Material.AIR);
			if(player != null)
			{
				BlockBreakEvent bBreakEvent = new BlockBreakEvent(b, player);
				Bukkit.getServer().getPluginManager().callEvent(bBreakEvent);
				
				if(bBreakEvent.isCancelled()) continue;
			}

			b.setType(Material.AIR);

		}
		
		
		
//		entity.getWorld().playSound(loc.getChunk().getBlock(0, 0, 0).getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1, 1);
//		entity.getWorld().playSound(loc.getChunk().getBlock(15, 0, 0).getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1, 1);
//		entity.getWorld().playSound(loc.getChunk().getBlock(15, 0, 15).getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1, 1);
//		entity.getWorld().playSound(loc.getChunk().getBlock(0, 0, 15).getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1, 1);
//		entity.getWorld().playSound(loc.getChunk().getBlock(8, 0, 8).getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1, 1);
//		
//		entity.getWorld().playEffect(loc.getChunk().getBlock(0, 0, 0).getLocation(), Effect.SMOKE, 100);
//		entity.getWorld().playEffect(loc.getChunk().getBlock(15, 0, 0).getLocation(), Effect.SMOKE, 100);
//		entity.getWorld().playEffect(loc.getChunk().getBlock(0, 0, 15).getLocation(), Effect.SMOKE, 100);
//		entity.getWorld().playEffect(loc.getChunk().getBlock(15, 0, 15).getLocation(), Effect.SMOKE, 100);
//		entity.getWorld().playEffect(loc.getChunk().getBlock(7, 0, 7).getLocation(), Effect.SMOKE, 100);
		
		
	}

	@Override
	public List<Block> GetBlocks(Location loc)
	{
		List<Block> blocks = new ArrayList<>();
		for(int x = 0; x < X; x++)
		{
			for(int z = 0; z < Z; z++)
			{
				Block b = loc.getChunk().getBlock(x, loc.getBlockY(), z);
				
				if(b.getType() == Material.AIR) continue;
				
				if(b.getType().getBlastResistance() >= BlastResistanceReshold) continue;
				
				if(IsIgnoreMat(b.getType())) continue;
				
				if(b.getType().isInteractable()) continue;
				
				blocks.add(b);

			}
		}
		return blocks;
	}

	@Override
	public void OnIgnite(Player player, Entity entity)
	{
		entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_TNT_PRIMED, 1, 1);
		
		
	}

	

}
