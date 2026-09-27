package imu.iAPI.Other;


import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import imu.iAPI.Main.ImusAPI;

public class ParticleAnimations {
	

	Location _startLoc;

	//double t = 0;
	//double t_count = 0;
	Location _animLoc;
	
	public ParticleAnimations(Location loc) 
	{
		_startLoc = loc;
		_animLoc = loc;
	}
	
//	public boolean canTeleport()
//	{
//		if(_player == null)
//			return false;
//		
//		boolean canTele=true;
//		
//		if(_player.getLocation().distance(_startLoc) > 1)
//		{
//			canTele=false;
//		}
//
//		return canTele;
//	}
	
	
	void DrawCircleHurrican(Particle particle,int particle_count, double radius , double t_delta, double y_deltaM, double tick)
	{
		double x, y, z;
		Location loc = new Location(_startLoc.getWorld(), _startLoc.getX(), _startLoc.getY(), _startLoc.getZ());
		double tt = (Math.PI/10 * tick) ;
			
		x = radius * Math.cos(tt + t_delta);
		y = tt * y_deltaM;		
		z = radius * Math.sin(tt + t_delta);
		
		loc.add(x,y,z);
		_startLoc.getWorld().spawnParticle(particle,loc, particle_count,0,0,0);
	}
	
	public void DrawCircle(double radius, Particle particle, int particle_count)
	{
		double x, y, z;
		Location loc;
		for(int angle = 0 ; angle < 360 ; angle++)
		{
			loc = new Location(_startLoc.getWorld(), _startLoc.getX(), _startLoc.getY(), _startLoc.getZ());
			x = radius * Math.cos(angle);
			y = 0;
			z = radius * Math.sin(angle);
			loc.add(x,y,z);
			_startLoc.getWorld().spawnParticle(particle, loc, particle_count,0,0,0);
		}
	
	}
	public BukkitTask DrawHurricaneAsync(Particle particle,double radius, int startPos,int timeS)
	{
		BukkitTask task = new BukkitRunnable() 
		{
			
			public int _seconds = 0;
			public int _ticks = 0;
			@Override
			public void run() 
			{

				for(int i = 0; i < 2; i++)
				{
					DrawCircleHurrican(particle, 1, radius, i*startPos, 0.1, _ticks);
				}
				

				if((_ticks % 20) == 0)
				{
					_seconds++;
				}
				
				if(_seconds >= timeS)
				{
					this.cancel();
				}
				
				_ticks++;
			}
		}.runTaskTimerAsynchronously(ImusAPI._instance, 0, 1);
		
		return task;
	}
	public boolean DrawHurricaneCircleTeleportAsync(Particle particle, double total_time_seconds)
	{
		new BukkitRunnable() 
		{
			public int _seconds = 0;
			public int _ticks = 0;
			final double change = 20 * total_time_seconds;
			@Override
			public void run() 
			{
				
				if(_ticks < change*0.3 )
				{
					for(int i = 0; i < 2; i++)
					{
						DrawCircleHurrican(particle, 1, 1, i*10, 0.1, _ticks);
					}
					
					for(int i = 0; i < 2; i++)
					{
						DrawCircleHurrican(particle, 1, 2, i*20, 0.2, _ticks);
					}	
					
				}
				else if(_ticks < change*0.6)
				{

					DrawCircle((_ticks-change*0.3)*0.03, particle, 1);
							
				}
				
				
//				
//				if(t_count >= change)
//					return true;
				
				
				if((_ticks % 20) == 0)
				{
					_seconds++;
				}
				
				if(_seconds >= total_time_seconds)
				{
					this.cancel();
				}
				
				_ticks++;
			}
		}.runTaskTimerAsynchronously(ImusAPI._instance, 0, 1);
		
		
		
		return false;
	}
}
