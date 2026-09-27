package me.bullterrier292.WorldRestore;

import java.util.ArrayList;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public class ChunkINFO {
	WorldRestore _p;
	
	String orginalWorld="world";
	String worldWhereToCopy="dworld";
	
	Chunk _chunkID;
	int _minY=300;
	int _maxY=-1;
	long _timeStamp;
	ArrayList<Player> whoHasVisited=new ArrayList<Player>();
	
	public long get_timeStamp() {
		return _timeStamp;
	}

	public void set_timeStamp(long _timeStamp) {
		this._timeStamp = _timeStamp;
	}
	
	
	public ChunkINFO(WorldRestore plugin)
	{
		_p=plugin;
	}
	
	public Chunk get_chunkID() 
	{
		return _chunkID;
	}

	public void set_chunkID(Chunk _chunkID) 
	{
		this._chunkID = _chunkID;
	}

	public int get_minY() 
	{
		return _minY;
	}

	public void set_minY(int _minY) 
	{
		this._minY = _minY;
	}

	public int get_maxY() 
	{
		return _maxY;
	}

	public void set_maxY(int _maxY) 
	{
		this._maxY = _maxY;
	}

	public void addVisited(Player player)
	{
		whoHasVisited.add(player);
	}

	public void setMinMaxY(int y)
	{
		
		if(y < _minY)
		{
			_minY=y;			
		}
		
			
		if(y > _maxY) 
		{
			_maxY=y;			
		}
			
	}

	public void fixChunk()
	{
		Location firstBlockLoc=_chunkID.getBlock(0, 0, 0).getLocation();
		int x,y,z;
		x=firstBlockLoc.getBlockX();
		y=_minY;
		z=firstBlockLoc.getBlockZ();
		
		World world=_p.getServer().getWorld(orginalWorld);
		World dworld=_p.getServer().getWorld(worldWhereToCopy);
		
		Block block,dblock;
		ArrayList<Block> inventories=new ArrayList<>();
		// GOING THROUGH CHUNK x y z
		for(int i=0; i < 16; i++)
		{
			for(int j=0; j < 16;j++)
			{
				for(int k=y; k < _maxY+1; k++)
				{
					block=world.getBlockAt(x, y, z);
					dblock=dworld.getBlockAt(x,y,z);
					Material dType=dblock.getType();
					
					// if there is chest, or any other inventory put to the array and put their invs laiter to them after loops.. and clear them and place with air to remove item drop
					if(dblock.getState() instanceof InventoryHolder)
					{
						if(block.getType()==dType) 
						{
							inventories.add(dblock);
							InventoryHolder inv=(InventoryHolder)block.getState();
							inv.getInventory().clear();
							block.setType(Material.AIR);
						}
						
					}
					
					//replace block from other world if there is difference
					if(block.getType() != dType)
					{
						//get main blocks front so it will speed up process
						if(dType == Material.STONE || dType == Material.DIRT || dType == Material.GRAVEL || dType == Material.SAND || dType== Material.COBBLESTONE)
						{
							block.setType(dType);
						}
						else 
						{
							// double blocks like doors need to be set to no physics so it can be placed
							if(dType.toString().contains("GRASS") || dType.toString().contains("DOOR") || dType.toString().contains("FLOWER") || dType.toString().contains("BUSH") | dType.toString().contains("BANNER"))
							{
								block.setType(dType,false);
								
							}
							else
							{
								block.setType(dType);
							}	
							
							block.setBlockData(dblock.getBlockData());
							
													
						}				
					}
										
				y++;
				}			
			z++;
			y=firstBlockLoc.getBlockY();
			}		
		x++;
		z=firstBlockLoc.getBlockZ();
		}
		
		//going through inv blocks and set invs in
		if(!inventories.isEmpty())
		{
			InventoryHolder dInv;
			InventoryHolder inv;
			Location loc;
			for(Block b:inventories)
			{
				loc=b.getLocation();
				dInv=(InventoryHolder)b.getState();
				inv=(InventoryHolder)world.getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).getState();
				inv.getInventory().setContents(dInv.getInventory().getContents());
			}
			
		}
		
		
		
	}

	
	

}
