package imu.iAPI.Utilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import imu.iAPI.Enums.ENCHANTMENT_TIER;
import imu.iAPI.Enums.ITEM_CATEGORY;

public class EnchantUtil
{
	private static final Map<ENCHANTMENT_TIER, Set<Enchantment>> enchantmentsByTier = new HashMap<>();
	private static final Map<ITEM_CATEGORY, Set<Enchantment>> enchantmentsByCategory = new HashMap<>();
	
    static 
    {
        for (ENCHANTMENT_TIER tier : ENCHANTMENT_TIER.values()) 
        {
            enchantmentsByTier.put(tier, new HashSet<>());
        }
        
        Set<Enchantment> tier1Enchantments = new HashSet<>();
        tier1Enchantments.add(Enchantment.WATER_WORKER); // Aqua Affinity
        tier1Enchantments.add(Enchantment.DAMAGE_ARTHROPODS); // Bane of Arthropods
        tier1Enchantments.add(Enchantment.PROTECTION_EXPLOSIONS); // Blast Protection
        tier1Enchantments.add(Enchantment.DEPTH_STRIDER); // Depth Strider
        tier1Enchantments.add(Enchantment.DIG_SPEED); // Efficiency
        tier1Enchantments.add(Enchantment.PROTECTION_FALL); // Feather Falling
        tier1Enchantments.add(Enchantment.FIRE_ASPECT); // Fire Aspect
        tier1Enchantments.add(Enchantment.PROTECTION_FIRE); // Fire Protection
        
        tier1Enchantments.add(Enchantment.IMPALING); // Impaling       
        tier1Enchantments.add(Enchantment.KNOCKBACK); // Knockback
        tier1Enchantments.add(Enchantment.ARROW_KNOCKBACK); // Punch
        tier1Enchantments.add(Enchantment.OXYGEN); // Respiration
        
        tier1Enchantments.add(Enchantment.DAMAGE_UNDEAD); // Smite
        tier1Enchantments.add(Enchantment.DURABILITY); // Unbreaking
        //tier1Enchantments.add(Enchantment.BINDING_CURSE); // Curse of Binding
        //tier1Enchantments.add(Enchantment.VANISHING_CURSE); // Curse of Vanishing
       
        // Tier 2: Intermediate or More Specialized Enchantments
        Set<Enchantment> tier2Enchantments = new HashSet<>();
        tier2Enchantments.add(Enchantment.CHANNELING); // Channeling
        tier2Enchantments.add(Enchantment.MULTISHOT); // Multishot
        tier2Enchantments.add(Enchantment.PIERCING); // Piercing
        tier2Enchantments.add(Enchantment.RIPTIDE); // Riptide
        tier2Enchantments.add(Enchantment.SWEEPING_EDGE); // Sweeping Edge
        tier2Enchantments.add(Enchantment.QUICK_CHARGE); // Quick Charge
        tier2Enchantments.add(Enchantment.ARROW_FIRE); // Flame
        tier2Enchantments.add(Enchantment.LUCK); // Luck of the Sea
        tier2Enchantments.add(Enchantment.LURE); // Lure
        tier2Enchantments.add(Enchantment.LOOT_BONUS_MOBS); // Looting
        tier2Enchantments.add(Enchantment.LOYALTY); // Loyalty
        tier2Enchantments.add(Enchantment.PROTECTION_ENVIRONMENTAL); // Protection
        tier2Enchantments.add(Enchantment.PROTECTION_PROJECTILE); // Projectile Protection
        tier2Enchantments.add(Enchantment.DAMAGE_ALL); // Sharpness
        
        //extra
        tier2Enchantments.add(Enchantment.DURABILITY);
        
        // Tier 3: Rare or Highly Valuable Enchantments
        Set<Enchantment> tier3Enchantments = new HashSet<>();       
        tier3Enchantments.add(Enchantment.MENDING); // Mending
        tier3Enchantments.add(Enchantment.THORNS); // Thorns
        tier3Enchantments.add(Enchantment.SILK_TOUCH); // Silk Touch
        tier3Enchantments.add(Enchantment.ARROW_DAMAGE); // Power
        tier3Enchantments.add(Enchantment.LOOT_BONUS_BLOCKS); // Fortune
        tier3Enchantments.add(Enchantment.ARROW_INFINITE); // Infinity
        tier3Enchantments.add(Enchantment.FROST_WALKER); // Frost Walker
      
        //extra

        enchantmentsByTier.put(ENCHANTMENT_TIER.TIER_1, tier1Enchantments);
        enchantmentsByTier.put(ENCHANTMENT_TIER.TIER_2, tier2Enchantments);
        enchantmentsByTier.put(ENCHANTMENT_TIER.TIER_3, tier3Enchantments);

    }
    
    static 
    {
        for (ITEM_CATEGORY category : ITEM_CATEGORY.values()) 
        {
            enchantmentsByCategory.put(category, new HashSet<>());
        }

        Set<Enchantment> toolEnchantments = new HashSet<>();
        toolEnchantments.add(Enchantment.DIG_SPEED);
        toolEnchantments.add(Enchantment.SILK_TOUCH);
        toolEnchantments.add(Enchantment.DURABILITY);
        toolEnchantments.add(Enchantment.LOOT_BONUS_BLOCKS);
        toolEnchantments.add(Enchantment.MENDING);
        toolEnchantments.add(Enchantment.SWEEPING_EDGE);
        toolEnchantments.add(Enchantment.LOOT_BONUS_MOBS);
        toolEnchantments.add(Enchantment.KNOCKBACK);
        
        Set<Enchantment> armorEnchantments = new HashSet<>();
        armorEnchantments.add(Enchantment.PROTECTION_ENVIRONMENTAL);
        armorEnchantments.add(Enchantment.PROTECTION_FIRE);
        armorEnchantments.add(Enchantment.PROTECTION_FALL);
        armorEnchantments.add(Enchantment.PROTECTION_EXPLOSIONS);
        armorEnchantments.add(Enchantment.PROTECTION_PROJECTILE);
        armorEnchantments.add(Enchantment.OXYGEN);
        armorEnchantments.add(Enchantment.WATER_WORKER);
        armorEnchantments.add(Enchantment.THORNS);
        armorEnchantments.add(Enchantment.DEPTH_STRIDER);
        armorEnchantments.add(Enchantment.FROST_WALKER);
        armorEnchantments.add(Enchantment.BINDING_CURSE);
        armorEnchantments.add(Enchantment.VANISHING_CURSE);
        armorEnchantments.add(Enchantment.MENDING);
        armorEnchantments.add(Enchantment.DURABILITY);
        
        Set<Enchantment> weaponEnchantments = new HashSet<>();
        weaponEnchantments.add(Enchantment.DAMAGE_ALL);
        weaponEnchantments.add(Enchantment.DAMAGE_UNDEAD);
        weaponEnchantments.add(Enchantment.DAMAGE_ARTHROPODS);
        weaponEnchantments.add(Enchantment.KNOCKBACK);
        weaponEnchantments.add(Enchantment.FIRE_ASPECT);
        weaponEnchantments.add(Enchantment.LOOT_BONUS_MOBS);
        weaponEnchantments.add(Enchantment.SWEEPING_EDGE);
        weaponEnchantments.add(Enchantment.ARROW_DAMAGE);
        weaponEnchantments.add(Enchantment.ARROW_KNOCKBACK);
        weaponEnchantments.add(Enchantment.ARROW_FIRE);
        weaponEnchantments.add(Enchantment.ARROW_INFINITE);
        weaponEnchantments.add(Enchantment.MENDING);
        weaponEnchantments.add(Enchantment.VANISHING_CURSE);
        weaponEnchantments.add(Enchantment.DURABILITY);
        
        enchantmentsByCategory.put(ITEM_CATEGORY.TOOL, toolEnchantments);
        enchantmentsByCategory.put(ITEM_CATEGORY.ARMOR, armorEnchantments);
        enchantmentsByCategory.put(ITEM_CATEGORY.WEAPON, weaponEnchantments);

    }

    public static boolean ContainsInTier(ENCHANTMENT_TIER tier, Enchantment enchantment) 
    {
        return enchantmentsByTier.getOrDefault(tier, new HashSet<>()).contains(enchantment);
    }

    public static void AddEnchantment(ENCHANTMENT_TIER tier, Enchantment enchantment) 
    {
        enchantmentsByTier.getOrDefault(tier, new HashSet<>()).add(enchantment);
    }

    public static void RemoveEnchantment(ENCHANTMENT_TIER tier, Enchantment enchantment) 
    {
        enchantmentsByTier.getOrDefault(tier, new HashSet<>()).remove(enchantment);
    }
    
    public static boolean ContainsInCategory(ITEM_CATEGORY category, Enchantment enchantment) 
    {
        return enchantmentsByCategory.getOrDefault(category, new HashSet<>()).contains(enchantment);
    }
    
    public static Enchantment GetRandomEnchantmentForTier(ENCHANTMENT_TIER tier) 
    {
        Set<Enchantment> enchantments = enchantmentsByTier.getOrDefault(tier, new HashSet<>());
        return ImusUtilities.GetRandomElement(enchantments);
    }
    public static Enchantment GetRandomEnchantmentForTier(ENCHANTMENT_TIER tier, Set<Enchantment> excludedEnchantments)
    {
        Set<Enchantment> enchantments = new HashSet<>(enchantmentsByTier.getOrDefault(tier, new HashSet<>()));
        enchantments.removeAll(excludedEnchantments);
        return enchantments.isEmpty() ? null : ImusUtilities.GetRandomElement(enchantments);
    }

    public static Enchantment GetRandomEnchantmentForCategory(ITEM_CATEGORY category) 
    {
        Set<Enchantment> enchantments = enchantmentsByCategory.getOrDefault(category, new HashSet<>());
        return ImusUtilities.GetRandomElement(enchantments);
    }

    public static Enchantment GetRandomEnchantmentForCategory(ITEM_CATEGORY category, Set<Enchantment> excludedEnchantments)
    {
        Set<Enchantment> enchantments = new HashSet<>(enchantmentsByCategory.getOrDefault(category, new HashSet<>()));
        enchantments.removeAll(excludedEnchantments);
        return enchantments.isEmpty() ? null : ImusUtilities.GetRandomElement(enchantments);
    }


    public static Enchantment GetRandomEnchantment(ITEM_CATEGORY category, ENCHANTMENT_TIER tier, Set<Enchantment> excludedEnchantments)
    {
        Set<Enchantment> enchantmentsForCategory = new HashSet<>(enchantmentsByCategory.getOrDefault(category, new HashSet<>()));
        Set<Enchantment> intersection;
        ENCHANTMENT_TIER currentTier = tier;
        do
        {
            Set<Enchantment> enchantmentsForTier = new HashSet<>(enchantmentsByTier.getOrDefault(currentTier, new HashSet<>()));
            intersection = new HashSet<>(enchantmentsForTier);
            intersection.retainAll(enchantmentsForCategory);

            // Check if excludedEnchantments is not null before removing them
            if (excludedEnchantments != null) {
                intersection.removeAll(excludedEnchantments);
            }

            if (!intersection.isEmpty())
            {
                return ImusUtilities.GetRandomElement(intersection);
            }

            // Move to a lower tier
            currentTier = ENCHANTMENT_TIER.GetLowerTier(currentTier);

        } while (currentTier != null);

        return null;
    }


    /*public static Enchantment GetRandomEnchantment(ITEM_CATEGORY category, ENCHANTMENT_TIER tier)
    {
        Set<Enchantment> enchantmentsForCategory = enchantmentsByCategory.getOrDefault(category, new HashSet<>());
        Set<Enchantment> enchantmentsForTier;
        Set<Enchantment> intersection;

        ENCHANTMENT_TIER currentTier = tier;
        do 
        {
            enchantmentsForTier = enchantmentsByTier.getOrDefault(currentTier, new HashSet<>());
            intersection = new HashSet<>(enchantmentsForTier);
            intersection.retainAll(enchantmentsForCategory);

            if (!intersection.isEmpty()) 
            {
                return ImusUtilities.GetRandomElement(intersection);
            }

            // Move to a lower tier
            currentTier = ENCHANTMENT_TIER.GetLowerTier(currentTier);

        } while (currentTier != null);

        return null; 
    }*/
    
    public static ItemStack GetEnchantedBook(Enchantment enchantment) 
    {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();

        if (meta != null) 
        {
            meta.addEnchant(enchantment, enchantment.getStartLevel(), true);
            book.setItemMeta(meta);
        }

        return book;
    }

}
