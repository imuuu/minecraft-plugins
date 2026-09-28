package me.imu.imusenchants.CustomEnchants;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EnchantEffects
{
	private static final Set<UUID> _dealingExtraDamage = new HashSet<>();

	// Keeps a potion effect running while the enchant is active. Refreshed from OnTick so it
	// runs out a few seconds after the item is taken off.
	public static void KeepEffect(LivingEntity entity, PotionEffectType type, int amplifier, int durationTicks)
	{
		PotionEffect current = entity.getPotionEffect(type);
		if (current != null)
		{
			if (current.getAmplifier() > amplifier)
				return;
			if (current.getAmplifier() == amplifier && current.getDuration() > durationTicks - 40)
				return;
		}
		entity.addPotionEffect(new PotionEffect(type, durationTicks, amplifier, true, false, true));
	}

	// Timed effect from a hit, doesn't shorten a stronger or longer effect the target already has
	public static void AddEffect(LivingEntity entity, PotionEffectType type, int amplifier, int durationTicks)
	{
		PotionEffect current = entity.getPotionEffect(type);
		if (current != null && current.getAmplifier() >= amplifier && current.getDuration() >= durationTicks)
			return;

		entity.addPotionEffect(new PotionEffect(type, durationTicks, amplifier));
	}

	public static double GetMaxHealth(LivingEntity entity)
	{
		AttributeInstance attribute = entity.getAttribute(Attribute.MAX_HEALTH);
		return attribute != null ? attribute.getValue() : 20.0;
	}

	// Heals through EntityRegainHealthEvent so other plugins can change or cancel it
	public static void Heal(LivingEntity entity, double amount)
	{
		if (amount <= 0 || entity.isDead())
			return;

		double max = GetMaxHealth(entity);
		if (entity.getHealth() >= max)
			return;

		EntityRegainHealthEvent event = new EntityRegainHealthEvent(entity, amount, EntityRegainHealthEvent.RegainReason.CUSTOM);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;

		entity.setHealth(Math.min(max, entity.getHealth() + event.getAmount()));
	}

	// Damage caused by an enchant (Cleave, Reflect). Hits made while this runs don't trigger
	// enchants again, so enchants can't bounce damage back and forth.
	public static void DealExtraDamage(LivingEntity target, double amount, LivingEntity source)
	{
		if (amount <= 0 || target.isDead())
			return;

		_dealingExtraDamage.add(source.getUniqueId());
		try
		{
			target.damage(amount, source);
		}
		finally
		{
			_dealingExtraDamage.remove(source.getUniqueId());
		}
	}

	public static boolean IsDealingExtraDamage(LivingEntity entity)
	{
		return _dealingExtraDamage.contains(entity.getUniqueId());
	}

	// Plain durability loss without Unbreaking, returns false if the item broke
	public static boolean DamageItem(ItemStack item, int amount)
	{
		if (item == null || item.getType().getMaxDurability() <= 0)
			return true;

		ItemMeta meta = item.getItemMeta();
		if (!(meta instanceof Damageable) || meta.isUnbreakable())
			return true;

		Damageable damageable = (Damageable) meta;
		int newDamage = damageable.getDamage() + amount;
		if (newDamage >= item.getType().getMaxDurability())
		{
			item.setAmount(0);
			return false;
		}

		damageable.setDamage(newDamage);
		item.setItemMeta(meta);
		return true;
	}
}
