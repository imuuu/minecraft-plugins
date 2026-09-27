package imu.iMiniGames.Handlers;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iAPI.Other.ConfigMaker;
import imu.iMiniGames.Main.ImusMiniGames;
import imu.iMiniGames.Other.MiniGame;
import imu.iMiniGames.Other.MiniGameSpleef;
import imu.iMiniGames.Other.SpleefGameCard;

public class SpleefGameHandler extends GameHandeler implements Listener
{
	int _anti_block_time = 7;

	public SpleefGameHandler(ImusMiniGames main)
	{
		super(main, "Spleef");		
		_main.getServer().getPluginManager().registerEvents(this, _main);
	}
	
	public int get_anti_block_time() {
		return _anti_block_time;
	}


	public void set_anti_block_time(int _anti_block_time) {
		this._anti_block_time = _anti_block_time;
	}
	
	@Override
	public void afterMatchEnd(GameCard gameCard, Player winner) 
	{
		
	}

	@Override
	public MiniGame afterMatchStart(GameCard gameCard) {
		SpleefGameCard spleefGameCard = (SpleefGameCard) gameCard;
		MiniGameSpleef miniGame = new MiniGameSpleef(_main, this, spleefGameCard,"SPLEEF");
		miniGame.set_roundTime(_roundTime);
		
		miniGame.set_anti_stand(_anti_block_time);
		for(UUID uuid : gameCard.get_players_accept().keySet())
		{
			Player p = Bukkit.getPlayer(uuid);
			miniGame.addPlayer(p);
		}
		
		//match starts after this
		return miniGame;
	}

	@Override
	public void afterDefaultRequest(Player p, GameCard card) 
	{
		// TODO Auto-generated method stub
		
	}
	
	public void loadSettingConfig(boolean refresh)
	{

		new BukkitRunnable() 
		{
			
			@Override
			public void run() 
			{
				ConfigMaker cm = new ConfigMaker(_main, "Spleef_settings.yml");
				FileConfiguration config = cm.getConfig();
				if(refresh)
				{
					if(cm.isExists())
					{
						setCd_invite_time(config.getInt("Cd_for_invite_acceptTime(Integer)"));
						setRoundTime(config.getInt("Spleef_roundTime(Integer)"));
						setBet_fee_percent(config.getDouble("Spleef_bet_fee(Double)"));
						_enable_broadcast = config.getBoolean("Enable_spleef_broadCast(Boolean)");
						set_anti_block_time(config.getInt("Spleef_antiBlock_time(Integer)"));
					}
					cm.clearConfig();
				}
				try 
				{
					setCd_invite_time(cm.addDefault("Cd_for_invite_acceptTime", getCd_invite_time(),"Cd_for_invite_acceptTime: how long invite stays before expires"));
					setRoundTime(cm.addDefault("Spleef_roundTime", getRoundTime(),"Spleef_roundTime: Round time for spleef"));
					setBet_fee_percent(cm.addDefault("Spleef_bet_fee", getBet_fee_percent(),"Spleef_bet_fee: How much fee is. Between 0.00 - 1.00 (0.05 = 5%)"));
					_enable_broadcast = (cm.addDefault("Enable_spleef_broadCast", _enable_broadcast,"Enable_spleef_broadCast: If true everybody see in server who startet game and result"));
					set_anti_block_time(cm.addDefault("Spleef_antiBlock_time", get_anti_block_time(),"Spleef_antiBlock_time: How many seconds before anti_block shows. If 0 => disabled"));
					
					cm.addComments();
					
				} catch (Exception e) 
				{
					_main.getServer().getConsoleSender().sendMessage(ChatColor.RED +"WARNING: Something got wrong imusMiniGame fileNamed: "+cm.getFileName());
					_main.getServer().getConsoleSender().sendMessage(ChatColor.RED +"WARNING: Maybe you casted some value as Integer When it should be Double?");
				}		
			}
		}.runTaskAsynchronously(_main);
		
	}

	@Override
	public void beforeMatchStart(GameCard gameCard) {
		// TODO Auto-generated method stub
		
	}
	
}
