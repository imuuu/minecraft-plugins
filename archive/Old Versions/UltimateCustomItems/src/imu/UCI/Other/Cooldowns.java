package imu.UCI.Other;

import java.util.HashMap;

public class Cooldowns 
{

	HashMap<String, Long> cooldowns = new HashMap<String, Long>();
	
	public void addCooldownInSeconds(String cd_name, double seconds)
	{
		if(cooldowns.containsKey(cd_name.toLowerCase()))
			return;
		
		cooldowns.put(cd_name.toLowerCase(), System.currentTimeMillis()+(long)(seconds*1000));
	}
	
	public boolean isCooldownReady(String cd_name)
	{
		
		if(!cooldowns.containsKey(cd_name.toLowerCase()) || System.currentTimeMillis() > cooldowns.get(cd_name.toLowerCase()))
		{
			//cooldown is finnished
			cooldowns.remove(cd_name.toLowerCase());
			return true;
		}
		return false;
	}
	
	public boolean isTimePastThis(Long timeStamp, int seconds)
	{
		if(System.currentTimeMillis() > timeStamp+(long)(seconds*1000))
		{
			System.out.println("time passed");
			return true;
		}
		return false;
	}
	
	public int GetCdInSeconds(String cd_name)
	{
		int left = (int) ((cooldowns.get(cd_name.toLowerCase()) - System.currentTimeMillis())*0.001);

		return left;
	}
	
	public void removeCooldown(String cd_name)
	{
		cooldowns.remove(cd_name.toLowerCase());
	}
}
