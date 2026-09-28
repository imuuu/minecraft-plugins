package me.imu.imusenchants.CustomEnchants.Enchants.Fishing;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.entity.Item;

import java.util.Set;

public class SurvivalistEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "survivalist"; }
	@Override public String GetName() { return "Survivalist"; }
	@Override public int GetMaxLevel() { return 1; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.TOOL; }
	@Override public Set<ItemTarget> GetTargets() { return ItemTarget.SetOf(ItemTarget.FISHING_ROD); }
	@Override public int GetPriority() { return 10; }

	@Override
	public String GetDescription(int level)
	{
		return "Caught fish come out cooked";
	}

	@Override
	public void OnFish(PlayerFishEvent event, ItemStack rod, int level)
	{
		if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH || !(event.getCaught() instanceof Item))
			return;

		Item caught = (Item) event.getCaught();
		ItemStack stack = caught.getItemStack();
		Material cooked = stack.getType() == Material.COD ? Material.COOKED_COD
				: stack.getType() == Material.SALMON ? Material.COOKED_SALMON : null;
		if (cooked == null)
			return;

		caught.setItemStack(new ItemStack(cooked, stack.getAmount()));
	}
}
