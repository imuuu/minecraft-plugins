package me.imu.imusenchants.CustomEnchants.Enchants.Universal;

import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

// Comes attached to cursed books found in loot
public class CurseOfFragilityEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "curse_of_fragility"; }
	@Override public String GetName() { return "Curse of Fragility"; }
	@Override public int GetMaxLevel() { return 1; }
	@Override public Set<ItemTarget> GetTargets() { return ItemTarget.ALL; }
	@Override public boolean IsCurse() { return true; }

	@Override
	public String GetDescription(int level)
	{
		return "The item wears out twice as fast";
	}

	@Override
	public void OnItemDamage(PlayerItemDamageEvent event, ItemStack item, int level)
	{
		event.setDamage(event.getDamage() * 2);
	}
}
