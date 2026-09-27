package imu.iMiniGames.Leaderbords;

import java.util.HashMap;
import java.util.UUID;

public abstract class PlayerBoard 
{
	String _pName;
	UUID _uuid;
	
	int _Wins = 0;
	int _Loses = 0;

	double _total_bet_wins_amount = 0;
	double _total_bet_lost_amount = 0;
	
	HashMap<UUID, PlayerVsPlayerBoard> _pvp_target_board = new HashMap<>();
	
	public PlayerBoard(String name, UUID uuid) 
	{
		_pName = name;
		_uuid = uuid;
	}

	public String get_pName() {
		return _pName;
	}

	public void set_pName(String _pName) {
		this._pName = _pName;
	}

	public UUID get_uuid() {
		return _uuid;
	}

	public void set_uuid(UUID _uuid) {
		this._uuid = _uuid;
	}

	public int get_Wins() {
		return _Wins;
	}

	public void set_Wins(int _Wins) {
		this._Wins = _Wins;
	}

	public int get_Loses() {
		return _Loses;
	}

	public void set_Loses(int _Loses) {
		this._Loses = _Loses;
	}

	public double get_total_bet_wins_amount() {
		return _total_bet_wins_amount;
	}

	public void set_total_bet_wins_amount(double _total_bet_wins_amount) {
		this._total_bet_wins_amount = _total_bet_wins_amount;
	}

	public double get_total_bet_lost_amount() {
		return _total_bet_lost_amount;
	}

	public void set_total_bet_lost_amount(double _total_bet_lost_amount) {
		this._total_bet_lost_amount = _total_bet_lost_amount;
	}

	public HashMap<UUID,  PlayerVsPlayerBoard> get_pvp_boards() {
		return _pvp_target_board;
	}
	
	public void putPvpBoard(UUID target, PlayerVsPlayerBoard board)
	{
		_pvp_target_board.put(target, board);
	}
	
	public PlayerVsPlayerBoard get_pvp_playerBoard(UUID target)
	{
		if(!_pvp_target_board.containsKey(target))
		{
			PlayerVsPlayerBoard board =  new PlayerVsPlayerBoard(target);	
			_pvp_target_board.put(target, board);
		}
		return _pvp_target_board.get(target);
	}

	public HashMap<UUID, PlayerVsPlayerBoard> get_pvp_target_board() {
		return _pvp_target_board;
	}
	
	
}
