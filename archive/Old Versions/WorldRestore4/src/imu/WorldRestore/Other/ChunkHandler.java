package imu.WorldRestore.Other;

import java.util.ArrayList;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import imu.WorldRestore.main.Main;
import imu.iAPI.Main.ImusAPI;

public class ChunkHandler 
{
	Main _main = null;
	WorldGuard _wg = null;
	ItemMetods itemM = null;	
	ChunkFileHandler _chunkFileHandler = null;
	
	int _auto_chunkFixDelay = 10; // how often auto
	int _auto_chunkAmount = 2;
	
	double _chunkFixDelay = 0.5;
	
	BukkitTask _chunkfixer = null;
	RegionQuery _quarry = null;
	public ChunkHandler(Main main, int autoFixDelay, int autoAmount, double chunkfixDelay) 
	{
		_main = main;
		_wg = _main.getWorldGuard();
		itemM = _main.getItemM();
		
		_auto_chunkFixDelay = autoFixDelay;
		_auto_chunkAmount = autoAmount;
		_chunkFixDelay = chunkfixDelay;
		
		autoChunkFixer();
	}
	void setupWGquarry()
	{
		_quarry =  _wg.getPlatform().getRegionContainer().createQuery();
	}
	public void SetupHanddlers()
	{
		_chunkFileHandler = _main.getChunkFileHandler();
	}
	
	public void set_auto_chunkFixDelay(int _auto_chunkFixDelay) 
	{
		this._auto_chunkFixDelay = _auto_chunkFixDelay;
	}

	public void set_auto_chunkAmount(int _auto_chunkAmount) 
	{
		this._auto_chunkAmount = _auto_chunkAmount;
	}

	public void set_chunkFixDelay(double _chunkFixDelay) 
	{
		this._chunkFixDelay = _chunkFixDelay;
	}

	public void fixChunk(ChunkCard card, boolean goLayers, boolean checkProtection)
	{
		Chunk chunk = card.getChunk();
		
		int x = chunk.getBlock(0, 0, 0).getLocation().getBlockX();
		int y = card.get_minY();
		int z = chunk.getBlock(0, 0, 0).getLocation().getBlockZ();

		int dy = y;
		int dz = z;
		
		World world = card.getWorld();
		World targetWorld = card.getTargetWorld();
		
		Block block, dblock;
		
		setupWGquarry();
		if(_main.getPlayerData().hasPlayersInThisChunk(chunk))
		{
			_main.getPlayerData().setPlayersTaggedInChunk(chunk);
		}
		
		for(int i=0; i < 16; i++)
		{
			for(int j=0; j < 16;j++)
			{
				if(goLayers)
				{
					for(int k : card.getLayers().keySet())
					{
						block = world.getBlockAt(x, k, z);
						dblock = targetWorld.getBlockAt(x,k,z);
						blockChangeOperation(block, dblock, checkProtection);
					}
				}
				else
				{
					for(int k=y; k < card.get_maxY()+1; k++)
					{					
						block = world.getBlockAt(x, y, z);
						dblock = targetWorld.getBlockAt(x,y,z);
						blockChangeOperation(block, dblock, checkProtection);
						
					y++;
					}			
				}
				
			z++;
			y=dy;
			}		
		x++;
		z=dz;
		}
		
	}
	void blockChangeOperation(Block block, Block dblock, boolean checkProtection)
	{
		Material dType = dblock.getType();
		//replace block from other world if there is difference			
		BlockState bState = block.getState();
		if((block.getType() != dType || bState != dblock.getState()))
		{
			if(!checkProtection || !isBlockProtectedV2(block))
			{
				//get main blocks front so it will speed up process
				if(dType == Material.STONE || dType == Material.DIRT || dType == Material.AIR || dType == Material.GRAVEL || dType == Material.SAND || dType== Material.COBBLESTONE)
				{
					
					if(bState instanceof InventoryHolder)
					{
						InventoryHolder holder = (InventoryHolder) bState;
						holder.getInventory().clear();
					}
					
					block.setType(dType);
					
				}
				else 
				{
					//itemM.copyBlock(dblock, block);
					ImusAPI._metods.copyBlock(dblock, block);
				}
			}			
		}

	}	
	// ONLY ENTER HERE AFTER QUARY HAS BEEN SETUP! void setupWGquary!
	boolean isBlockProtectedV2(Block block)
	{
		ApplicableRegionSet set = _quarry.getApplicableRegions(BukkitAdapter.adapt(block.getLocation()));
		return set.getRegions().size() > 0;
	}
	public void fixChunk(ChunkCard card, int minY, int maxY, boolean checkProtection)
	{
		card.set_minY(minY);
		card.set_maxY(maxY);
		fixChunk(card,false, checkProtection);
	}
	
	public void fixChunks(ArrayList<ChunkCard> cards, boolean goLayers, boolean checkProtection)
	{
		if(cards.isEmpty())
		{
			return;
		}
		new BukkitRunnable() 
		{
			
			@Override
			public void run() 
			{

				fixChunk(cards.get(0), goLayers, checkProtection);
				
				if(!cards.isEmpty())
				{
					cards.remove(0);
				}

				if(cards.isEmpty())
				{
					cancel();
				}
				
				
			}
		}.runTaskTimer(_main, 0, (int)(20 *_chunkFixDelay));
	}
	
	void autoChunkFixer()
	{
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				if(_chunkFileHandler == null)
				{
					System.out.println("cfh is null in chunkhandler");
					return;
				}
				//ItemMetods itemM = new ItemMetods(_main);
				//long start = System.currentTimeMillis();
				ArrayList<ChunkCard> cards = _chunkFileHandler.getBeingRepairedChunks(_auto_chunkAmount);
				//long elapsedTime = System.currentTimeMillis()-start;
				//System.out.println("autoChunkFixer: TIME WAS: "+elapsedTime);	

				fixChunks(cards,true,true);
				
			}
		}.runTaskTimer(_main, 10, 20*_auto_chunkFixDelay);
	}
	
	
	//void do_not_use_fixChunkExperiment(ChunkCard card)
	//{
	//	
	//	Chunk fromBukkitChunk = card.getTargetWorld().getChunkAt(card.getChunk().getX(), card.getChunk().getZ());
	//	Chunk toBukkitChunk = card.getChunk();
	//	fromBukkitChunk.load();
	//	toBukkitChunk.load();
	//	
	//	
	//	CraftChunk fromCC = (CraftChunk) fromBukkitChunk;
	//	CraftChunk toCC = (CraftChunk) toBukkitChunk;
	//	net.minecraft.server.v1_15_R1.Chunk fromChunk = fromCC.getHandle();
	//	ChunkSection[] sec = fromChunk.getSections().clone();
	//	net.minecraft.server.v1_15_R1.Chunk toChunk = toCC.getHandle();
	//	//ChunkSection[] x = toChunk.getSections();
	//	//x = sec.clone();
	//	
	//}
	
}
