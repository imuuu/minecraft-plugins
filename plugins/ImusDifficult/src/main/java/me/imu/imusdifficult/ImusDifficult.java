package me.imu.imusdifficult;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import imu.iAPI.Config.ConfigMenu;
import me.imu.imusdifficult.Events.DotEvents;
import me.imu.imusdifficult.Events.SurvivalEvents;
import me.imu.imusdifficult.other.AntiAfk;

public class ImusDifficult extends JavaPlugin
{
	public static ImusDifficult Instance;
	public static final String PERM_CONFIG = "imusdifficult.config";

	private SurvivalEvents _survivalEvents;
	private DotEvents _dotEvents;
	private AntiAfk _antiAfk;
	private ConfigMenu _configMenu;

	@Override
	public void onEnable()
	{
		Instance = this;
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();

		_survivalEvents = new SurvivalEvents();
		_dotEvents = new DotEvents();
		_antiAfk = new AntiAfk();
		getServer().getPluginManager().registerEvents(_survivalEvents, this);
		getServer().getPluginManager().registerEvents(_dotEvents, this);
		getServer().getPluginManager().registerEvents(_antiAfk, this);

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
		_survivalEvents.LoadSettings(getConfig());
		_dotEvents.LoadSettings(getConfig());
		_antiAfk.LoadSettings(getConfig());
	}

	private ConfigMenu CreateConfigMenu()
	{
		ConfigMenu menu = new ConfigMenu(this, "Difficulty Tweaks", PERM_CONFIG, this::ReloadSettings);

		menu.addString("portals.nether-open-date", "Nether opens", Material.NETHERRACK)
				.description("dd/MM/yyyy/HH:mm, e.g. 10/02/2023/18:00")
				.description("Ops can always go through").note("now");
		menu.addString("portals.end-open-date", "End opens", Material.END_STONE)
				.description("dd/MM/yyyy/HH:mm").note("now");
		menu.addInt("damage-over-time.armor-durability-loss", "Armor wear from poison/wither/fire", Material.SPIDER_EYE, 0, 100)
				.description("Durability every armor piece loses per hit, at most 5 times a second")
				.description("Full Hell leggings + chestplate protect from the fire part").note("now");
		menu.addInt("skeleton-arrows.food-loss", "Food lost per skeleton arrow", Material.BONE, 0, 20).note("now");
		menu.addInt("skeleton-arrows.food-loss-chance", "Skeleton arrow hunger chance", Material.ARROW, 0, 100)
				.description("% per hit, minus 3 per Projectile Protection level on the armor").note("now");
		menu.addBoolean("shulker-bullet-extra-damage", "Shulker bullets hit hard", Material.SHULKER_SHELL)
				.description("Damage depends on Protection, Projectile Protection and armor toughness")
				.description("A raised shield blocks it").note("now");
		menu.addDouble("mending.repair-multiplier", "Mending repair", Material.EXPERIENCE_BOTTLE, 0, 10)
				.description("1.0 = the vanilla repair amount").note("now");
		menu.addBoolean("iron-golem-dies-on-wither", "Iron golems die hitting a Wither", Material.IRON_BLOCK).note("now");
		menu.addDouble("enchanted-carrot.drop-chance", "Enchanted Carrot drop chance", Material.GOLDEN_CARROT, 0, 1)
				.description("Chance per fully grown carrot block, 0.1 = 10%").note("now");
		menu.addInt("enchanted-carrot.xp", "Enchanted Carrot XP", Material.CARROT, 0, 100000)
				.description("XP points (not levels) from eating one").note("now");
		menu.addInt("totem-jokes.chance", "Totem joke chance", Material.TOTEM_OF_UNDYING, 0, 100)
				.description("% per hit taken with a totem in the off-hand").note("now");
		menu.addBoolean("treasure-maps.remove", "Remove treasure maps", Material.FILLED_MAP)
				.description("Overworld chests: 10% become a Heart of the Sea, the rest are removed").note("now");
		menu.addBoolean("anti-afk.enabled", "AFK kick", Material.RED_BED).note("now");
		menu.addInt("anti-afk.warning-minutes", "AFK warning after", Material.BELL, 1, 1440)
				.description("Minutes without moving, survival players only").note("now");
		menu.addInt("anti-afk.kick-minutes", "AFK kick after", Material.BARRIER, 1, 1440).note("now");
		return menu;
	}
}
