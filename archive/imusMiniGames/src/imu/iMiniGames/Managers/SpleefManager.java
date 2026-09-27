package imu.iMiniGames.Managers;

import java.io.File;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iAPI.Other.ConfigMaker;
import imu.iMiniGames.Arenas.Arena;
import imu.iMiniGames.Arenas.SpleefArena;
import imu.iMiniGames.Main.ImusMiniGames;

public class SpleefManager extends GameManager
{
	public SpleefManager(ImusMiniGames main) 
	{
		super(main, "Spleef");
		_main = main;
		addPotionEffects();
	}
	
	void addPotionEffects()
	{
		for(PotionEffectType t : PotionEffectType.values())
		{
			_potionEffects_positive_enabled.put(t, true);
		}
		_potionEffects_positive_enabled.put(PotionEffectType.LUCK, false);
		_potionEffects_positive_enabled.put(PotionEffectType.LEVITATION,false);
		_potionEffects_positive_enabled.put(PotionEffectType.FAST_DIGGING,false);
		_potionEffects_positive_enabled.put(PotionEffectType.HEAL,false);
		_potionEffects_positive_enabled.put(PotionEffectType.ABSORPTION,false);
		_potionEffects_positive_enabled.put(PotionEffectType.UNLUCK,false);
		_potionEffects_positive_enabled.put(PotionEffectType.CONDUIT_POWER,false);
		_potionEffects_positive_enabled.put(PotionEffectType.DOLPHINS_GRACE,false);
		_potionEffects_positive_enabled.put(PotionEffectType.BAD_OMEN,false);
		_potionEffects_positive_enabled.put(PotionEffectType.HERO_OF_THE_VILLAGE,false);
		_potionEffects_positive_enabled.put(PotionEffectType.HARM,false);
		_potionEffects_positive_enabled.put(PotionEffectType.SATURATION,false);
		_potionEffects_positive_enabled.put(PotionEffectType.HUNGER,false);
		_potionEffects_positive_enabled.put(PotionEffectType.WATER_BREATHING,false);
	}
	
	public boolean setupPlatformCorners(Player p, Location loc)
	{		
		return false;
	}
	

	@Override
	public void onEnabled() {
		loadArenas();
	}

	@Override
	public void onDisabled() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveArena(Arena arena) 
	{
		SpleefArena sArena = (SpleefArena) arena;
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				ConfigMaker cm = new ConfigMaker(_main, _text_arena_yml+"/"+sArena.get_name().toLowerCase()+".yml");
				FileConfiguration config = cm.getConfig();
				
				config.set("Name", sArena.get_name().toString());
				config.set("Desc", sArena.get_description());
				config.set("MaxPlayers",sArena.get_maxPlayers());
				config.set("CornerLoc1", sArena.getPlatformCorner(0));
				config.set("CornerLoc2", sArena.getPlatformCorner(1));
				config.set("LobbyLoc", sArena.get_spectator_lobby());
				
				for(int i = 0; i < sArena.getTotalSpawnPositions(); ++i)
				{
					config.set("spawn_pos"+i, arena.getSpawnpointLoc(i));
				}
				
				cm.saveConfig();
			}
		}.runTaskAsynchronously(_main);
		
	}

	@Override
	public void loadArenas() 
	{
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				try 
				{
					for(File file : new File(_main.getDataFolder().getAbsoluteFile()+File.separator + _text_arena_yml).listFiles())
					{
						if(!file.exists())
							continue;
						
						FileConfiguration config = YamlConfiguration.loadConfiguration(file);
						
						
						String name = config.getString("Name");
						String desc = config.getString("Desc");
						Integer max_p = config.getInt("MaxPlayers");
						Location loc1 = config.getLocation("CornerLoc1");
						Location loc2 = config.getLocation("CornerLoc2");
						Location lobby_loc = config.getLocation("LobbyLoc");
						
						SpleefArena arena = new SpleefArena(name);
						arena.set_maxPlayers(max_p);
						arena.setPlatformCorner(0, loc1);
						arena.setPlatformCorner(1, loc2);
						arena.set_spectator_lobby(lobby_loc);
						arena.set_description(desc);
						
						for(int i = 0; i < max_p; ++i)
						{
							arena.addSpawnPosition(config.getLocation("spawn_pos"+i));
						}
						
						addArena(arena);
						System.out.println("Arena added: "+name);
					}
				} 
				catch (Exception e) 
				{
					System.out.println("imusMiniGames: Didn't find any created spleef arenas");
				}
				
			}
		}.runTaskAsynchronously(_main);
		
	}
}
		