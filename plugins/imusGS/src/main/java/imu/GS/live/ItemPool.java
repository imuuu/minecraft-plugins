package imu.GS.live;

import imu.GS.engine.live.LiveRules;
import imu.GS.prices.MaterialPrices;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Random materials for rotating stock and flash deals: those of the given material tags that have a price,
 * rarer (pricier) ones less often.
 */
final class ItemPool
{
    private ItemPool() {}

    /**
     * Up to {@code count} different materials, picked at random by rarity.
     */
    static List<Material> pick(MaterialPrices materials, Collection<String> tags, double valueScale, int count, Random random)
    {
        Set<Material> pool = new LinkedHashSet<>();
        for (String tag : tags)
        {
            for (Material material : materials.withTag(tag))
            {
                if (material.isItem() && !material.isAir() && materials.price(material) > 0)
                    pool.add(material);
            }
        }

        List<Material> candidates = new ArrayList<>(pool);
        List<Material> picked = new ArrayList<>();
        while (picked.size() < count && !candidates.isEmpty())
        {
            double total = 0;
            for (Material material : candidates)
                total += LiveRules.rarity(materials.price(material), valueScale);
            double roll = random.nextDouble() * total;
            int index = 0;
            for (; index < candidates.size() - 1; index++)
            {
                roll -= LiveRules.rarity(materials.price(candidates.get(index)), valueScale);
                if (roll <= 0)
                    break;
            }
            picked.add(candidates.remove(index));
        }
        return picked;
    }
}
