package imu.WorldRestore.Other;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import imu.WorldRestore.Managers.ChunkManager;
import imu.WorldRestore.main.Main;

public class ChunkFileHandler 
{
	String _fileName = "";
	String _fixFileName = "";
	String _cd = "x";
	
	int _chunkLifeTime = 10;
	int _chunkFileCheckDelay = 10;
	
	
	Main _main = null;
	ChunkManager _cManager = null;
	Cooldowns _cds = null;

	public ChunkFileHandler(Main main, int chunklifetime, int chunkCheckFileDelay, boolean enableAutoFileReader) 
	{
		_main = main;
		_cManager = main.getChunkManager();
		_fileName = _cManager.getChunksYAML();
		_fixFileName = _cManager.getChunksFixYAML();
		_cds = new Cooldowns();
		
		_chunkLifeTime = chunklifetime;
		_chunkFileCheckDelay = chunkCheckFileDelay;

		if(enableAutoFileReader)
		{
			chunksFileReader();
		}
		
	}

	
	public void setChunkFileCheckDelay(int seconds)
	{
		_chunkFileCheckDelay = seconds;
	}
	public void setChunkLifeTime(int seconds)
	{
		_chunkLifeTime = seconds;
	}
	
	public void clearChunksFile()
	{
		ConfigMaker cm = new ConfigMaker(_main, _fileName);
		cm.clearConfig();
	}
	
	public void saveCard(ChunkCard card)
	{
		ConfigMaker cm = new ConfigMaker(_main, _fileName);
		FileConfiguration config = cm.getConfig();
		config.set(card.getId(), compileChunkCard(card));				
		cm.saveConfig();
	}
	
	public void saveCardList(Collection<ChunkCard> cards)
	{
		ConfigMaker cm = new ConfigMaker(_main, _fileName);
		FileConfiguration config = cm.getConfig();
		for(ChunkCard card : cards)
		{
			config.set(card.getId(), compileChunkCard(card));
		}						
		cm.saveConfig();		
	}
	public void saveCardList(HashMap<Chunk, ChunkCard> cards)
	{ 
		saveCardList(cards.values());
	}
	
	public String compileChunkCard(ChunkCard cCard)
	{
		String sep = ";";
		
		String layerStr = "";
		for(Integer i : cCard.getLayers().values())
		{
			layerStr += ":"+String.valueOf(i);
		}
		if(layerStr.charAt(0) == ':')
		{
			layerStr = layerStr.substring(1);
		}
		
		String str = cCard.getId() +sep+ 
				cCard.getWorld().getName() +sep+ 
				cCard.get_targetWorldName() +sep+
				cCard.get_minY() +sep+
				cCard.get_maxY() +sep+
				cCard.get_timeStamp()+sep+
				layerStr;
		
		return str;
	}
	
	public ChunkCard decompileString(String str)
	{
		String[] parts = str.split(";");
		String[] id = parts[0].split(":");
		Chunk chunk = _main.getServer().getWorld(parts[1]).getChunkAt(Integer.parseInt(id[0]), Integer.parseInt(id[1]));
		String targetWorld = parts[2];
		int minY = Integer.parseInt(parts[3]);
		int maxY = Integer.parseInt(parts[4]);
		long stamp = Long.parseLong(parts[5]);
		
		String layerStr = parts[6];
		HashMap<Integer, Integer> layers = new HashMap<>();
		int l;
		if(layerStr.contains(":"))
		{
			for(String layer : layerStr.split(":"))
			{
				l = Integer.parseInt(layer);
				layers.put(l, l);
			}
		}else
		{
			l = Integer.parseInt(layerStr);
			layers.put(l, l);
			
		}
		
		ChunkCard card = new ChunkCard(_main.getChunkManager(), chunk, targetWorld, minY, maxY, stamp,layers);
		return card;
	}
	
	void readChunkFile(int secondsExpireTime)
	{
		_cManager.saveAllChunks();
		
		ConfigMaker cm = new ConfigMaker(_main,_fileName);
		FileConfiguration config = cm.getConfig();
		
		
		ConfigMaker cm2 = new ConfigMaker(_main, _fixFileName);
		FileConfiguration config2 = cm2.getConfig();
		
		for (String key : config.getConfigurationSection("").getKeys(false)) 
		{
			String value = config.getString(key);
			ChunkCard card = decompileString(value);
			if(_cds.isTimePastThis(card._timeStamp, secondsExpireTime))
			{
				//_cManager.removeChunkCardFromChunks(card);
				config2.set(key, value);
				config.set(key, null);
				cm2.saveConfig();			
			}		
		}
		cm.saveConfig();			

	}
	public void setAllChunksToFix()
	{
		readChunkFile(-1);
	}
	ArrayList<ChunkCard> getBeingRepairedChunks(int size)
	{
		ArrayList<ChunkCard> chunks = new ArrayList<ChunkCard>();
	
		ConfigMaker cm = new ConfigMaker(_main, _fixFileName);
		FileConfiguration config = cm.getConfig();
		int count = 0;
		for (String key : config.getConfigurationSection("").getKeys(false)) 
		{
			ChunkCard card = decompileString(config.getString(key));
			ChunkCard checkCard = _cManager.getChunkCard(card);
			if(checkCard != null)
			{
				saveCard(checkCard);
			}
			else 
			{
				chunks.add(card);
				config.set(key, null);
				
				count++;
			}
						
			if(count == size)
			{
				break;
			}
		}
		cm.saveConfig();

		
		return chunks;
	}
	
	void chunksFileReader()
	{		
		new BukkitRunnable() 
		{			
			@Override
			public void run() 
			{		
				readChunkFile(_chunkLifeTime);
			}
		}.runTaskTimer(_main, 9, 20 * _chunkFileCheckDelay);
	}
}
