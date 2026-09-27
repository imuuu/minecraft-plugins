package me.bullterrier292;

import java.net.NoRouteToHostException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_12_R1.CommandExecute;

public class Commands extends CommandExecute implements Listener, CommandExecutor {
	
	public String cmd1 = "geneores";
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		
		
		if (sender instanceof Player) {
			// -285 65 190
			
			Calendar testTime = Calendar.getInstance();
			Long start = testTime.getTimeInMillis();
			
			sender.sendMessage("moi");
			
			List cord = Arrays.asList(-285, 65, 190);
			
			String oreStr=avaivableOreDirection(cord);
			
			sender.sendMessage(oreStr);
			
			geneOres(oreStr, cord);
			
			
			
			Long stop = Calendar.getInstance().getTimeInMillis()-start;

			
			System.out.println("gene time: "+ stop);
			return true;
			
		}else {
			sender.sendMessage(ChatColor.RED + "Only players can use this command!");
		}
		return false;
	}




public static String avaivableOreDirection(List cordinates) {
	// Returns string with directions where is stone in one cordinate for instance str = "NSWEAB"
	
	int x = (int)cordinates.get(0);
	int y = (int)cordinates.get(1);
	int z = (int)cordinates.get(2);
	
	String worldName = "world";
	String str="";
	Material detectedBlock = Material.STONE;
	int i = 0;
	switch (i) {
		
			case 0:
				//North
				Material northBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x,y,z-1).getType();
				if (northBlock == detectedBlock) {
					str+="N";
				} 
			case 1:
				Material southBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x,y,z+1).getType();
				if (southBlock == detectedBlock) {
					str+="S";
				} 
			case 2:
				Material eastBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x+1,y,z).getType();
				if (eastBlock == detectedBlock) {
					str+="E";
				} 
			case 3:
				Material westBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x-1,y,z).getType();
				if (westBlock == detectedBlock) {
					str+="W";
				} 
			case 4:
				Material aboveBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x,y+1,z).getType();
				if (aboveBlock == detectedBlock) {
					str+="A";
				}
			case 5:
				Material belowBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x,y-1,z).getType();
				if (belowBlock == detectedBlock) {
					str+="B";
				} 
		
				}
	
	return str;
}
public static Material checkOresNearby(List cordinates) {
	int x = (int)cordinates.get(0);
	int y = (int)cordinates.get(1);
	int z = (int)cordinates.get(2);
	int i = 0;
	String worldName = "world";
	Material blockType = Material.STONE;
	switch (i) {
	
	case 0:
		Material middleBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x,y,z).getType();
		if (oreInsideOfcube.allowedBlocks().contains(middleBlock)) {
			blockType = middleBlock;
			break;
		}
	case 1:
		//North
		Material northBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x,y,z-1).getType();
		if (oreInsideOfcube.allowedBlocks().contains(northBlock)) {
			blockType = northBlock;
			break;
		}
		
		List northBlockCord = Arrays.asList(x,y,z-1);
		northBlock=checkOresNearby2(northBlockCord);
		
		if (northBlock != Material.STONE) {
			blockType = northBlock;
			break;
		}
	case 2:
		Material southBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x,y,z+1).getType();
		if (oreInsideOfcube.allowedBlocks().contains(southBlock)) {
			blockType = southBlock;
			break;
		}
		List southBlockCord = Arrays.asList(x,y,z+1);
		southBlock=checkOresNearby2(southBlockCord);
		
		if (southBlock != Material.STONE) {
			blockType = southBlock;
			break;
		}
	case 3:
		Material eastBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x+1,y,z).getType();
		if (oreInsideOfcube.allowedBlocks().contains(eastBlock)) {
			blockType = eastBlock;
			break;
		}
		List eastBlockCord = Arrays.asList(x+1,y,z);
		eastBlock=checkOresNearby2(eastBlockCord);

		if (eastBlock != Material.STONE) {
			blockType = eastBlock;
			break;
		}
	case 4:
		Material westBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x-1,y,z).getType();
		if (oreInsideOfcube.allowedBlocks().contains(westBlock)) {
			blockType = westBlock;
			break;
		}
		List westBlockCord = Arrays.asList(x-1,y,z);
		westBlock=checkOresNearby2(westBlockCord);

		if (westBlock != Material.STONE) {
			blockType = westBlock;
			break;
		}
	case 5:
		Material aboveBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x,y+1,z).getType();
		if (oreInsideOfcube.allowedBlocks().contains(aboveBlock)) {
			blockType = aboveBlock;
			break;
		}
		List aboveBlockCord = Arrays.asList(x,y+1,z);
		aboveBlock=checkOresNearby2(aboveBlockCord);

		if (aboveBlock != Material.STONE) {
			blockType = aboveBlock;
			break;
		}
		
	case 6:
		Material belowBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x,y-1,z).getType();
		if (oreInsideOfcube.allowedBlocks().contains(belowBlock)) {
			blockType = belowBlock;
			break;
		}
		List belowBlockCord = Arrays.asList(x,y+1,z);
		belowBlock=checkOresNearby2(belowBlockCord);

		if (belowBlock != Material.STONE) {
			blockType = belowBlock;
			break;
		}
	
	}
		return blockType;
}
public static Material checkOresNearby2(List cordinates) {
	int x = (int)cordinates.get(0);
	int y = (int)cordinates.get(1);
	int z = (int)cordinates.get(2);
	int i = 0;
	String worldName = "world";
	Material blockType = Material.STONE;
	switch (i) {
	
	case 0:
		//North
		Material northBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x,y,z-1).getType();
		if (oreInsideOfcube.allowedBlocks().contains(northBlock)) {
			blockType = northBlock;
			break;
		}
		
	case 1:
		Material southBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x,y,z+1).getType();
		if (oreInsideOfcube.allowedBlocks().contains(southBlock)) {
			blockType = southBlock;
			break;
		} 
	case 2:
		Material eastBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x+1,y,z).getType();
		if (oreInsideOfcube.allowedBlocks().contains(eastBlock)) {
			blockType = eastBlock;
			break;
		} 
	case 3:
		Material westBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x-1,y,z).getType();
		if (oreInsideOfcube.allowedBlocks().contains(westBlock)) {
			blockType = westBlock;
			break;
		} 
	case 4:
		Material aboveBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x,y+1,z).getType();
		if (oreInsideOfcube.allowedBlocks().contains(aboveBlock)) {
			blockType = aboveBlock;
			break;
		}
	case 5:
		Material belowBlock = Bukkit.getServer().getWorld(worldName).getBlockAt(x,y-1,z).getType();
		if (oreInsideOfcube.allowedBlocks().contains(belowBlock)) {
			blockType = belowBlock;
			break;
		}
	
	}
		return blockType;
}
	
public static void geneOres(String directionStr, List cordinates) {
	int x = (int)cordinates.get(0);
	int y = (int)cordinates.get(1);
	int z = (int)cordinates.get(2);
	
	Random rand = new Random();
	
	
	String worldName = "world";
	int orePos = rand.nextInt(100);
	System.out.println(orePos);
	int spawnboost;
	Material ore=checkOresNearby(cordinates);
	
	if (ore == Material.DIAMOND_ORE) {
		orePos = 1;
	}else if(ore == Material.GOLD_ORE) {
		orePos = 4;
	}else if(ore == Material.IRON_ORE) {
		orePos = 11;
	}else if(ore == Material.REDSTONE_ORE) {
		orePos = 32;
	}else if(ore == Material.EMERALD_ORE) {
		orePos = 43;
	}else if(ore == Material.QUARTZ_ORE ) {
		orePos = 46;
	}else if(ore == Material.LAPIS_ORE) {
		orePos = 57;
	}else if(ore == Material.COAL_ORE) {
		orePos = 72;
	}
	
	System.out.println(orePos);
	
	
	if (oreInsideOfcube.between(orePos, 0, 2)) {
		ore = Material.DIAMOND_ORE;
		spawnboost = 0;
	}else if (oreInsideOfcube.between(orePos, 3, 10)) {
		ore = Material.GOLD_ORE;
		spawnboost = 10;
	} else if(oreInsideOfcube.between(orePos, 11, 30)) {
		ore = Material.IRON_ORE;
		spawnboost = 20;
	}else if(oreInsideOfcube.between(orePos, 31, 41)) {
		ore = Material.REDSTONE_ORE;
		spawnboost = 20;
	}else if(oreInsideOfcube.between(orePos, 42, 43)) {
		ore = Material.EMERALD_ORE;
		spawnboost = 0;
	}else if(oreInsideOfcube.between(orePos, 45, 55)) {
		ore = Material.QUARTZ_ORE;
		spawnboost = 10;
	}else if(oreInsideOfcube.between(orePos, 56, 71)) {
		ore = Material.LAPIS_ORE;
		spawnboost = 15;
	}else {
		ore = Material.COAL_ORE;
		spawnboost = 60;
	}
	
	Bukkit.getServer().getWorld(worldName).getBlockAt(x,y,z).setType(ore);
	
	if (directionStr.indexOf('N') >= 0) {
		
		if (oreChance(spawnboost)) {
			Bukkit.getServer().getWorld(worldName).getBlockAt(x,y,z-1).setType(ore);
			if (oreChange2()) {
				List northBlockCord = Arrays.asList(x,y,z-1);
				String northBlockDir = avaivableOreDirection(northBlockCord);
				geneOres2(northBlockDir, northBlockCord, ore, spawnboost);
			}
		}
		
	}
	if (directionStr.indexOf('S') >= 0) {
		
		if (oreChance(spawnboost)) {
			Bukkit.getServer().getWorld(worldName).getBlockAt(x,y,z+1).setType(ore);
			if (oreChange2()) {
				List southBlockCord = Arrays.asList(x,y,z+1);
				String southBlockDir = avaivableOreDirection(southBlockCord);
				geneOres2(southBlockDir, southBlockCord, ore , spawnboost);
			}
		}
		
	}
	if (directionStr.indexOf('E') >= 0) {
		
		if (oreChance(spawnboost)) {
			Bukkit.getServer().getWorld(worldName).getBlockAt(x+1,y,z).setType(ore);
			if (oreChange2()) {
				List eastBlockCord = Arrays.asList(x+1,y,z);
				String eastBlockDir = avaivableOreDirection(eastBlockCord);
				geneOres2(eastBlockDir, eastBlockCord, ore , spawnboost);
			}
		}
		
	}
	if (directionStr.indexOf('W') >= 0) {
		
		if (oreChance(spawnboost)) {
			Bukkit.getServer().getWorld(worldName).getBlockAt(x-1,y,z).setType(ore);
			if (oreChange2()) {
				List westBlockCord = Arrays.asList(x-1,y,z);
				String westBlockDir = avaivableOreDirection(westBlockCord);
				geneOres2(westBlockDir, westBlockCord, ore , spawnboost);
			}
		}
		
	}
	if (directionStr.indexOf('A') >= 0) {
		
		if (oreChance(spawnboost)) {
			Bukkit.getServer().getWorld(worldName).getBlockAt(x,y+1,z).setType(ore);
			if (oreChange2()) {
				List aboveBlockCord = Arrays.asList(x,y+1,z);
				String aboveBlockDir = avaivableOreDirection(aboveBlockCord);
				geneOres2(aboveBlockDir,aboveBlockCord, ore, spawnboost);
			}
		}
		
	}
	if (directionStr.indexOf('B') >= 0) {
		
		if (oreChance(spawnboost)) {
			Bukkit.getServer().getWorld(worldName).getBlockAt(x,y-1,z).setType(ore);
			/*
			if (oreChange2()) {
				List belowBlockCord = Arrays.asList(x,y+1,z);
				String belowBlockDir = avaivableOreDirection(belowBlockCord);
				geneOres2(belowBlockDir,belowBlockCord, ore , spawnboost);
			}*/
		}
		
	}
	
}
public static boolean oreChange2() {
	Random rand = new Random();
	int oreChance = rand.nextInt(100);
	if (oreInsideOfcube.between(oreChance, 0, 30)) {
		return true;
	}
	return false;
}
public static boolean oreChance(int spawnboost) {
	Random rand = new Random();
	int oreChance = rand.nextInt(100);
	if (oreInsideOfcube.between(oreChance, 0, 30+spawnboost)) {
		return true;
	}
	return false;
}
public static void geneOres2(String directionStr, List cordinates, Material ore, int spawnboost) {
	int x = (int)cordinates.get(0);
	int y = (int)cordinates.get(1);
	int z = (int)cordinates.get(2);
	
	String worldName = "world";
	if (spawnboost > 0) {
		spawnboost = spawnboost/2;
	}
	System.out.println("spawboost2: "+ spawnboost);
	
	if (directionStr.indexOf('N') >= 0) {
		
		if (oreChance(spawnboost)) {
			Bukkit.getServer().getWorld(worldName).getBlockAt(x,y,z-1).setType(ore);
			
		}
		
	}
	if (directionStr.indexOf('S') >= 0) {
		
		if (oreChance(spawnboost)) {
			Bukkit.getServer().getWorld(worldName).getBlockAt(x,y,z+1).setType(ore);
		}
		
	}
	if (directionStr.indexOf('E') >= 0) {
		
		if (oreChance(spawnboost)) {
			Bukkit.getServer().getWorld(worldName).getBlockAt(x+1,y,z).setType(ore);
		}
		
	}
	if (directionStr.indexOf('W') >= 0) {
		
		if (oreChance(spawnboost)) {
			Bukkit.getServer().getWorld(worldName).getBlockAt(x-1,y,z).setType(ore);
		}
		
	}
	if (directionStr.indexOf('A') >= 0) {
		
		if (oreChance(spawnboost)) {
			Bukkit.getServer().getWorld(worldName).getBlockAt(x,y+1,z).setType(ore);
		}
		
	}
	if (directionStr.indexOf('B') >= 0) {
		
		if (oreChance(spawnboost)) {
			Bukkit.getServer().getWorld(worldName).getBlockAt(x,y-1,z).setType(ore);
		}
		
	}
	
}

}