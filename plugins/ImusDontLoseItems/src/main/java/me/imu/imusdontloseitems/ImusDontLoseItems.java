package me.imu.imusdontloseitems;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import imu.iAPI.Config.ConfigMenu;
import me.imu.imusdontloseitems.Events.MainEvents;

public class ImusDontLoseItems extends JavaPlugin
{
	public static ImusDontLoseItems Instance;
	public static final String PERM_CONFIG = "imusdontloseitems.config";

	private MainEvents _mainEvents;
	private ConfigMenu _configMenu;

	@Override
	public void onEnable()
	{
		Instance = this;
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();

		_mainEvents = new MainEvents(this);
		getServer().getPluginManager().registerEvents(_mainEvents, this);

		_configMenu = CreateConfigMenu();
		_configMenu.register();
	}

	@Override
	public void onDisable()
	{
		if (_configMenu != null)
			_configMenu.unregister();
	}

	public void ReloadSettings()
	{
		reloadConfig();
		_mainEvents.LoadSettings(getConfig());
	}

	private ConfigMenu CreateConfigMenu()
	{
		ConfigMenu menu = new ConfigMenu(this, "Don't Lose Items", PERM_CONFIG, this::ReloadSettings);

		menu.addBoolean("death.keep-hotbar", "Keep hotbar", Material.CHEST)
				.description("Hotbar items stay with the player on death").note("now");
		menu.addBoolean("death.keep-armor", "Keep armor and off-hand", Material.IRON_CHESTPLATE).note("now");
		menu.addBoolean("death.keep-tools", "Keep tools", Material.IRON_PICKAXE)
				.description("Pickaxes, axes, hoes, shovels, rods and elytras anywhere in the inventory").note("now");
		menu.addBoolean("death.keep-weapons", "Keep weapons", Material.IRON_SWORD)
				.description("Swords, bows, crossbows, tridents and shields anywhere in the inventory").note("now");
		menu.addDouble("death.pve-durability-penalty", "Durability loss, PvE death", Material.ANVIL, 0, 1)
				.description("0.25 = kept items lose 25% of their max durability").note("now");
		menu.addDouble("death.pvp-durability-penalty", "Durability loss, PvP death", Material.NETHERITE_SWORD, 0, 1)
				.description("Used when another player got the kill").note("now");
		menu.addBoolean("death.keep-xp", "Give levels back", Material.EXPERIENCE_BOTTLE)
				.description("No XP orbs drop, part of the levels comes back on respawn").note("now");
		menu.addDouble("death.xp-levels-kept", "Levels given back", Material.ENCHANTING_TABLE, 0, 1)
				.description("0.5 = half of the levels").note("now");
		menu.addInt("combat.tag-seconds", "Combat tag length", Material.CLOCK, 0, 600)
				.description("Seconds a player stays in combat after a monster fight").note("now");
		menu.addDouble("combat.logout-durability-penalty", "Durability loss, leaving in combat", Material.BARRIER, 0, 1)
				.description("All items lose this much when the player logs out in combat").note("now");
		menu.addBoolean("combat.disable-elytra-in-pvp", "No elytra in PvP combat", Material.ELYTRA).note("now");
		return menu;
	}
}
