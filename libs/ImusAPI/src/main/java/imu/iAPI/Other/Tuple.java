package imu.iAPI.Other;

import imu.iAPI.Interfaces.ITuple;

public class Tuple <T,V> implements ITuple<T, V>
{
	T _key;
	V _val;
	
	public Tuple(T key, V value)
	{
		_key = key;
		_val = value;
	}

	@Override
	public T GetKey() {
		return _key;
	}

	@Override
	public V GetValue() {
		return _val;
	}

	@Override
	public void SetKey(T key)
	{
		_key = key;
	}

	@Override
	public void SetValue(V value)
	{
		_val = value;
	}


	@Override
	public String toString() 
	{
		return "key:"+_key + " val: "+_val;
	}
}
