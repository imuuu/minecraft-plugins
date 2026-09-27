package me.bullterrier292;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class EventsClass implements Listener {
	private String worldname = "world";
	private static WorldRestore plugin = WorldRestore.getPlugin(WorldRestore.class);

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		// Commands.chunkCalculatorStarted();
		if (event.getBlock().getLocation().getWorld().getName().equalsIgnoreCase(worldname)) {
		Chunk c = event.getBlock().getChunk();

		int y = event.getBlock().getY();

		chunkLoggerV2(c, y);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		
		if (event.getBlock().getLocation().getWorld().getName().equalsIgnoreCase(worldname)) {
		Chunk c = event.getBlock().getChunk();

		int y = event.getBlock().getY();

		chunkLoggerV2(c, y);
		}
	}

	@EventHandler
	public void onExplotion(EntityExplodeEvent event) {
		
		if (event.getLocation().getWorld().getName().equalsIgnoreCase(worldname)) {
			for (Block b : event.blockList()) {
				Chunk c = b.getLocation().getChunk();

				int y = b.getY();
				chunkLoggerV2(c, y);

			}
		}
	}

	@EventHandler
	public void onLeft(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (player.getWorld().getName().equalsIgnoreCase(worldname)) {
			Chunk c = player.getLocation().getChunk();
			String section = "Main.playerQuitLocations.";
			int chunkIdPart1 = c.getX();
			int chunkIdPart2 = c.getZ();
			String chunkid = String.valueOf(chunkIdPart1) + ',' + String.valueOf(chunkIdPart2);

			plugin.getConfig().set(section + player.getUniqueId(),
					chunkid + ":" + player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + ","
							+ player.getLocation().getBlockZ() + ":" + player.getName());
			plugin.saveConfig();
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player.getWorld().getName().equalsIgnoreCase(worldname)) {
			String section = "Main.playerQuitLocations." + player.getUniqueId();

			plugin.getConfig().set(section, null);
			plugin.saveConfig();
		}
	}
	@EventHandler
	public void onInventoryInteract(InventoryOpenEvent event) {
		if (event.getPlayer().getLocation().getWorld().getName().equalsIgnoreCase(worldname)) {
			Chunk c = event.getPlayer().getLocation().getChunk();

			int y = event.getPlayer().getLocation().getBlockY();

			chunkLoggerV2(c, y);
		}
	}
	@EventHandler
	public void onBed(PlayerBedEnterEvent event) {
		if (event.getPlayer().getLocation().getWorld().getName().equalsIgnoreCase(worldname)) {
			Chunk c = event.getPlayer().getLocation().getChunk();

			int y = event.getPlayer().getLocation().getBlockY();

			chunkLoggerV2(c, y);
		}
	}
	@EventHandler
	public void leaveDecay(LeavesDecayEvent event) {
		if (event.getBlock().getLocation().getWorld().getName().equalsIgnoreCase(worldname)) {
			Block b = event.getBlock();
			Chunk c = b.getLocation().getChunk();

			int y = b.getY();
			chunkLoggerV2(c, y);
		}
	}

	public static void chunkLoggerV2(Chunk c, int y) {
		// x and z are c

		int x = c.getBlock(0, 0, 0).getX();
		int z = c.getBlock(0, 0, 0).getZ();

		int chunkIdPart1 = c.getX();
		int chunkIdPart2 = c.getZ();
		String chunkid = ' ' + String.valueOf(chunkIdPart1) + ',' + String.valueOf(chunkIdPart2) + ' ';

		Calendar cal = Calendar.getInstance();
		Long timeNow = cal.getTimeInMillis();

		if (y < 55 || y > 130) {

			plugin.getConfig().set("Main." + "wholeChunkIds." + chunkid, x + "," + z + ":" + timeNow);
			plugin.saveConfig();

		} else {
			System.out.println("surf");
			plugin.getConfig().set("Main." + "surfaceChunkIds." + chunkid, x + "," + z + ":" + timeNow);
			plugin.saveConfig();

		}

	}

	public static List switcher(int a, int b) {
		// a gonna be lower than b

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

	public static boolean between(int i, int min, int max) {
		if (i >= min && i <= max)
			return true;
		else
			return false;
	}

	public static boolean checkInArea(int x, int z, int x1, int z1, int x2, int z2) {

		List xxx = switcher(x1, x2);

		List zzz = switcher(z1, z2);

		if (between(x, (int) xxx.get(0), (int) xxx.get(1)) && between(z, (int) zzz.get(0), (int) zzz.get(1))) {
			return true;
		}
		return false;

	}

}
