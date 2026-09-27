package Events;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import me.bullterrier292.WorldRestore.ChunkINFO;
import me.bullterrier292.WorldRestore.WorldRestore;



public class WREventClass implements Listener
{
	private WorldRestore _p;
	
	public WREventClass(WorldRestore plugin)
	{
		_p=plugin;
	}
	
	@EventHandler
	public void onMove(BlockBreakEvent e)
	{
		Player player=(Player) e.getPlayer();
		Chunk c=e.getBlock().getChunk();
		int y = e.getBlock().getY();
		player.sendMessage("Chunk: " +e.getBlock().getChunk());
		

		if(_p.chunkInfos.isEmpty() || !_p.chunkInfos.containsKey(c))
		{
			ChunkINFO cInfo=new ChunkINFO(_p);
			cInfo.set_chunkID(c);
			cInfo.setMinMaxY(y);
			cInfo.addVisited(player);
			cInfo.set_timeStamp(System.currentTimeMillis());
			
			_p.chunkInfos.put(c, cInfo);
			player.sendMessage(ChatColor.GREEN +"Chunk added");
		}else 
		{
			ChunkINFO info=_p.chunkInfos.get(c);
			info.setMinMaxY(y);
			//System.out.println("SYS: " + (System.currentTimeMillis() - info.get_timeStamp()));
			info.set_timeStamp(System.currentTimeMillis());
			//info.fixChunk();
			player.sendMessage(ChatColor.RED + "Chunk is already in list");
			
		}
		
		
		
		
	}
}
