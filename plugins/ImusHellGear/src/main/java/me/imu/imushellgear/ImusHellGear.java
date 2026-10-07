package me.imu.imushellgear;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import imu.iAPI.Config.ConfigMenu;
import me.imu.imushellgear.Events.VoidTotemEvents;
import me.imu.imushellgear.Managers.Manager_HellArmor;
import me.imu.imushellgear.Managers.Manager_HellTools;
import me.imu.imushellgear.Managers.Manager_IronArmor;
import me.imu.imushellgear.Managers.Manager_LegendaryUpgrades;
import me.imu.imushellgear.Managers.Manager_VoidStones;

public class ImusHellGear extends JavaPlugin
{
	public static ImusHellGear Instance;
	public static final String PERM_CONFIG = "imushellgear.config";

	private Manager_LegendaryUpgrades _manager_leg_upgrades;
	private Manager_IronArmor _manager_ironArmor;
	private ConfigMenu _configMenu;

	@Override
	public void onEnable()
	{
		Instance = this;
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();

		// same order as in DontLoseItems
		_manager_leg_upgrades = new Manager_LegendaryUpgrades();
		getServer().getPluginManager().registerEvents(new Manager_VoidStones(), this);
		getServer().getPluginManager().registerEvents(new Manager_HellArmor(), this);
		getServer().getPluginManager().registerEvents(new Manager_HellTools(), this);
		getServer().getPluginManager().registerEvents(new VoidTotemEvents(), this);
		_manager_ironArmor = new Manager_IronArmor();
		getServer().getPluginManager().registerEvents(_manager_ironArmor, this);

		_manager_leg_upgrades.SetTestItems();
		Manager_HellArmor.Instance.SetArmorTestItems();
		Manager_VoidStones.Instance.SetTestItems();

		_configMenu = CreateConfigMenu();
		_configMenu.register();
	}

	@Override
	public void onDisable()
	{
		if (Manager_HellTools.Instance != null)
			Manager_HellTools.Instance.OnDisable();
		if (_configMenu != null)
			_configMenu.unregister();
	}

	public void ReloadSettings()
	{
		reloadConfig();
		_manager_ironArmor.LoadSettings(getConfig());
	}

	public static boolean HasServerImusEnchants()
	{
		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("ImusEnchants");

		return (plugin != null && plugin.isEnabled());
	}

	private ConfigMenu CreateConfigMenu()
	{
		ConfigMenu menu = new ConfigMenu(this, "Hell Gear", PERM_CONFIG, this::ReloadSettings);

		menu.addBoolean("reinforced-iron-armor", "Reinforced Iron Armor", Material.IRON_CHESTPLATE)
				.description("8 iron ingots around an iron armor piece in a crafting table")
				.description("Each piece takes 0.5 less damage from every hit and wears faster")
				.note("damage now, recipes after a restart");
		return menu;
	}
}
