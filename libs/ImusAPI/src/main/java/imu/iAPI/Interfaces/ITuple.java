package imu.iAPI.Interfaces;

public interface ITuple <T,V>
{
	T GetKey();
	V GetValue();

	void SetKey(T key);

	void SetValue(V value);
}
