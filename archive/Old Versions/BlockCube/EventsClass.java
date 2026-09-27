package me.bullterrier292;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class EventsClass implements Listener{
	
	
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		
		if (event.getBlock().getType() == Material.GLOWSTONE) {
			
			Location location = event.getBlock().getLocation();
			int blockLocationX = location.getBlockX();
			int blockLocationY = location.getBlockY();
			int blockLocationZ = location.getBlockZ();
			player.sendMessage(Integer.toString(blockLocationX) +','+ Integer.toString(blockLocationY) +','+ Integer.toString(blockLocationZ));
			
			
			
			for (int i=0; i<100 ; i++) {
				location.getWorld().getBlockAt(blockLocationX,blockLocationY+i,blockLocationZ).setType(Material.REDSTONE_BLOCK);
				
			}
		}
		
	
	}
	
}
