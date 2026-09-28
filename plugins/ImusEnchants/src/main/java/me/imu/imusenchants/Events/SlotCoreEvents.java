package me.imu.imusenchants.Events;

import imu.iAPI.Other.Metods;
import me.imu.imusenchants.Enchants.EnchantedItem;
import me.imu.imusenchants.Items.SlotCore;
import me.imu.imusenchants.Managers.ChestLoot;
import me.imu.imusenchants.Managers.ManagerEnchants;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SlotCoreEvents implements Listener
{
	// Loot tables are generated only when a chest (barrel, chest minecart...) is opened for the first time
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void OnLootGenerate(LootGenerateEvent event)
	{
		if (event.getInventoryHolder() == null)
			return;

		event.getLoot().addAll(ChestLoot.RollExtras());
	}

	// Slot core on cursor clicked on top of a tool/armor in the player's own inventory
	@EventHandler(ignoreCancelled = true)
	public void OnApplySlotCore(InventoryClickEvent event)
	{
		if (!(event.getWhoClicked() instanceof Player))
			return;

		if (!(event.getClickedInventory() instanceof PlayerInventory))
			return;

		if (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT)
			return;

		ItemStack cursor = event.getCursor();
		if (!SlotCore.IsSlotCore(cursor))
			return;

		ItemStack target = event.getCurrentItem();
		if (target == null || target.getType().isAir())
			return;

		Player player = (Player) event.getWhoClicked();
		event.setCancelled(true);

		if (EnchantedItem.HasSlots(target))
		{
			player.sendMessage(Metods.msgC("&cThis item already has slots!"));
			return;
		}

		if (!SlotCore.CanApply(target))
		{
			player.sendMessage(Metods.msgC("&cSlot Core can't be used on this item!"));
			return;
		}

		event.setCurrentItem(SlotCore.Apply(target));

		cursor.setAmount(cursor.getAmount() - 1);
		event.getView().setCursor(cursor.getAmount() > 0 ? cursor : null);

		player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1f);
		player.sendMessage(Metods.msgC("&dItem has been given &6slots&d! Open it with an &eenchanting table&d."));
	}

	// Enchanting table works normally, only a slotted item in the main hand opens the slot GUI
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void OnEnchantingTable(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (event.getHand() != EquipmentSlot.HAND)
			return;

		Block block = event.getClickedBlock();
		if (block == null || block.getType() != Material.ENCHANTING_TABLE)
			return;

		Player player = event.getPlayer();
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (!EnchantedItem.HasSlots(hand))
			return;

		event.setCancelled(true);

		ItemStack stack = hand.clone();
		player.getInventory().setItemInMainHand(null);
		ManagerEnchants.Instance.OpenEnchantingInventory(player, stack);
	}

	@EventHandler(ignoreCancelled = true)
	public void OnPrepareVanillaEnchant(PrepareItemEnchantEvent event)
	{
		if (EnchantedItem.HasSlots(event.getItem()))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void OnVanillaEnchant(EnchantItemEvent event)
	{
		if (EnchantedItem.HasSlots(event.getItem()))
			event.setCancelled(true);
	}

	// No enchanted books on slotted items with an anvil, enchants come only from the slot GUI
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnAnvilBook(PrepareAnvilEvent event)
	{
		AnvilInventory inv = event.getInventory();
		ItemStack first = inv.getItem(0);
		ItemStack second = inv.getItem(1);

		if (second == null || second.getType() != Material.ENCHANTED_BOOK)
			return;

		if (EnchantedItem.HasSlots(first))
			event.setResult(null);
	}

	// Don't let the core get eaten by vanilla amethyst shard recipes
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnCraftWithCore(PrepareItemCraftEvent event)
	{
		for (ItemStack stack : event.getInventory().getMatrix())
		{
			if (SlotCore.IsSlotCore(stack))
			{
				event.getInventory().setResult(null);
				return;
			}
		}
	}
}
