package imu.spawners.engine;

import java.util.Locale;

/**
 * Readable names for entity types.
 */
public final class Names
{
    private Names()
    {
    }

    /**
     * "CAVE_SPIDER" becomes "Cave Spider".
     */
    public static String pretty(String enumName)
    {
        StringBuilder name = new StringBuilder();
        for (String word : enumName.toLowerCase(Locale.ROOT).split("_"))
        {
            if (word.isEmpty())
                continue;
            if (!name.isEmpty())
                name.append(' ');
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }
}
