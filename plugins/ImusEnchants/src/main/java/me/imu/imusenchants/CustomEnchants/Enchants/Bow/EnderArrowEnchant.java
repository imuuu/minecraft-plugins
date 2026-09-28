package me.imu.imusenchants.CustomEnchants.Enchants.Bow;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.EnchantEffects;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import me.imu.imusenchants.ImusEnchants;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Set;

// Arrows shot while sneaking teleport the shooter where they land, like an ender pearl
public class EnderArrowEnchant extends CustomEnchant
{
	private static final int EXTRA_DURABILITY = 5;
	private static final double DAMAGE = 2.0;

	private NamespacedKey _key;

	@Override public String GetKey() { return "ender_arrow"; }
	@Override public String GetName() { return "Ender Arrow"; }
	@Override public int GetMaxLevel() { return 1; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.WEAPON; }

	@Override
	public Set<ItemTarget> GetTargets()
	{
		return ItemTarget.SetOf(ItemTarget.BOW);
	}

	@Override
	public String GetDescription(int level)
	{
		return "Shoot while sneaking to teleport where the arrow lands";
	}

	private NamespacedKey Key()
	{
		if (_key == null)
			_key = new NamespacedKey(ImusEnchants.Instance, "ie_ender_arrow");
		return _key;
	}

	@Override
	public void OnShoot(EntityShootBowEvent event, Player shooter, ItemStack bow, int level)
	{
		if (!shooter.isSneaking())
			return;

		event.getProjectile().getPersistentDataContainer().set(Key(), PersistentDataType.INTEGER, 1);
		// The event's bow can be a copy, damage the one in the hand
		ItemStack held = shooter.getInventory().getItemInMainHand();
		if (held.getType() != bow.getType())
			held = shooter.getInventory().getItemInOffHand();
		EnchantEffects.DamageItem(held, EXTRA_DURABILITY);
	}

	@Override
	public void OnProjectileHit(ProjectileHitEvent event, Player shooter, Projectile projectile, int level)
	{
		if (!projectile.getPersistentDataContainer().has(Key(), PersistentDataType.INTEGER))
			return;

		projectile.getPersistentDataContainer().remove(Key());
		if (!shooter.isOnline() || shooter.isDead() || shooter.getWorld() != projectile.getWorld())
			return;

		Location target = projectile.getLocation();
		if (event.getHitBlock() != null && event.getHitBlockFace() != null)
			target = event.getHitBlock().getRelative(event.getHitBlockFace()).getLocation().add(0.5, 0, 0.5);

		if (target.getBlock().getType().isSolid() || target.clone().add(0, 1, 0).getBlock().getType().isSolid())
			return;

		target.setYaw(shooter.getLocation().getYaw());
		target.setPitch(shooter.getLocation().getPitch());

		// Teleport cause lets claim plugins block it like a pearl
		if (shooter.teleport(target, PlayerTeleportEvent.TeleportCause.ENDER_PEARL))
		{
			shooter.setFallDistance(0);
			shooter.damage(DAMAGE);
			projectile.remove();
		}
	}
}
