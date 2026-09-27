package imu.imusSpawners.Other;

import java.util.UUID;

public class PlayerSpawnerData
{
	public UUID Uuid;
	public int TotalMinedSpawners = 0;
	public int CurrentChanceBonus = 0;
	public int TotalSpawnerGot = 0;
	
	public PlayerSpawnerData(UUID uuid)
	{
		this.Uuid = uuid;
	}
}
