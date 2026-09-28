package me.imu.imusenchants.CustomEnchants;

import imu.iAPI.Enums.ITEM_CATEGORY;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Random;
import java.util.Set;

// Base for enchants that don't exist in vanilla. They are stored in the item's persistent data
// (CustomEnchantData) and shown as lore. To add one: extend this, override the hooks it needs and
// register it in CustomEnchantRegistry.RegisterDefaults. CustomEnchantEvents calls the hooks with
// the enchant's level on the item that carries it (main hand, armor piece, bow or rod).
public abstract class CustomEnchant
{
	protected static final Random _random = new Random();

	// Unique id stored on items and books, never change it once items exist with it
	public abstract String GetKey();

	public abstract String GetName();

	public abstract int GetMaxLevel();

	public abstract Set<ItemTarget> GetTargets();

	public abstract String GetDescription(int level);

	// Which shop category may roll this enchant, null = not sold in the shop
	public ITEM_CATEGORY GetShopCategory()
	{
		return null;
	}

	// Keys of custom enchants that can't be on the same item as this one.
	// Checked both ways, so it's enough that one of the two lists the other.
	public Set<String> GetConflicts()
	{
		return Collections.emptySet();
	}

	public Set<Enchantment> GetVanillaConflicts()
	{
		return Collections.emptySet();
	}

	// Curses are never sold or found on their own, they come attached to cursed books
	public boolean IsCurse()
	{
		return false;
	}

	// Higher priority enchants get each event first
	public int GetPriority()
	{
		return 0;
	}

	// ===== Hooks =====

	// Return true when the break was fully handled so lower priority enchants skip it
	public boolean OnBlockBreak(BlockBreakEvent event, ItemStack tool, int level) { return false; }

	// Also fires for blocks broken by Tunnel, Vein Miner, Timber...
	public void OnBlockDrop(BlockDropItemEvent event, ItemStack tool, int level) { }

	public void OnBlockDamage(BlockDamageEvent event, ItemStack tool, int level) { }

	// Melee hit by a player
	public void OnAttack(EntityDamageByEntityEvent event, Player attacker, LivingEntity victim, ItemStack weapon, int level) { }

	public void OnKill(EntityDeathEvent event, Player killer, ItemStack weapon, int level) { }

	// Wearer of the armor piece took damage
	public void OnDamaged(EntityDamageEvent event, Player victim, ItemStack armor, int level) { }

	// 1.0 = normal, multiplied over all worn pieces
	public double GetKnockbackMultiplier(int level) { return 1.0; }

	public void OnShoot(EntityShootBowEvent event, Player shooter, ItemStack bow, int level) { }

	// Projectile enchants are copied from the bow when it is shot
	public void OnProjectileDamage(EntityDamageByEntityEvent event, Player shooter, Projectile projectile, LivingEntity victim, int level) { }

	public void OnProjectileHit(ProjectileHitEvent event, Player shooter, Projectile projectile, int level) { }

	public void OnFish(PlayerFishEvent event, ItemStack rod, int level) { }

	public void OnHunger(FoodLevelChangeEvent event, Player player, ItemStack armor, int level) { }

	public void OnMove(PlayerMoveEvent event, Player player, ItemStack armor, int level) { }

	public void OnItemDamage(PlayerItemDamageEvent event, ItemStack item, int level) { }

	// Once a second for the held item and worn armor. seconds counts up so enchants can act every N seconds.
	public void OnTick(Player player, ItemStack item, int level, long seconds) { }

	// ===== Helpers =====

	public final boolean CanApplyTo(Material material)
	{
		ItemTarget target = ItemTarget.Of(material);
		return target != null && GetTargets().contains(target);
	}

	public final boolean CanApplyToItem(ItemStack stack)
	{
		return stack != null && CanApplyTo(stack.getType());
	}

	public final String GetAppliesToText()
	{
		return ItemTarget.ToText(GetTargets());
	}

	public final String GetDisplayName(int level)
	{
		if (GetMaxLevel() <= 1)
			return GetName();

		return GetName() + " " + ToRoman(level);
	}

	// Chance for the level, clamped to the max level
	protected static double Scale(int level, double... perLevel)
	{
		int index = Math.max(1, Math.min(level, perLevel.length)) - 1;
		return perLevel[index];
	}

	protected static boolean Roll(double chance)
	{
		return _random.nextDouble() < chance;
	}

	protected static String Percent(double value)
	{
		return Math.round(value * 100) + "%";
	}

	public static String ToRoman(int number)
	{
		final int[] values = {10, 9, 5, 4, 1};
		final String[] numerals = {"X", "IX", "V", "IV", "I"};

		if (number <= 0)
			return String.valueOf(number);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < values.length; i++)
		{
			while (number >= values[i])
			{
				number -= values[i];
				sb.append(numerals[i]);
			}
		}
		return sb.toString();
	}
}
