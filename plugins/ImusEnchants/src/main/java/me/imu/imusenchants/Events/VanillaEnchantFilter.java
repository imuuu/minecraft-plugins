package me.imu.imusenchants.Events;

import me.imu.imusenchants.CustomEnchants.EnchantSettings;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.HashMap;
import java.util.Map;

// Vanilla enchants switched off in /ien admin can't be obtained from anywhere:
// loot, the vanilla table, anvils, fishing or villagers. The shop and the slot grid check
// EnchantSettings themselves. Items that already have the enchant keep it.
public class VanillaEnchantFilter implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void OnLootGenerate(LootGenerateEvent event)
	{
		for (ItemStack stack : event.getLoot())
			EnchantSettings.StripDisabled(stack);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void OnVanillaEnchant(EnchantItemEvent event)
	{
		event.getEnchantsToAdd().keySet().removeIf(enchant -> !EnchantSettings.IsEnabled(enchant));

		// Nothing left to give, don't take the player's levels for nothing
		if (event.getEnchantsToAdd().isEmpty())
			event.setCancelled(true);
	}

	// Blocks anvil results that would add a disabled enchant. An item that already had it can
	// still be repaired or renamed.
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnAnvil(PrepareAnvilEvent event)
	{
		ItemStack result = event.getResult();
		if (result == null)
			return;

		Map<Enchantment, Integer> before = GetEnchants(event.getInventory().getItem(0));
		for (Enchantment enchant : GetEnchants(result).keySet())
		{
			if (!EnchantSettings.IsEnabled(enchant) && !before.containsKey(enchant))
			{
				event.setResult(null);
				return;
			}
		}
	}

	private static Map<Enchantment, Integer> GetEnchants(ItemStack stack)
	{
		Map<Enchantment, Integer> enchants = new HashMap<>();
		if (stack == null || stack.getItemMeta() == null)
			return enchants;

		enchants.putAll(stack.getItemMeta().getEnchants());
		if (stack.getItemMeta() instanceof EnchantmentStorageMeta)
			enchants.putAll(((EnchantmentStorageMeta) stack.getItemMeta()).getStoredEnchants());
		return enchants;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void OnFish(PlayerFishEvent event)
	{
		if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH || !(event.getCaught() instanceof Item))
			return;

		Item caught = (Item) event.getCaught();
		ItemStack stack = caught.getItemStack();
		if (EnchantSettings.StripDisabled(stack))
			caught.setItemStack(stack);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void OnVillagerTrade(VillagerAcquireTradeEvent event)
	{
		MerchantRecipe recipe = event.getRecipe();
		ItemStack result = recipe.getResult().clone();
		if (!EnchantSettings.StripDisabled(result))
			return;

		// MerchantRecipe's result can't be changed, only the whole trade dropped
		event.setCancelled(true);
	}
}
