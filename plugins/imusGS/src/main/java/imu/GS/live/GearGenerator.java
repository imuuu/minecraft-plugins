package imu.GS.live;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Enchanted gear for flash deals: enchants that fit the item and don't conflict, sometimes over their normal max.
 */
final class GearGenerator
{
    private GearGenerator() {}

    /**
     * @param overMaxChance chance per enchant to be one or two levels over its normal max
     */
    static ItemStack generate(Material material, int minEnchants, int maxEnchants, double overMaxChance, Random random)
    {
        ItemStack item = new ItemStack(material);
        List<Enchantment> candidates = new ArrayList<>();
        for (Enchantment enchant : RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT))
        {
            if (enchant.canEnchantItem(item) && !enchant.isCursed())
                candidates.add(enchant);
        }
        Collections.shuffle(candidates, random);

        int wanted = minEnchants + random.nextInt(Math.max(1, maxEnchants - minEnchants + 1));
        int added = 0;
        for (Enchantment enchant : candidates)
        {
            if (added >= wanted)
                break;
            if (item.getEnchantments().keySet().stream().anyMatch(enchant::conflictsWith))
                continue;
            int max = enchant.getMaxLevel();
            int level = 1 + random.nextInt(max);
            if (max > 1 && random.nextDouble() < overMaxChance)
                level = max + 1 + (random.nextDouble() < 0.3 ? 1 : 0);
            else if (random.nextDouble() < 0.5)
                level = max;
            item.addUnsafeEnchantment(enchant, level);
            added++;
        }
        return item;
    }
}
