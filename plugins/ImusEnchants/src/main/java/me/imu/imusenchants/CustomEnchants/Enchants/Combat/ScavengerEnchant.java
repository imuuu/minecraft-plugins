package me.imu.imusenchants.CustomEnchants.Enchants.Combat;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Chance to get one of a mob's drops twice. Only loot is copied: never players, mobs that carry
// an inventory (chested donkeys and llamas, allays, villagers) or what a mob has equipped, since
// those can be items a player gave or lost to it.
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
		LivingEntity entity = event.getEntity();
		if (entity instanceof Player || entity instanceof InventoryHolder)
			return;

		if (!Roll(GetChance(level)))
			return;

		List<ItemStack> candidates = new ArrayList<>();
		for (ItemStack drop : event.getDrops())
		{
			if (drop != null && !drop.getType().isAir() && drop.getType().getMaxStackSize() > 1 && !IsEquipped(entity, drop))
				candidates.add(drop);
		}
		if (candidates.isEmpty())
			return;

		event.getDrops().add(candidates.get(_random.nextInt(candidates.size())).clone());
	}

	private static boolean IsEquipped(LivingEntity entity, ItemStack drop)
	{
		EntityEquipment equipment = entity.getEquipment();
		if (equipment == null)
			return false;

		for (ItemStack worn : equipment.getArmorContents())
		{
			if (drop.isSimilar(worn))
				return true;
		}
		return drop.isSimilar(equipment.getItemInMainHand()) || drop.isSimilar(equipment.getItemInOffHand());
	}
}
