package imu.iMiniGames.Leaderbords;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import imu.iAPI.Other.ConfigMaker;
import imu.iMiniGames.Invs.CombatLeaderBoardStats;
import imu.iMiniGames.Main.ImusMiniGames;

public class CombatLeaderBoard extends Leaderboard implements ILeaderboard
{
	LocalDate _weeklyStartDate = null;
	LocalDate _weeklyEndDate = null;
	BukkitTask _runnable = null;
	public CombatLeaderBoard(ImusMiniGames main,String name) 
	{
		super(main,name);
		_path = "/Combat/Leaderboards";
		_weeklyStartDate = LocalDate.now();
		_weeklyEndDate = _weeklyStartDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
		runnableAsync();
	}
	
	@Override
	public void saveToFile() 
	{
		ConfigMaker cm,cm2,cm3,cm4;
		FileConfiguration config,config2,config3,config4 = null;
		
		cm = new ConfigMaker(_main, _path + "/all_time.yml");
		config = cm.getConfig();
		
		cm2 = new ConfigMaker(_main, _path +"/weekly.yml");
		config2 = cm2.getConfig();
		
		cm3 = new ConfigMaker(_main, _path + "/pvp_stats.yml");
		config3 = cm3.getConfig();
		
		cm4 = new ConfigMaker(_main, _path+"/LeaderBoardSettings.yml");
		config4 = cm4.getConfig();
		
		config4.set("WeeklyStartDate", _weeklyStartDate.toString());
		config4.set("WeeklyEndDate",  _weeklyEndDate.toString());
		
		for(PlayerBoard board : _boards_alltime.values())
		{
			CombatPlayerBoard cb = (CombatPlayerBoard) board;
			
			//alltime
			String uuid = cb.get_uuid().toString();
			config.set(uuid+".Name",cb.get_pName());
			config.set(uuid+".Wins", cb.get_Wins());
			config.set(uuid+".Loses", cb.get_Loses());
			config.set(uuid+".Kills",cb.get_total_kills());
			config.set(uuid+".Deaths", cb.get_total_deaths());
			config.set(uuid+".TotalDmg", cb.get_total_dmg_done());
			config.set(uuid+".TotalTakenDmg", cb.get_total_dmg_taken());
			config.set(uuid+".TotalBetWinsAmount", cb.get_total_bet_wins_amount());
			config.set(uuid+".TotalBetLostAmount", cb.get_total_bet_lost_amount());
			
			//weekly
			config2.set(uuid+".Name",cb.get_weekly().get_pName());
			config2.set(uuid+".Wins", cb.get_weekly().get_Wins());
			config2.set(uuid+".Loses", cb.get_weekly().get_Loses());
			config2.set(uuid+".Kills",cb.get_weekly().get_total_kills());
			config2.set(uuid+".Deaths", cb.get_weekly().get_total_deaths());
			config2.set(uuid+".TotalDmg", cb.get_weekly().get_total_dmg_done());
			config2.set(uuid+".TotalTakenDmg", cb.get_weekly().get_total_dmg_taken());
			config2.set(uuid+".TotalBetWinsAmount", cb.get_weekly().get_total_bet_wins_amount());
			config2.set(uuid+".TotalBetLostAmount", cb.get_weekly().get_total_bet_lost_amount());
			
			//pvp
			for(Entry<UUID, PlayerVsPlayerBoard> pvp: cb.get_pvp_boards().entrySet())
			{
				String targetUUID = pvp.getKey().toString();
				PlayerVsPlayerBoard pvp_board = pvp.getValue();
				config3.set(uuid+".Name", cb.get_pName());
				config3.set(uuid+".vs."+targetUUID+".Wins", pvp_board.get_wins());
				config3.set(uuid+".vs."+targetUUID+".Lost", pvp_board.get_lost());
				config3.set(uuid+".vs."+targetUUID+".TotalBetWinsAmount", pvp_board.get_total_bet_wons_amount());
				config3.set(uuid+".vs."+targetUUID+".TotalBetLostAmount", pvp_board.get_total_bet_lost_amount());
			}
		}
		cm.saveConfig();
		cm2.saveConfig();
		cm3.saveConfig();
		cm4.saveConfig();
	}
	
	@Override
	public void loadFromFile() 
	{
		ConfigMaker cm;
		FileConfiguration config;
		CombatPlayerBoard cb;
		cm = new ConfigMaker(_main, _path + "/all_time.yml");
				
		if(cm.isExists())
		{
			config = cm.getConfig();
			for (String key : config.getConfigurationSection("").getKeys(false)) 
			{
				UUID uuid_real = UUID.fromString(key);
				String uuid = key;
				cb = new CombatPlayerBoard(config.getString(uuid+".Name"),uuid_real);
				cb.set_Wins(config.getInt(uuid+".Wins"));
				cb.set_Loses(config.getInt(uuid+".Loses"));
				cb.set_total_kills(config.getInt(uuid+".Kills"));
				cb.set_total_deaths(config.getInt(uuid+".Deaths"));
				cb.set_total_dmg_done(config.getDouble(uuid+".TotalDmg"));
				cb.set_total_dmg_taken(config.getDouble(uuid+".TotalTakenDmg"));
				cb.set_total_bet_wins_amount(config.getDouble(uuid+".TotalBetWinsAmount"));
				cb.set_total_bet_lost_amount(config.getDouble(uuid+".TotalBetLostAmount"));
				_boards_alltime.put(uuid_real, cb);
			}
		}
		
		
		cm = new ConfigMaker(_main, _path +"/weekly.yml");
		if(cm.isExists())
		{
			config = cm.getConfig();
			for (String key : config.getConfigurationSection("").getKeys(false)) 
			{
				UUID uuid_real = UUID.fromString(key);
				String uuid = key;
				cb = (CombatPlayerBoard)_boards_alltime.get(uuid_real);
				if(cb == null)
					cb = new CombatPlayerBoard("noName", uuid_real);
				
				cb.set_pName(config.getString(uuid+".Name"));
				cb.checkWeekly();
				cb.get_weekly().set_Wins(config.getInt(uuid+".Wins"));
				cb.get_weekly().set_Loses(config.getInt(uuid+".Loses"));
				cb.get_weekly().set_total_kills(config.getInt(uuid+".Kills"));
				cb.get_weekly().set_total_deaths(config.getInt(uuid+".Deaths"));
				cb.get_weekly().set_total_dmg_done(config.getDouble(uuid+".TotalDmg"));
				cb.get_weekly().set_total_dmg_taken(config.getDouble(uuid+".TotalTakenDmg"));
				cb.get_weekly().set_total_bet_wins_amount(config.getDouble(uuid+".TotalBetWinsAmount"));
				cb.get_weekly().set_total_bet_lost_amount(config.getDouble(uuid+".TotalBetLostAmount"));
				_boards_alltime.put(uuid_real, cb);
			}
		}
		
		cm = new ConfigMaker(_main, _path + "/pvp_stats.yml");
		if(cm.isExists())
		{
			config = cm.getConfig();
			for (String key : config.getConfigurationSection("").getKeys(false)) 
			{
				UUID uuid_real = UUID.fromString(key);
				String uuid = key;
				cb = (CombatPlayerBoard)_boards_alltime.get(uuid_real);
				if(cb == null)
					cb = new CombatPlayerBoard("noName", uuid_real);
				
				cb.set_pName(config.getString(uuid+".Name"));
				PlayerVsPlayerBoard pvp_board = new PlayerVsPlayerBoard(UUID.randomUUID());
				for (String key2 : config.getConfigurationSection(key+".vs").getKeys(false)) 
				{
					String uuid_vs = key2;
					UUID uuid_vs_real = UUID.fromString(uuid_vs);
					pvp_board.set_uuid(uuid_vs_real);
					pvp_board.set_wins(config.getInt(uuid+".vs."+uuid_vs+".Wins"));
					pvp_board.set_lost(config.getInt(uuid+".vs."+uuid_vs+".Lost"));
					pvp_board.set_total_bet_wons_amount(config.getDouble(uuid+".vs."+uuid_vs+".TotalBetWinsAmount"));
					pvp_board.set_total_bet_lost_amount(config.getDouble(uuid+".vs."+uuid_vs+".TotalBetLostAmount"));
					cb.get_pvp_boards().put(uuid_vs_real, pvp_board);
				}
				_boards_alltime.put(uuid_real, cb);
			}
		}
		
		cm = new ConfigMaker(_main, _path+"/LeaderBoardSettings.yml");
		if(cm.isExists())
		{
			config = cm.getConfig();
			_weeklyStartDate = LocalDate.parse(config.getString("WeeklyStartDate"));
			_weeklyEndDate = LocalDate.parse(config.getString("WeeklyEndDate"));
			
			if(_weeklyEndDate.isBefore(LocalDate.now()))
			{
				_weeklyStartDate = LocalDate.now();
				_weeklyEndDate = _weeklyStartDate.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
				for(PlayerBoard b : _boards_alltime.values())
				{
					CombatPlayerBoard bo = (CombatPlayerBoard) b;
					bo.set_weekly(new CombatPlayerBoard(bo.get_pName(), bo.get_uuid()));
				}
				
			}
		}

	}
	
	@Override
	public PlayerBoard getPlayerBoard(UUID uuid) 
	{
		return _boards_alltime.get(uuid);
	}
	
	public PlayerBoard getPlayerBoard(Player p) 
	{
		if(_boards_alltime.get(p.getUniqueId()) == null)
			_boards_alltime.put(p.getUniqueId(), new CombatPlayerBoard(p.getName(), p.getUniqueId()));
		return _boards_alltime.get(p.getUniqueId());
	}

	@Override
	public void setPlayerBoard(UUID uuid, CombatPlayerBoard board) 
	{
		_boards_alltime.put(uuid, board);
		
	}

	@Override
	public void showStats(Player p) 
	{
		new CombatLeaderBoardStats(_main, p);
		
	}

	void runnableAsync()
	{
		if(_runnable != null)
			_runnable.cancel();
		
		_runnable = new BukkitRunnable() 
		{		
			@Override
			public void run() 
			{
				saveToFile();
			}
		}.runTaskTimerAsynchronously(_main, 20*60*29, 20*60*30);
	}
}
