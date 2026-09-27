package imu.iMiniGames.Leaderbords;

import java.util.UUID;

public class CombatPlayerBoard extends PlayerBoard
{
	int _total_kills = 0;
	int _total_deaths = 0;
	double _total_dmg_done = 0;
	double _total_dmg_taken = 0;
	
	CombatPlayerBoard _weekly = null;

	public CombatPlayerBoard(String name, UUID uuid) 
	{
		super(name, uuid);
	}
	
	public CombatPlayerBoard get_weekly() {
		return _weekly;
	}
	
	public void checkWeekly()
	{
		if(_weekly == null)
		{
			_weekly = new CombatPlayerBoard(_pName, _uuid);
		}
	}
	
	public void set_weekly(CombatPlayerBoard _weekly) {
		this._weekly = _weekly;
	}

	public int get_total_kills() {
		return _total_kills;
	}

	public void set_total_kills(int _total_kills) {
		this._total_kills = _total_kills;
	}

	public int get_total_deaths() {
		return _total_deaths;
	}

	public void set_total_deaths(int _total_deaths) {
		this._total_deaths = _total_deaths;
	}

	public double get_total_dmg_done() {
		return _total_dmg_done;
	}

	public void set_total_dmg_done(double _total_dmg_done) {
		this._total_dmg_done = _total_dmg_done;
	}

	public double get_total_dmg_taken() {
		return _total_dmg_taken;
	}

	public void set_total_dmg_taken(double _total_dmg_taken) {
		this._total_dmg_taken = _total_dmg_taken;
	}

	
	
}
