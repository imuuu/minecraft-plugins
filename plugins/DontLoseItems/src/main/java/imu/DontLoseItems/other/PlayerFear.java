package imu.DontLoseItems.other;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerFear
{
	private double _fearLevel = 0;
	private BossBar _bossBar;
	private Random _rand;
	private int _fearState = 0;
	public PlayerFear(Player player)
	{
		_rand = new Random();
        
	}
	public BossBar GetBossBar()
	{
		return _bossBar;
	}
	
	public void SetBossBar(BossBar bar)
	{
		_bossBar = bar;
	}
	public void SetPlayer(Player player)
	{
	}
	public void SetFear(int fearLevel)
	{
		_fearLevel = fearLevel;
		CorrectLevel();
		
	}
	
	public double GetFearLevel()
	{
		return _fearLevel;
	}
	
	public void AddFearLevel(double amount)
	{
		_fearLevel += amount;
		CorrectLevel();
	}
	
	private void CorrectLevel()
	{
		if(_fearLevel > 100) _fearLevel = 100;
		if(_fearLevel < 0) _fearLevel = 0;
	}
	
	public void TriggerFear(Player player)
	{
		boolean damageGiven = false;
    	if(_fearLevel >= 100)
		{
    		_fearState += 1;
    		
    		damageGiven = GiveDamage(player, -1);
    		//player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, false));
    		  		
    		//player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 70 * _fearState, 1, false));
    		if(_fearState > 2)
    		{
    			player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60 , 1, false));
    			player.addPotionEffect(new PotionEffect(PotionEffectType.POISON,   60 , 0, false));
    			
    		}
    		if(_fearState > 3)
    		{
    			player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS,     	100, 10, false));
    			player.addPotionEffect(new PotionEffect(PotionEffectType.POISON,       	100, 0, false));
    			player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 		100 , 1, false));
    			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 	100, 1, false));
    		}
    		
    		if(_fearState > 4)
    		{
    			player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 		200, 0, false));
    			player.addPotionEffect(new PotionEffect(PotionEffectType.POISON,   		200, 2, false));
    			player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 		200, 1, false));
    			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 	200, 1, false));
    			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,     		200, 1, false));
    		}
    		
    		
    		
    		
    		if(_rand.nextInt(100) < 30)
    			player.sendMessage(ChatColor.DARK_PURPLE+ "Fear "+ChatColor.GRAY+"is consuming you!");
		}
    	else if(_fearLevel >= 70)
		{
    		damageGiven = GiveDamage(player, -2);
    		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false));
    		player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1, false));
    		player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 150, 1, false));
    		_fearState = 2;
		}
    	else if(_fearLevel >= 60)
		{
    		damageGiven = GiveDamage(player, -2);
    		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false));
    		player.sendMessage(ChatColor.DARK_PURPLE+ "Fear"+ChatColor.BLUE+ " is increasing!");
    		player.sendMessage(ChatColor.BLUE+ "Try to find"+ ChatColor.YELLOW+ " light "+ChatColor.BLUE+"source!");
    		_fearState = 0;
		}
    	else if(_fearLevel >= 50)
		{
    		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 0, false));
    		_fearState = 0;
		}
		
		

	    if(!damageGiven) return;

	}
	
	@SuppressWarnings("deprecation")
	private boolean GiveDamage(Player player, double damage)
	{
		double dmg = player.getHealth() + damage;
		//System.out.println("Healt scale: "+player.getHealthScale() + " health: "+player.getMaxHealth());
		if(dmg < 0 ) dmg = 0;
		
		if(dmg > player.getMaxHealth()) dmg = player.getMaxHealth();
		
		player.setHealth(dmg);
		
		return true;
	}
}
