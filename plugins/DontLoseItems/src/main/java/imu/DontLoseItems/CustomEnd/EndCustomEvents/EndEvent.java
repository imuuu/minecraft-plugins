package imu.DontLoseItems.CustomEnd.EndCustomEvents;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import imu.DontLoseItems.CustomEnd.EndEvents;
import imu.DontLoseItems.CustomEnd.UnstableEnd;
import imu.DontLoseItems.main.DontLoseItems;
import imu.iAPI.Other.Metods;
import net.md_5.bungee.api.ChatColor;

public abstract class EndEvent implements Listener
{
	private String _name;
	private boolean _stay = false;
	private double _duration = 0;
	
	private HashMap<UUID, Player> _activePlayer;
	public int ChestLootAmount = 0;
	
	public EndEvent(String name, double duration)
	{
		_name = name;
		_activePlayer = new HashMap<>();
		_duration = duration;
	}
	
	public void AddPlayer(Player player)
	{
		if(_activePlayer.containsKey(player.getUniqueId()));
		_activePlayer.put(player.getUniqueId(),player);
	}
	
	public void RemovePlayer(Player player)
	{
		if(!_activePlayer.containsKey(player.getUniqueId())) return;
		
		_activePlayer.remove(player.getUniqueId());
 	}
	
	public void ClearPlayers()
	{
		_activePlayer.clear();
	}
	public boolean HasPlayer(Player player)
	{
		return _activePlayer.containsKey(player.getUniqueId());
	}
	public Set<UUID> GetPlayersByUUID()
	{
		//return _activePlayer.toArray(new UUID[_activePlayer.size()]);
		return _activePlayer.keySet();
	}
	
	public LinkedList<Player> GetPlayers()
	{
		LinkedList<Player> list = new LinkedList<>();
		for(UUID uuid : _activePlayer.keySet())
		{
			Player player = Bukkit.getPlayer(uuid);
			if(player == null) continue;
			
			if(player.getGameMode() == GameMode.SPECTATOR) continue;
			
			if(!EndEvents.Instance.IsPlayerUnstableArea(player)) 
			{				
				continue;
			}
			
			list.add(player);
		}
		
		return list;
	}
	public EndEvent SetStay(boolean b)
	{
		_stay = b;
		return this;
	}
	
	public String GetName()
	{
		return _name;
	}
	public abstract void OnEventStart();
	public abstract void OnEventEnd();
	public abstract void OnPlayerLeftMiddleOfEvent(Player player);
	public abstract void OnPlayerJoinMiddleOfEvent(Player player);
	public abstract void OnOneTickLoop();
	public abstract String GetEventName();
	public abstract String GetRewardInfo();
	public abstract String GetDescription();
	
	
	public double GetDuration()
	{
		return _duration;
	}
	public void PrintToPlayer(Player player)
	{
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage("");
		player.sendMessage(ChatColor.BLUE+"======================");
		
		if(GetEventName() != null) 		player.sendMessage(Metods.msgC("&3Name &2"+GetEventName()));
		if(_duration >= 0) 		   		player.sendMessage(Metods.msgC("&4Duration: &2"+(int)_duration+" seconds"));
		if(GetRewardInfo() != null)		player.sendMessage(Metods.msgC("&aReward: &e"+GetRewardInfo()));
		if(GetDescription() != null) 	player.sendMessage(Metods.msgC("&3Description: &e"+GetDescription())); 
		
		player.sendMessage(ChatColor.BLUE+"======================");
	}

	public void TitleToPlayer(Player player) {
		String durText = "";
		if(GetDuration() < 0) {
			durText = ChatColor.DARK_AQUA +"Until the next " + ChatColor.DARK_PURPLE + "Unstable Void";
		} else {
			durText = ChatColor.DARK_AQUA + "For the next " + ChatColor.GOLD + (int)GetDuration() + ChatColor.DARK_AQUA + " seconds";
		}

		Color tclr = new Color(214, 76, 245);
		player.sendTitle(
				ChatColor.DARK_PURPLE + "End Event has started!",
				   durText + " " + ChatColor.of(tclr) + GetEventName(),
				10,70,20);

		Color clr = new Color(22, 156, 115);

		player.sendMessage(ChatColor.of(clr)+"~~~~~~~~~~~~~~~~~~~~~");
		player.sendMessage(ChatColor.DARK_PURPLE + "End Event: " + ChatColor.DARK_AQUA + GetEventName());


		if(GetRewardInfo() != null) 
		{
			//player.sendMessage(ChatColor.GREEN + "During the event: " + ChatColor.YELLOW + GetRewardInfo());
			player.sendMessage(Metods.msgC("&After the event: &e"+GetRewardInfo()));
		}
		/*if(GetDescription() != null) {
			player.sendMessage();
		}*/

		player.sendMessage(ChatColor.of(clr)+"\n~~~~~~~~~~~~~~~~~~~~~");
	}
	
	protected void AddChestLootBaseToAll(int amount)
	{
		for(Player player : GetPlayers())
		{
			AddChestLootBaseToPlayer(player,amount);
		}
	}
	
	protected void AddChestLootBaseToPlayer(Player player, int amount)
	{
		UnstableEnd.Instance.AddPlayerChestlootBase(player, amount);
	}
	public void RegisterBukkitEvents()
	{
		Bukkit.getServer().getPluginManager().registerEvents(this, DontLoseItems.Instance);
	}
	
	public void UnRegisterBukkitEvents()
	{
		HandlerList.unregisterAll(this);
	}
}
