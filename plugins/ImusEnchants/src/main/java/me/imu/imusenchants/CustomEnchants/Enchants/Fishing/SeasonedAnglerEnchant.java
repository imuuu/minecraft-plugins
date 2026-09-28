package me.imu.imusenchants.CustomEnchants.Enchants.Fishing;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import java.util.Set;

public class SeasonedAnglerEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "seasoned_angler"; }
	@Override public String GetName() { return "Seasoned Angler"; }
	@Override public int GetMaxLevel() { return 3; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.TOOL; }
	@Override public Set<ItemTarget> GetTargets() { return ItemTarget.SetOf(ItemTarget.FISHING_ROD); }

	private static double GetBonus(int level) { return Scale(level, 0.25, 0.50, 0.75); }

	@Override
	public String GetDescription(int level)
	{
		return "+" + Percent(GetBonus(level)) + " XP from fishing";
	}

	@Override
	public void OnFish(PlayerFishEvent event, ItemStack rod, int level)
	{
		if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getExpToDrop() > 0)
			event.setExpToDrop((int) Math.round(event.getExpToDrop() * (1 + GetBonus(level))));
	}
}
