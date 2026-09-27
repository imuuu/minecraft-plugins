package imu.iCasino.DataCards;

import java.util.HashMap;

import imu.iCasino.Interfaces.PlanerDataCardSlot;

public class CrashDataPlanerCard extends PlanerCard
{
	String _tableName;
	HashMap<Integer, DATA> _datas = new HashMap<>();
	public CrashDataPlanerCard() 
	{
		settupData();
	}
	enum DATA implements PlanerDataCardSlot
	{
		NAME;
	}
	
	void settupData()
	{
		_datas.put(0, DATA.NAME);
	}
	
	public String get_tableName() {
		return _tableName;
	}

	public void set_tableName(String _tableName) 
	{
		this._tableName = _tableName;
		System.out.println("Name set to: "+_tableName);
	}
	
	
	@Override
	public boolean setData(Integer i, String data) 
	{
		switch (_datas.get(i)) 
		{
		case NAME:
			set_tableName(data);
			return true;

		}
		
		return false;
	}
	
	
}
