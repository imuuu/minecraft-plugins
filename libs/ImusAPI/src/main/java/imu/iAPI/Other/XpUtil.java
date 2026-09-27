package imu.iAPI.Other;

import org.bukkit.entity.Player;

public class XpUtil 
{
	static int GetExp(Player player) 
	{
		return GetExpFromLevel(player.getLevel())
				+ Math.round(GetExpToNext(player.getLevel()) * player.getExp());
	}
	static int GetExpFromLevel(int level) {
		if (level > 30) {
			return (int) (4.5 * level * level - 162.5 * level + 2220);
		}
		if (level > 15) {
			return (int) (2.5 * level * level - 40.5 * level + 360);
		}
		return level * level + 6 * level;
	}
	static double GetLevelFromExp(long exp) {
		if (exp > 1395) {
			return (Math.sqrt(72 * exp - 54215) + 325) / 18;
		}
		if (exp > 315) {
			return Math.sqrt(40 * exp - 7839) / 10 + 8.1;
		}
		if (exp > 0) {
			return Math.sqrt(exp + 9) - 3;
		}
		return 0;
	}
	
	static int GetExpToNext(int level) {
		if (level > 30) {
			return 9 * level - 158;
		}
		if (level > 15) {
			return 5 * level - 38;
		}
		return 2 * level + 7;
	}
	
	static void ChangeExp(Player player, int exp) {
		exp += GetExp(player);

		if (exp < 0) {
			exp = 0;
		}

		double levelAndExp = GetLevelFromExp(exp);

		int level = (int) levelAndExp;
		player.setLevel(level);
		player.setExp((float) (levelAndExp - level));
	}
	
	
	public static double GetPlayerLevel(Player player)
	{
		return player.getLevel()+player.getExp();
	}
	
	public static void SetPlayerLevel(Player player, double level)
	{
		if(level < 0) level = 0;
		
		player.setLevel(0);
		player.setExp(0);
		
		double fullLevel = Math.floor(level);
		double leftProsent = level - fullLevel;

		int xpToNextLevel = GetExpToNext((int)fullLevel);

		player.setLevel((int)fullLevel);
		
		player.giveExp((int)(xpToNextLevel * leftProsent));
	}
}
