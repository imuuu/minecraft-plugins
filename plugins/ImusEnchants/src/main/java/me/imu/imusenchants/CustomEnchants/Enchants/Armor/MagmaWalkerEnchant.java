package me.imu.imusenchants.CustomEnchants.Enchants.Armor;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.EnchantEffects;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import me.imu.imusenchants.CustomEnchants.TemporaryBlocks;
import me.imu.imusenchants.ImusEnchants;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Levelled;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Set;

// Frost Walker for lava: lava under the player turns into magma for a few seconds.
// The wearer doesn't burn on magma blocks.
public class MagmaWalkerEnchant extends CustomEnchant
{
	private static final long DURATION_TICKS = 100;

	@Override public String GetKey() { return "magma_walker"; }
	@Override public String GetName() { return "Magma Walker"; }
	@Override public int GetMaxLevel() { return 2; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.ARMOR; }
	@Override public Set<ItemTarget> GetTargets() { return ItemTarget.SetOf(ItemTarget.BOOTS); }
	@Override public Set<Enchantment> GetVanillaConflicts() { return Collections.singleton(Enchantment.FROST_WALKER); }

	private static int GetRadius(int level) { return Math.max(1, Math.min(level, 2)) + 1; }

	@Override
	public String GetDescription(int level)
	{
		return "Walk on lava, radius " + GetRadius(level);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void OnMove(PlayerMoveEvent event, Player player, ItemStack armor, int level)
	{
		if (!player.isOnGround() || player.isFlying() || player.isGliding())
			return;

		Block center = event.getTo().getBlock().getRelative(0, -1, 0);
		int radius = GetRadius(level);
		for (int x = -radius; x <= radius; x++)
		{
			for (int z = -radius; z <= radius; z++)
			{
				if (x * x + z * z > radius * radius)
					continue;

				Block block = center.getRelative(x, 0, z);
				if (block.getType() != Material.LAVA || !block.getRelative(0, 1, 0).getType().isAir())
					continue;

				// Only still lava sources, like Frost Walker
				if (!(block.getBlockData() instanceof Levelled) || ((Levelled) block.getBlockData()).getLevel() != 0)
					continue;

				// Same event as vanilla Frost Walker, so claim and region plugins can refuse it
				BlockState magma = block.getState();
				magma.setType(Material.MAGMA_BLOCK);
				EntityBlockFormEvent form = new EntityBlockFormEvent(player, block, magma);
				Bukkit.getPluginManager().callEvent(form);
				if (form.isCancelled())
					continue;

				TemporaryBlocks.Place(ImusEnchants.Instance, block, Material.MAGMA_BLOCK, DURATION_TICKS + _random.nextInt(40));
			}
		}
	}

	@Override
	public void OnDamaged(EntityDamageEvent event, Player victim, ItemStack armor, int level)
	{
		if (event.getCause() == EntityDamageEvent.DamageCause.HOT_FLOOR)
			event.setCancelled(true);
	}
}
