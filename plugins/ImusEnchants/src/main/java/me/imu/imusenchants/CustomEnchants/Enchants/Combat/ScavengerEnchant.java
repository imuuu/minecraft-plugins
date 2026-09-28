package me.imu.imusenchants.CustomEnchants.Enchants.Combat;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

// Chance to get one of a mob's drops twice. Never on players, their items would be duplicated.
public class ScavengerEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "scavenger"; }
	@Override public String GetName() { return "Scavenger"; }
	@Override public int GetMaxLevel() { return 2; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.WEAPON; }
	@Override public int GetPriority() { return 150; } // before Telekinesis collects drops

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.SetOf(ItemTarget.SWORD);
	}

	private static double GetChance(int level) { return Scale(level, 0.10, 0.20); }

	@Override
	public String GetDescription(int level)
	{
		return Percent(GetChance(level)) + " chance for extra mob loot";
	}

	@Override
	public void OnKill(EntityDeathEvent event, Player killer, ItemStack weapon, int level)
	{
		if (event.getEntity() instanceof Player)
			return;

		List<ItemStack> drops = event.getDrops();
		if (drops.isEmpty() || !Roll(GetChance(level)))
			return;

		ItemStack drop = drops.get(_random.nextInt(drops.size()));
		if (drop != null && !drop.getType().isAir() && drop.getType().getMaxStackSize() > 1)
			drops.add(drop.clone());
	}
}
