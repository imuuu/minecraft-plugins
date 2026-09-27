package imu.UCI.INVs;

import java.util.HashMap;

public class CustomInvData 
{
	HashMap<String, Object> _dataContainer = new HashMap<String, Object>();
	
	public void putData(String id, Object obj)
	{
		_dataContainer.put(id, obj);
	}
	
	public void setDataContainer(HashMap<String, Object> dataContainer)
	{
		_dataContainer = dataContainer;
	}
	
	public Object getData(String id)
	{
		return _dataContainer.get(id);
	}
	
	public Object getOrDefData(String id, Object obj)
	{
		return _dataContainer.getOrDefault(id, obj);
	}
}
