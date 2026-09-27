package imu.iAPI.Utilities;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import imu.iAPI.Enums.DEFAULT_FONT_INFO;
import imu.iAPI.Main.ImusAPI;

public  class ImusUtilities
{
	private static Random _random = new Random();
	
	public static int LevenshteinDistance(String a, String b) 
	{
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) 
        {
            for (int j = 0; j <= b.length(); j++) 
            {
                if (i == 0) 
                {
                    dp[i][j] = j;
                }
                else if (j == 0) 
                {
                    dp[i][j] = i;
                }
                else 
                {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j - 1] 
                                + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1), 
                                dp[i - 1][j] + 1), 
                                dp[i][j - 1] + 1);
                }
            }
        }

        return dp[a.length()][b.length()];
    }

	
	@SuppressWarnings("unchecked")
	public static <T> T[] CombineArrays(T[] array1, T[] array2) 
	{
	    List<T> list = new ArrayList<>(Arrays.asList(array1));
	    list.addAll(Arrays.asList(array2));
	    return list.toArray((T[])Array.newInstance(array1.getClass().getComponentType(), list.size()));
	}
	
	public static <T> List<T> CombineArrays(List<T> list1, List<T> list2) 
	{
	    List<T> combinedList = new ArrayList<>(list1);
	    combinedList.addAll(list2);
	    return combinedList;
	}
	
	public static <T> T[] AddElementAtIndex(T[] array, T element, int index) 
	{
	    if (index < 0 || index > array.length) {
	        throw new IndexOutOfBoundsException("Invalid index: " + index);
	    }
	    @SuppressWarnings("unchecked")
		T[] new_array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), array.length + 1);
	    System.arraycopy(array, 0, new_array, 0, index);
	    new_array[index] = element;
	    System.arraycopy(array, index, new_array, index + 1, array.length - index);
	    return new_array;
	}
	
	public static <T> T[] RemoveElementAtIndex(T[] array, int index) 
	{
	    if (index < 0 || index >= array.length) 
	    {
	        throw new IndexOutOfBoundsException("Invalid index: " + index);
	    }
	    @SuppressWarnings("unchecked")
		T[] new_array = (T[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), array.length - 1);
	    System.arraycopy(array, 0, new_array, 0, index);
	    System.arraycopy(array, index + 1, new_array, index, array.length - index - 1);
	    return new_array;
	}
	
	public static <T> T GetRandomElement(Set<T> set) 
    {
        int size = set.size();
        if (size == 0) return null;
        int item = _random.nextInt(size);
        int i = 0;
        for(T obj : set) 
        {
            if (i == item)
                return obj;
            i++;
        }
        return null;
    }
	
	public static LinkedList<Location> CreateSphere(Location center, int radius, HashSet<Material> ignoreSet, HashSet<Material> includeSet) 
	{
		
		LinkedList<Location> positions = new LinkedList<>();
	    int radiusSquared = radius * radius;

	    for (int x = -radius; x <= radius; x++) {
	        for (int y = -radius; y <= radius; y++) {
	            for (int z = -radius; z <= radius; z++) {
	                if (x * x + y * y + z * z <= radiusSquared) {
	                	
	                	Location loc = center.clone().add(x, y, z);
	    				Block b = loc.getBlock();
	    				
	    				if(b == null) continue;
	    				
	    				if(includeSet != null && !includeSet.isEmpty() && !includeSet.contains(b.getType()))
	    				{
	    					//System.out.println("werent in include list");
	    					continue;
	    				}
	    				
	    				if(ignoreSet != null && ignoreSet.contains(b.getType()))
	    				{
	    					//System.out.println("werent in ignore list");
	    					continue;
	    				}
	    				
	    				positions.add(loc);	
	                }
	            }
	        }
	    }
	    //System.out.println("Got blocks size of: "+positions.size());
	    return positions;
	}
	
	public static LinkedList<Location> CreateCirclePlatform(Location center, int radius, HashSet<Material> ignoreSet, HashSet<Material> includeSet)
	{
		LinkedList<Location> positions = new LinkedList<>();
		int x = center.getBlockX();
		int y = center.getBlockY();
		int z = center.getBlockZ();
		
		for (int i = x - radius; i <= x + radius; i++)
		{
			for (int k = z - radius; k <= z + radius; k++)
			{
				Location loc = new Location(center.getWorld(), i, y, k);
				Block b = loc.getBlock();
				if(b == null) continue;
				
				if(includeSet != null && !includeSet.isEmpty() && !includeSet.contains(b.getType()))
				{
					continue;
				}
				
				if(ignoreSet != null && ignoreSet.contains(b.getType())) continue;
				
				positions.add(loc);	
			}
		}
		
		return positions;
	}
	
	public static void ChangeBlockType(Iterable<Block> list, Material[] mat_list, long delay, int index) {
	    
		Bukkit.getScheduler().runTaskLater(ImusAPI._instance, () -> 
	    {
	    	for(Block block : list)
	    	{
	    		block.setType(mat_list[index]);
	    	}
	    	
	    	int newIndex  = index + 1;
	    	
	    	if(newIndex < mat_list.length)
	    	{
	    		ChangeBlockType(list, mat_list, delay, newIndex);
	    	}

	    }, delay);
	}
	
	
//	public static boolean IsPositionBehind(Location currentLoc, Location targetLocation)
//	{
////		double distance = targetLocation.distance(currentLoc);
////		Vector targetRightDir = targetLocation.subtract(currentLoc).toVector().normalize().multiply(distance);
////		targetLocation = targetRightDir.add(targetRightDir)
////		Location loc = new Location(targetLocation.getWorld(), targetRightDir.getX(),targetRightDir.getY(),targetRightDir.getZ());
//		
//		return IsPositionBehind(currentLoc, targetLocation,targetLocation.clone().subtract(currentLoc).toVector().normalize());
//	}
	
	public static boolean IsPositionBehind(Location currentLoc, Location targetLocation, Vector direction)
	{		
		Vector current = currentLoc.toVector();
		Vector target = targetLocation.toVector();		
		Vector first = target.clone().subtract(current.clone());
		
		double dot =  first.dot(direction);

		if(dot < 0) return true;
		
		return false;
		
		
	}
	
	public static float[] GetYawPitch(Location loc)
	{
		return GetYawPitch(loc.toVector());
	}
	
	public static float[] GetYawPitch(Vector vector)
	{
		vector = vector.normalize();
		float yaw = (float) Math.toDegrees(Math.atan2(vector.getZ(), vector.getX())) - 90;
		float pitch = (float) Math.toDegrees(Math.asin(vector.getY()));
		
		return new float[] {yaw, pitch};
	}
	
	public static <T> T[] ShuffleArray(T[] array) {
	    int n = array.length;
	   
	    for (int i = 0; i < n; i++) 
	    {
	        // Pick a new index higher than current for each item in the array
	        //int r = i + random.nextInt(n - i);
	        int r = i + ThreadLocalRandom.current().nextInt(n-i);

	        // Swap item into new spot
	        T t = array[r];
	        array[r] = array[i];
	        array[i] = t;
	    }
	    return array;
	}
	
	public static Chunk[] Get9ChunksAround(Location loc)
	{
		Chunk[] chunks = new Chunk[9];
		
		World world = loc.getWorld();
		
		chunks[0] = loc.getChunk();
		
		int x = chunks[0].getX();
		int z = chunks[0].getZ();
		
		chunks[1] = world.getChunkAt(x+1, z);
		chunks[2] = world.getChunkAt(x, z+1);
		chunks[3] = world.getChunkAt(x+1, z+1);
		
		chunks[4] = world.getChunkAt(x-1, z);
		chunks[5] = world.getChunkAt(x, z-1);
		chunks[6] = world.getChunkAt(x-1, z-1);
		
		chunks[7] = world.getChunkAt(x-1, z+1);
		chunks[8] = world.getChunkAt(x+1, z-1);
		
		return chunks;
	}
	public static Chunk[] GetChunksAround(Location loc, int range) {
	    Chunk[] chunks = new Chunk[(range * 2 + 1) * (range * 2 + 1)];
	    World world = loc.getWorld();
	    int chunkX = loc.getChunk().getX();
	    int chunkZ = loc.getChunk().getZ();
	    int index = 0;
	    for (int x = chunkX - range; x <= chunkX + range; x++) {
	        for (int z = chunkZ - range; z <= chunkZ + range; z++) {
	            chunks[index++] = world.getChunkAt(x, z);
	        }
	    }
	    return chunks;
	}
	
	public static void SetFakeBlock(Player player, Material material, Location location) 
	{
	    if (material == null || location == null) 
	    {
	    	return;
	    }

	    player.sendBlockChange(location, material.createBlockData());
	}
	
	private final static int CENTER_PX = 154;
	public static void SendCenteredMessage(Player player, String message)
	{
	        if(message == null || message.equals("")) player.sendMessage("");
	                message = ChatColor.translateAlternateColorCodes('&', message);
	 
	                int messagePxSize = 0;
	                boolean previousCode = false;
	                boolean isBold = false;
	 
	                for(char c : message.toCharArray())
	                {
	                        if(c == '§'){
	                                previousCode = true;
	                                continue;
	                        }else if(previousCode == true){
	                                previousCode = false;
	                                if(c == 'l' || c == 'L'){
	                                        isBold = true;
	                                        continue;
	                                }else isBold = false;
	                        }else{
	                        		DEFAULT_FONT_INFO dFI = DEFAULT_FONT_INFO.GetDefaultFontInfo(c);
	                                messagePxSize += isBold ? dFI.GetBoldLength() : dFI.GetLength();
	                                messagePxSize++;
	                        }
	                }
	 
	                int halvedMessageSize = messagePxSize / 2;
	                int toCompensate = CENTER_PX - halvedMessageSize;
	                int spaceLength = DEFAULT_FONT_INFO.SPACE.GetLength() + 1;
	                int compensated = 0;
	                StringBuilder sb = new StringBuilder();
	                while(compensated < toCompensate){
	                        sb.append(" ");
	                        compensated += spaceLength;
	                }
	                player.sendMessage(sb.toString() + message);
	        }
	
	
//	public static LinkedList<Block> CreateSphere(Location center, int radius) 
//	{
//		LinkedList<Block> positions = new LinkedList<>();
//	    int radiusSquared = radius * radius;
//
//	    for (int x = -radius; x <= radius; x++) {
//	        for (int y = -radius; y <= radius; y++) {
//	            for (int z = -radius; z <= radius; z++) {
//	                if (x * x + y * y + z * z <= radiusSquared) {
//	                    Location loc = center.clone().add(x, y, z);
//
//	                    positions.add(loc.getBlock());
//	                }
//	            }
//	        }
//	    }
//	    
//	    return positions;
//	}
	
}
