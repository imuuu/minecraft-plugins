package imu.iMiniGames.Managers;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iAPI.Other.ConfigMaker;
import imu.iMiniGames.Arenas.Arena;
import imu.iMiniGames.Arenas.CombatArena;
import imu.iMiniGames.Leaderbords.CombatLeaderBoard;
import imu.iMiniGames.Main.ImusMiniGames;
import imu.iMiniGames.Other.ArenaKit;

public class CombatManager extends GameManager
{
	ArrayList<ArenaKit> arena_kits = new ArrayList<>();
		
	String text_kits_yml="Combat/Kits";

	CombatLeaderBoard _leaderboard;
	
	public CombatManager(ImusMiniGames main) 
	{
		super(main, "Combat");
		addPotionEffects();
		_leaderboard = new CombatLeaderBoard(main, "CombatLeaderBoards");
		
	}
	@Override
	public void onEnabled() 
	{
		loadArenas();
		loadKits();
		
		new BukkitRunnable() 
		{			
			@Override
			public void run() 
			{
				_leaderboard.loadFromFile();
			}
		}.runTaskAsynchronously(_main);
		
	}

	@Override
	public void onDisabled() 
	{
		_leaderboard.saveToFile();
	}
		
	public CombatLeaderBoard getLeaderBoard()
	{
		return _leaderboard;
	}
	
	
	public ArrayList<ArenaKit> getArena_kits() {
		return arena_kits;
	}
	
	public ArenaKit getKit(String name)
	{
		for(int i = 0 ; i < arena_kits.size(); ++i)
		{
			if(arena_kits.get(i).get_kitName().equalsIgnoreCase(name))
			{
				return arena_kits.get(i);
			}
		}
		return null;
	}
		
	
	public void addKit(String name, ItemStack[] _kitInv)
	{
		ArenaKit kit = new ArenaKit(name, _kitInv);
		for(int i = 0 ; i < arena_kits.size(); ++i)
		{
			if(arena_kits.get(i).get_kitName().equalsIgnoreCase(name))
			{
				arena_kits.set(i, kit);
				saveKit(kit);
				return;
			}
				
		}
		
		arena_kits.add(kit);
		saveKit(kit);
	}
	void saveKit(ArenaKit kit)
	{
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				ConfigMaker cm = new ConfigMaker(_main, text_kits_yml+"/kit_"+kit.get_kitName()+".yml");
				FileConfiguration config = cm.getConfig();
				config.set("Kit_Name", kit.get_kitName());
				
				ItemStack[] content = kit.get_kitInv();
				for(int i = 0; i <content.length; ++i)
				{
					ItemStack s = content[i];
					if(s != null)
					{
						config.set("KitContent."+i, s );
					}else
					{
						config.set("KitContent."+i, "null");
					}
				}
				cm.saveConfig();
			}
		}.runTaskAsynchronously(_main);
	}
		
	void addPotionEffects()
	{
		for(PotionEffectType t : PotionEffectType.values())
		{
			_potionEffects_positive_enabled.put(t, true);
		}
		_potionEffects_positive_enabled.put(PotionEffectType.LUCK, false);
		_potionEffects_positive_enabled.put(PotionEffectType.LEVITATION,false);
		_potionEffects_positive_enabled.put(PotionEffectType.UNLUCK,false);
		_potionEffects_positive_enabled.put(PotionEffectType.CONDUIT_POWER,false);
		_potionEffects_positive_enabled.put(PotionEffectType.DOLPHINS_GRACE,false);
		_potionEffects_positive_enabled.put(PotionEffectType.BAD_OMEN,false);
		_potionEffects_positive_enabled.put(PotionEffectType.HERO_OF_THE_VILLAGE,false);
		_potionEffects_positive_enabled.put(PotionEffectType.HARM,false);
		_potionEffects_positive_enabled.put(PotionEffectType.WITHER,false);
		

	}
		
	public void loadKits()
	{
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				try 
				{
					for(File file : new File(_main.getDataFolder().getAbsoluteFile()+File.separator + text_kits_yml).listFiles())
					{
						if(!file.exists())
							continue;
						
						FileConfiguration config = YamlConfiguration.loadConfiguration(file);
						
						
						String name = config.getString("Kit_Name");
						
						ItemStack[] stacks = new ItemStack[41];
						
						for (String key : config.getConfigurationSection("KitContent.").getKeys(false)) 
						{
							if(config.getString("KitContent."+key).equalsIgnoreCase("null"))
							{
								stacks[Integer.parseInt(key)] = null;
							}
							else
							{
								stacks[Integer.parseInt(key)] = config.getItemStack("KitContent."+key);
							}
							
						}
						
						arena_kits.add(new ArenaKit(name, stacks));
						System.out.println("Combat kit added: "+name);
					}
									
				} 
				catch (Exception e) 
				{
					System.out.println("imusMiniGames: Didn't find any created combat arenas");
				}
				
			}
		}.runTaskAsynchronously(_main);
	}
	

	@Override
	public void saveArena(Arena arena) 
	{
		CombatArena cArena = (CombatArena) arena;
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				ConfigMaker cm = new ConfigMaker(_main, _text_arena_yml+"/"+arena.get_name().toLowerCase()+".yml");
				FileConfiguration config = cm.getConfig();
				
				config.set("Name", cArena.get_name().toString());
				config.set("Desc", cArena.get_description());
				config.set("MaxPlayers",cArena.get_maxPlayers());
				config.set("MiddleLoc", cArena.getArenas_middleloc());
				config.set("LobbyLoc", cArena.get_spectator_lobby());
				config.set("Max_Radius", cArena.getArena_radius());
				
				for(int i = 0; i < cArena.getTotalSpawnPositions(); ++i)
				{
					config.set("spawn_pos"+i, cArena.getSpawnpointLoc(i));
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
//				ConfigMaker cm = new ConfigMaker(_main, text_arena_yml+"/1temp1.yml");
//				cm.saveConfig();
//				cm.removeConfig();
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
						Location lobby_loc = config.getLocation("LobbyLoc");
						Location middle_loc = config.getLocation("MiddleLoc");
						Integer max_radius = config.getInt("Max_Radius");
						
						CombatArena arena = new CombatArena(name);
						arena.set_maxPlayers(max_p);
						arena.set_spectator_lobby(lobby_loc);
						arena.set_description(desc);
						arena.setArenas_middleloc(middle_loc);
						arena.setArena_radius(max_radius);
						
						for(int i = 0; i < max_p; ++i)
						{
							arena.addSpawnPosition(config.getLocation("spawn_pos"+i));
						}
						
						addArena(arena);
						System.out.println("Combat arena added: "+name);
					}
				} 
				catch (Exception e) 
				{
					System.out.println("imusMiniGames: Didn't find any created combat arenas");
				}
				
			}
		}.runTaskAsynchronously(_main);
	}
	
	

	
}
