package me.imu.imusminiquests.Hooks;

import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.CustomEnchantBook;
import me.imu.imusenchants.CustomEnchants.CustomEnchantRegistry;
import me.imu.imusenchants.CustomEnchants.EnchantSettings;
import me.imu.imusenchants.Items.SlotCore;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * Rewards made with ImusEnchants: custom enchant books, Slot Cores and tools that already have
 * slots. ImusEnchants is only a soft dependency, so check {@link #isEnabled()} before calling
 * anything else here; the other methods load ImusEnchants' classes.
 */
public final class ImusEnchantsHook
{
    private ImusEnchantsHook() {}

    public static boolean isEnabled()
    {
        return Bukkit.getPluginManager().isPluginEnabled("ImusEnchants");
    }

    /**
     * A custom enchant book. The key picks the enchant (e.g. "timber"); "random" or null picks a
     * random enabled, non-curse one. Returns null when the key is unknown.
     */
    public static ItemStack createBook(String key, int level)
    {
        CustomEnchant enchant = key == null || key.equalsIgnoreCase("random")
                ? CustomEnchantRegistry.GetRandom()
                : CustomEnchantRegistry.Get(key);
        return enchant == null ? null : CustomEnchantBook.Create(enchant, Math.max(1, Math.min(level, enchant.GetMaxLevel())));
    }

    /**
     * Display name of a custom enchant for the reward list on the quest panel, or null when the
     * key is unknown.
     */
    public static String getEnchantName(String key)
    {
        CustomEnchant enchant = CustomEnchantRegistry.Get(key);
        return enchant == null ? null : enchant.GetName();
    }

    /** False when the enchant is switched off in ImusEnchants' /ien admin. */
    public static boolean isVanillaEnchantEnabled(Enchantment enchantment)
    {
        return EnchantSettings.IsEnabled(enchantment);
    }

    /** Removes vanilla enchants that are switched off in ImusEnchants from the item. */
    public static void stripDisabledEnchants(ItemStack stack)
    {
        EnchantSettings.StripDisabled(stack);
    }

    public static ItemStack createSlotCore(int amount)
    {
        return SlotCore.Create(amount);
    }

    /**
     * The tool or armor piece with ImusEnchants slots, ready to be enchanted.
     */
    public static ItemStack addSlots(ItemStack stack)
    {
        return SlotCore.Apply(stack);
    }
}
