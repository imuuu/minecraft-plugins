package me.imu.imusunstablefearsystem;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import imu.iAPI.Config.ConfigMenu;
import me.imu.imusunstablefearsystem.CustomEnd.EndEvents;
import me.imu.imusunstablefearsystem.Events.BetterStructuresLootEvents;
import me.imu.imusunstablefearsystem.Events.ChestLootEvents;
import me.imu.imusunstablefearsystem.Events.ElytraGenerationEvents;
import me.imu.imusunstablefearsystem.Events.EndChestLootEvents;
import me.imu.imusunstablefearsystem.Events.NetherEvents;
import me.imu.imusunstablefearsystem.Managers.Manager_Difficult;

public class ImusUnstableFearSystem extends JavaPlugin
{
	public static ImusUnstableFearSystem Instance;
	public static final String PERM_CONFIG = "imusunstablefearsystem.config";

	private ElytraGenerationEvents _elytraEvents;
	private ConfigMenu _configMenu;

	@Override
	public void onEnable()
	{
		Instance = this;
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();

		// same order as in DontLoseItems
		getServer().getPluginManager().registerEvents(new NetherEvents(), this);
		getServer().getPluginManager().registerEvents(new EndEvents(), this);
		getServer().getPluginManager().registerEvents(new ChestLootEvents(), this);
		getServer().getPluginManager().registerEvents(new EndChestLootEvents(), this);
		if (getServer().getPluginManager().getPlugin("BetterStructures") != null)
			getServer().getPluginManager().registerEvents(new BetterStructuresLootEvents(), this);
		_elytraEvents = new ElytraGenerationEvents();
		getServer().getPluginManager().registerEvents(_elytraEvents, this);
		getServer().getPluginManager().registerEvents(new Manager_Difficult(), this);

		_configMenu = CreateConfigMenu();
		_configMenu.register();
	}

	@Override
	public void onDisable()
	{
		if (NetherEvents.Instance != null)
			NetherEvents.Instance.OnDisabled();
		if (EndEvents.Instance != null)
			EndEvents.Instance.OnDisabled();
		if (_configMenu != null)
			_configMenu.unregister();
	}

	public void ReloadSettings()
	{
		reloadConfig();
		NetherEvents.Instance.LoadSettings(getConfig());
		_elytraEvents.LoadSettings(getConfig());
	}

	private ConfigMenu CreateConfigMenu()
	{
		ConfigMenu menu = new ConfigMenu(this, "Unstable Fear System", PERM_CONFIG, this::ReloadSettings);

		menu.addBoolean("nether.speed-zombies", "Speed zombies", Material.ZOMBIE_HEAD)
				.description("5% of nether spawns become fast diamond-armored zombies").note("now");
		menu.addBoolean("nether.no-totem-in-offhand", "No totems in the off-hand", Material.TOTEM_OF_UNDYING)
				.description("A totem in the off-hand is moved into the inventory in the nether").note("now");
		menu.addBoolean("nether.mob-shield-reflects-arrows", "Mob shields reflect arrows", Material.SHIELD)
				.description("Nether mobs holding a shield send arrows back").note("now");
		menu.addBoolean("nether.wither-skeleton-speed", "Fast wither skeletons", Material.WITHER_SKELETON_SKULL).note("now");
		menu.addBoolean("nether.hoglin-speed", "Fast hoglins", Material.PORKCHOP).note("now");
		menu.addBoolean("nether.double-fire-damage", "Double fire damage", Material.BLAZE_POWDER)
				.description("Fire and burning, not lava, in the nether").note("now");
		menu.addBoolean("nether.ghast-fireball", "Ghast fireballs eat the ground", Material.FIRE_CHARGE)
				.description("Blocks around the hit turn to terracotta and vanish").note("now");
		menu.addBoolean("nether.blaze-fireball-spread", "Blaze fireballs spread fire", Material.BLAZE_ROD).note("now");
		menu.addBoolean("nether.magma-cube-lava", "Magma cubes leave lava", Material.MAGMA_CREAM).note("now");
		menu.addBoolean("nether.only-small-magma-cube-lava", "Only the smallest magma cubes", Material.MAGMA_BLOCK)
				.description("Only the last, smallest cube leaves a lava source").note("now");
		menu.addBoolean("nether.water-bottle-buff", "Water bottles put out fire", Material.POTION)
				.description("Drinking one in the nether gives Fire Resistance, 2 uses per bottle").note("now");
		menu.addInt("end.elytra-keep-chance", "End ship elytra chance", Material.ELYTRA, 0, 100)
				.description("% of End ship item frames that keep their elytra")
				.description("The rest get a diamond block").note("newly generated End ships");
		return menu;
	}

	public static boolean IsEnd(World world)
	{
		if (world == null)
			return false;

		return world.getEnvironment() == Environment.THE_END;
	}

	public static boolean IsEnd(Entity entity)
	{
		if (entity == null)
			return false;

		return IsEnd(entity.getWorld());
	}

	public static boolean IsEnd(Block block)
	{
		if (block == null)
			return false;

		return IsEnd(block.getWorld());
	}

	public static boolean IsEnd(Location loc)
	{
		if (loc == null)
			return false;

		return IsEnd(loc.getWorld());
	}
}
