package imu.waystones;

import imu.iAPI.Config.ConfigMenu;
import imu.iAPI.Other.MySQL;
import imu.iAPI.Utilities.TextUtils;
import imu.waystones.engine.Cost;
import imu.waystones.engine.Rules;
import imu.waystones.engine.UpgradeKind;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Waystones players build from a base block, a wall and a slab, and travel between. Each player upgrades the
 * waystones they use for themselves: faster casts, shorter cooldowns, other dimensions.
 */
public class ImusWaystones extends JavaPlugin
{
    static final String ADMIN = "imuswaystones.admin";

    private WaystoneDb _db;
    private Waystones _waystones;
    private Holograms _holograms;
    private Teleports _teleports;
    private final WaystoneGame _menuGame = new PaperWaystoneGame(this);
    private WaystoneCommands _commands;
    private Structure _structure;
    private Rules _rules;
    private ConfigMenu _configMenu;

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        readSettings();

        _db = new WaystoneDb(this, new MySQL(this, 4, "imusWaystones"));
        _waystones = new Waystones(_db);
        _holograms = new Holograms(this, _waystones);
        _waystones.setHolograms(_holograms);
        _teleports = new Teleports(this, _waystones);
        _teleports.start();
        _commands = new WaystoneCommands(this);

        getServer().getPluginManager().registerEvents(new WaystoneListener(this), this);
        getServer().getPluginManager().registerEvents(_holograms, this);
        getServer().getPluginManager().registerEvents(_teleports, this);
        PluginCommand command = getCommand("waystones");
        command.setExecutor(_commands);
        command.setTabCompleter(_commands);

        _configMenu = new ConfigMenu(this, "imusWaystones", ADMIN, this::readSettings);
        _configMenu.addInt("min-distance-between-own", "Room between own waystones", Material.COMPASS, 0, 100_000)
                .description("Blocks between a player's own waystones");
        _configMenu.addDouble("teleport.cast-seconds", "Cast time", Material.CLOCK, 0, 600)
                .description("Seconds to stand still before a teleport, without upgrades");
        _configMenu.addDouble("teleport.cast-seconds-per-tier", "Cast time per tier", Material.LAPIS_BLOCK, 0, 600)
                .description("Seconds every Cast Time tier takes off");
        _configMenu.addDouble("teleport.cooldown-percent-per-tier", "Cooldown % per tier", Material.DIAMOND, 0, 100)
                .description("% of the cooldown every Cooldown tier takes off");
        _configMenu.addInt("teleport.xp-levels", "XP levels per teleport", Material.EXPERIENCE_BOTTLE, 0, 1000)
                .description("Levels a teleport costs without upgrades; 0 = free");
        _configMenu.addDouble("cancel-distance", "Allowed movement", Material.LEATHER_BOOTS, 0, 10)
                .description("Blocks a player may move while the teleport gets ready");
        _configMenu.addDouble("name-view-distance", "Name view distance", Material.NAME_TAG, 1, 128)
                .description("Blocks from which the floating name shows");
        _configMenu.register();

        _db.load(loaded ->
        {
            _waystones.load(loaded);
            _holograms.removeOldEverywhere();
            _holograms.showAll();
            getLogger().info(_waystones.all().size() + " waystones");
        });
    }

    @Override
    public void onDisable()
    {
        if (_configMenu != null)
            _configMenu.unregister();
        if (_teleports != null)
            _teleports.stop();
        if (_holograms != null)
            _holograms.hideAll();
        if (_db != null)
            _db.close();
    }

    /**
     * /waystones reload: the settings, and the waystones from the database.
     */
    void reload()
    {
        reloadConfig();
        readSettings();
        _db.load(loaded ->
        {
            _waystones.load(loaded);
            _holograms.showAll();
        });
    }

    private void readSettings()
    {
        FileConfiguration config = getConfig();
        _structure = new Structure(config.getStringList("structure.base-blocks"), getLogger());
        Map<UpgradeKind, List<List<Cost>>> ladders = new EnumMap<>(UpgradeKind.class);
        for (UpgradeKind kind : UpgradeKind.BUYABLE)
        {
            List<List<Cost>> steps = new ArrayList<>();
            for (String step : config.getStringList("upgrades." + kind.key()))
            {
                try
                {
                    List<Cost> cost = Cost.parse(step);
                    for (Cost part : cost)
                    {
                        if (Material.matchMaterial(part.material()) == null)
                            throw new IllegalArgumentException("no item " + part.material());
                    }
                    steps.add(cost);
                } catch (IllegalArgumentException e)
                {
                    // A broken step ends the ladder there, so the tiers after it can't be bought by mistake
                    getLogger().warning("upgrades." + kind.key() + ": " + e.getMessage());
                    break;
                }
            }
            ladders.put(kind, steps);
        }
        _rules = new Rules(config.getDouble("teleport.cast-seconds", 30), config.getDouble("teleport.cast-seconds-per-tier", 3),
                config.getDouble("teleport.min-cast-seconds", 1), config.getDoubleList("teleport.foundation-cooldown-seconds"),
                config.getDouble("teleport.cooldown-percent-per-tier", 5), config.getInt("teleport.xp-levels", 0),
                config.getInt("teleport.xp-levels-per-tier", 1), ladders);
    }

    Rules rules()
    {
        return _rules;
    }

    Structure structure()
    {
        return _structure;
    }

    Waystones waystones()
    {
        return _waystones;
    }

    Teleports teleports()
    {
        return _teleports;
    }

    /**
     * What the menus use of the server.
     */
    WaystoneGame menuGame()
    {
        return _menuGame;
    }

    WaystoneCommands commands()
    {
        return _commands;
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
}
