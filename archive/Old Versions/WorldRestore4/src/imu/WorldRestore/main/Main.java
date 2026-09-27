package imu.WorldRestore.main;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.WorldGuard;

import imu.WorldRestore.Commands.WorldRestoreCmd;
import imu.WorldRestore.Events.ChunkVisitinEvents;
import imu.WorldRestore.Handlers.CommandHandler;
import imu.WorldRestore.Managers.ChunkManager;
import imu.WorldRestore.Other.ChunkFileHandler;
import imu.WorldRestore.Other.ChunkHandler;
import imu.WorldRestore.Other.ConfigMaker;
import imu.WorldRestore.Other.ItemMetods;
import imu.WorldRestore.Other.PlayerData;
import imu.WorldRestore.SubCommands.subWrFixAll;
import imu.WorldRestore.SubCommands.subWrReloadConfigCmd;
import imu.WorldRestore.SubCommands.subTeleportCmd;
import imu.WorldRestore.SubCommands.subWrAreaCmd;
import imu.WorldRestore.SubCommands.subWrClearChunkDataCmd;

public class Main extends JavaPlugin
{
	final Main main = this;
	WorldGuard wg = null;
	ItemMetods itemM = null;
	
	ChunkManager _chunkManager = null;
	ChunkHandler _chunkHandler = null;
	ChunkFileHandler _chunkFileHandler = null;
	PlayerData _playerData = null;

	String _playWorld = "world"; //season 			//world
	String _cloneWorld = "clone_world"; //default_world		//dw
	
	boolean _enable_autoChunkUpdate = false;
	int _maxSavedChunkCardsInMemory = 100;
	int _auto_chunkFixDelay = 30; // how often checks config for fixed chunks
	int _auto_chunkAmount = 10; // how many chunks it takes from config
	
	double _chunkFixDelay = 0.5; // how fast fixes chunks 
	int _chunkLifeTime= 60*60*24*3; //how many seconds chunk can live before fix
	int _chunkFileCheckDelay = 60*10; //How often chunkDatafile will be checked
	
	public void registerCommands() 
    {  	
        CommandHandler handler = new CommandHandler();

        String cmd1="wr";
        handler.registerCmd(cmd1, new WorldRestoreCmd(this));  
        handler.setPermissionOnLastCmd("wr");
        handler.registerSubCmd(cmd1, "area", new subWrAreaCmd(this));
        handler.registerSubCmd(cmd1, "fix all", new subWrFixAll(this));
        handler.registerSubCmd(cmd1, "tp", new subTeleportCmd(this));
        handler.registerSubCmd(cmd1, "reload", new subWrReloadConfigCmd(this));
        handler.registerSubCmd(cmd1, "remove chunks data", new subWrClearChunkDataCmd(this));
       
        
        getCommand(cmd1).setExecutor(handler);
        
    }
	
	@Override
	public void onEnable() 
	{		
		ConfigsSetup();

		wg = WorldGuard.getInstance();
		itemM = new ItemMetods(this);
		_chunkManager = new ChunkManager(this, _playWorld,_cloneWorld,_maxSavedChunkCardsInMemory);
		_chunkHandler = new ChunkHandler(this, _auto_chunkFixDelay, _auto_chunkAmount,_chunkFixDelay);
		_chunkFileHandler = new ChunkFileHandler(this, _chunkLifeTime, _chunkFileCheckDelay, _enable_autoChunkUpdate);
		_chunkManager.SetupHandlers();
		_chunkHandler.SetupHanddlers();
		_playerData = new PlayerData(this);
		
		
		registerCommands();
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN +" World Restore Active!");
		getServer().getPluginManager().registerEvents(new ChunkVisitinEvents(this), this);

	}
		
	@Override
	public void onDisable()
	{
		getChunkManager().saveAllChunks();
	}
	public Main getInstance()
	{
		return main;
	}
	public WorldGuard getWorldGuard()
	{
		return wg;
	}
		
	void ConfigsSetup()
	{
		makeSettingsConfig();
	}
	
	public ChunkManager getChunkManager()
	{
		return _chunkManager;
	}
	
	public ChunkFileHandler getChunkFileHandler()
	{
		return _chunkFileHandler;
	}
	public ChunkHandler getChunkHandler()
	{
		return _chunkHandler;
	}
	
	public ItemMetods getItemM() 
	{
		return itemM;
	}
	
	public PlayerData getPlayerData()
	{
		return _playerData;
	}
	
	void setupValues()
	{
		_chunkManager.setMaxChunkSize(_maxSavedChunkCardsInMemory);
		_chunkHandler.set_auto_chunkFixDelay(_auto_chunkFixDelay);
		_chunkHandler.set_auto_chunkAmount(_auto_chunkAmount);
		_chunkHandler.set_chunkFixDelay(_chunkFixDelay);
		_chunkFileHandler.setChunkFileCheckDelay(_chunkFileCheckDelay);
		_chunkFileHandler.setChunkLifeTime(_chunkLifeTime);
	}
	
	public void UpdateSettingConfig()
	{
		makeSettingsConfig();
		setupValues();
	}
	void makeSettingsConfig()
	{
		ConfigMaker cm = new ConfigMaker(this, "settings.yml");

		try 
		{	_playWorld = cm.addDefault("PlayWorldName", _playWorld, "World where player play and what will be fixed.. Changing this requires restart.. remember after changing this clear chunk.yml and chunk_being_fixed.yml");
			_cloneWorld = cm.addDefault("CloneWorldName", _cloneWorld, "Where people not play and where everything is copied to playworld");
			_maxSavedChunkCardsInMemory = cm.addDefault("MaxSavedChunkDataInMemory", _maxSavedChunkCardsInMemory, "How many chunksData classes it keeps in memory before saving them to file");
			_enable_autoChunkUpdate = cm.addDefault("AutoChunkUpdateEnabled", _enable_autoChunkUpdate , "Fixing automaticly chunks after expire time if enabled. Changing this requires restart");
			_auto_chunkFixDelay = cm.addDefault("Automatic_FixedChunksFileReadDelay", _auto_chunkFixDelay, "How often it checks fixed chunks file and put chunks to fix");
			_auto_chunkAmount = cm.addDefault("Automatic_FixedChunksAmount", _auto_chunkAmount, "When fixedChunksfile is read this is amount how many chunks it takes to update");
			_chunkFixDelay = cm.addDefault("ChunkFixDelay", _chunkFixDelay, "How often it fixes one chunk in seconds");
			_chunkLifeTime = cm.addDefault("ChunksLifeTime", _chunkLifeTime, "How many seconds chunk is life before updated to default. milliseconds");
			_chunkFileCheckDelay = cm.addDefault("ChunkFileCheckDelay", _chunkFileCheckDelay, "How often all chunk data are loopped though and put to fixed file if time is expired. Seconds");
			cm.addComments();
			
		} catch (Exception e) 
		{
			getServer().getConsoleSender().sendMessage(ChatColor.RED +"WARNING: Something got wrong World Restore fileNamed: "+cm.getFileName());
			getServer().getConsoleSender().sendMessage(ChatColor.RED +"WARNING: Maybe you casted some value as Integer When it should be Double?");
		}
		
		
	}
}
