package imu.iAPI.Database;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * The database section of imusAPI's config.yml, the same for every plugin.
 *
 * @param databases MySQL database names that differ from the plugin's own, by the name the plugin asks for
 * @param copyFrom  copy everything from this other database on startup, or null
 */
public record DatabaseSettings(DatabaseType type, String host, int port, String username, String password,
                               Map<String, String> databases, DatabaseType copyFrom)
{
    public static DatabaseSettings sqlite()
    {
        return new DatabaseSettings(DatabaseType.SQLITE, "localhost", 3306, "root", "", Map.of(), null);
    }

    /**
     * @throws IllegalArgumentException with a message for the console when a value is wrong
     */
    public static DatabaseSettings read(ConfigurationSection section)
    {
        if (section == null)
            return sqlite();

        DatabaseType type = DatabaseType.parse(section.getString("type", "sqlite"));
        if (type == null)
            throw new IllegalArgumentException("database.type must be sqlite or mysql, not '" + section.getString("type") + "'");

        String copyText = section.getString("copy-from", "");
        DatabaseType copyFrom = copyText == null || copyText.isBlank() ? null : DatabaseType.parse(copyText);
        if (copyText != null && !copyText.isBlank() && copyFrom == null)
            throw new IllegalArgumentException("database.copy-from must be sqlite, mysql or empty, not '" + copyText + "'");
        if (copyFrom == type)
            copyFrom = null;

        Map<String, String> databases = new HashMap<>();
        ConfigurationSection names = section.getConfigurationSection("mysql.databases");
        if (names != null)
        {
            for (String key : names.getKeys(false))
                databases.put(key, names.getString(key));
        }
        return new DatabaseSettings(type, section.getString("mysql.host", "localhost"), section.getInt("mysql.port", 3306),
                section.getString("mysql.username", "root"), section.getString("mysql.password", ""), Map.copyOf(databases), copyFrom);
    }

    /**
     * The MySQL database of a plugin's data: its own name unless config.yml names another.
     */
    public String databaseName(String name)
    {
        return databases.getOrDefault(name, name);
    }

    /**
     * Older versions kept a DataBaseSettings.yml in each plugin's folder. Their MySQL settings, taken from the
     * imusAPI one first, or null when there is none.
     *
     * @param pluginsFolder the server's plugins folder
     * @param names         plugin folder to the database name it asks for, e.g. "imusGS" -> "imusGS"
     */
    public static DatabaseSettings fromOldFiles(File pluginsFolder, Map<String, String> names)
    {
        DatabaseSettings found = null;
        Map<String, String> databases = new HashMap<>();
        for (Map.Entry<String, String> plugin : names.entrySet())
        {
            File file = new File(new File(pluginsFolder, plugin.getKey()), "DataBaseSettings.yml");
            if (!file.isFile())
                continue;
            YamlConfiguration old = YamlConfiguration.loadConfiguration(file);
            String database = old.getString("Database", plugin.getValue());
            if (database != null && !database.isBlank() && !database.equals(plugin.getValue()))
                databases.put(plugin.getValue(), database);
            if (found == null || plugin.getKey().equalsIgnoreCase("imusAPI"))
            {
                found = new DatabaseSettings(DatabaseType.MYSQL, old.getString("Host", "localhost"), old.getInt("Port", 3306),
                        old.getString("Username", "root"), old.getString("Password", ""), Map.of(), null);
            }
        }
        if (found == null)
            return null;
        return new DatabaseSettings(found.type(), found.host(), found.port(), found.username(), found.password(),
                Map.copyOf(databases), null);
    }

    /**
     * Writes these settings to the database section.
     */
    public void write(ConfigurationSection section)
    {
        section.set("type", type.key());
        section.set("mysql.host", host);
        section.set("mysql.port", port);
        section.set("mysql.username", username);
        section.set("mysql.password", password);
        databases.forEach((name, database) -> section.set("mysql.databases." + name, database));
        section.set("copy-from", copyFrom == null ? "" : copyFrom.key());
    }
}
