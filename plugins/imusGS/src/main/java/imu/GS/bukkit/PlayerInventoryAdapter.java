package imu.GS.bukkit;

import imu.GS.engine.ItemCategory;
import imu.GS.engine.ItemKey;
import imu.GS.engine.customer.CustomerInventory;
import imu.iAPI.Main.ImusAPI;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A player's inventory for the trade engine.
 *
 * <p>While the hotbar and armor slots are excluded ({@link #setIncludeHotbarAndArmor}), counting and taking items
 * leaves them alone too, so the sword in the player's hand is never sold by accident.</p>
 */
public class PlayerInventoryAdapter implements CustomerInventory
{
    private static final int HOTBAR_SIZE = 9;
    // Player inventory: 0-8 hotbar, 9-35 main, 36-39 armor, 40 offhand
    private static final int LAST_MAIN_SLOT = 35;

    private final Player _player;
    private boolean _includeHotbarAndArmor;

    public PlayerInventoryAdapter(Player player)
    {
        _player = player;
    }

    public void setIncludeHotbarAndArmor(boolean include)
    {
        _includeHotbarAndArmor = include;
    }

    public boolean isIncludeHotbarAndArmor()
    {
        return _includeHotbarAndArmor;
    }

    private boolean isIncluded(int slot, boolean includeHotbarAndArmor)
    {
        return includeHotbarAndArmor || (slot >= HOTBAR_SIZE && slot <= LAST_MAIN_SLOT);
    }

    @Override
    public List<Entry> contents(boolean includeHotbarAndArmor)
    {
        List<Entry> entries = new ArrayList<>();
        ItemStack[] contents = _player.getInventory().getContents();
        for (int slot = 0; slot < contents.length; slot++)
        {
            ItemStack stack = contents[slot];
            if (isEmpty(stack) || !isIncluded(slot, includeHotbarAndArmor))
                continue;
            entries.add(new Entry(slot, ItemKeys.of(stack), stack.getAmount(), category(stack)));
        }
        return entries;
    }

    public static ItemCategory category(ItemStack stack)
    {
        if (ImusAPI._metods.isShulkerBox(stack))
            return ItemCategory.SHULKER_BOX;
        if (ImusAPI._metods.isArmor(stack) || ImusAPI._metods.isTool(stack))
            return ItemCategory.GEAR;
        return ItemCategory.BLOCKS;
    }

    @Override
    public int count(ItemKey item)
    {
        ItemStack template = ItemKeys.template(item);
        int count = 0;
        ItemStack[] contents = _player.getInventory().getContents();
        for (int slot = 0; slot < contents.length; slot++)
        {
            ItemStack stack = contents[slot];
            if (!isEmpty(stack) && isIncluded(slot, _includeHotbarAndArmor) && template.isSimilar(stack))
                count += stack.getAmount();
        }
        return count;
    }

    @Override
    public boolean remove(ItemKey item, int amount)
    {
        if (count(item) < amount)
            return false;

        ItemStack template = ItemKeys.template(item);
        PlayerInventory inventory = _player.getInventory();
        ItemStack[] contents = inventory.getContents();
        int left = amount;
        // From the last slot back, like the old shop: the hotbar end is emptied last
        for (int slot = contents.length - 1; slot >= 0 && left > 0; slot--)
        {
            ItemStack stack = contents[slot];
            if (isEmpty(stack) || !isIncluded(slot, _includeHotbarAndArmor) || !template.isSimilar(stack))
                continue;
            int taken = Math.min(left, stack.getAmount());
            left -= taken;
            if (taken == stack.getAmount())
                inventory.setItem(slot, null);
            else
            {
                stack.setAmount(stack.getAmount() - taken);
                inventory.setItem(slot, stack);
            }
        }
        return true;
    }

    @Override
    public void give(ItemKey item, int amount)
    {
        ItemStack template = ItemKeys.template(item);
        int maxStack = Math.max(1, template.getMaxStackSize());
        List<ItemStack> stacks = new ArrayList<>();
        for (int left = amount; left > 0; left -= maxStack)
            stacks.add(ItemKeys.stack(item, Math.min(left, maxStack)));

        Map<Integer, ItemStack> leftovers = _player.getInventory().addItem(stacks.toArray(new ItemStack[0]));
        for (ItemStack leftover : leftovers.values())
            _player.getWorld().dropItem(_player.getLocation(), leftover);
    }

    @Override
    public Map<ItemKey, Integer> shulkerContents(int slot)
    {
        Map<ItemKey, Integer> contents = new LinkedHashMap<>();
        ShulkerBox box = shulkerAt(slot);
        if (box == null)
            return contents;
        for (ItemStack stack : box.getInventory().getContents())
        {
            if (!isEmpty(stack))
                contents.merge(ItemKeys.of(stack), stack.getAmount(), Integer::sum);
        }
        return contents;
    }

    @Override
    public boolean emptyShulker(int slot)
    {
        ItemStack stack = _player.getInventory().getItem(slot);
        if (isEmpty(stack) || !(stack.getItemMeta() instanceof BlockStateMeta meta) || !(meta.getBlockState() instanceof ShulkerBox box))
            return false;
        box.getInventory().clear();
        meta.setBlockState(box);
        stack.setItemMeta(meta);
        _player.getInventory().setItem(slot, stack);
        return true;
    }

    private ShulkerBox shulkerAt(int slot)
    {
        ItemStack stack = _player.getInventory().getItem(slot);
        if (isEmpty(stack) || !(stack.getItemMeta() instanceof BlockStateMeta meta) || !(meta.getBlockState() instanceof ShulkerBox box))
            return null;
        return box;
    }

    private static boolean isEmpty(ItemStack stack)
    {
        return stack == null || stack.getType() == Material.AIR || stack.getAmount() <= 0;
    }
}
