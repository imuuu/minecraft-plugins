package imu.spawners;

import imu.iAPI.Config.ConfigMenu;
import imu.iAPI.Utilities.TextUtils;
import imu.spawners.engine.Luck;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Spawners with silk touch: a natural spawner drops on a lucky roll, and every miss makes the next roll likelier.
 * A spawner a player placed always comes back with silk touch, as the same mob.
 */
public class ImusSpawners extends JavaPlugin
{
    private SpawnerItems _items;
    private PlacedSpawners _placed;
    private LuckStore _stats;
    private ConfigMenu _configMenu;

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        migrateOldSettings();

        _items = new SpawnerItems(this);
        _placed = new PlacedSpawners(this);
        _stats = new LuckStore(this);
        getServer().getPluginManager().registerEvents(new SpawnerListener(this, _items, _placed, _stats), this);

        SpawnerCommands commands = new SpawnerCommands(this, _items, _stats);
        PluginCommand command = getCommand("spawners");
        command.setExecutor(commands);
        command.setTabCompleter(commands);

        _configMenu = new ConfigMenu(this, "ImusSpawners", "imusspawners.admin", this::reloadConfig);
        _configMenu.addDouble("silk-touch-chance", "Drop chance", Material.SPAWNER, 0, 100)
                .description("% chance a natural spawner drops with silk touch");
        _configMenu.addDouble("chance-increase-per-miss", "Increase per miss", Material.EXPERIENCE_BOTTLE, 0, 100)
                .description("% a miss adds to the player's chance");
        _configMenu.addDouble("max-chance", "Highest chance", Material.NETHER_STAR, 0, 100)
                .description("The chance never goes above this %");
        _configMenu.addDouble("random-type-chance", "Random mob chance", Material.ZOMBIE_HEAD, 0, 100)
                .description("% chance a dropped natural spawner is a random mob");
        _configMenu.register();
    }

    @Override
    public void onDisable()
    {
        if (_configMenu != null)
            _configMenu.unregister();
        if (_stats != null)
            _stats.saveNow();
        if (_placed != null)
            _placed.saveLegacyNow();
    }

    public Luck luck()
    {
        return new Luck(getConfig().getDouble("silk-touch-chance", 14), getConfig().getDouble("chance-increase-per-miss", 13),
                getConfig().getDouble("max-chance", 100));
    }

    /**
     * The old imusSpawners kept its settings in settings.yml; they move to config.yml once.
     */
    private void migrateOldSettings()
    {
        File old = new File(getDataFolder(), "settings.yml");
        if (!old.exists() || getConfig().getBoolean("migrated-old-settings", false))
            return;
        YamlConfiguration settings = YamlConfiguration.loadConfiguration(old);
        if (settings.contains("settings.spawnerDropChanceSilkTouch"))
            getConfig().set("silk-touch-chance", settings.getDouble("settings.spawnerDropChanceSilkTouch"));
        if (settings.contains("settings.spawnerRandomEntityChance"))
            getConfig().set("random-type-chance", settings.getDouble("settings.spawnerRandomEntityChance"));
        if (settings.contains("settings.playerBonusChanceIncrease"))
            getConfig().set("chance-increase-per-miss", settings.getDouble("settings.playerBonusChanceIncrease"));
        getConfig().set("migrated-old-settings", true);
        saveConfig();
        getLogger().info("Moved the old settings.yml into config.yml");
    }

    public String text(String key, Object... placeholders)
    {
        String text = getConfig().getString("messages." + key, key);
        for (int i = 0; i + 1 < placeholders.length; i += 2)
            text = text.replace("{" + placeholders[i] + "}", String.valueOf(placeholders[i + 1]));
        return text;
    }

    /**
     * Sends a message of config.yml; an empty one isn't sent.
     */
    public void send(CommandSender to, String key, Object... placeholders)
    {
        String text = text(key, placeholders);
        if (!text.isEmpty())
            to.sendMessage(TextUtils.Color(text));
    }

    /**
     * A % for messages: whole numbers without decimals.
     */
    static String percent(double value)
    {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(Math.round(value * 10) / 10.0);
    }
}
