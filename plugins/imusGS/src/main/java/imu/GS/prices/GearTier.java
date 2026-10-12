package imu.GS.prices;

import imu.iAPI.Utilities.ItemUtils;
import imu.iAPI.Main.ImusAPI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Material tier of a tool or armor piece. Enchants on better gear are worth more.
 */
public enum GearTier
{
    WOOD(0.1),
    STONE(0.2),
    GOLD(0.5),
    IRON(0.4),
    DIAMOND(1.0),
    NETHERITE(1.4),
    /** Gear with no material tier: leather, chainmail, bows, tridents, fishing rods... */
    OTHER(0.5);

    private final double _defaultMultiplier;

    GearTier(double defaultMultiplier)
    {
        _defaultMultiplier = defaultMultiplier;
    }

    public double getDefaultMultiplier()
    {
        return _defaultMultiplier;
    }

    /**
     * The tier of a tool or armor piece, null for anything else.
     */
    public static GearTier of(ItemStack stack)
    {
        Material type = stack.getType();
        switch (type)
        {
            case SHIELD, SHEARS, FLINT_AND_STEEL:
                return IRON;
            case ELYTRA:
                return DIAMOND;
            default:
                break;
        }
        if (!ItemUtils.IsArmor(stack) && !ItemUtils.IsTool(stack))
            return null;

        String name = type.name();
        if (name.startsWith("NETHERITE")) return NETHERITE;
        if (name.startsWith("DIAMOND")) return DIAMOND;
        if (name.startsWith("GOLDEN")) return GOLD;
        if (name.startsWith("IRON")) return IRON;
        if (name.startsWith("STONE")) return STONE;
        if (name.startsWith("WOODEN")) return WOOD;
        return OTHER;
    }
}
