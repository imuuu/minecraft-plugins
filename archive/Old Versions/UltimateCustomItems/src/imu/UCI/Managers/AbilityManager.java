package imu.UCI.Managers;

import java.util.HashSet;

import imu.UCI.Abilities.Ability;
import imu.UCI.main.Main;

public final class AbilityManager 
{
	Main _main = null;
	
	HashSet<Ability> all_abilities = new HashSet<>();
	
	public AbilityManager(Main main) 
	{
		_main = main;
	}
}
