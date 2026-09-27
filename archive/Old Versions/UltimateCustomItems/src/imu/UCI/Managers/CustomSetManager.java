package imu.UCI.Managers;

import java.util.HashSet;

import imu.UCI.Other.CustomSet;
import imu.UCI.main.Main;

public final class CustomSetManager 
{
	Main _main = null;
	
	HashSet<CustomSet> all_sets = new HashSet<>();
	
	public CustomSetManager(Main main) 
	{
		_main = main;
	}
}
