package imu.iAPI.Database;

import java.util.Locale;

/**
 * Where the plugins keep their data.
 */
public enum DatabaseType
{
    /**
     * A file in each plugin's own folder; nothing to install.
     */
    SQLITE,
    /**
     * A MySQL or MariaDB server, for big servers or several servers sharing the data.
     */
    MYSQL;

    /**
     * "sqlite" or "mysql" (also "mariadb"), any case; null when it is neither.
     */
    public static DatabaseType parse(String text)
    {
        if (text == null)
            return null;
        return switch (text.trim().toLowerCase(Locale.ROOT))
        {
            case "sqlite" -> SQLITE;
            case "mysql", "mariadb" -> MYSQL;
            default -> null;
        };
    }

    public String key()
    {
        return name().toLowerCase(Locale.ROOT);
    }
}
