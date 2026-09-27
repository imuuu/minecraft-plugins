package imu.iAPI.LootTables;

public interface ILootTableItem<T>
{
    public T get_value();
    public void set_value(T _value);

    public int get_weight();
    public void set_weight(int _weight);

    public int get_maxAmount();
    public void set_maxAmount(int _maxAmount);

    public int get_minAmount();
    public void set_minAmount(int _minAmount);
}
