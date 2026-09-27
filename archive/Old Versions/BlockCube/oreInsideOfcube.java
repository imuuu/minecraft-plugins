package me.bullterrier292;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import net.md_5.bungee.api.ChatColor;

public class oreInsideOfcube implements Listener{

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		// -294.540, 62.0 , 188.628
		// -306.361, 68 ,176.65
		
		Location location = event.getBlock().getLocation();

		List blockCordinates = Arrays.asList(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		List areaCordinates1 = Arrays.asList(-295, 62, 188);
		List areaCordinates2 = Arrays.asList(-307, 68, 177);
		
		List allowedBlocksMine = allowedBlocks();
		
		if (checkInArea(blockCordinates, areaCordinates1, areaCordinates2)) {
			player.sendMessage(ChatColor.GREEN + "You are in area!");
			
			if (allowedBlocksMine.contains(event.getBlock().getType())) {
				player.sendMessage("You are allowed to break it");
				
			} else event.setCancelled(true);
		}
	}

	public static List switcher(int a, int b) {
		List myList = new ArrayList();
		
		int xx;
		
		if (a > b) {
			xx = a;
			a = b;
			b = xx;
		}
		
		myList.add(a);
		myList.add(b);
		
		return myList;
	}
	
	public static List allowedBlocks() {
		List allowedBlocks = Arrays.asList(Material.DIAMOND_ORE,Material.GOLD_ORE,Material.COAL_ORE,
												Material.IRON_ORE,Material.LAPIS_ORE,Material.REDSTONE_ORE,
												Material.EMERALD_ORE,Material.QUARTZ_ORE);
		return allowedBlocks;
	}
	public static boolean between(int i, int min, int max) {
	    if (i >= min && i <= max)
	        return true;
	    else
	        return false;
	}
	public static boolean checkInArea(List cordinates,List areaCordinates1, List areaCordinates2) {
		int x = (int)cordinates.get(0);
		int y = (int)cordinates.get(1);
		int z = (int)cordinates.get(2);
		
		List xxx = switcher((int)areaCordinates1.get(0), (int)areaCordinates2.get(0));
		List yyy = switcher((int)areaCordinates1.get(1), (int)areaCordinates2.get(1));
		List zzz = switcher((int)areaCordinates1.get(2), (int)areaCordinates2.get(2));
		
		if (between(x, (int)xxx.get(0), (int)xxx.get(1)) && between(y, (int)yyy.get(0), (int)yyy.get(1)) && between(z, (int)zzz.get(0), (int)zzz.get(1))){
			return true;
			} 
				return false;
		
		
		
	}
}