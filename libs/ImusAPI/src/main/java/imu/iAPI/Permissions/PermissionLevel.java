package imu.iAPI.Permissions;

import java.util.Locale;

/**
 * How much a permission lets a player do, shown next to it in the permissions menu so an admin knows what they
 * hand out. Set in plugin.yml as {@code level: low|medium|high|very_high}.
 */
public enum PermissionLevel
{
    LOW("Low", "&a", "Everyday features, nothing that harms others"),
    MEDIUM("Medium", "&e", "Small advantages, or things others can notice"),
    HIGH("High", "&6", "Admin tools: free items, other players' data, teleports"),
    VERY_HIGH("Very high", "&c", "Full control: settings, server commands, permissions");

    private final String _label;
    private final String _colour;
    private final String _meaning;

    PermissionLevel(String label, String colour, String meaning)
    {
        _label = label;
        _colour = colour;
        _meaning = meaning;
    }

    public String label()
    {
        return _label;
    }

    /**
     * The &amp; colour code of the level.
     */
    public String colour()
    {
        return _colour;
    }

    /**
     * The label in its colour, e.g. "&amp;cVery high".
     */
    public String coloured()
    {
        return _colour + _label;
    }

    /**
     * What a permission of this level usually gives.
     */
    public String meaning()
    {
        return _meaning;
    }

    /**
     * Whether giving it asks first in the menu.
     */
    public boolean asksFirst()
    {
        return this == HIGH || this == VERY_HIGH;
    }

    /**
     * "low", "Very high", "very_high" or "very-high"; null for null.
     *
     * @throws IllegalArgumentException for anything else
     */
    public static PermissionLevel parse(String text)
    {
        if (text == null)
            return null;
        String key = text.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        for (PermissionLevel level : values())
        {
            if (level.name().equals(key))
                return level;
        }
        throw new IllegalArgumentException("Unknown permission level '" + text + "', use low, medium, high or very_high");
    }
}
