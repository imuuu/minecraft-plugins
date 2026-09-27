package imu.WorldRestore.Events;

import java.util.HashSet;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import imu.WorldRestore.Managers.ChunkManager;
import imu.WorldRestore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class ChunkVisitinEvents implements Listener
{
	Main _main = null;
	ChunkManager _cManager = null;
	
	String _worldName = "";
	String _targetWorldName = "";
	public ChunkVisitinEvents(Main main)
	{
		_main = main;
		_cManager = main.getChunkManager();
		_worldName = _cManager.getWorldName();
		_targetWorldName = _cManager.getDefTargetWorldName();
	}
	
	@EventHandler
	public void onPlayerLeave(org.bukkit.event.player.PlayerQuitEvent e)
	{
		if(isRightWorld(e.getPlayer().getLocation()))
		{
			_main.getPlayerData().savePlayerQuitLocation(e.getPlayer(),false);
		}
		
	}
	@EventHandler
	public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent e)
	{
		if(isRightWorld(e.getPlayer().getLocation()))
		{
			if(_main.getPlayerData().checkIfPlayerIsTagged(e.getPlayer()))
			{
				e.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "You have been teleported to spawn due to chunk was updated while you were offline!");
			}
		}
		
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent e)
	{		
		if(isRightWorld(e.getBlock().getLocation()))
		{
			_cManager.setChunkAuto(e.getBlock(),-1, 1);
		}	
	}
	@EventHandler
	public void onBreak(BlockPlaceEvent e)
	{
		if(isRightWorld(e.getBlock().getLocation()))
		{
			_cManager.setChunkAuto(e.getBlock(),-1, 1);
		}	
	}
	
	@EventHandler
	public void onExplotion(EntityExplodeEvent e) 
	{		
		if (isRightWorld(e.getLocation())) 
		{			
			HashSet<Chunk> cs = new HashSet<>();
			for (Block b : e.blockList()) 
			{
				Chunk bChunk = b.getChunk();
				if(!cs.contains(bChunk))
				{		
					cs.add(bChunk);
					_cManager.setChunkAuto(b,-10, 10);					
				}
			}
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) 
	{
		Block b = e.getClickedBlock();
		if (b != null && isRightWorld(b.getLocation())) 
		{
			_cManager.setChunkAuto(b, 0, 0);
		}
	}
	
	@EventHandler
	public void leaveDecay(LeavesDecayEvent e) 
	{
		Block b = e.getBlock();
		if (isRightWorld(b.getLocation())) 
		{
			_cManager.setChunkAuto(b, 0, 0);
		}
	}
	
	boolean isValidToSave(Location loc)
	{
		if(isRightWorld(loc) && !isInRegion(loc))
		{
			System.out.println("its valid loc");
			return true;
		}
		System.out.println("its not valid");
		return false;
	}
	
	boolean isRightWorld(Location loc)
	{
		if(loc.getWorld().getName().equalsIgnoreCase(_worldName))
			return true;
		
		return false;		
	}
	
	boolean isInRegion(Location loc)
	{
		return _cManager.isLocationInRegion(loc);
	}
	
	
	
	
}
