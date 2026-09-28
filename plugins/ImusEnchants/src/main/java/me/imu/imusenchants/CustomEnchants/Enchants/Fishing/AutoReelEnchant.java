package me.imu.imusenchants.CustomEnchants.Enchants.Fishing;

import imu.iAPI.Enums.ITEM_CATEGORY;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.ItemTarget;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import me.imu.imusenchants.CustomEnchants.EnchantEffects;
import me.imu.imusenchants.CustomEnchants.PaperCompat;
import me.imu.imusenchants.ImusEnchants;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Set;

// Reels in by itself when a fish bites. Needs Paper's FishHook#retrieve.
public class AutoReelEnchant extends CustomEnchant
{
	@Override public String GetKey() { return "auto_reel"; }
	@Override public String GetName() { return "Auto Reel"; }
	@Override public int GetMaxLevel() { return 1; }
	@Override public ITEM_CATEGORY GetShopCategory() { return ITEM_CATEGORY.TOOL; }
	@Override public Set<ItemTarget> GetTargets() { return ItemTarget.SetOf(ItemTarget.FISHING_ROD); }

	@Override
	public String GetDescription(int level)
	{
		return "Reels in by itself when a fish bites";
	}

	@Override
	public void OnFish(PlayerFishEvent event, ItemStack rod, int level)
	{
		if (event.getState() != PlayerFishEvent.State.BITE)
			return;

		Player player = event.getPlayer();
		FishHook hook = event.getHook();
		EquipmentSlot hand = player.getInventory().getItemInMainHand().getType() == Material.FISHING_ROD
				? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;

		Bukkit.getScheduler().runTask(ImusEnchants.Instance, () ->
		{
			if (!hook.isValid() || !player.isOnline())
				return;

			if (!PaperCompat.RetrieveHook(hook, hand))
				return;

			if (hand == EquipmentSlot.HAND)
				player.swingMainHand();
			else
				player.swingOffHand();

			ItemStack held = hand == EquipmentSlot.HAND ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
			EnchantEffects.DamageItem(held, 1);
		});
	}
}
