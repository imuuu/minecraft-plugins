package imu.iAPI.LootTables;

import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class LootTableItemStack extends ImusLootTable
{
    private ImusLootTable _dropAmounts;

    public LootTableItemStack()
    {
        InitDropAmounts();
    }

    private void InitDropAmounts()
    {
        _dropAmounts = new ImusLootTable();
        _dropAmounts.add(5, 100);
        _dropAmounts.add(18, 90);
        _dropAmounts.add(19, 60);
        _dropAmounts.add(28, 45);
        _dropAmounts.add(44, 28);
        _dropAmounts.add(64, 9);
    }


    @Override
    public ItemStack getLoot()
    {
        int randomIndex = ThreadLocalRandom.current().nextInt(getTotalWeight());

        for (ILootTableItem<?> item : items)
        {
            if (randomIndex < item.get_weight())
            {
                ItemStack stack  = (ItemStack)item.get_value();
                if(item.get_maxAmount() != -1)
                {
                    stack.setAmount(Math.min(item.get_maxAmount(),getRandomAmountWithinDropRange(item.get_maxAmount())));
                }
                else
                {
                    stack.setAmount(getRandomAmountWithinDropRange(item.get_maxAmount()));
                }

                if(stack.getAmount() > stack.getMaxStackSize())
                {
                    stack.setAmount(stack.getMaxStackSize());
                }

                return stack.clone();
            }
            randomIndex -= item.get_weight();
        }
        return null;
    }

    private int getRandomAmountWithinDropRange(int maxAmount)
    {
        int amount1 = (int)_dropAmounts.getLoot();
        int amount2 = (int)_dropAmounts.getLoot();

        if(maxAmount != -1 && amount1 > maxAmount)
        {
        	amount1 = maxAmount;
        }

        if(maxAmount != -1 && amount2 > maxAmount)
        {
        	amount2 = maxAmount;
        }

        return ThreadLocalRandom.current().nextInt(Math.min(amount1, amount2), Math.max(amount1, amount2) + 1);
    }
}
