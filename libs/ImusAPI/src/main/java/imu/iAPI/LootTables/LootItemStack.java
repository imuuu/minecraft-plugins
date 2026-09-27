package imu.iAPI.LootTables;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class LootItemStack implements ILootTableItem<ItemStack>
{
    private ItemStack _stack;
    private int _weight;
    private int _maxAmount = -1;
    private int _minAmount = 0;

    public LootItemStack(ItemStack stack, int weight, int minAmount, int maxAmount)
    {
        _stack = stack;
        _weight = weight;
        _maxAmount = maxAmount;
        _minAmount = minAmount;
    }

    public LootItemStack(Material material, int weight, int minAmount, int maxAmount)
    {
        _stack = new ItemStack(material);
        _weight = weight;
        _maxAmount = maxAmount;
        _minAmount = minAmount;

    }

    @Override
    public ItemStack get_value()
    {
        return _stack;
    }

    @Override
    public void set_value(ItemStack stack)
    {
        _stack = stack;
    }

    @Override
    public int get_weight()
    {
        return _weight;
    }

    @Override
    public void set_weight(int weight)
    {
        _weight = weight;
    }

    @Override
    public int get_maxAmount()
    {
        return _maxAmount;
    }

    @Override
    public void set_maxAmount(int maxAmount)
    {
        _maxAmount = maxAmount;
    }

    @Override
    public int get_minAmount()
    {
        return _minAmount;
    }

    @Override
    public void set_minAmount(int minAmount)
    {
        _minAmount = minAmount;
    }
}
