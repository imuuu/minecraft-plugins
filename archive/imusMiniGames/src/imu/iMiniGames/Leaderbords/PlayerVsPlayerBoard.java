package imu.iMiniGames.Leaderbords;

import java.util.UUID;

public class PlayerVsPlayerBoard 
{
	private UUID _uuid;
	private int _wins = 0;
	private int _lost = 0;
	
	private double _total_bet_wons_amount = 0;
	private double _total_bet_lost_amount = 0;

	public PlayerVsPlayerBoard(UUID uuid)
	{
		_uuid = uuid;
	}
	public int get_wins() {
		return _wins;
	}

	public void set_wins(int _wins) {
		this._wins = _wins;
	}

	public int get_lost() {
		return _lost;
	}

	public void set_lost(int _lost) {
		this._lost = _lost;
	}

	public double get_total_bet_wons_amount() {
		return _total_bet_wons_amount;
	}

	public void set_total_bet_wons_amount(double _total_bet_wons_amount) {
		this._total_bet_wons_amount = _total_bet_wons_amount;
	}

	public double get_total_bet_lost_amount() {
		return _total_bet_lost_amount;
	}

	public void set_total_bet_lost_amount(double _total_bet_lost_amount) {
		this._total_bet_lost_amount = _total_bet_lost_amount;
	}
	public UUID get_uuid() {
		return _uuid;
	}
	public void set_uuid(UUID _uuid) {
		this._uuid = _uuid;
	}

	
	
}
