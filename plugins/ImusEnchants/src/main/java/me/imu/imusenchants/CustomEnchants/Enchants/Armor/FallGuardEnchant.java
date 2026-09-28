package me.imu.imusenchants.CustomEnchants.Enchants.Armor;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.EnchantEffects;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

// Extra fall damage reduction on top of Feather Falling
public class FallGuardEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "fall_guard"; }
	@Override public String GetName() { return "Fall Guard"; }
	@Override public int GetMaxLevel() { return 2; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.ARMOR; }
	@Override public Set<ItemTarget> GetTargets() { return ItemTarget.SetOf(ItemTarget.BOOTS); }

	private static double GetReduction(int level) { return Scale(level, 0.25, 0.50); }

	@Override
	public String GetDescription(int level)
	{
		return "-" + Percent(GetReduction(level)) + " fall damage";
	}

	@Override
	public void OnDamaged(EntityDamageEvent event, Player victim, ItemStack armor, int level)
	{
		if (event.getCause() == EntityDamageEvent.DamageCause.FALL)
			event.setDamage(event.getDamage() * (1 - GetReduction(level)));
	}
}
