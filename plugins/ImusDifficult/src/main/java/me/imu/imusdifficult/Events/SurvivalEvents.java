package me.imu.imusdifficult.Events;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.DateParser;
import imu.iAPI.Utilities.ItemUtils;
import me.imu.imusdifficult.ImusDifficult;
import me.imu.imusdifficult.other.MinecraftJokes;

// The survival tweaks that used to live in DontLoseItems' MainEvents next to the death handling
public class SurvivalEvents implements Listener
{
	private Cooldowns _cd;
	private Random _rand;
	private MinecraftJokes _joker;

	private int _totemJokeChance = 20;
	private boolean _shulkerBulletExtraDamage = true;
	private Date _netherOpenDate;
	private Date _endOpenDate;
	private double _mendNerf = 1.0;
	private boolean _enableGolemDieOnWither = true;
	private boolean _removeTreasureMaps = true;

	public SurvivalEvents()
	{
		_cd = new Cooldowns();
		_rand = new Random();
		_joker = new MinecraftJokes();
		LoadSettings(ImusDifficult.Instance.getConfig());
	}

	// config.yml, re-read when the settings menu changes a value
	public void LoadSettings(FileConfiguration config)
	{
		_netherOpenDate = DateParser.ParseDate(config.getString("portals.nether-open-date", "10/02/2023/18:00"));
		_endOpenDate = DateParser.ParseDate(config.getString("portals.end-open-date", "24/02/2023/18:00"));
		_enchantedCarrotXP = config.getDouble("enchanted-carrot.xp", 10);
		_chanceToDropEnchantedCarrot = config.getDouble("enchanted-carrot.drop-chance", 0.1);
		_totemJokeChance = config.getInt("totem-jokes.chance", 20);
		_shulkerBulletExtraDamage = config.getBoolean("shulker-bullet-extra-damage", true);
		_enableGolemDieOnWither = config.getBoolean("iron-golem-dies-on-wither", true);
		_mendNerf = config.getDouble("mending.repair-multiplier", 1.0);
		_removeTreasureMaps = config.getBoolean("treasure-maps.remove", true);
	}

	@EventHandler
	public void OnEntityDamage(EntityDamageEvent e)
	{

		if (!(e.getEntity() instanceof Player))
			return;

		ItemStack stack = ((Player) e.getEntity()).getInventory().getItemInOffHand();

		if (stack == null || stack.getType() != Material.TOTEM_OF_UNDYING)
			return;

		if (_rand.nextInt(100) >= _totemJokeChance)
			return;

		((Player) e.getEntity()).sendMessage(" ");
		((Player) e.getEntity()).sendMessage(ChatColor.BLUE + _joker.GetTotemJoke());
	}

	@EventHandler
	public void ProjectileLaunch(ProjectileHitEvent e)
	{
		if (!_shulkerBulletExtraDamage)
			return;

		if (e.getEntity().getShooter() instanceof Shulker)
		{
			if (e.getHitEntity() instanceof Player)
			{

				Player player = (Player) e.getHitEntity();

				if (player.getGameMode() != GameMode.SURVIVAL)
					return;

				if (player.isBlocking() && !player.hasCooldown(Material.SHIELD))
				{
					player.setCooldown(Material.SHIELD, 60);
					return;
				}

				int protectionLevel = ImusAPI._metods.GetArmorSlotEnchantCount(player,
						Enchantment.PROTECTION);
				int protectileLevel = ImusAPI._metods.GetArmorSlotEnchantCount(player,
						Enchantment.PROJECTILE_PROTECTION);
				double toughnest = player.getAttribute(Attribute.ARMOR_TOUGHNESS).getValue();

				if (toughnest < 1)
					toughnest = 1;
				double health = player.getHealth()
						- (16.0 * (17.0 / (1.0 + 0.2 * protectionLevel + 0.4 * protectileLevel)) / (toughnest * 2));

				if (health < 0)
					health = 0;

				player.setHealth(health);
			}
		}
	}

	@EventHandler
	public void CancelPortals(PlayerPortalEvent e)
	{
		if (e.getPlayer().isOp())
			return;

		String id = "portal." + e.getPlayer().getUniqueId().toString();
		final float portalCd = 2f;

		Location to = e.getTo();
		if (to == null)
			return;

		World world = to.getWorld();
		if (world == null)
			return;

		switch (world.getName())
		{
		case "world_nether" ->
		{
			if (!IsNetherAllowed())
			{
				if (!_cd.isCooldownReady(id))
				{
					e.setCancelled(true);
					return;
				}

				e.getPlayer().sendMessage(ChatColor.RED + "Nether isn't opened yet!");
				e.getPlayer().sendMessage(
						ChatColor.DARK_PURPLE + "Nether opens in " + DateParser.GetTimeDifference(_netherOpenDate));
				e.setCancelled(true);

				_cd.addCooldownInSeconds(id, portalCd);
			}
		}
		case "world_the_end" ->
		{
			if (!IsEndAllowed())
			{
				if (!_cd.isCooldownReady(id))
				{
					e.setCancelled(true);
					return;
				}

				e.getPlayer().sendMessage(ChatColor.RED + "End isn't opened yet!");
				e.getPlayer().sendMessage(
						ChatColor.DARK_PURPLE + "End opens in " + DateParser.GetTimeDifference(_endOpenDate));
				e.setCancelled(true);

				_cd.addCooldownInSeconds(id, portalCd);
			}
		}
		}
	}

	public boolean IsNetherAllowed()
	{
		return DateParser.IsDateNowOrPassed(_netherOpenDate);
	}

	public boolean IsEndAllowed()
	{
		return DateParser.IsDateNowOrPassed(_endOpenDate);
	}

	@EventHandler
	public void MendingXp(PlayerItemMendEvent e)
	{
		e.setCancelled(true);

		int amount = (int) (e.getRepairAmount() * -1 * _mendNerf);

		if (amount >= 0)
			amount = -1;

		ImusAPI._metods.giveDamage(e.getItem(), amount, false);

	}

//============================ >>> ENCHANTED CARROT ==================================
	private ItemStack _itemStackEnchantedCarrot = null;
	private double _enchantedCarrotXP = 10;
	private double _chanceToDropEnchantedCarrot = 0.1f;

	private ItemStack getEnchantedCarrot()
	{
		if (_itemStackEnchantedCarrot != null)
			return _itemStackEnchantedCarrot.clone();

		ItemStack stack = new ItemStack(Material.CARROT);
		ItemUtils.SetDisplayName(stack, "&5Enchanted Carrot");
		ItemUtils.AddLore(stack, "&5Consume to get &2XP", false);
		ItemUtils.AddGlow(stack);
		ItemUtils.SetTag(stack, "dli_enchanted_carrot");
		_itemStackEnchantedCarrot = stack;

		return _itemStackEnchantedCarrot.clone();
	}

	@EventHandler
	public void OnCarrotDrop(BlockBreakEvent event)
	{
		if (event.getBlock().getType() == Material.CARROTS)
		{
			Ageable ageable = (Ageable) event.getBlock().getBlockData();
			if (ageable.getAge() == ageable.getMaximumAge())
			{
				if (_rand.nextFloat() < _chanceToDropEnchantedCarrot)
				{
					event.setDropItems(false);
					event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), getEnchantedCarrot());
				}
			}
		}
	}

	@EventHandler
	public void OnPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		ItemStack itemInHand = player.getInventory().getItemInMainHand();

		// Check if right-click on air with enchanted carrot
		if (event.getAction() == Action.RIGHT_CLICK_AIR && ItemUtils.HasTag(itemInHand, "dli_enchanted_carrot"))
		{
			event.setCancelled(true);
			// Consume the enchanted carrot
			if (itemInHand.getAmount() > 1)
			{
				itemInHand.setAmount(itemInHand.getAmount() - 1);
			} else
			{
				player.getInventory().setItemInMainHand(null);
			}

			player.giveExp((int) _enchantedCarrotXP);
		}
	}
	//============================ <<<< ENCHANTED CARROT ==================================

	@EventHandler
	public void OnGolemHitsWither(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getDamager() instanceof IronGolem && event.getEntity() instanceof Wither)
		{
			if (!_enableGolemDieOnWither)
				return;
			IronGolem golem = (IronGolem) event.getDamager();
			golem.setHealth(0);
			event.setCancelled(true);
		}
	}

	// reason that treasure map lags the server at 1.20.2!
	@EventHandler
	public void OnLootGenerateRemoveMAP(LootGenerateEvent event)
	{
		if (!_removeTreasureMaps)
			return;

		if (event.getWorld().getEnvironment() != World.Environment.NORMAL)
			return;

		List<ItemStack> loot = event.getLoot();
		for (int i = 0; i < loot.size(); i++)
		{
			ItemStack item = loot.get(i);

			if (item != null && item.getType() == Material.FILLED_MAP)
			{

				if (Math.random() < 0.1)
				{
					loot.set(i, new ItemStack(Material.HEART_OF_THE_SEA));
				} else
				{
					// Remove the item
					loot.remove(i);
				}
				break; // Assuming only one map per loot
			}
		}
	}
}
