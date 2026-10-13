package imu.waystones.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * An item and how many of it an upgrade takes.
 */
public record Cost(String material, int amount)
{
    /**
     * "LAPIS_BLOCK:40 DIAMOND:2" is 40 lapis blocks and 2 diamonds; an amount left out is 1.
     *
     * @throws IllegalArgumentException when a part isn't MATERIAL or MATERIAL:amount
     */
    public static List<Cost> parse(String text)
    {
        List<Cost> costs = new ArrayList<>();
        if (text == null)
            return costs;
        for (String part : text.trim().split("[\\s,]+"))
        {
            if (part.isEmpty())
                continue;
            String[] pieces = part.split(":");
            if (pieces.length > 2 || pieces[0].isEmpty())
                throw new IllegalArgumentException("Not MATERIAL:amount: " + part);
            int amount;
            try
            {
                amount = pieces.length == 2 ? Integer.parseInt(pieces[1]) : 1;
            } catch (NumberFormatException e)
            {
                throw new IllegalArgumentException("Not a number in " + part);
            }
            if (amount < 1)
                throw new IllegalArgumentException("An amount is 1 or more: " + part);
            costs.add(new Cost(pieces[0].toUpperCase(Locale.ROOT), amount));
        }
        return costs;
    }
}
