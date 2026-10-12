package imu.hometele;

import imu.hometele.engine.Cooldowns;
import imu.hometele.engine.HomeBook;
import imu.iAPI.Config.ConfigMenu;
import imu.iAPI.Utilities.TextUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Homes players can teleport to: /sethome, /home, /delhome, /homes. The teleport takes a moment of standing
 * still, and has a cooldown. Admins can list and visit everyone's homes.
 */
public class HomeTele extends JavaPlugin
{
    private HomeBook _book;
    private HomeStore _store;
    private Teleports _teleports;
    private ConfigMenu _configMenu;

    @Override
    public void onEnable()
    {
        saveDefaultConfig();
        // Settings added in an update go into an existing config.yml
        getConfig().options().copyDefaults(true);
        saveConfig();
        migrateOldSettings();

        _store = new HomeStore(this);
        _book = _store.load();
        Cooldowns cooldowns = new Cooldowns();
        _teleports = new Teleports(this, cooldowns);
        _teleports.start();
        getServer().getPluginManager().registerEvents(_teleports, this);

        HomeCommands commands = new HomeCommands(this, _book, cooldowns, _teleports);
        for (String name : new String[] { "sethome", "home", "delhome", "homes", "homelist", "homeof" })
        {
            PluginCommand command = getCommand(name);
            command.setExecutor(commands);
            command.setTabCompleter(commands);
        }

        _configMenu = new ConfigMenu(this, "HomeTele", "hometele.admin", this::reloadConfig);
        _configMenu.addInt("max-homes", "Homes per player", Material.RED_BED, 1, 50)
                .description("How many homes a player can set");
        _configMenu.addDouble("warmup-seconds", "Teleport warmup", Material.CLOCK, 0, 120)
                .description("Seconds to stand still before the teleport");
        _configMenu.addDouble("teleport-cooldown-seconds", "Teleport cooldown", Material.ENDER_PEARL, 0, 86_400)
                .description("Seconds between teleports home");
        _configMenu.addDouble("sethome-cooldown-seconds", "Sethome cooldown", Material.COMPASS, 0, 86_400)
                .description("Seconds between setting homes");
        _configMenu.addDouble("cancel-cooldown-seconds", "Wait after a cancel", Material.BARRIER, 0, 3600)
                .description("Seconds to wait after moving or being hit");
        _configMenu.addDouble("cancel-distance", "Allowed movement", Material.LEATHER_BOOTS, 0, 10)
                .description("Blocks a player may move during the warmup");
        _configMenu.addBoolean("cancel-on-damage", "Damage cancels", Material.IRON_SWORD)
                .description("Taking damage cancels the teleport");
        _configMenu.register();

        getLogger().info(_book.owners().size() + " players have homes");
    }

    @Override
    public void onDisable()
    {
        if (_configMenu != null)
            _configMenu.unregister();
        if (_teleports != null)
            _teleports.stop();
        if (_book != null)
            _store.save(_book);
    }

    public void saveHomes()
    {
        _store.save(_book);
    }

    /**
     * The old HomeTele kept its settings in settings.yml; they move to config.yml once.
     */
    private void migrateOldSettings()
    {
        File old = new File(getDataFolder(), "settings.yml");
        if (!old.exists() || getConfig().getBoolean("migrated-old-settings", false))
            return;
        YamlConfiguration settings = YamlConfiguration.loadConfiguration(old);
        if (settings.contains("settings.teleCastTime"))
            getConfig().set("warmup-seconds", settings.getDouble("settings.teleCastTime"));
        if (settings.contains("settings.teleCooldown"))
            getConfig().set("teleport-cooldown-seconds", settings.getDouble("settings.teleCooldown"));
        if (settings.contains("settings.setHomeCooldown"))
            getConfig().set("sethome-cooldown-seconds", settings.getDouble("settings.setHomeCooldown"));
        getConfig().set("migrated-old-settings", true);
        saveConfig();
        getLogger().info("Moved the old settings.yml into config.yml");
    }

    /**
     * The message from config.yml with its placeholders filled in, &amp; codes not turned yet.
     */
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
