package imu.UCI.Other;

import java.util.ArrayList;
import java.util.HashMap;

import imu.UCI.Abilities.Ability;
import imu.UCI.Attributes.Attribute;
import imu.UCI.Gems.Gem;

public class CustomSet 
{
	String _name = "";
	
	int _pieceSize = 0;
	int _socketsSize = 0;
	
	HashMap<Integer, ArrayList<Ability>> _abilities = new HashMap<>(); // size cap and what abilities   //2 set bonues gives double damage 4 setbonues gives triple
	
	ArrayList<Gem> _sockets = new ArrayList<Gem>();
	ArrayList<Attribute> _attributes = new ArrayList<Attribute>();

	public CustomSet(String name, int pieceSize, int sockets) 
	{
		_name = name;
		_pieceSize = pieceSize;
		setSockets(sockets);
		
		System.out.println("CUSTOM SET CREATED: name:"+ name+"pSize"+ pieceSize+ "sSize"+sockets);
	}
	
	public void setSockets(int size)
	{
		_socketsSize = size;
	}
	
	public String get_name() 
	{
		return _name;
	}
	
	public int get_pieceSize() 
	{
		return _pieceSize;
	}
	
	public int get_socketsSize()
	{
		return _socketsSize;
	}
	
	public void set_name(String name) 
	{
		this._name = name;
	}
	
	public void set_pieceSize(int pieceSize) 
	{
		this._pieceSize = pieceSize;
	}
	
	
}
