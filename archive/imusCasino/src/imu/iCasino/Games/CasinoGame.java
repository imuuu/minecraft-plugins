package imu.iCasino.Games;

public abstract class CasinoGame 
{
	String _name;
	double _bet = 0;
	
	double _bet_min = -1;
	double _bet_max = -1;
	
	public CasinoGame(String name) 
	{
		_name = name;
	}
	
	public void setBetRange(double min, double max)
	{
		_bet_min = min;
		_bet_max = max;
	}

	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}

	public double get_bet() {
		return _bet;
	}

	public void set_bet(double _bet) {
		this._bet = _bet;
	}

	public double get_bet_min() {
		return _bet_min;
	}

	public void set_bet_min(double _bet_min) {
		this._bet_min = _bet_min;
	}

	public double get_bet_max() {
		return _bet_max;
	}

	public void set_bet_max(double _bet_max) {
		this._bet_max = _bet_max;
	}
	
	
}
