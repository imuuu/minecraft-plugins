package imu.HomeTele.Commands;

import java.awt.TextComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import imu.HomeTele.Other.ConfigMaker;
import imu.HomeTele.Other.Cooldowns;
import imu.HomeTele.Other.TeleChecks;

public class Commands implements CommandExecutor
{

	public String[] cmds = {"sethome","home","homelist"};
	
	public HashMap<Player, Location> playerHomes = new HashMap<Player, Location>();
	public HashMap<Player, TeleChecks> playerChecks = new HashMap<Player, TeleChecks>();
	
	Plugin _plugin;
	
	HashMap<UUID, Cooldowns> player_cds = new HashMap<UUID, Cooldowns>();
	
	HashMap<UUID, Location> allHomes = new HashMap<UUID, Location>();
	
	double teleport_castTime = 10;
	double teleport_cooldown = 60*5; // 5min
	double setHome_cooldown = 60*30; // 30min
	
	
	public Commands(Plugin plugin)
	{
		_plugin = plugin;
		
		checkPlayerHomes();
		runnable_checkTeles();
		getSettings();
		setAllHomes();
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arg) 
	{
		if(sender instanceof Player)
		{
			Player player = (Player)sender;
			
			if(cmd.getName().equalsIgnoreCase(cmds[0])) //sethome
			{				
				settingHome(player);
				
			}else if(cmd.getName().equalsIgnoreCase(cmds[1])) //home
			{
				if(arg.length == 0)
				{
					if(hasHome(player.getUniqueId()))
					{
						if(!isCd(player, "home", teleport_cooldown))
							playerChecks.put(player, new TeleChecks(player,_plugin));
					}
				}
				if(arg.length == 1 && player.isOp())
				{
					Player target_player = _plugin.getServer().getPlayer(arg[0]);
					
					
					if(target_player != null)
					{
						teleportHome(player.getUniqueId(), target_player.getUniqueId());
					}else
					{
						try {
							UUID uuid = UUID.fromString(arg[0]);
							if(uuid != null)
								teleportHome(player.getUniqueId(), uuid);
						} catch (Exception e) {
							System.out.println("HOME TELE: UUID FAILED");
						}
					}
				}
				
				
			}else if(cmd.getName().equalsIgnoreCase(cmds[2]) && player.isOp()) //homelist
			{
				homeList(player);
				
			}else
			{
				return false;
			}
			
			return true;
		}else
		{
			sender.sendMessage(ChatColor.RED +"Only player can use this command!");
			return true;
		}
				
	}
	
	
	
	void homeList(Player player)
	{		
		ConfigMaker cm = new ConfigMaker(_plugin, "homes.yml");
		FileConfiguration config=cm.getConfig();
		
		for(Map.Entry<UUID, Location> entry : allHomes.entrySet())
		{	
			UUID player_uuid = entry.getKey();

			String lastName = config.getString("homes."+player_uuid+".name");
						
			TextComponent message = new TextComponent("Teleport home of ");	
			message.setColor( net.md_5.bungee.api.ChatColor.GRAY );
			TextComponent pName = new TextComponent(lastName);
			pName.setBold( true );
			pName.setColor(net.md_5.bungee.api.ChatColor.AQUA);
			message.addExtra(pName);
			message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/home "+ player_uuid));
			message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Teleport").create()));
			player.spigot().sendMessage(message);
		}
		
	}
	
	
	public void updateNameConfig(Player player)
	{
		ConfigMaker cm = new ConfigMaker(_plugin, "homes.yml");
		FileConfiguration config=cm.getConfig();
		if(config.contains("homes."+player.getUniqueId())) 
		{
			config.set("homes."+player.getUniqueId()+".name", player.getName());
			cm.saveConfig();
		}
		
	}
	void getSettings()
	{
		ConfigMaker cm = new ConfigMaker(_plugin, "settings.yml");
		FileConfiguration config=cm.getConfig();
		if(!config.contains("settings.")) 
		{
			//default values
			_plugin.getServer().getConsoleSender().sendMessage(ChatColor.AQUA +" home teles: Default config made!");	
			config.set("settings.teleCastTime",teleport_castTime);
			config.set("settings.teleCooldown",teleport_cooldown);
			config.set("settings.setHomeCooldown",setHome_cooldown);		
			cm.saveConfig();
			return;
		}
		
		teleport_castTime = config.getDouble("settings.teleCastTime");
		teleport_cooldown = config.getDouble("settings.teleCooldown");
		setHome_cooldown = config.getDouble("settings.setHomeCooldown");
				
	}
	
	public void cancelTeleport(Player p)
	{
		p.sendMessage("Teleport canceled!");
		playerChecks.remove(p);
	}
	
	void checkPlayerHomes()
	{
		for (Player p : _plugin.getServer().getOnlinePlayers())
		{
			setHomeToHash(p);
		}
	}
	
	public void setAllHomes()
	{
		Location loc;
		ConfigMaker conM = new ConfigMaker(_plugin, "homes.yml");
		FileConfiguration customConfig = conM.getConfig();
		if(!conM.isExists())
			conM.saveConfig();
		
		if(customConfig.contains("homes."))
		{
			for(final String uuid : customConfig.getConfigurationSection("homes.").getKeys(false))
			{

				loc = customConfig.getLocation("homes."+ uuid +".loc");
				allHomes.put(UUID.fromString(uuid), loc);
			}
		}
		
		
	}
	
	public Location getPlayerHomeFromConfig(Player player)
	{
		ConfigMaker conM = new ConfigMaker(_plugin, "homes.yml");
		FileConfiguration customConfig = conM.getConfig();
		Location loc = customConfig.getLocation("homes."+ player.getUniqueId()+".loc");
		
		if(loc == null)
		{
			System.out.println("not home found in config");
			return null;
		}
		return loc;
	}
	public void setHomeToHash(Player p)
	{		
		Location loc = getPlayerHomeFromConfig(p);
		if(loc != null)
		{
			playerHomes.put(p, loc);
			allHomes.put(p.getUniqueId(),loc);
		}
			
		
	}
	
	boolean hasHome(UUID uuid)
	{
		Location homeLoc = allHomes.get(uuid);
		if(homeLoc == null)
		{
			System.out.println("home not found by uuid"+uuid);
			Player p = Bukkit.getPlayer(uuid);
			if(p != null)
			{
				p.sendMessage(ChatColor.RED + "You don't have home setted!");
			}
			return false;
		}
		return true;
	}
	
	void teleportHome(UUID uuid, UUID uuid_target)
	{
		if(!hasHome(uuid) || !hasHome(uuid_target))
			return;
		Player p = Bukkit.getServer().getPlayer(uuid);
		if(p == null)
		{
			return;
		}
		p.sendMessage("You have teleported to home!");
		p.teleport(allHomes.get(uuid_target));
	}
	boolean isCd(Player p , String cd_name, double cd)
	{
		Cooldowns cds = player_cds.get(p.getUniqueId());
		if(cds == null)
		{
			cds = new Cooldowns();
			cds.addCooldownInSeconds(cd_name, cd);
			player_cds.put(p.getUniqueId(), cds);
		}
		else
		{
			if(!cds.isCooldownReady(cd_name))
			{
				p.sendMessage("You have "+ChatColor.RED+cds.GetCdInSeconds(cd_name)+ChatColor.WHITE+"s cooldown left!");
				return true;
			}else
			{
				cds.addCooldownInSeconds(cd_name, cd);
			}
		}
		
		return false;
	}
	void settingHome(Player p)
	{
		if(isCd(p, "sethome", setHome_cooldown))
			return;
		
		p.sendMessage("Home has been set!");
		
		
		ConfigMaker conM = new ConfigMaker(_plugin, "homes.yml");
		FileConfiguration customConfig = conM.getConfig();
			
		customConfig.set("homes." + p.getUniqueId()+".loc", p.getLocation());
		customConfig.set("homes." + p.getUniqueId()+".name", p.getName());
		conM.saveConfig();
		playerHomes.put(p, p.getLocation());
		allHomes.put(p.getUniqueId(), p.getLocation());
		
	}
	
	void runnable_checkTeles()
	{
		new BukkitRunnable() 
		{
			
			@Override
			public void run() 
			{
				if(playerChecks.size() == 0)
					return;
				
				Player player;
				for(Map.Entry<Player,TeleChecks> entry : playerChecks.entrySet())
				{	
					player = entry.getKey();
					if(!entry.getValue().canTeleport())
					{
						cancelTeleport(player);
						player_cds.get(player.getUniqueId()).removeCooldown("home");
						player_cds.get(player.getUniqueId()).addCooldownInSeconds("home", 15);
						continue;
					}
					
					if(entry.getValue().drawAnimation(teleport_castTime))
					{
						teleportHome(player.getUniqueId(), player.getUniqueId());
						playerChecks.remove(player);
					}
										
				}
				
			}
		}.runTaskTimer(_plugin, 0, 1);
	}

}
