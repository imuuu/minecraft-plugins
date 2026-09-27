package imu.WorldRestore.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import imu.WorldRestore.Other.ChunkCard;
import imu.WorldRestore.Other.ChunkFileHandler;
import imu.WorldRestore.Other.ChunkHandler;
import imu.WorldRestore.main.Main;

public class ChunkManager 
{
	Main _main = null;
	
	ChunkHandler _chunkHandler = null;
	ChunkFileHandler _chunkFileHandler = null;
	
	String _worldName = "world";
	String _defTargetWorld = "dw";
	
	String _chunkYAML = "chunks.yml";
	String _chunkFixYAML = "chunks_being_fixed.yml";
	
	int _maxSizeChunkHash = 100;
	
	HashMap<Chunk, ChunkCard> _chunks = new HashMap<Chunk, ChunkCard>();
	
	HashMap<UUID, Location> _player_locs = new HashMap<UUID, Location>();
	boolean _chunksInUse = false;
	
	
	public ChunkManager(Main main, String playWorld, String cloneWorld,int maxChunkHashSize) 
	{
		_main = main;			
		_maxSizeChunkHash = maxChunkHashSize;
		_worldName = playWorld;
		_defTargetWorld = cloneWorld;
	}
	
	public void SetupHandlers()
	{
		_chunkHandler = _main.getChunkHandler();
		_chunkFileHandler = _main.getChunkFileHandler();
	}
	public Main getMain()
	{
		return _main;
	}
		
	public String getChunksYAML()
	{
		return _chunkYAML;
	}
	public String getChunksFixYAML()
	{
		return _chunkFixYAML;
	}
	
	public void setMaxChunkSize(int size)
	{
		_maxSizeChunkHash = size;
	}
	
	public void setChunk(Chunk chunk, String targetWorldName, int y)
	{
		ChunkCard cCard = _chunks.get(chunk);
		if(cCard != null)
		{
			cCard.Refresh(y);
		}else
		{
			cCard = new ChunkCard(this, chunk, targetWorldName, y);
			newChunk(cCard);
		}
		
	}
	public void setChunk(Block block)
	{
		
		setChunk(block.getChunk(), getDefTargetWorldName(), block.getLocation().getBlockY());
	}
	public void setChunkAuto(Block block,int yMinDelta,int yMaxDelta)
	{
		Chunk chunk = block.getChunk();
		Location loc = block.getLocation();
		ChunkCard card = _chunks.get(chunk);
		int min = loc.getBlockY()+yMinDelta;
		int max = loc.getBlockY()+yMaxDelta;
		if(card != null)
		{
			card.RefreshAndLayers(min, max);
		}else
		{
			card = new ChunkCard(this, chunk, getDefTargetWorldName(), min, max, System.currentTimeMillis(),true);
			newChunk(card);
		}
		
	}
	public void setChunk(Player player, String targetWorldName, int y)
	{
		setChunk(player.getWorld().getChunkAt(player.getLocation()), targetWorldName, y);
	}
	
	public String createChunkID(Chunk chunk)
	{
		return String.valueOf(chunk.getX())+":"+String.valueOf(chunk.getZ());
	}
	
	public String getDefTargetWorldName()
	{
		return _defTargetWorld;
	}
	
	public String getWorldName()
	{
		return _worldName;
	}
	public void newChunk(ChunkCard cCard)
	{
		checkHashSize();
		_chunks.put(cCard.getChunk(), cCard);
		_chunkFileHandler.saveCard(cCard);
	}
	
	public void removeChunkCardFromChunks(ChunkCard card)
	{
		_chunks.remove(card.getChunk());
	}
	public boolean isChunksInHash(ChunkCard card)
	{
		if(_chunks.containsKey(card.getChunk()))
		{
			return true;
		}
		return false;
	}
	public ChunkCard getChunkCard(ChunkCard card)
	{
		return _chunks.get(card.getChunk());
	}
	void checkHashSize()
	{
		if(_chunks.size() > _maxSizeChunkHash)
		{
			saveAllChunks();
		}
	}
	
	public void fixChunkLayers(ChunkCard card, boolean checkProtection)
	{
		_chunkHandler.fixChunk(card,true,checkProtection);
	}
	public void fixChunks(ArrayList<ChunkCard> cards, boolean goLayers, boolean checkProtection)
	{
		_chunkHandler.fixChunks(cards, goLayers, checkProtection);
	}
	

	public void saveAllChunks()
	{		
		_chunkFileHandler.saveCardList(_chunks);
		_chunks.clear();			
	}
	
	public boolean isLocationInRegion(Location loc)
	{
		RegionContainer con = _main.getWorldGuard().getPlatform().getRegionContainer();
		RegionQuery quarry = con.createQuery();
		ApplicableRegionSet set = quarry.getApplicableRegions(BukkitAdapter.adapt(loc));		
		return set.getRegions().size() > 0;
	}

	
	
	
	
}
