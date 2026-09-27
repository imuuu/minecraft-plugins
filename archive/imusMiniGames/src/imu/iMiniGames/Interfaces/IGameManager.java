package imu.iMiniGames.Interfaces;

import imu.iMiniGames.Arenas.Arena;

public interface IGameManager 
{
	void onEnabled();
	void onDisabled();
	public void loadArenas();
	public void saveArena(Arena arena);
}
