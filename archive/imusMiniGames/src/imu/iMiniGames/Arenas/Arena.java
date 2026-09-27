package imu.iMiniGames.Arenas;



import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public abstract class Arena 
{
	String _name;
	String _description = "";
	
	

	int _maxPlayers = 0;
	
	ArrayList<Location> _spawnPositions = new ArrayList<>();
	Location _spectator_lobby = null;
	
	public Arena(String name) 
	{	
		_name = name;
		//_displayName = name;
		
	}
	
	public void sendArenaCreationgINFO(Player p)
	{
		String pre1 = ChatColor.GOLD+"";
		String pre2 = ChatColor.AQUA+"";
		p.sendMessage(ChatColor.DARK_PURPLE + "==== "+ get_arenaNameWithColor()+" ====");
		p.sendMessage(pre1 + "Description: "+pre2+_description);
		p.sendMessage(pre1 + "MaxPlayers: "+pre2+_maxPlayers);
		p.sendMessage(pre1 + "Total SpawnPos: "+pre2+getTotalSpawnPositions());
		p.sendMessage(pre1 + "Lobby set: "+pre2+ (_spectator_lobby == null ? "false":"true"));

	}
	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}

	
	public String get_arenaNameWithColor()
	{
		return ChatColor.translateAlternateColorCodes('&', _name);
	}
	public int get_maxPlayers() {
		return _maxPlayers;
	}

	public void set_maxPlayers(int _maxPlayers) 
	{
		this._maxPlayers = _maxPlayers;
	}
	
	public void addSpawnPosition(Location loc)
	{
		_spawnPositions.add(loc);

	}
	public int getTotalSpawnPositions()
	{
		return _spawnPositions.size();
	}
	
	public Location getSpawnpointLoc(int idx)
	{
		if(_spawnPositions.isEmpty())
		{
			return new Location(Bukkit.getWorlds().get(0),0,0,0);
		}
				
		return _spawnPositions.get(idx);
	}
	
	public void clearSpawnPositions()
	{
		_spawnPositions = new ArrayList<>();
		_maxPlayers = 0;
	}

	public String get_description() {
		return _description;
	}

	public void set_description(String _description) {
		this._description = _description;
	}
	

	public Location get_spectator_lobby() {
		return _spectator_lobby;
	}


	public void set_spectator_lobby(Location _spectator_lobby) {
		this._spectator_lobby = _spectator_lobby;
	}
	
	
	
}
