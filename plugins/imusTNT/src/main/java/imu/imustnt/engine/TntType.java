package imu.imustnt.engine;

import java.util.Locale;

/**
 * The special TNTs. The enum names are stored on items and placed blocks, so they stay as they are.
 */
public enum TntType
{
    /** Clears the layer of the chunk it goes off in */
    CHUNK_TNT("chunk-tnt", "AIR", 3),
    /** Turns the layer of the chunk into stone */
    CHUNK_STONE_TNT("chunk-stone-tnt", "STONE", 2);

    private final String _key;
    private final String _replacement;
    private final int _tier;

    TntType(String key, String replacement, int tier)
    {
        _key = key;
        _replacement = replacement;
        _tier = tier;
    }

    /**
     * Its name in config.yml and commands, e.g. "chunk-tnt".
     */
    public String key()
    {
        return _key;
    }

    /**
     * The material the changed blocks become.
     */
    public String replacement()
    {
        return _replacement;
    }

    /**
     * How valuable it is for shops, 1 to 3. Clearing a whole layer is worth more than covering it with stone.
     */
    public int tier()
    {
        return _tier;
    }

    /**
     * The id other plugins (imusGS) know it by, e.g. "imustnt:chunk_tnt".
     */
    public String id()
    {
        return "imustnt:" + name().toLowerCase(Locale.ROOT);
    }

    /**
     * A type by its key ("chunk-tnt") or enum name ("CHUNK_TNT"), or null.
     */
    public static TntType parse(String text)
    {
        if (text == null)
            return null;
        for (TntType type : values())
        {
            if (type.name().equalsIgnoreCase(text) || type._key.equalsIgnoreCase(text))
                return type;
        }
        return null;
    }
}
