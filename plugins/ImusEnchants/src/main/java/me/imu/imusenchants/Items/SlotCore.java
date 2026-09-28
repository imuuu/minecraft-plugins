package me.imu.imusenchants.Items;

import imu.iAPI.Utilities.ItemUtils;
import me.imu.imusenchants.CONSTANTS;
import me.imu.imusenchants.Enchants.EnchantedItem;
import me.imu.imusenchants.Managers.ManagerEnchants;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class SlotCore
{
	private static final String PD_SLOT_CORE = "ie_slot_core";

	public static ItemStack Create(int amount)
	{
		ItemStack stack = new ItemStack(CONSTANTS.SLOT_CORE_MATERIAL, Math.max(1, amount));
		ItemUtils.SetDisplayName(stack, "&d&lSlot Core");
		ItemUtils.AddLore(stack, "&3Click on a &etool &3or &earmor", true);
		ItemUtils.AddLore(stack, "&3in your inventory to give it &6slots", true);
		ItemUtils.AddLore(stack, "&3▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
		ItemUtils.AddLore(stack, "&cRemoves all enchants from the item!", true);
		ItemUtils.AddGlow(stack);
		ItemUtils.SetTag(stack, PD_SLOT_CORE);
		return stack;
	}

	public static boolean IsSlotCore(ItemStack stack)
	{
		if (stack == null || stack.getType() != CONSTANTS.SLOT_CORE_MATERIAL)
			return false;

		return ItemUtils.HasTag(stack, PD_SLOT_CORE);
	}

	public static boolean CanApply(ItemStack target)
	{
		if (target == null || target.getType().isAir())
			return false;

		if (!(ItemUtils.IsTool(target) || ItemUtils.IsArmor(target)))
			return false;

		if (EnchantedItem.HasSlots(target))
			return false;

		// Wipe enchants so the void stone check is the only thing left from IsValidToEnchant
		ItemStack wiped = target.clone();
		ItemUtils.RemoveEnchantments(wiped);
		return ManagerEnchants.IsValidToEnchant(wiped);
	}

	// Returns a fresh copy of the item with slots. Everything except the durability is reset,
	// keeping the damage so the core can't be used as a free repair.
	public static ItemStack Apply(ItemStack target)
	{
		ItemStack result = new ItemStack(target.getType());

		ItemMeta oldMeta = target.getItemMeta();
		if (oldMeta instanceof Damageable)
		{
			ItemUtils.SetDamage(result, ((Damageable) oldMeta).getDamage());
		}

		EnchantedItem eItem = new EnchantedItem(result);
		eItem.SetTooltip();
		return result;
	}
}
