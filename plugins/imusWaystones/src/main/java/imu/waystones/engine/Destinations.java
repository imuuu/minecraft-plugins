package imu.waystones.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Where a player can go from a waystone.
 */
public final class Destinations
{
    private Destinations()
    {
    }

    /**
     * The waystones the player can pick at the source: the ones for everyone first, then the ones they have
     * touched, each by name. Touched ones in another dimension need the Dimension upgrade; ones for everyone
     * don't.
     *
     * @param discovered the waystones the player has touched
     * @param panel      the player's upgrades on the source
     */
    public static List<Stone> from(Stone source, Collection<Stone> all, Set<UUID> discovered, Panel panel, Rules rules)
    {
        List<Stone> everyone = new ArrayList<>();
        List<Stone> touched = new ArrayList<>();
        for (Stone stone : all)
        {
            if (stone.id().equals(source.id()) || !stone.enabled() || !stone.visibility().isDestination())
                continue;
            if (stone.visibility().forEveryone())
                everyone.add(stone);
            else if (discovered.contains(stone.id()) && rules.reaches(panel, source.dimension(), stone.dimension()))
                touched.add(stone);
        }
        Comparator<Stone> byName = Comparator.comparing(stone -> stone.name().toLowerCase(Locale.ROOT));
        everyone.sort(byName);
        touched.sort(byName);
        everyone.addAll(touched);
        return everyone;
    }

    /**
     * The distance to the owner's nearest other waystone in the same world, or -1 when none is closer than
     * minDistance.
     */
    public static double tooCloseToOwn(UUID owner, String world, int x, int y, int z, Collection<Stone> all, double minDistance)
    {
        double nearest = -1;
        for (Stone stone : all)
        {
            if (!stone.owner().equals(owner))
                continue;
            double distance = stone.distance(world, x, y, z);
            if (distance < minDistance && (nearest < 0 || distance < nearest))
                nearest = distance;
        }
        return nearest;
    }
}
