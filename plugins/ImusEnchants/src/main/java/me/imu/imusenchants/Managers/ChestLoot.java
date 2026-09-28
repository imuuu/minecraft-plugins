package me.imu.imusenchants.Managers;

import me.imu.imusenchants.CONSTANTS;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.CustomEnchantBook;
import me.imu.imusenchants.CustomEnchants.CustomEnchantRegistry;
import me.imu.imusenchants.Items.SlotCore;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Extra items this plugin adds to unopened loot chests
public class ChestLoot
{
	private static final Random _random = new Random();

	public static List<ItemStack> RollExtras()
	{
		List<ItemStack> extras = new ArrayList<>();

		if (SlotCore.RollChestChance())
			extras.add(SlotCore.Create(1));

		if (_random.nextDouble() < CONSTANTS.CUSTOM_BOOK_CHEST_CHANCE)
		{
			// Found books are always level I, higher levels come from boosters
			CustomEnchant enchant = CustomEnchantRegistry.GetRandom();
			if (enchant != null)
			{
				CustomEnchant curse = _random.nextDouble() < CONSTANTS.CURSED_BOOK_CHANCE ? CustomEnchantRegistry.GetRandomCurse() : null;
				if (curse != null && !curse.GetTargets().containsAll(enchant.GetTargets()))
					curse = null;

				extras.add(CustomEnchantBook.Create(enchant, 1, curse));
			}
		}
		return extras;
	}
}
