package imu.GeneralStore.Other;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import imu.GeneralStore.main.Main;
import net.milkbowl.vault.economy.Economy;


public class BalanceTracker 
{
	Main _main;
	Economy _econ ;
	String yml_path = "balances";
	HashMap<UUID, ImmutablePair<String, Double>> _balances = new HashMap<>();
	Cooldowns _cd;
	public BalanceTracker(Main main)
	{
		_main = main;
		_econ = main.getEconomy();
		_cd = new Cooldowns();
	}
	
	public void printTop(Player p, int range)
	{
		if(_balances.isEmpty())
		{
			p.sendMessage(ChatColor.translateAlternateColorCodes('&', 
					"&cThere isn't any recorded balances yet!"));
			return;
		}
		
		new BukkitRunnable() {

			Player player = p;
			@Override
			public void run() 
			{
				if(_cd.isCooldownReady("balance"))
				{
					refreshOnlineBalances();
					_cd.addCooldownInSeconds("balance", 60);
				}
				
				HashMap<String, Double> name_bal = new HashMap<>();
				HashMap<Double, String> bal_name = new HashMap<>();
				for(Map.Entry<UUID, Pair<String, Double>> entry : _balances.entrySet())
				{
					name_bal.put(entry.getValue().getFirst(), entry.getValue().getSecond());
					bal_name.put(entry.getValue().getSecond(), entry.getValue().getFirst());
				}
				
				List<Double> test = new ArrayList<>(name_bal.values());
				Collections.sort(test);
				Collections.reverse(test);

				int count = 0;
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
						"&e===== Balance Top ====="));
				for(double d : test)
				{
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
							"&6"+(count+1)+". &9"+bal_name.get(d)+" :&2 "+String.format("%,d",(int)d))+" "+_econ.currencyNamePlural()); //Math.round(d * 100.0)/100.0)
					count++;
					if(count >= range)
						break;
				}

				
				
			}
		}.runTaskAsynchronously(_main);
	}
	
	public void saveBalanceOfPlayer(Player p)
	{
		if(_econ == null)
			return;
		
		setBalance(p.getUniqueId(), p.getName(), _econ.getBalance(p));
	}
	
	public void refreshOnlineBalances()
	{
		if(_econ == null)
			return;
		
		for(Player p : _main.getServer().getOnlinePlayers())
		{
			saveBalanceOfPlayer(p);
		}
	}
	
	public void setBalance(UUID uuid, String name, double balance)
	{
		_balances.put(uuid, new Pair<>(name,balance));
	}
	public Double getBalance(UUID uuid)
	{
		if(_balances.containsKey(uuid))
			return _balances.get(uuid).getSecond();
		
		return 0.0;
	}
	
	public void saveALLbalances()
	{
		if(_balances.isEmpty())
			return;
		
		ConfigMaker cm;
		FileConfiguration config;
		for(Map.Entry<UUID, Pair<String, Double>> entry : _balances.entrySet())
		{
			String name = entry.getValue().getFirst();
			Double balance = entry.getValue().getSecond();
			cm = new ConfigMaker(_main, yml_path+"/"+entry.getKey().toString()+".yml");
			config = cm.getConfig();
			config.set("Name", name);
			config.set("UUID", entry.getKey().toString());
			config.set("Balance", balance);
			cm.saveConfig();
		}
		
	}
	
	public void loadAllbalances()
	{
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				try {
					for(File file : new File(_main.getDataFolder().getAbsoluteFile()+File.separator + yml_path).listFiles())
					{
						if(!file.exists())
							continue;
						
						FileConfiguration config = YamlConfiguration.loadConfiguration(file);
						String name = config.getString("Name");
						UUID uuid = UUID.fromString(config.getString("UUID"));
						Double balance = config.getDouble("Balance");
						setBalance(uuid, name, balance);
					}
				} 
				catch (Exception e) 
				{
					System.out.println("[GeneralStore] Couldn't find any balances");
				}
				
				
			}
		}.runTaskAsynchronously(_main);
	}
}
