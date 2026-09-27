package imu.iMiniGames.Invs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;
import imu.iMiniGames.Leaderbords.CombatLeaderBoard;
import imu.iMiniGames.Leaderbords.CombatPlayerBoard;
import imu.iMiniGames.Leaderbords.PlayerBoard;
import imu.iMiniGames.Leaderbords.PlayerVsPlayerBoard;
import imu.iMiniGames.Main.ImusMiniGames;
import net.md_5.bungee.api.ChatColor;

public class CombatLeaderBoardStats extends CustomInvLayout  
{
	String pd_buttonType = "img.buttonType";
	CombatLeaderBoard _leaderboards;
	CombatPlayerBoard _board;

	int _page = 0;
	BUTTON _state = BUTTON.NONE;
	SORT_TYPE _sortType = SORT_TYPE.GAMES;
	boolean _sortConfirm = true;
	boolean _sorted = false;
	
	ArrayList<PlayerBoard> _boards;
	ArrayList<PlayerBoard> _boards_weekly = new ArrayList<>();
	ArrayList<PlayerVsPlayerBoard> _pvp_boards = new ArrayList<>();
	
	
	boolean _ascending = false; //ascending
	boolean _weekly = false;
	
	ImusMiniGames _main;
	Cooldowns _cds = new Cooldowns();
	public CombatLeaderBoardStats(ImusMiniGames main, Player player) {
		super(main, player, ChatColor.BLACK + "" + ChatColor.BOLD + "====== Leaderboards ======", 9 * 3);
		
		_main = main;
		_leaderboards = main.get_combatManager().getLeaderBoard();
		_board = (CombatPlayerBoard) _leaderboards.getPlayerBoard(player);
		_pvp_boards = new ArrayList<>(_board.get_pvp_target_board().values());
		_boards = new ArrayList<>(_leaderboards.getBoards().values());
		
		for(PlayerBoard b : _boards)
		{
			CombatPlayerBoard bo = (CombatPlayerBoard) b;
			bo.checkWeekly();
			_boards_weekly.add( bo.get_weekly());
		}
		
		
		openThis();
		refreshMainMenu();

//		for (int i = 0; i < 18; i++) 
//		{
//			Random r = new Random();
//			CombatPlayerBoard cp = new CombatPlayerBoard("Random123", UUID.randomUUID());
//			cp.set_Loses(r.nextInt(100));
//			cp.set_Wins(r.nextInt(100));
//			cp.set_total_deaths(r.nextInt(100));
//			cp.set_total_kills(r.nextInt(100));
//			_boards.add(cp);
//			
//			PlayerVsPlayerBoard pvp = new PlayerVsPlayerBoard(UUID.randomUUID());
//			pvp.set_wins(r.nextInt(100));
//			pvp.set_lost(r.nextInt(100));
//			pvp.set_total_bet_lost_amount(r.nextInt(1000));
//			pvp.set_total_bet_wons_amount(r.nextInt(1000));
//			_pvp_boards.add(pvp);
//		}
	}
	
	void defValues()
	{
		_page = 0;
		_state = BUTTON.NONE;
		_sortType = SORT_TYPE.GAMES;
		_sortConfirm = true;
		_sorted = false;
		_ascending = false;
	}
	
	public enum BUTTON {
		NONE, ALL_TIME, WEEKLY, PVP, BACK, PAGE_RIGHT, PAGE_LEFT,CHANGE_SORT;
	}
	
	public enum SORT_TYPE{
		WINS,LOSES, WIN_RATIO, GAMES, KILLS, DEATHS, KD_RATIO, DAMAGE_DONE,DAMAGE_TAKEN, BET_WON,BET_LOST,BET_RATIO;
	}

	void setButton(ItemStack stack, BUTTON b) {
		Metods._ins.setPersistenData(stack, pd_buttonType, PersistentDataType.STRING, b.toString());
	}

	BUTTON getButton(ItemStack stack) {
		String button = Metods._ins.getPersistenData(stack, pd_buttonType, PersistentDataType.STRING);
		if (button != null)
			return BUTTON.valueOf(button);

		return BUTTON.NONE;
	}

	ItemStack setupButton(BUTTON b, Material material, String displayName, int itemSlot) {
		ItemStack sbutton = new ItemStack(material);
		Metods.setDisplayName(sbutton, displayName);
		setButton(sbutton, b);
		_inv.setItem(itemSlot, sbutton);
		return _inv.getItem(itemSlot);
	}

	void refreshMainMenu() {
		defValues();
		for (int i = 0; i < _size; ++i) {
			_inv.setItem(i, null);
		}

		setupButton(BUTTON.ALL_TIME, Material.NETHERITE_INGOT, ChatColor.AQUA + "All time", 9 + 2);
		setupButton(BUTTON.WEEKLY, Material.DIAMOND, ChatColor.AQUA + "Weekly", 9 + 4);
		setupButton(BUTTON.PVP, Material.PLAYER_HEAD, ChatColor.AQUA + "PlayerVsPlayer", 9 + 6);

	}

	double twoDesimals(double d) {
		return Math.round(d * 100.00) / 100.00;
	}

	void setOptionPanel() {
		ItemStack none_item = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
		Metods.setDisplayName(none_item, " ");
		setupButton(BUTTON.BACK, Material.RED_WOOL, ChatColor.AQUA + "BACK", 18);
		setupButton(BUTTON.BACK, Material.RED_WOOL, ChatColor.AQUA + "BACK", _size - 1);
		setupButton(BUTTON.PAGE_RIGHT, Material.BIRCH_SIGN, ChatColor.AQUA + ">>>", 23);
		setupButton(BUTTON.PAGE_LEFT, Material.BIRCH_SIGN, ChatColor.AQUA + "<<<", 21);
		_inv.setItem(19, none_item);
		_inv.setItem(20, none_item);
		_inv.setItem(22, none_item);
		_inv.setItem(24, none_item);
		_inv.setItem(25, none_item);
	}
	
	void setSortPanel()
	{
		ItemStack s = new ItemStack(Material.BOOK);
		Metods.setDisplayName(s, ChatColor.AQUA + "SORT BY..");
		Metods._ins.addLore(s, Metods.msgC("&5MODE: "+ (!_ascending ? "&cDescending " : "&2Ascending ") + "&e(&bChance by Middle Click!&e)"), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.GAMES ? "&e&l" : "")+"GAMES "			+(!(!_sortConfirm && _sortType == SORT_TYPE.GAMES) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.WINS ? "&e&l" : "")+"WINS "				+(!(!_sortConfirm && _sortType == SORT_TYPE.WINS) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.LOSES ? "&e&l" : "")+"LOSES "			+(!(!_sortConfirm && _sortType == SORT_TYPE.LOSES) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.WIN_RATIO ? "&e&l" : "")+"WIN RATIO "	+(!(!_sortConfirm && _sortType == SORT_TYPE.WIN_RATIO) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.KILLS ? "&e&l" : "")+"KILLS " 			+(!(!_sortConfirm && _sortType == SORT_TYPE.KILLS) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.DEATHS ? "&e&l" : "")+"DEATHS " 			+(!(!_sortConfirm && _sortType == SORT_TYPE.DEATHS) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.KD_RATIO ? "&e&l" : "")+"KD RATIO "		+(!(!_sortConfirm && _sortType == SORT_TYPE.KD_RATIO) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.BET_WON ? "&e&l" : "")+"BET WON "		+(!(!_sortConfirm && _sortType == SORT_TYPE.BET_WON) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.BET_LOST ? "&e&l" : "")+"BET LOST "		+(!(!_sortConfirm && _sortType == SORT_TYPE.BET_LOST) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.BET_RATIO ? "&e&l" : "")+"BET RATIO"		+(!(!_sortConfirm && _sortType == SORT_TYPE.BET_RATIO) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.DAMAGE_DONE ? "&e&l" : "")+"DMG DONE "	+(!(!_sortConfirm && _sortType == SORT_TYPE.DAMAGE_DONE) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.DAMAGE_TAKEN ? "&e&l" : "")+"DMG TAKEN "	+(!(!_sortConfirm && _sortType == SORT_TYPE.DAMAGE_TAKEN) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		
		setButton(s, BUTTON.CHANGE_SORT);
		_inv.setItem(22, s);
		
		
	}
	
	void setSortPanelPVP()
	{
		ItemStack s = new ItemStack(Material.BOOK);
		Metods.setDisplayName(s, ChatColor.AQUA + "SORT BY..");
		Metods._ins.addLore(s, Metods.msgC("&5MODE: "+ (!_ascending ? "&cDescending " : "&2Ascending ") + "&e(&bChance by Middle Click!&e)"), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.GAMES ? "&e&l" : "")+"GAMES "			+(!(!_sortConfirm && _sortType == SORT_TYPE.GAMES) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.WINS ? "&e&l" : "")+"WINS "				+(!(!_sortConfirm && _sortType == SORT_TYPE.WINS) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.LOSES ? "&e&l" : "")+"LOSES "			+(!(!_sortConfirm && _sortType == SORT_TYPE.LOSES) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.WIN_RATIO ? "&e&l" : "")+"WIN RATIO "	+(!(!_sortConfirm && _sortType == SORT_TYPE.WIN_RATIO) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.BET_WON ? "&e&l" : "")+"BET WON "		+(!(!_sortConfirm && _sortType == SORT_TYPE.BET_WON) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.BET_LOST ? "&e&l" : "")+"BET LOST "		+(!(!_sortConfirm && _sortType == SORT_TYPE.BET_LOST) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		Metods._ins.addLore(s, Metods.msgC("&9"+(_sortType == SORT_TYPE.BET_RATIO ? "&e&l" : "")+"BET RATIO "	+(!(!_sortConfirm && _sortType == SORT_TYPE.BET_RATIO) ? "":"&e(&5Confirm by Right Click!&e)")), true);
		
		setButton(s, BUTTON.CHANGE_SORT);
		_inv.setItem(22, s);
		
		
	}
	
	void refreshAllTimeAndWeekly() {

		for (int i = 0; i < _size; ++i) {
			_inv.setItem(i, null);
		}

		ItemStack stats_item;

		int pages = (int) Math.round(((_boards.size() - 1) / (_size - 9)) + 0.5) - 1;
		if (_page > pages)
			_page = pages;

		setOptionPanel();
		setSortPanel();
		
		ArrayList<PlayerBoard> boards = (_weekly ? _boards_weekly : _boards);
		int count = _page * (_size - 9);
		if(!_sorted)
		{
			Comparator<PlayerBoard> comparator = new Comparator<PlayerBoard>() {

				@Override
				public int compare(PlayerBoard b1, PlayerBoard b2) 
				{
					CombatPlayerBoard bo1 = (CombatPlayerBoard) b1;
					CombatPlayerBoard bo2 = (CombatPlayerBoard) b2;
					
					switch (_sortType) 
					{
					case KILLS:
						if(!_ascending)
							return (bo2.get_total_kills() - bo1.get_total_kills());
						return (bo1.get_total_kills() - bo2.get_total_kills());
					case DEATHS:
						if(!_ascending)
							return (bo2.get_total_deaths() - bo1.get_total_deaths());
						return (bo1.get_total_deaths() - bo2.get_total_deaths());
					case GAMES:
						if(!_ascending)
							return ((bo2.get_Wins()+bo2.get_Loses()) - (bo1.get_Wins() + bo1.get_Loses() ));
						return ((bo1.get_Wins()+bo1.get_Loses()) - (bo2.get_Wins() + bo2.get_Loses() ));
						
					case WINS:
						if(!_ascending)
							return (bo2.get_Wins() - bo1.get_Wins());
						return (bo1.get_Wins() - bo2.get_Wins());
					case LOSES:
						if(!_ascending)
							return (bo2.get_Loses() - bo1.get_Loses());
						return (bo1.get_Loses() - bo2.get_Loses());
					case WIN_RATIO:
						double wr2 = (bo2.get_Wins() / (bo2.get_Loses() == 0 ? 1.0 : (double)bo2.get_Loses()));
						double wr1 = (bo1.get_Wins() / (bo1.get_Loses() == 0 ? 1.0 : (double)bo1.get_Loses()));
						if(!_ascending)
							return ((wr1 - wr2) < 0 ? 1 : -1);
						return ((wr2 - wr1) < 0 ? 1 : -1);
					case KD_RATIO:
						
						double r2 = bo2.get_total_kills() / (bo2.get_total_deaths() == 0 ? 1.0 : (double) bo2.get_total_deaths());
						double r1 = bo1.get_total_kills() / (bo1.get_total_deaths() == 0 ? 1.0 : (double) bo1.get_total_deaths());
						if(!_ascending)
							return ((r1 - r2) < 0 ? 1 : -1);
						return ((r2 - r1) < 0 ? 1 : -1);
						
					case BET_WON:
						
						if(!_ascending)
							return ((bo2.get_total_bet_wins_amount() - bo1.get_total_bet_wins_amount()) > 0 ? 1 : -1);
						return ((bo1.get_total_bet_wins_amount() - bo2.get_total_bet_wins_amount()) > 0 ? 1 : -1);
					case BET_LOST:
						if(!_ascending)
							return ((bo2.get_total_bet_lost_amount() - bo1.get_total_bet_lost_amount()) > 0 ? 1 : -1);
						return ((bo2.get_total_bet_lost_amount() - bo1.get_total_bet_lost_amount()) > 0 ? 1 : -1);
					case BET_RATIO:
						double bet1 = bo1.get_total_bet_wins_amount() / (bo1.get_total_bet_lost_amount() == 0 ? 1.0 : (double)bo1.get_total_bet_lost_amount());
						double bet2 = bo2.get_total_bet_wins_amount() / (bo2.get_total_bet_lost_amount() == 0 ? 1.0 : (double)bo2.get_total_bet_lost_amount());
						if(!_ascending)
							return ((bet1 - bet2)< 0 ? 1 : -1);
						return ((bet2 - bet1) < 0 ? 1 : -1);
					case DAMAGE_DONE:
						if(!_ascending)
							return ((bo1.get_total_dmg_done() - bo2.get_total_dmg_done())< 0 ? 1 : -1);
						return ((bo2.get_total_dmg_done() - bo1.get_total_dmg_done())< 0 ? 1 : -1);
					case DAMAGE_TAKEN:
						if(!_ascending)
							return ((bo1.get_total_dmg_taken() - bo2.get_total_dmg_taken()) < 0 ? 1 : -1);
						return ((bo2.get_total_dmg_taken() - bo1.get_total_dmg_taken()) < 0 ? 1 : -1);
					default:
						break;
										
					}
					return 0;	
				}
			};
			Collections.sort(boards,comparator);
			
			_sorted=true;
		}		
		
		
		for (int i = 0; i < _size - 9; ++i) {
			if (count >= boards.size())
				break;

			CombatPlayerBoard cp = (CombatPlayerBoard) boards.get(count++);
			stats_item = (cp.get_uuid().equals(_player.getUniqueId()) ? new ItemStack(Material.SKELETON_SKULL):new ItemStack(Material.WITHER_SKELETON_SKULL));
			Metods.setDisplayName(stats_item, ChatColor.translateAlternateColorCodes('&', "&6&l" + cp.get_pName()));

			Metods._ins.addLore(stats_item, Metods.msgC("&5Games played: &b" + (cp.get_Wins() + cp.get_Loses())), true);
			Metods._ins.addLore(stats_item, Metods.msgC(""), true);
			Metods._ins.addLore(stats_item, Metods.msgC("&aWins&7/&cLoses&7/&eRatio: &a" + cp.get_Wins() + "&7/&c"
					+ cp.get_Loses() + "&7/&e" + (cp.get_Wins() / (cp.get_Loses() == 0 ? 1 : cp.get_Loses()))), true);
			Metods._ins.addLore(stats_item,
					Metods.msgC("&aKills&7/&cDeaths&7/&eRatio: &a" + cp.get_total_kills() + "&7/&c"
							+ cp.get_total_deaths() + "&7/&e"
							+twoDesimals(((double)cp.get_total_kills() / (cp.get_total_deaths() == 0.0 ? 1.0 : (double)cp.get_total_deaths())))),
					true);
			Metods._ins.addLore(stats_item, Metods.msgC("&6Bets won: &9" + cp.get_total_bet_wins_amount()), true);
			Metods._ins.addLore(stats_item, Metods.msgC("&6Bets lost: &9" + cp.get_total_bet_lost_amount()), true);
			Metods._ins.addLore(stats_item, Metods.msgC("&eBets ratio: &9" + twoDesimals(cp.get_total_bet_wins_amount() / (cp.get_total_bet_lost_amount() == 0 ? 1.0 : (double)cp.get_total_bet_lost_amount()))), true);
			Metods._ins.addLore(stats_item, Metods.msgC("&3Damage done: &9" + twoDesimals(cp.get_total_dmg_done()/2)), true);
			Metods._ins.addLore(stats_item, Metods.msgC("&3Damage taken: &9" + twoDesimals(cp.get_total_dmg_taken()/2)), true);
			_inv.setItem(i, stats_item);

		}

	}

	void refreshPVP()
	{
		for (int i = 0; i < _size; ++i) {
			_inv.setItem(i, null);
		}

		ItemStack stats_item;

		int pages = (int) Math.round(((_pvp_boards.size() - 1) / (_size - 9)) + 0.5) - 1;
		if (_page > pages)
			_page = pages;

		setOptionPanel();
		setSortPanelPVP();

		int count = _page * (_size - 9);
		
		if(_pvp_boards == null) return;
		
		if(!_sorted)
		{
			Comparator<PlayerVsPlayerBoard> comparator = new Comparator<PlayerVsPlayerBoard>() {

				@Override
				public int compare(PlayerVsPlayerBoard bo1, PlayerVsPlayerBoard bo2) 
				{
					
					switch (_sortType) 
					{
					case GAMES:
						if(!_ascending)
							return ((bo2.get_wins()+bo2.get_lost()) - (bo1.get_wins() + bo1.get_lost()));
						return ((bo1.get_wins()+bo1.get_lost()) - (bo2.get_wins() + bo2.get_lost()));						
					case WINS:
						if(!_ascending)
							return (bo2.get_wins() - bo1.get_wins());
						return (bo1.get_wins() - bo2.get_wins());
					case LOSES:
						if(!_ascending)
							return (bo2.get_lost() - bo1.get_lost());
						return (bo1.get_lost() - bo2.get_lost());
					case WIN_RATIO:
						double wr2 = (bo2.get_wins() / (bo2.get_lost() == 0 ? 1.0 : (double)bo2.get_lost()));
						double wr1 = (bo1.get_wins() / (bo1.get_lost() == 0 ? 1.0 : (double)bo1.get_lost()));
						if(!_ascending)
							return ((wr1 - wr2) < 0 ? 1 : -1);
						return ((wr2 - wr1) < 0 ? 1 : -1);

					case BET_WON:
						
						if(!_ascending)
							return ((bo2.get_total_bet_wons_amount() - bo1.get_total_bet_wons_amount()) > 0 ? 1 : -1);
						return ((bo1.get_total_bet_wons_amount() - bo2.get_total_bet_wons_amount()) > 0 ? 1 : -1);
					case BET_LOST:
						if(!_ascending)
							return ((bo2.get_total_bet_lost_amount() - bo1.get_total_bet_lost_amount()) > 0 ? 1 : -1);
						return ((bo2.get_total_bet_lost_amount() - bo1.get_total_bet_lost_amount()) > 0 ? 1 : -1);
					case BET_RATIO:
						double bet1 = bo1.get_total_bet_wons_amount() / (bo1.get_total_bet_lost_amount() == 0 ? 1.0 : (double)bo1.get_total_bet_lost_amount());
						double bet2 = bo2.get_total_bet_wons_amount() / (bo2.get_total_bet_lost_amount() == 0 ? 1.0 : (double)bo2.get_total_bet_lost_amount());
						if(!_ascending)
							return ((bet1 - bet2)< 0 ? 1 : -1);
						return ((bet2 - bet1) < 0 ? 1 : -1);
					default:
						break;
										
					}
					return 0;	
				}
			};
			Collections.sort(_pvp_boards,comparator);		
			_sorted=true;
		}		
		
		for (int i = 0; i < _size - 9; ++i) {
			
			if (count >= _pvp_boards.size())
				break;

			PlayerVsPlayerBoard pvp_board = _pvp_boards.get(count++);
			stats_item = new ItemStack(Material.WITHER_SKELETON_SKULL);
			Metods.setDisplayName(stats_item, ChatColor.translateAlternateColorCodes('&', "&6&l" + _main.get_leaderboardUUIDData().getName(pvp_board.get_uuid())));

			Metods._ins.addLore(stats_item, Metods.msgC("&5Games played with him/her: &b" + (pvp_board.get_wins() + pvp_board.get_lost())), true);
			Metods._ins.addLore(stats_item, Metods.msgC(""), true);
			Metods._ins.addLore(stats_item, Metods.msgC("&aYou have won agains him/her: &9"+pvp_board.get_wins()),true);
			Metods._ins.addLore(stats_item, Metods.msgC("&cYou have lost agains him/her: &9"+pvp_board.get_lost()),true);
			Metods._ins.addLore(stats_item, Metods.msgC("&eWin/Lost ratio: &9"+twoDesimals((pvp_board.get_wins() / (pvp_board.get_lost() == 0 ? 1.0 : (double)pvp_board.get_lost())))),true);
			Metods._ins.addLore(stats_item, Metods.msgC("&6Bets &2won&6 from him/her: &9" + pvp_board.get_total_bet_wons_amount()), true);
			Metods._ins.addLore(stats_item, Metods.msgC("&6Bets &clost&6 to him/her: &9" + pvp_board.get_total_bet_lost_amount()), true);
			Metods._ins.addLore(stats_item, Metods.msgC("&eBets ratio: &9" + twoDesimals(pvp_board.get_total_bet_wons_amount() / (pvp_board.get_total_bet_lost_amount() == 0 ? 1.0 : (double)pvp_board.get_total_bet_lost_amount()))), true);

			_inv.setItem(i, stats_item);

		}
	}
	void pressButton(BUTTON button, ClickType cType) 
	{
		if (_state != button)
			_page = 0;

		switch (button) {
		case NONE:
			return;
		case ALL_TIME:
			_state = button;
			_weekly = false;
			refreshAllTimeAndWeekly();
			break;
		case BACK:
			_state = BUTTON.NONE;
			refreshMainMenu();
			break;
		case WEEKLY:
			_state = button;
			_weekly = true;
			refreshAllTimeAndWeekly();
			break;
		case PVP:
			_state = button;
			refreshPVP();
			break;
		case PAGE_RIGHT:
			_page++;
			pressButton(_state,cType);
			break;
		case PAGE_LEFT:
			_page--;
			if (_page < 0)
				_page = 0;

			pressButton(_state,cType);
			break;
			
		case CHANGE_SORT:
			if(cType.isRightClick() && !_sortConfirm)
			{
				_sortConfirm = true;
				_sorted = false;
				pressButton(_state,cType);
				return;
			}
			
			if(cType == ClickType.MIDDLE)
			{
				_ascending = ((_ascending) ? false : true);
				_sorted = false;
				_sortConfirm = false;
				if(_state == BUTTON.PVP)
				{
					setSortPanelPVP();
					return;
				}
				setSortPanel();
				return;
				
			}
			
			if(!cType.isLeftClick())
				return;
			
			if(_state == BUTTON.ALL_TIME || _state == BUTTON.WEEKLY)
			{
				switch (_sortType) 
				{
				//order matters
				case GAMES:
					_sortType = SORT_TYPE.WINS;				
					break;
				case WINS:
					_sortType = SORT_TYPE.LOSES;	
					break;
				case LOSES:
					_sortType = SORT_TYPE.WIN_RATIO;
					break;
				case WIN_RATIO:
					_sortType = SORT_TYPE.KILLS;
					break;
				case KILLS:
					_sortType = SORT_TYPE.DEATHS;
					break;
				case DEATHS:
					_sortType = SORT_TYPE.KD_RATIO;
					break;
				case KD_RATIO:
					_sortType = SORT_TYPE.BET_WON;
					break;
				case BET_WON:
					_sortType = SORT_TYPE.BET_LOST;
					break;
				case BET_LOST:
					_sortType = SORT_TYPE.BET_RATIO;
					break;
				case BET_RATIO:
					_sortType = SORT_TYPE.DAMAGE_DONE;
					break;
				case DAMAGE_DONE:
					_sortType = SORT_TYPE.DAMAGE_TAKEN;
					break;
				case DAMAGE_TAKEN:
					_sortType = SORT_TYPE.GAMES;
					break;
				
				default:
					break;
				}
				_sortConfirm = false;
				setSortPanel();
			}
			if(_state == BUTTON.PVP)
			{
				switch (_sortType) 
				{
				//order matters
				case GAMES:
					_sortType = SORT_TYPE.WINS;				
					break;
				case WINS:
					_sortType = SORT_TYPE.LOSES;	
					break;
				case LOSES:
					_sortType = SORT_TYPE.WIN_RATIO;
					break;
				case WIN_RATIO:
					_sortType = SORT_TYPE.BET_WON;
					break;
				case BET_WON:
					_sortType = SORT_TYPE.BET_LOST;
					break;
				case BET_LOST:
					_sortType = SORT_TYPE.BET_RATIO;
					break;
				case BET_RATIO:
					_sortType = SORT_TYPE.GAMES;
					break;
				
				default:
					break;
				}
				_sortConfirm = false;
				setSortPanelPVP();
			}
			
							
			return;
			
		default:
			break;

		}
	}

	

	@Override
	public void invClosed(InventoryCloseEvent arg0) {
		
	}

	@Override
	public void onClickInsideInv(InventoryClickEvent e) 
	{
//		if(!_cds.isCooldownReady("press")) return;
//		
//		_cds.setCooldownInSeconds("press", 0.2);
		ItemStack stack = e.getCurrentItem();

		BUTTON button = getButton(stack);
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				pressButton(button, e.getClick());
			}
		}.runTaskAsynchronously(_main);
		
	}

	@Override
	public void setupButtons() {
		
	}
}
