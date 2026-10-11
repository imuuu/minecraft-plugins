package imu.GS.engine.customer;

import imu.GS.engine.ItemCategory;
import imu.GS.engine.ItemKey;

import java.util.List;
import java.util.Map;

/**
 * The customer's own inventory. Implemented on the Bukkit side over the player's inventory; tests use a fake.
 */
public interface CustomerInventory
{
    /**
     * One stack of the inventory.
     *
     * @param slot slot of the stack, used to sell a shulker box
     */
    record Entry(int slot, ItemKey item, int amount, ItemCategory category) {}

    /**
     * Every stack in the inventory, in slot order.
     *
     * @param includeHotbarAndArmor false leaves out the hotbar, armor and offhand slots
     */
    List<Entry> contents(boolean includeHotbarAndArmor);

    /**
     * Units of the item in the whole inventory.
     */
    int count(ItemKey item);

    /**
     * Takes units of the item anywhere in the inventory. Returns false, and takes nothing, when there aren't enough.
     */
    boolean remove(ItemKey item, int amount);

    /**
     * Gives units of the item; what doesn't fit is dropped at the customer's feet.
     */
    void give(ItemKey item, int amount);

    /**
     * What a shulker box in this slot holds, merged by item. Empty when the slot has no shulker box.
     */
    Map<ItemKey, Integer> shulkerContents(int slot);

    /**
     * Empties the shulker box in this slot; the box itself stays. Returns false when there was no box.
     */
    boolean emptyShulker(int slot);
}
