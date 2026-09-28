package me.imu.imusenchants.Events;

import imu.iAPI.Other.Metods;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.CustomEnchantBook;
import me.imu.imusenchants.CustomEnchants.CustomEnchantData;
import me.imu.imusenchants.CustomEnchants.CustomEnchantRegistry;
import me.imu.imusenchants.CustomEnchants.EnchantEffects;
import me.imu.imusenchants.CustomEnchants.EnchantSettings;
import me.imu.imusenchants.CustomEnchants.PaperCompat;
import me.imu.imusenchants.CustomEnchants.TemporaryBlocks;
import me.imu.imusenchants.CustomEnchants.Enchants.Universal.SoulboundEnchant;
import me.imu.imusenchants.Enchants.EnchantedItem;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

// Calls the custom enchant hooks. Each hook gets the enchants of the item that carries them:
// main hand for tools and weapons, worn armor, the bow for projectiles and the rod for fishing.
public class CustomEnchantEvents implements Listener
{
	private final Plugin _plugin;
	private final NamespacedKey _projectileKey;
	private final boolean _paperKnockback;
	private long _seconds = 0;

	public CustomEnchantEvents(Plugin plugin)
	{
		_plugin = plugin;
		_projectileKey = new NamespacedKey(plugin, "ie_projectile_enchants");

		_paperKnockback = PaperCompat.RegisterKnockback(plugin, this, (player, knockback) ->
		{
			double multiplier = GetKnockbackMultiplier(player);
			if (multiplier < 1.0)
				knockback.multiply(multiplier);
		});

		Bukkit.getScheduler().runTaskTimer(plugin, this::Tick, 20L, 20L);
	}

	public void OnDisable()
	{
		TemporaryBlocks.RevertAll();
	}

	// ===== Dispatch helpers =====

	private static List<Map.Entry<CustomEnchant, Integer>> Sorted(Map<CustomEnchant, Integer> enchants)
	{
		List<Map.Entry<CustomEnchant, Integer>> list = new ArrayList<>(enchants.entrySet());
		list.sort(Comparator.comparingInt((Map.Entry<CustomEnchant, Integer> e) -> e.getKey().GetPriority()).reversed());
		return list;
	}

	private static void ForEach(ItemStack item, BiConsumer<CustomEnchant, Integer> action)
	{
		if (item == null || !CustomEnchantData.HasAny(item))
			return;

		for (Map.Entry<CustomEnchant, Integer> entry : Sorted(CustomEnchantData.GetActive(item)))
			action.accept(entry.getKey(), entry.getValue());
	}

	private interface ArmorAction
	{
		void Run(CustomEnchant enchant, ItemStack piece, int level);
	}

	private static void ForEachArmor(Player player, ArmorAction action)
	{
		for (ItemStack piece : player.getInventory().getArmorContents())
		{
			ForEach(piece, (enchant, level) -> action.Run(enchant, piece, level));
		}
	}

	private static boolean IsActivePlayer(Player player)
	{
		return player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR;
	}

	private double GetKnockbackMultiplier(Player player)
	{
		double[] multiplier = {1.0};
		ForEachArmor(player, (enchant, piece, level) -> multiplier[0] *= enchant.GetKnockbackMultiplier(level));
		return multiplier[0];
	}

	// ===== Mining =====

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void OnBlockBreak(BlockBreakEvent event)
	{
		Player player = event.getPlayer();
		if (!IsActivePlayer(player))
			return;

		ItemStack tool = player.getInventory().getItemInMainHand();
		if (!CustomEnchantData.HasAny(tool))
			return;

		for (Map.Entry<CustomEnchant, Integer> entry : Sorted(CustomEnchantData.GetActive(tool)))
		{
			if (entry.getKey().OnBlockBreak(event, tool, entry.getValue()))
				return;
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void OnBlockDrop(BlockDropItemEvent event)
	{
		Player player = event.getPlayer();
		if (!IsActivePlayer(player))
			return;

		ItemStack tool = player.getInventory().getItemInMainHand();
		ForEach(tool, (enchant, level) -> enchant.OnBlockDrop(event, tool, level));
	}

	@EventHandler(ignoreCancelled = true)
	public void OnBlockDamage(BlockDamageEvent event)
	{
		if (!IsActivePlayer(event.getPlayer()))
			return;

		ItemStack tool = event.getItemInHand();
		ForEach(tool, (enchant, level) -> enchant.OnBlockDamage(event, tool, level));
	}

	// ===== Combat =====

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void OnEntityDamageByEntity(EntityDamageByEntityEvent event)
	{
		if (!(event.getEntity() instanceof LivingEntity))
			return;

		LivingEntity victim = (LivingEntity) event.getEntity();
		Entity damager = event.getDamager();

		if (damager instanceof Player)
		{
			Player attacker = (Player) damager;
			if (EnchantEffects.IsDealingExtraDamage(attacker))
				return;

			if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
					&& event.getCause() != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)
				return;

			ItemStack weapon = attacker.getInventory().getItemInMainHand();
			ForEach(weapon, (enchant, level) -> enchant.OnAttack(event, attacker, victim, weapon, level));
			return;
		}

		if (damager instanceof Projectile)
		{
			Projectile projectile = (Projectile) damager;
			if (!(projectile.getShooter() instanceof Player))
				return;

			Player shooter = (Player) projectile.getShooter();
			for (Map.Entry<CustomEnchant, Integer> entry : Sorted(GetProjectileEnchants(projectile)))
				entry.getKey().OnProjectileDamage(event, shooter, projectile, victim, entry.getValue());
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void OnEntityDamage(EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		if (event instanceof EntityDamageByEntityEvent)
		{
			Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
			if (damager instanceof LivingEntity && EnchantEffects.IsDealingExtraDamage((LivingEntity) damager))
				return;
		}

		Player victim = (Player) event.getEntity();
		ForEachArmor(victim, (enchant, piece, level) -> enchant.OnDamaged(event, victim, piece, level));
	}

	// Spigot fallback for Stopping Force when Paper's knockback event isn't there
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void OnKnockbackFallback(EntityDamageByEntityEvent event)
	{
		if (_paperKnockback || !(event.getEntity() instanceof Player))
			return;

		Player player = (Player) event.getEntity();
		double multiplier = GetKnockbackMultiplier(player);
		if (multiplier >= 1.0)
			return;

		Bukkit.getScheduler().runTask(_plugin, () ->
		{
			Vector velocity = player.getVelocity();
			player.setVelocity(new Vector(velocity.getX() * multiplier, velocity.getY(), velocity.getZ() * multiplier));
		});
	}

	// PlayerDeathEvent comes here too. HIGHEST so Soulbound (HIGH) has taken its items out first.
	@EventHandler(priority = EventPriority.HIGHEST)
	public void OnEntityDeath(EntityDeathEvent event)
	{
		Player killer = event.getEntity().getKiller();
		if (killer == null)
			return;

		ItemStack weapon = killer.getInventory().getItemInMainHand();
		ForEach(weapon, (enchant, level) -> enchant.OnKill(event, killer, weapon, level));
	}

	// ===== Bows =====

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void OnShoot(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player) || !(event.getProjectile() instanceof Projectile))
			return;

		Player shooter = (Player) event.getEntity();
		ItemStack bow = event.getBow();
		if (bow == null || !CustomEnchantData.HasAny(bow))
			return;

		Map<CustomEnchant, Integer> enchants = CustomEnchantData.GetActive(bow);
		if (enchants.isEmpty())
			return;

		// Projectile keeps the bow's enchants so hits work even if the bow is switched away
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<CustomEnchant, Integer> entry : enchants.entrySet())
		{
			if (sb.length() > 0)
				sb.append(",");
			sb.append(entry.getKey().GetKey()).append(":").append(entry.getValue());
		}
		event.getProjectile().getPersistentDataContainer().set(_projectileKey, PersistentDataType.STRING, sb.toString());

		for (Map.Entry<CustomEnchant, Integer> entry : Sorted(enchants))
			entry.getKey().OnShoot(event, shooter, bow, entry.getValue());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void OnProjectileHit(ProjectileHitEvent event)
	{
		Projectile projectile = event.getEntity();
		ProjectileSource source = projectile.getShooter();
		if (!(source instanceof Player))
			return;

		Player shooter = (Player) source;
		for (Map.Entry<CustomEnchant, Integer> entry : Sorted(GetProjectileEnchants(projectile)))
			entry.getKey().OnProjectileHit(event, shooter, projectile, entry.getValue());
	}

	private Map<CustomEnchant, Integer> GetProjectileEnchants(Projectile projectile)
	{
		Map<CustomEnchant, Integer> enchants = new LinkedHashMap<>();
		String data = projectile.getPersistentDataContainer().get(_projectileKey, PersistentDataType.STRING);
		if (data == null || data.isEmpty())
			return enchants;

		for (String part : data.split(","))
		{
			String[] keyLevel = part.split(":");
			CustomEnchant enchant = CustomEnchantRegistry.Get(keyLevel[0]);
			if (enchant == null || !EnchantSettings.IsEnabled(enchant) || keyLevel.length < 2)
				continue;

			try
			{
				enchants.put(enchant, Integer.parseInt(keyLevel[1]));
			}
			catch (NumberFormatException ignored)
			{
			}
		}
		return enchants;
	}

	// ===== Fishing, hunger, movement, durability =====

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void OnFish(PlayerFishEvent event)
	{
		PlayerInventory inventory = event.getPlayer().getInventory();
		ItemStack rod = inventory.getItemInMainHand();
		if (rod.getType() != Material.FISHING_ROD)
			rod = inventory.getItemInOffHand();
		if (rod.getType() != Material.FISHING_ROD)
			return;

		final ItemStack finalRod = rod;
		ForEach(rod, (enchant, level) -> enchant.OnFish(event, finalRod, level));
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void OnHunger(FoodLevelChangeEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		Player player = (Player) event.getEntity();
		ForEachArmor(player, (enchant, piece, level) -> enchant.OnHunger(event, player, piece, level));
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void OnMove(PlayerMoveEvent event)
	{
		if (event.getTo() == null)
			return;

		if (event.getFrom().getBlockX() == event.getTo().getBlockX()
				&& event.getFrom().getBlockY() == event.getTo().getBlockY()
				&& event.getFrom().getBlockZ() == event.getTo().getBlockZ())
			return;

		Player player = event.getPlayer();
		if (!IsActivePlayer(player))
			return;

		ForEachArmor(player, (enchant, piece, level) -> enchant.OnMove(event, player, piece, level));
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void OnItemDamage(PlayerItemDamageEvent event)
	{
		ItemStack item = event.getItem();
		ForEach(item, (enchant, level) -> enchant.OnItemDamage(event, item, level));
	}

	private void Tick()
	{
		_seconds++;
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (player.isDead() || !IsActivePlayer(player))
				continue;

			ItemStack hand = player.getInventory().getItemInMainHand();
			ForEach(hand, (enchant, level) -> enchant.OnTick(player, hand, level, _seconds));
			ForEachArmor(player, (enchant, piece, level) -> enchant.OnTick(player, piece, level, _seconds));
		}
	}

	// ===== Soulbound =====

	// Runs after DontLoseItems (NORMAL), which already keeps the hotbar and armor
	@EventHandler(priority = EventPriority.HIGH)
	public void OnPlayerDeath(PlayerDeathEvent event)
	{
		CustomEnchant soulbound = CustomEnchantRegistry.Get(SoulboundEnchant.KEY);
		if (soulbound instanceof SoulboundEnchant && EnchantSettings.IsEnabled(soulbound))
			((SoulboundEnchant) soulbound).OnPlayerDeath(event);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void OnRespawn(PlayerRespawnEvent event)
	{
		CustomEnchant soulbound = CustomEnchantRegistry.Get(SoulboundEnchant.KEY);
		if (!(soulbound instanceof SoulboundEnchant))
			return;

		// A tick later so DontLoseItems has put the hotbar and armor back first
		Player player = event.getPlayer();
		Bukkit.getScheduler().runTask(_plugin, () -> ((SoulboundEnchant) soulbound).GiveBack(player));
	}

	// ===== Temporary blocks (Magma Walker) can't be taken, pushed or blown up =====

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void OnTemporaryBlockBreak(BlockBreakEvent event)
	{
		if (TemporaryBlocks.IsTemporary(event.getBlock()))
			event.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void OnPistonExtend(BlockPistonExtendEvent event)
	{
		for (Block block : event.getBlocks())
		{
			if (TemporaryBlocks.IsTemporary(block))
			{
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void OnPistonRetract(BlockPistonRetractEvent event)
	{
		for (Block block : event.getBlocks())
		{
			if (TemporaryBlocks.IsTemporary(block))
			{
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void OnEntityExplode(EntityExplodeEvent event)
	{
		event.blockList().removeIf(TemporaryBlocks::IsTemporary);
	}

	@EventHandler(ignoreCancelled = true)
	public void OnBlockExplode(BlockExplodeEvent event)
	{
		event.blockList().removeIf(TemporaryBlocks::IsTemporary);
	}

	// ===== Applying books to items without slots =====

	// Custom book on cursor clicked on an item without slots: adds the enchant at level I.
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

		if (!EnchantSettings.IsEnabled(enchant))
		{
			player.sendMessage(Metods.msgC("&d" + enchant.GetName() + " &cis disabled on this server!"));
			return;
		}

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

		for (Enchantment vanilla : target.getEnchantments().keySet())
		{
			if (enchant.GetVanillaConflicts().contains(vanilla))
			{
				player.sendMessage(Metods.msgC("&d" + enchant.GetName() + " &ccan't be combined with &e"
						+ vanilla.getKey().getKey().replace('_', ' ') + "&c!"));
				return;
			}
		}

		current.put(enchant, 1);

		CustomEnchant curse = CustomEnchantBook.ReadCurse(cursor);
		if (curse != null && !current.containsKey(curse) && CustomEnchantRegistry.FindConflict(curse, current.keySet()) == null)
			current.put(curse, 1);

		CustomEnchantData.Set(target, current);
		event.setCurrentItem(target);

		cursor.setAmount(cursor.getAmount() - 1);
		event.getView().setCursor(cursor.getAmount() > 0 ? cursor : null);

		player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f);
		player.sendMessage(Metods.msgC("&dApplied &e" + enchant.GetDisplayName(1) + "&d!"
				+ (curse != null ? " &cThe book was cursed with &4" + curse.GetName() + "&c!" : "")));
	}
}
