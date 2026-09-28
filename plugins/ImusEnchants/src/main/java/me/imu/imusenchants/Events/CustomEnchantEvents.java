package me.imu.imusenchants.Events;

import imu.iAPI.Other.Metods;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.CustomEnchantBook;
import me.imu.imusenchants.CustomEnchants.CustomEnchantData;
import me.imu.imusenchants.CustomEnchants.CustomEnchantRegistry;
import me.imu.imusenchants.CustomEnchants.MultiBreak;
import me.imu.imusenchants.Enchants.EnchantedItem;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CustomEnchantEvents implements Listener
{
	// Runs late so protection plugins have already cancelled the break if they want to
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void OnBlockBreak(BlockBreakEvent event)
	{
		Player player = event.getPlayer();
		if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)
			return;

		// Blocks broken by a mining enchant don't trigger enchants again
		if (MultiBreak.IsBreaking(player))
			return;

		ItemStack tool = player.getInventory().getItemInMainHand();
		if (!CustomEnchantData.HasAny(tool))
			return;

		List<Map.Entry<CustomEnchant, Integer>> enchants = new ArrayList<>(CustomEnchantData.Get(tool).entrySet());
		enchants.sort(Comparator.comparingInt((Map.Entry<CustomEnchant, Integer> e) -> e.getKey().GetPriority()).reversed());

		for (Map.Entry<CustomEnchant, Integer> entry : enchants)
		{
			CustomEnchant enchant = entry.getKey();
			if (!enchant.CanApplyToItem(tool))
				continue;

			if (enchant.OnBlockBreak(event, tool, entry.getValue()))
				return;
		}
	}

	// Custom book on cursor clicked on a tool without slots: adds the enchant at level I.
	// Higher levels only come from the slot grid with boosters.
	@EventHandler(ignoreCancelled = true)
	public void OnApplyBook(InventoryClickEvent event)
	{
		if (!(event.getWhoClicked() instanceof Player))
			return;

		if (!(event.getClickedInventory() instanceof PlayerInventory))
			return;

		if (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT)
			return;

		ItemStack cursor = event.getCursor();
		Map.Entry<CustomEnchant, Integer> book = CustomEnchantBook.Read(cursor);
		if (book == null)
			return;

		ItemStack target = event.getCurrentItem();
		if (target == null || target.getType().isAir())
			return;

		CustomEnchant enchant = book.getKey();
		Player player = (Player) event.getWhoClicked();

		// Clicking the book on something it doesn't go on is a normal item swap
		if (!enchant.CanApplyToItem(target))
			return;

		event.setCancelled(true);

		if (EnchantedItem.HasSlots(target))
		{
			player.sendMessage(Metods.msgC("&cThis item has slots, put the book in its &eenchanting table &cgrid instead!"));
			return;
		}

		Map<CustomEnchant, Integer> current = CustomEnchantData.Get(target);
		if (current.containsKey(enchant))
		{
			player.sendMessage(Metods.msgC("&cThis item already has &d" + enchant.GetName() + "&c!"));
			return;
		}

		CustomEnchant conflict = CustomEnchantRegistry.FindConflict(enchant, current.keySet());
		if (conflict != null)
		{
			player.sendMessage(Metods.msgC("&d" + enchant.GetName() + " &ccan't be combined with &d" + conflict.GetName() + "&c!"));
			return;
		}

		CustomEnchantData.Add(target, enchant, 1);
		event.setCurrentItem(target);

		cursor.setAmount(cursor.getAmount() - 1);
		event.getView().setCursor(cursor.getAmount() > 0 ? cursor : null);

		player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f);
		player.sendMessage(Metods.msgC("&dApplied &e" + enchant.GetDisplayName(1) + "&d!"));
	}
}
