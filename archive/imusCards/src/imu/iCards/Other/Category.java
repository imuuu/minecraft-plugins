package imu.iCards.Other;

import java.util.ArrayList;

import imu.iCards.Enums.DROP_EVENT;

public class Category
{
	private String _name;
	
	private ArrayList<DROP_EVENT> _events;
	
	public Category(String name) 
	{
		_name = name.toLowerCase();
		_events = new ArrayList<>();
	}
	
	public void AddDropEvent(DROP_EVENT dropEvent)
	{
		_events.add(dropEvent);
	}
	
	public String GetName()
	{
		return _name;
	}
}
