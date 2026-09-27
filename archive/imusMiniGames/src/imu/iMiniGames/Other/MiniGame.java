package imu.iMiniGames.Other;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import imu.iAPI.Other.Metods;
import imu.iAPI.Other.PlayerDataCard;
import imu.iMiniGames.Interfaces.IMiniGame;
import imu.iMiniGames.Main.ImusMiniGames;
import net.md_5.bungee.api.ChatColor;

public abstract class MiniGame implements IMiniGame
{
	protected ImusMiniGames _main;
	protected Metods _itemM;
	String _miniGameName;
	
	int _roundTime = 0;
	protected int _round = 0;
	
	HashMap<Player, MiniGamePlayerStats> _players_ingame = new HashMap<>();
	HashMap<Player, MiniGamePlayerStats> _players_off_game = new HashMap<>();
	
	HashMap<Player, PlayerDataCard> _players_spectators = new HashMap<>();
	
	protected Location _spectator_loc = null;

	public MiniGame(ImusMiniGames main, String minigameName)
	{
		_main = main;
		_itemM = main.get_itemM();
		_miniGameName= minigameName;
	}
	
	public void addPlayer(Player p)
	{
		_players_ingame.put(p, new MiniGamePlayerStats());
	}
	
	public int get_roundTime() {
		return _roundTime;
	}

	public void set_roundTime(int _roundTime) 
	{
		this._roundTime = _roundTime;
	}
	
	public void movePlayerToLobbyHash(Player p)
	{
		_players_off_game.put(p, _players_ingame.get(p));
		_players_ingame.remove(p);
	}
	
	public void addPointsPlayer(Player p,int amount)
	{
		_players_ingame.get(p).addScore(amount);
	}
	
	public void addLobbyPlayersToScore()
	{
		for(Entry<Player,MiniGamePlayerStats> p : _players_off_game.entrySet())
		{
			_players_ingame.put(p.getKey(), p.getValue());
		}
		_players_off_game.clear();
	}
	
	public void addSpectator(ImusMiniGames main, Player p)
	{
		_players_spectators.put(p, new PlayerDataCard(main, p, "null"));
	}
	
	public void teleportSpectatorToSpectate(Player p)
	{
		if(_players_spectators.containsKey(p) && _spectator_loc != null)
		{
			p.teleport(_spectator_loc);
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9You have teleported to spectate! You need to watch whole match order to leave :)"));
		}
	}
	
	public void teleportSpectatorToBack(Player p)
	{
		if(_players_spectators.containsKey(p))
		{
			p.teleport(_players_spectators.get(p).get_location());
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9You have teleported to back!"));
			_players_spectators.remove(p);
		}
	}
	public void teleportSpectatorAllBack()
	{
		for(Entry<Player,PlayerDataCard> entry : _players_spectators.entrySet())
		{
			entry.getKey().teleport(entry.getValue().get_location());
		}
		_players_spectators.clear();
	}
	
	public Collection<PlayerDataCard> getSpectators()
	{
		return _players_spectators.values();
	}
	
}
