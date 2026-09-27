package imu.iMiniGames.Handlers;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iAPI.Other.ConfigMaker;
import imu.iMiniGames.Leaderbords.CombatLeaderBoard;
import imu.iMiniGames.Leaderbords.CombatPlayerBoard;
import imu.iMiniGames.Main.ImusMiniGames;
import imu.iMiniGames.Other.CombatDataCard;
import imu.iMiniGames.Other.CombatGameCard;
import imu.iMiniGames.Other.MiniGame;
import imu.iMiniGames.Other.MiniGameCombat;

public class CombatGameHandler extends GameHandeler
{	
	CombatLeaderBoard _leaderBoard;
	
	Material[] blackList_mat_gear = 
		{
				Material.PAPER,
				Material.SHULKER_BOX,
				Material.TOTEM_OF_UNDYING,


		};
	public CombatGameHandler(ImusMiniGames main)
	{
		super(main,"Combat");		
		_leaderBoard = _main.get_combatManager().getLeaderBoard();
		_main.getServer().getPluginManager().registerEvents(this, _main);//hmm
		
	}

	@Override
	public void afterDefaultRequest(Player p, GameCard card)
	{
		CombatGameCard cCard = (CombatGameCard) card;
		p.sendMessage(ChatColor.GOLD + "Attributs: "+ cCard.getDataCard().getAttributStringWithColor());
		p.sendMessage(ChatColor.GOLD + "Combat Kit: "+ ((CombatDataCard) cCard.getDataCard()).get_kit().get_kitNameWithColor());
		if(((CombatDataCard) cCard.getDataCard()).isOwnGearKit())
		{
			p.sendMessage(ChatColor.GRAY + "Tip: Consumed consumables in arena will not recover after the battle! Mostly food and potions");
		}

	}


	public void loadSettingConfig(boolean refresh)
	{
		new BukkitRunnable() 
		{

			@Override
			public void run() 
			{
				ConfigMaker cm = new ConfigMaker(_main, "Combat_settings.yml");
				FileConfiguration config = cm.getConfig();
				if(refresh)
				{
					if(cm.isExists())
					{
						setCd_invite_time(config.getInt("Cd_for_invite_acceptTime(Integer)"));
						setRoundTime(config.getInt("RoundTime(Integer)"));
						setBet_fee_percent(config.getDouble("Bet_fee(Double)"));
						_enable_broadcast = config.getBoolean("BroadCast(Boolean)");

					}
					cm.clearConfig();
				}
				try 
				{
					setCd_invite_time(cm.addDefault("Cd_for_invite_acceptTime", getCd_invite_time(),"Cd_for_invite_acceptTime: how long invite stays before expires"));
					setRoundTime(cm.addDefault("RoundTime", getRoundTime(),"RoundTime: Round time"));
					setBet_fee_percent(cm.addDefault("Bet_fee", getBet_fee_percent(),"Bet_fee: How much fee is. Between 0.00 - 1.00 (0.05 = 5%)"));
					_enable_broadcast = cm.addDefault("BroadCast", _enable_broadcast,"BroadCast: If true everybody see in server who startet game and result");

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
	public MiniGame afterMatchStart(GameCard gameCard) 
	{
		CombatGameCard combatGameCard = (CombatGameCard) gameCard;
		combatGameCard.setupKits(blackList_mat_gear); //TODO
//		setupOwnGear(combatGameCard);
		
		MiniGame miniGame = new MiniGameCombat(_main, this, combatGameCard, "COMBAT");
		miniGame.set_roundTime(_roundTime);

		
		for(UUID uuid : gameCard.get_players_accept().keySet())
		{
			Player p = Bukkit.getPlayer(uuid);
			miniGame.addPlayer(p);
			if(_leaderBoard.getPlayerBoard(uuid) == null)
			{
				_leaderBoard.setPlayerBoard(uuid, new CombatPlayerBoard(p.getName(), uuid));
			}
			((CombatPlayerBoard)_leaderBoard.getPlayerBoard(uuid)).checkWeekly();
			
		}
		
		//match starts after this
		return miniGame;
	}

	@Override
	public void afterMatchEnd(GameCard gameCard, Player winner)
	{
		
	}

	@Override
	public void beforeMatchStart(GameCard gameCard) 
	{
		// TODO Auto-generated method stub
		
	}
	
//	void setupOwnGea(GameCard gameCard)
//	{
//		CombatDataCard dataCard = (CombatDataCard) gameCard.getDataCard();
//		if(!dataCard.isOwnGearKit())
//			return;
//		
//		for(UUID uuid : gameCard.get_players_accept().keySet())
//		{
//			Player p = Bukkit.getPlayer(uuid);
//			
//		}
//	}



	



}
