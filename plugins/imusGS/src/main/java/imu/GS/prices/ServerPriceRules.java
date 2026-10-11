package imu.GS.prices;

import imu.GS.bukkit.ItemKeys;
import imu.GS.engine.ItemKey;
import imu.GS.engine.pricing.Overflow;
import imu.GS.engine.pricing.PriceRules;
import imu.GS.engine.pricing.SellLimit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.function.DoubleSupplier;

/**
 * The engine's prices from the server's material prices, enchant values and unique items.
 */
public class ServerPriceRules implements PriceRules
{
    private final MaterialPrices _materials;
    private final EnchantPrices _enchants;
    private final UniqueItems _uniques;
    private final DoubleSupplier _durabilityPenalty;
    private final ItemKey _bucket;

    /**
     * @param durabilityPenalty how much less than its durability a damaged item is worth, from config.yml
     */
    public ServerPriceRules(MaterialPrices materials, EnchantPrices enchants, UniqueItems uniques, DoubleSupplier durabilityPenalty)
    {
        _materials = materials;
        _enchants = enchants;
        _uniques = uniques;
        _durabilityPenalty = durabilityPenalty;
        _bucket = ItemKeys.of(new ItemStack(Material.BUCKET));
    }

    public MaterialPrices getMaterials()
    {
        return _materials;
    }

    public EnchantPrices getEnchants()
    {
        return _enchants;
    }

    public UniqueItems getUniques()
    {
        return _uniques;
    }

    /**
     * Value of one unit of the stack, see {@link #value(ItemKey)}.
     */
    public double value(ItemStack stack)
    {
        double value = _materials.price(stack.getType()) + _enchants.price(stack);
        return Math.max(0, value) * condition(stack);
    }

    @Override
    public double value(ItemKey item)
    {
        return value(ItemKeys.template(item));
    }

    @Override
    public Overflow overflow(ItemKey item)
    {
        return _materials.get(ItemKeys.template(item).getType()).getOverflow();
    }

    @Override
    public String overflowGroup(ItemKey item)
    {
        // Tools and armor differ by damage and enchants; their overflow counts every piece of the material
        Material type = ItemKeys.template(item).getType();
        return type.getMaxDurability() > 0 ? type.name() : null;
    }

    @Override
    public Double uniquePrice(ItemKey item)
    {
        UniqueItems.Unique unique = _uniques.get(ItemKeys.template(item));
        return unique == null ? null : unique.price();
    }

    @Override
    public double condition(ItemKey item)
    {
        return condition(ItemKeys.template(item));
    }

    /**
     * 1.0 for an undamaged item; a damaged one is worth its durability left minus the penalty.
     */
    public double condition(ItemStack stack)
    {
        int maxDurability = stack.getType().getMaxDurability();
        if (maxDurability <= 0 || !(stack.getItemMeta() instanceof Damageable meta) || !meta.hasDamage())
            return 1.0;
        double left = 1.0 - (double) meta.getDamage() / maxDurability;
        if (left >= 1.0)
            return 1.0;
        return Math.max(0, Math.min(1, left - _durabilityPenalty.getAsDouble()));
    }

    @Override
    public String sellLimitGroup(ItemKey item)
    {
        return _materials.get(ItemKeys.template(item).getType()).getLimitGroup();
    }

    @Override
    public SellLimit sellLimit(String group)
    {
        return _materials.getLimits().get(group);
    }

    @Override
    public ItemKey leftover(ItemKey item)
    {
        return switch (ItemKeys.template(item).getType())
        {
            case WATER_BUCKET, LAVA_BUCKET, MILK_BUCKET -> _bucket;
            default -> null;
        };
    }
}
