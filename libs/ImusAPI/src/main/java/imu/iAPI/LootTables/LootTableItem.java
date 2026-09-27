package imu.iAPI.LootTables;

public class LootTableItem<T> implements ILootTableItem<T>
{
	private T _value;
	private int _weight;
	private int _maxAmount = -1;
	private int _minAmount = 0;

	public LootTableItem(T value, int weight)
	{
		this._value = value;
		this._weight = weight;
	}

	public LootTableItem(T value, int weight, int maxAmount)
	{
		this._value = value;
		this._weight = weight;
		this._maxAmount = maxAmount;
	}

	@Override
	public T get_value()
	{
		return this._value;
	}

	@Override
	public void set_value(T _value)
	{
		this._value = _value;
	}

	@Override
	public int get_weight()
	{
		return this._weight;
	}

	@Override
	public void set_weight(int _weight)
	{
		this._weight = _weight;
	}

	@Override
	public int get_maxAmount()
	{
		return this._maxAmount;
	}

	@Override
	public void set_maxAmount(int _maxAmount)
	{
		this._maxAmount = _maxAmount;
	}

	@Override
	public int get_minAmount()
	{
		return _minAmount;
	}

	@Override
	public void set_minAmount(int _minAmount)
	{
		this._minAmount = _minAmount;
	}
}
