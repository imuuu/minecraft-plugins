package me.imu.imusenchants.CustomEnchants.Enchants.Fishing;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import java.util.Set;

public class LongCastEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "long_cast"; }
	@Override public String GetName() { return "Long Cast"; }
	@Override public int GetMaxLevel() { return 2; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.TOOL; }
	@Override public Set<ItemTarget> GetTargets() { return ItemTarget.SetOf(ItemTarget.FISHING_ROD); }

	private static double GetMultiplier(int level) { return Scale(level, 1.3, 1.6); }

	@Override
	public String GetDescription(int level)
	{
		return "Casts " + Percent(GetMultiplier(level) - 1) + " further";
	}

	@Override
	public void OnFish(PlayerFishEvent event, ItemStack rod, int level)
	{
		if (event.getState() == PlayerFishEvent.State.FISHING)
			event.getHook().setVelocity(event.getHook().getVelocity().multiply(GetMultiplier(level)));
	}
}
