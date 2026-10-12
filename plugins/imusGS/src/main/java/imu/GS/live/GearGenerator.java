package imu.GS.live;

import imu.GS.engine.live.ServerProgress;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Enchanted gear for flash deals: enchants that fit the item and don't conflict, sometimes over their normal max.
 * With the server's progress, only enchants players have had, up to the level they have had.
 */
final class GearGenerator
{
    private GearGenerator() {}

    /**
     * @param overMaxChance chance per enchant to be one or two levels over its normal max
     * @param progress      what players have had, or null for any enchant at any level
     */
    static ItemStack generate(Material material, int minEnchants, int maxEnchants, double overMaxChance, ServerProgress progress, Random random)
    {
        ItemStack item = new ItemStack(material);
        List<Enchantment> candidates = new ArrayList<>();
        for (Enchantment enchant : RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT))
        {
            if (enchant.canEnchantItem(item) && !enchant.isCursed()
                    && (progress == null || progress.seenLevel(enchant.getKey().toString()) > 0))
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
            int seen = progress == null ? max : progress.seenLevel(enchant.getKey().toString());
            int cap = ServerProgress.levelCap(max, seen);
            int level = 1 + random.nextInt(cap);
            if (max > 1 && ServerProgress.mayGoOverMax(max, seen) && random.nextDouble() < overMaxChance)
                level = max + 1 + (random.nextDouble() < 0.3 ? 1 : 0);
            else if (random.nextDouble() < 0.5)
                level = cap;
            item.addUnsafeEnchantment(enchant, level);
            added++;
        }
        return item;
    }

    /**
     * An enchanted book of one random enchant; with the server's progress only an enchant players have had, up to
     * the level they have had. Null when there is none.
     */
    static ItemStack book(ServerProgress progress, double overMaxChance, Random random)
    {
        List<Enchantment> candidates = new ArrayList<>();
        for (Enchantment enchant : RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT))
        {
            if (!enchant.isCursed() && (progress == null || progress.seenLevel(enchant.getKey().toString()) > 0))
                candidates.add(enchant);
        }
        if (candidates.isEmpty())
            return null;
        Enchantment enchant = candidates.get(random.nextInt(candidates.size()));
        int max = enchant.getMaxLevel();
        int seen = progress == null ? max : progress.seenLevel(enchant.getKey().toString());
        int cap = ServerProgress.levelCap(max, seen);
        int level = 1 + random.nextInt(cap);
        if (max > 1 && ServerProgress.mayGoOverMax(max, seen) && random.nextDouble() < overMaxChance)
            level = max + 1;
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        meta.addStoredEnchant(enchant, level, true);
        book.setItemMeta(meta);
        return book;
    }
}
