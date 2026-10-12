package imu.GS.menu;

import imu.GS.text.Messages;

import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * Items for menu buttons. Names and lore take &amp; colour codes.
 */
public final class Icons
{
    private Icons() {}

    public static ItemStack icon(Material material, String name, String... lore)
    {
        return icon(material, name, Arrays.asList(lore));
    }

    public static ItemStack icon(Material material, String name, List<String> lore)
    {
        ItemStack stack = new ItemStack(material);
        ItemUtils.SetDisplayName(stack, name);
        if (!lore.isEmpty())
            ItemUtils.AddLore(stack, lore);
        ItemUtils.HideFlag(stack, ItemFlag.HIDE_ATTRIBUTES);
        return stack;
    }

    /**
     * A copy of the item with these lines added under its own lore.
     */
    public static ItemStack withLore(ItemStack item, List<String> lore)
    {
        ItemStack stack = item.clone();
        stack.setAmount(1);
        if (!lore.isEmpty())
            ItemUtils.AddLore(stack, lore);
        return stack;
    }

    public static ItemStack filler(Material material)
    {
        return ItemUtils.SetDisplayNameEmpty(new ItemStack(material));
    }

    public static ItemStack glow(ItemStack stack)
    {
        return ItemUtils.AddGlow(stack);
    }

    /**
     * Money as the shop shows it, e.g. "12.5 &amp;2$".
     */
    public static String money(double amount)
    {
        return Messages.get("shop.money", "amount", Metods.Round(amount));
    }
}
