package me.imu.imusdontloseitems.Events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;
import imu.iAPI.Other.XpUtil;

public class MainEvents implements Listener
{
	// player PDC, pending until respawn
	private final String PD_DEATH_PENALTY = "dli_death_penalty";
	private final String PD_DEATH_XP_LEVEL = "dli_death_xp_level";

	final String[] tools = { "PICKAXE", "AXE", "HOE", "SHOVEL", "ROD", "ELYTRA" };
	final String[] weapons = { "SWORD", "BOW", "TRIDENT", "SHIELD", "CROSSBOW" };

	boolean saveArmor = true;
	boolean saveHotBar = true;
	boolean saveTools = false;
	boolean saveWeapons = false;
	boolean _saveXp = true;

	private boolean _disableElytraOnPVP = true;

	double durability_penalty_pve = 0.25;
	double durability_penalty_pvp = 0.6;
	double durability_penalty_mob = 0.1;
	double _death_xp_multiplier = 0.5; // if 0 doesnt give xp back
	Plugin _plugin;
	Metods _itemM = null;
	Cooldowns _cd;

	String _cd_in_combat_dmg = "entity_combat_";
	int _cd_in_combat_cooldown = 10;

	HashMap<Player, HashSet<EntityType>> _player_combat_with = new HashMap<>();
	HashMap<UUID, Double> _player_combat_penalty_join = new HashMap<>();

	private List<String> _ingnoreWorlds = new ArrayList<>();

	public MainEvents(Plugin plugin)
	{
		_plugin = plugin;
		_itemM = ImusAPI._metods;
		_cd = new Cooldowns();
		LoadSettings(plugin.getConfig());
		runnable();
	}

	// config.yml, re-read when the settings menu changes a value
	public void LoadSettings(FileConfiguration config)
	{
		saveHotBar = config.getBoolean("death.keep-hotbar", true);
		saveArmor = config.getBoolean("death.keep-armor", true);
		saveTools = config.getBoolean("death.keep-tools", false);
		saveWeapons = config.getBoolean("death.keep-weapons", false);
		durability_penalty_pve = config.getDouble("death.pve-durability-penalty", 0.25);
		durability_penalty_pvp = config.getDouble("death.pvp-durability-penalty", 0.6);
		_saveXp = config.getBoolean("death.keep-xp", true);
		_death_xp_multiplier = config.getDouble("death.xp-levels-kept", 0.5);

		_cd_in_combat_cooldown = config.getInt("combat.tag-seconds", 10);
		durability_penalty_mob = config.getDouble("combat.logout-durability-penalty", 0.1);
		_disableElytraOnPVP = config.getBoolean("combat.disable-elytra-in-pvp", true);

		_ingnoreWorlds = config.getStringList("ignore-worlds");
	}

	@EventHandler
	public void OnMove(PlayerMoveEvent e)
	{
		if (e.getPlayer().isGliding())
		{

			if (!_disableElytraOnPVP)
				return;

			if (!_player_combat_with.containsKey(e.getPlayer()))
				return;

			if (!_player_combat_with.get(e.getPlayer()).contains(EntityType.PLAYER))
				return;

			e.getPlayer().setGliding(false);
			if (!_cd.isCooldownReady(e.getPlayer().getUniqueId().toString() + "elytraPVP"))
				return;

			_cd.setCooldownInSeconds(e.getPlayer().getUniqueId().toString() + "elytraPVP", 2);

			e.getPlayer().sendMessage(Metods.msgC("&cYou are unable to use elytra on PVP combat!"));

		}
	}

	@EventHandler
	public void onInteract(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getDamager() instanceof Player && !(event.getEntity() instanceof Player)
				&& event.getEntity() instanceof Monster)
		{
			Player p = (Player) event.getDamager();
			setInCombat(p, event.getEntity());

		}

		if (event.getDamager() instanceof Monster && event.getEntity() instanceof Player)
		{
			Player p = (Player) event.getEntity();
			setInCombat(p, event.getDamager());
		}

		if (event.getDamager() instanceof Projectile && event.getEntity() instanceof Player)
		{
			Projectile pr = (Projectile) event.getDamager();
			if (!(pr.getShooter() instanceof LivingEntity))
			{
				return;
			}
			Player p = (Player) event.getEntity();

			setInCombat(p, ((Entity) pr.getShooter()));
		}

	}

	void setInCombat(Player p, Entity with_mob)
	{
		_cd.setCooldownInSeconds(_cd_in_combat_dmg + p.getName(), _cd_in_combat_cooldown);

		boolean send_smg2 = true;
		if (_player_combat_with.containsKey(p))
		{
			send_smg2 = false;
			if (!_player_combat_with.get(p).contains(with_mob.getType()))
			{
				_player_combat_with.get(p).add(with_mob.getType());
			}

		} else
		{
			HashSet<EntityType> arr = new HashSet<>();
			arr.add(with_mob.getType());
			_player_combat_with.put(p, arr);

		}
		if (send_smg2)
		{
			if (new Random().nextInt(10) <= 1)
				p.sendMessage(ChatColor.RED + "If you log out, in combat you will lose durability from all items! ");

		}
	}

	void runnable()
	{
		new BukkitRunnable() {

			@Override
			public void run()
			{
				ArrayList<Player> removeThesePlayers = new ArrayList<>();
				for (Map.Entry<Player, HashSet<EntityType>> entry : _player_combat_with.entrySet())
				{
					Player p = entry.getKey();
					if (_cd.isCooldownReady(_cd_in_combat_dmg + p.getName()))
					{
						removeThesePlayers.add(p);
					}
				}

				for (Player p : removeThesePlayers)
				{
					_player_combat_with.remove(p);
				}

			}
		}.runTaskTimer(_plugin, 0, 20);
	}

	void removeCooldownAndCombat(Player p)
	{
		_player_combat_with.remove(p);
		_cd.removeCooldown(_cd_in_combat_dmg + p.getName());
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e)
	{
		if (_player_combat_with.containsKey(e.getPlayer()))
		{
			_player_combat_penalty_join.put(e.getPlayer().getUniqueId(), durability_penalty_mob);
			removeCooldownAndCombat(e.getPlayer());
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e)
	{
		if (_player_combat_penalty_join.containsKey(e.getPlayer().getUniqueId()))
		{
			e.getPlayer().sendMessage(ChatColor.RED + "You have forfeited good fight! Next time do not leave!");
			setDurabilityPenalty(e.getPlayer(), _player_combat_penalty_join.get(e.getPlayer().getUniqueId()));
			_player_combat_penalty_join.remove(e.getPlayer().getUniqueId());
		}
	}

	void setDurabilityPenalty(Player p, double prosent)
	{
		PlayerInventory inv = p.getInventory();

		ItemStack[] s_items = inv.getContents();

		if (s_items == null)
		{
			return;
		}

		for (int i = 0; i < inv.getContents().length; ++i)
		{
			ItemStack item = s_items[i];
			if (item != null)
			{
				_itemM.giveDamage(item, (int) (item.getType().getMaxDurability() * prosent), false);

				inv.setItem(i, item);
			}
		}

		prosent = (int) (prosent * 100);
		if (prosent > 0)
		{
			p.sendMessage(ChatColor.GRAY + "All your items has lost durability: " + ChatColor.RED + prosent + "%");
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e)
	{
		Player player = e.getEntity();
		PlayerInventory inv = player.getInventory();

		if (_ingnoreWorlds.contains(player.getWorld().getName()))
		{
			return;
		}

		// keepInventory already keeps everything, nothing to save or punish
		if (e.getKeepInventory())
			return;

		removeCooldownAndCombat(player);

		ItemStack[] content = inv.getContents();
		boolean anyKept = false;

		for (int l = 0; l < content.length; ++l)
		{
			ItemStack stack = content[l];
			if (stack == null || stack.getType() == Material.AIR)
				continue;

			// vanilla destroys these
			if (Metods._ins.HasEnchant(stack, Enchantment.VANISHING_CURSE))
				continue;

			boolean keep = false;

			if (saveHotBar && l < 9)
				keep = true;

			if (saveTools && stringEndsWith(stack.getType().toString(), tools))
				keep = true;

			if (saveWeapons && stringEndsWith(stack.getType().toString(), weapons))
				keep = true;

			// 36-39 armor, 40 offhand
			if (saveArmor && l > content.length - 6)
				keep = true;

			if (!keep)
				continue;

			// Paper leaves kept items in their slots on the player, so they are saved with the
			// player data and survive a restart before respawn; they still have to come out of the drops
			e.getItemsToKeep().add(stack);
			e.getDrops().remove(stack);
			anyKept = true;
		}

		// Stored on the player (not in memory) so a restart between death and respawn doesn't lose them
		if (anyKept)
		{
			boolean pvp = player.getKiller() != null && !player.getKiller().equals(player);
			Metods._ins.setPersistenData(player, PD_DEATH_PENALTY, PersistentDataType.DOUBLE,
					pvp ? durability_penalty_pvp : durability_penalty_pve);
		}

		if (_saveXp)
		{
			// part of the levels comes back on respawn, so no orbs on top of that
			e.setDroppedExp(0);
			Metods._ins.setPersistenData(player, PD_DEATH_XP_LEVEL, PersistentDataType.INTEGER, player.getLevel());
		}
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent e)
	{
		Player player = e.getPlayer();

		Double penalty = Metods._ins.getPersistenData(player, PD_DEATH_PENALTY, PersistentDataType.DOUBLE);
		if (penalty == null)
			return;

		player.getPersistentDataContainer().remove(new NamespacedKey(ImusAPI._instance, PD_DEATH_PENALTY));

		// the inventory only holds what onDeath kept
		for (ItemStack item : player.getInventory().getContents())
		{
			if (item == null || item.getType().getMaxDurability() <= 0)
				continue;

			_itemM.giveDamage(item, (int) (item.getType().getMaxDurability() * penalty), false);
		}

		int prosent = (int) (penalty * 100);
		if (prosent > 0)
		{
			player.sendMessage(ChatColor.GRAY + "All your items has lost durability: " + ChatColor.RED + prosent + "%");
		}
	}

	@EventHandler
	public void onRespawnXpSet(PlayerRespawnEvent e)
	{
		Player player = e.getPlayer();
		Integer xpLevel = Metods._ins.getPersistenData(player, PD_DEATH_XP_LEVEL, PersistentDataType.INTEGER);
		if (xpLevel == null)
			return;

		player.getPersistentDataContainer().remove(new NamespacedKey(ImusAPI._instance, PD_DEATH_XP_LEVEL));

		if (!_saveXp || xpLevel <= 0)
			return;

		final int newLevel = (int) (xpLevel * _death_xp_multiplier);
		Bukkit.getScheduler().scheduleSyncDelayedTask(_plugin, () ->
		{
			XpUtil.SetPlayerLevel(player, newLevel < 1 ? 0 : newLevel);
		});
	}

	boolean stringEndsWith(String target, String[] array)
	{
		for (String str : array)
		{
			if (target.endsWith(str))
				return true;
		}

		return false;
	}

}
