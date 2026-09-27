package me.imu.imuschallenges.Factories;

import imu.iAPI.Utilities.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.ThreadLocalRandom;

public class ItemFactory
{
    public static ItemStack createMysteryDust()
    {
        ItemStack item = new ItemStack(Material.GLOWSTONE_DUST);
        ItemUtils.SetDisplayName(item, "&5Mystery &eDust");
        ItemUtils.AddGlow(item);
        ItemUtils.SetTag(item, "mystery_dust");
        int amount = ThreadLocalRandom.current().nextInt(1, 6);
        ItemUtils.AddLore(item, "&9Amount: &e"+amount, true);
        ItemUtils.SetPersistenData(item, "ic_mystery_dust_amount", PersistentDataType.INTEGER, amount);
        item.setAmount(1);
        return item;
    }

    public static boolean isMysteryDust(ItemStack item)
    {
        return ItemUtils.HasTag(item, "mystery_dust");
    }

    public static int getMysteryDustAmount(ItemStack item)
    {
        return ItemUtils.GetPersistenData(item, "ic_mystery_dust_amount", PersistentDataType.INTEGER);
    }

    public static ItemStack createMysteryBox()
    {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemUtils.SetDisplayName(item, "&5Mystery &eBox");
        ItemUtils.AddGlow(item);
        ItemUtils.SetTag(item, "mystery_box");
        int quality = ThreadLocalRandom.current().nextInt(1, 5); // 1-4
        ItemUtils.AddLore(item, "&9Quality: &e"+quality, true);
        ItemUtils.SetPersistenData(item, "ic_mystery_box_quality", PersistentDataType.INTEGER, quality);
        item.setAmount(ThreadLocalRandom.current().nextInt(1));
        return item;
    }

    public static boolean isMysteryBox(ItemStack item)
    {
        return ItemUtils.HasTag(item, "mystery_box");
    }

    public static int getMysteryBoxQuality(ItemStack item)
    {
        return ItemUtils.GetPersistenData(item, "ic_mystery_box_quality", PersistentDataType.INTEGER);
    }
}
