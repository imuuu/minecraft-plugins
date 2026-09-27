package me.bullterrier292.WorldRestore;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import Commands.WRCommands;
import Events.WREventClass;

public class WorldRestore extends JavaPlugin implements Listener 
{
	private WRCommands commands=new WRCommands(this);
	public HashMap<Chunk, ChunkINFO> chunkInfos=new HashMap<>();
	public ArrayList<ChunkINFO> experiedChunks=new ArrayList<>();
	private int chunkExpiredTime=10; // in seconds
	
	@Override
	public void onEnable() 
	{
		
		
		getCommand(commands.cmd1).setExecutor(commands);
		getCommand(commands.cmd2).setExecutor(commands);
		
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN +" WorldRestore has been activated!");
		getServer().getPluginManager().registerEvents(new WREventClass(this), this);
		checkChunkTimes();
	}
	
	public void checkChunkTimes() {
		new BukkitRunnable() {
			
			@Override
			public void run() {				
				ArrayList<Chunk> removedChunks=new ArrayList<>();
				if(!chunkInfos.isEmpty())
				{
					for(Entry<Chunk, ChunkINFO> entry : chunkInfos.entrySet())
					{
						Chunk c=entry.getKey();
						ChunkINFO info=entry.getValue();
						
						long infoTime=info.get_timeStamp();
						long period=System.currentTimeMillis()-infoTime;
						if(period > 1000*chunkExpiredTime)
						{
							experiedChunks.add(info);
							removedChunks.add(c);
							
						}
					}
				}
				
				for(Chunk c: removedChunks)
				{
					chunkInfos.remove(c);
				}
				
			}
		}.runTaskTimer(this, 0, 20*10);
	}
	public void runExpiredChunks() {
		new BukkitRunnable() {
			int i=0;
			
			
			@Override
			public void run() {
				int howMany=2;
				int counter=0;
				if(!experiedChunks.isEmpty()) 
				{
					for(; i <experiedChunks.size(); i++)
					{
						ChunkINFO info=experiedChunks.get(i);
						info.fixChunk();
						if(counter < howMany)
							break;
						counter++;
					}
					
				}
				
			}
		}.runTaskTimer(this, 0, 20*3);
	}

}
