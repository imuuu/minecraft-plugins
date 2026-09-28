package me.imu.imusenchants.CustomEnchants.Enchants.Fishing;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Item;

import java.util.Set;

public class DoubleCatchEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "double_catch"; }
	@Override public String GetName() { return "Double Catch"; }
	@Override public int GetMaxLevel() { return 3; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.TOOL; }
	@Override public Set<ItemTarget> GetTargets() { return ItemTarget.SetOf(ItemTarget.FISHING_ROD); }
	@Override public int GetPriority() { return -10; } // copies the catch after Survivalist has cooked it

	private static double GetChance(int level) { return Scale(level, 0.15, 0.25, 0.35); }

	@Override
	public String GetDescription(int level)
	{
		return Percent(GetChance(level)) + " chance to catch double";
	}

	@Override
	public void OnFish(PlayerFishEvent event, ItemStack rod, int level)
	{
		if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH || !(event.getCaught() instanceof Item))
			return;

		if (!Roll(GetChance(level)))
			return;

		Item caught = (Item) event.getCaught();
		Item copy = caught.getWorld().dropItem(caught.getLocation(), caught.getItemStack().clone());
		copy.setVelocity(caught.getVelocity());
	}
}
