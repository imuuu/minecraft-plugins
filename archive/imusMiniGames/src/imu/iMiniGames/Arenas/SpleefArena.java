package imu.iMiniGames.Arenas;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.Vector;


public class SpleefArena extends Arena
{
	Location[] _platformCorners = new Location[2];
	
	Location _spectator_lobby = null;
	
	public SpleefArena(String name) 
	{
		super(name);		
	}
	
		
	public Location getPlatformCorner(int idx)
	{
		return _platformCorners[idx];
	}
	public void setPlatformCorner(int idx, Location loc)
	{
		_platformCorners[idx] = loc;
	}
	public void clearPlatformCorners()
	{
		_platformCorners = new Location[_platformCorners.length];
	}
	
	public Location get_spectator_lobby() {
		return _spectator_lobby;
	}


	public void set_spectator_lobby(Location _spectator_lobby) {
		this._spectator_lobby = _spectator_lobby;
	}
	
	public void calculateCorners()
	{
		Vector vec1 = _platformCorners[0].toVector();
		Vector vec2 = _platformCorners[1].toVector();
		
		Vector vec_dis = vec2.subtract(vec1);
		
		Vector vec_dir_x = _platformCorners[0].toVector().add(new Vector(vec_dis.getBlockX(),0,0));
		Vector vec_dir_z = _platformCorners[0].toVector().add(new Vector(0,0,vec_dis.getBlockZ()));

//		_platformCorners[2] = vec_dir_x.toLocation(_platformCorners[0].getWorld());
//		_platformCorners[3] = vec_dir_z.toLocation(_platformCorners[0].getWorld());
		Location[] c_locs = {_platformCorners[0],_platformCorners[1],vec_dir_x.toLocation(_platformCorners[0].getWorld()),vec_dir_z.toLocation(_platformCorners[0].getWorld())};
		Location positive_loc = null;
		Location longest_loc = _platformCorners[0];
		
		double distance = 0;
		for(int i = 0 ; i < c_locs.length; ++i)
		{
			distance = 0;
			for(int l = 0 ; l < c_locs.length; ++l)
			{
				
				Vector v = c_locs[i].toVector();
				Vector v_test = c_locs[l].toVector().subtract(v);
				
				if(v_test.getX() > 0 && v_test.getBlockZ() > 0)
				{
					positive_loc = c_locs[i];
				}
				
				if(c_locs[i].distance(c_locs[l]) > distance)
				{
					distance = c_locs[i].distance(c_locs[l]);
					longest_loc = c_locs[l];
				}
				
				
			}
			if(positive_loc != null)
				break;
		}
		
		clearPlatformCorners();
		setPlatformCorner(0, positive_loc);
		setPlatformCorner(1, longest_loc);
		
		//fillWithSnow(positive_loc, longest_loc);
		
	}
	
	public void fillWithSnowpPlatform()
	{
		fillWithSnow(_platformCorners[0], _platformCorners[1]);
	}
	
	public void fillWithSnow(Location startLoc, Location endLoc)
	{
		int x = startLoc.getBlockX();
		int y = startLoc.getBlockY();
		int z = startLoc.getBlockZ();
		
		Material mat = Material.SNOW_BLOCK;
		World world = startLoc.getWorld();
		for(; x < endLoc.getBlockX()+1; ++x)
		{
			for(; z < endLoc.getBlockZ()+1; ++z)
			{
				world.getBlockAt(x, y, z).setType(mat);
			}
			z = startLoc.getBlockZ();
		}
	}
	

}
