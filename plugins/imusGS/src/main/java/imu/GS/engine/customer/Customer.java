package imu.GS.engine.customer;

import imu.GS.engine.ItemCategory;
import imu.GS.engine.ItemKey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A player trading with a shop.
 */
public record Customer(UUID id, String name, CustomerInventory inventory, CustomerContext context)
{
    /**
     * What the customer can sell from one tab of their side of the menu. Stacks of the same item are one entry;
     * every shulker box is an entry of its own, sold by its contents.
     */
    public List<SellEntry> sellEntries(ItemCategory tab, boolean includeHotbarAndArmor)
    {
        List<SellEntry> entries = new ArrayList<>();
        Map<ItemKey, SellEntry> merged = new LinkedHashMap<>();
        for (CustomerInventory.Entry stack : inventory.contents(includeHotbarAndArmor))
        {
            if (stack.category() != tab)
                continue;

            if (tab == ItemCategory.SHULKER_BOX)
            {
                entries.add(new SellEntry(stack.item(), stack.amount(), stack.slot()));
                continue;
            }

            SellEntry entry = merged.get(stack.item());
            if (entry == null)
            {
                entry = new SellEntry(stack.item(), 0, -1);
                merged.put(stack.item(), entry);
                entries.add(entry);
            }
            entry.add(stack.amount());
        }
        return entries;
    }

    /**
     * One item the customer can sell.
     */
    public static final class SellEntry
    {
        private final ItemKey _item;
        private int _amount;
        private final int _shulkerSlot;

        SellEntry(ItemKey item, int amount, int shulkerSlot)
        {
            _item = item;
            _amount = amount;
            _shulkerSlot = shulkerSlot;
        }

        public ItemKey getItem()
        {
            return _item;
        }

        public int getAmount()
        {
            return _amount;
        }

        public boolean isShulker()
        {
            return _shulkerSlot >= 0;
        }

        /**
         * Inventory slot of the shulker box, -1 for other items.
         */
        public int getShulkerSlot()
        {
            return _shulkerSlot;
        }

        void add(int amount)
        {
            _amount += amount;
        }
    }
}
