package imu.iMiniGames.Leaderbords;

import java.util.UUID;

import org.bukkit.entity.Player;

public interface ILeaderboard 
{
	
	public void saveToFile();
	public void loadFromFile();
	public PlayerBoard getPlayerBoard(UUID uuid);

	public void setPlayerBoard(UUID uuid, CombatPlayerBoard board);
	public void showStats(Player p);
}

