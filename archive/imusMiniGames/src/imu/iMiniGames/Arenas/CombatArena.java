package imu.iMiniGames.Arenas;

import org.bukkit.Location;

public class CombatArena extends Arena
{
	Location arenas_middleloc = null;
	int arena_radius = 10;
	
	public int getArena_radius() {
		return arena_radius;
	}

	public void setArena_radius(int arena_radius) {
		this.arena_radius = arena_radius;
	}

	public CombatArena(String name) 
	{
		super(name);
	}
	
	public Location getArenas_middleloc() {
		return arenas_middleloc;
	}

	public void setArenas_middleloc(Location arenas_middleloc) {
		this.arenas_middleloc = arenas_middleloc;
	}

}
