package imu.waystones.engine;

import java.util.Locale;

/**
 * What a player can upgrade on a waystone, for themselves. The names are stored in the database and on
 * upgrade items. BUILD and RENAME were the old plugin's and are only read.
 */
public enum UpgradeKind
{
    CAST_TIME, XP_USAGE, DIMENSION, COOLDOWN, BUILD, FOUNDATION, RENAME;

    /** The ones a player can buy */
    public static final UpgradeKind[] BUYABLE = { CAST_TIME, COOLDOWN, DIMENSION, XP_USAGE, FOUNDATION };

    /**
     * Its name in config.yml, e.g. "cast-time".
     */
    public String key()
    {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    public static UpgradeKind parse(String name)
    {
        if (name == null)
            return null;
        try
        {
            return valueOf(name.toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException e)
        {
            return null;
        }
    }
}
