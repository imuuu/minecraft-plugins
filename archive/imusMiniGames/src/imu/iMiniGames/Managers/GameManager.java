package imu.iMiniGames.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iAPI.Other.ConfigMaker;
import imu.iMiniGames.Arenas.Arena;
import imu.iMiniGames.Interfaces.IGameManager;
import imu.iMiniGames.Main.ImusMiniGames;
import imu.iMiniGames.Other.PlanerDataCard;

public abstract class GameManager implements IGameManager
{
	ImusMiniGames _main;
	
	HashMap<Integer, Arena> _arenas = new HashMap<>();
	HashMap<UUID, PlanerDataCard> _player_DataCards = new HashMap<>();
	
	HashMap<PotionEffectType, Boolean> _potionEffects_positive_enabled = new HashMap<>();
	
	String _text_arena_yml="Arenas_";
	String _tag;
	
	int _maximum_best_of = 5;
	
	public GameManager(ImusMiniGames main, String tag) 
	{
		_main = main;
		_text_arena_yml += tag;
		_tag = tag;
	}
	
	public void clearPlayerDataCards()
	{
		_player_DataCards.clear();
	}
	
	public void savePlayerDataCard(Player p, PlanerDataCard card)
	{
		_player_DataCards.put(p.getUniqueId(), card);
	}
	
	public PlanerDataCard getPlayerDataCard(Player p)
	{
		return _player_DataCards.get(p.getUniqueId());
	}
	
	public boolean hasPlayerDataCard(Player p)
	{
		return _player_DataCards.containsKey(p.getUniqueId());
	}
	
	public HashMap<PotionEffectType, Boolean> getPotionEffects()
	{
		return _potionEffects_positive_enabled;
	}
	
	public int get_maximum_best_of() {
		return _maximum_best_of;
	}

	public void set_maximum_best_of(int _maximum_best_of) {
		this._maximum_best_of = _maximum_best_of;
	}
	
//	public void createArena(String name)
//	{
//		addArena(new CombatArena(name));
//	}
//	
	public void addArena(Arena arena)
	{
		_arenas.put(_arenas.size(), arena);
	}
	
	public Arena getArena(String arena_name)
	{
		for(Entry<Integer, Arena> entry : _arenas.entrySet())
		{
			if(entry.getValue().get_name().toLowerCase().contains(arena_name.toLowerCase()))
			{
				return entry.getValue();
			}
		}

		return null;
	}
	
	void removeArenaHash(String arena_name)
	{
		int key = -1;
		for(Entry<Integer, Arena> entry : _arenas.entrySet())
		{
			if(entry.getValue().get_name().equalsIgnoreCase(arena_name))
			{
				key = entry.getKey();
				break;
			}
		}
		
		if(key != -1)
		{
			_arenas.remove(key);
		}

	}
	
	public ArrayList<Arena> getArenas()
	{
		ArrayList<Arena> ar = new ArrayList<>();
		for(Entry<Integer, Arena> entry : _arenas.entrySet())
		{
			ar.add(entry.getValue());
		}
		return ar;
	}
	public Arena getArena(int idx)
	{
		return _arenas.get(idx);
	}
	public void removeArena(Arena arena)
	{
		removeArenaHash(arena.get_name());
		ConfigMaker cm = new ConfigMaker(_main, _text_arena_yml+"/"+arena.get_name()+".yml");
		if(cm.isExists())
		{
			cm.removeConfig();
		}
		
	}
	public void loadPotionsConfig()
	{
		new BukkitRunnable() 
		{		
			@Override
			public void run() 
			{
				ConfigMaker cm = new ConfigMaker(_main, _tag+"/Enabled_PotionEffects.yml");
				FileConfiguration config = cm.getConfig();
				
				if(!cm.isExists())
				{
					for(Entry<PotionEffectType, Boolean> entry : getPotionEffects().entrySet())
					{
						config.set(entry.getKey().getName(), entry.getValue());
					}
				}
				else
				{
					getPotionEffects().clear();
					for(PotionEffectType t : PotionEffectType.values())
					{
						Boolean value = config.getBoolean(t.getName());
						getPotionEffects().put(t, value);
					}
				}
				
				cm.saveConfig();
			}
			
		}.runTaskAsynchronously(_main);
	}
	
	void saveAllArenas()
	{
		if(_arenas.isEmpty())
			return;
		
		for(Entry<Integer, Arena> entry : _arenas.entrySet())
		{
			saveArena(entry.getValue());
		}
	}
	
}
