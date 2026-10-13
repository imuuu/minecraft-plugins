package imu.imustnt.engine;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Which blocks of a chunk layer a special TNT changes.
 *
 * @param blastLimit      blocks this blast resistant or more stay (obsidian and ancient debris are 1200)
 * @param protectedBlocks material names that stay
 * @param keepUsable      blocks that can be used (chests, furnaces, doors...) stay
 */
public record LayerRule(double blastLimit, Set<String> protectedBlocks, boolean keepUsable)
{
    /**
     * A block in the layer.
     *
     * @param usable it does something when clicked: an inventory, a door, a button...
     */
    public record Cell(String material, double blastResistance, boolean air, boolean usable)
    {
    }

    public LayerRule
    {
        protectedBlocks = protectedBlocks.stream().map(name -> name.toUpperCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * True when the TNT turns the block into its replacement.
     */
    public boolean changes(TntType type, Cell cell)
    {
        if (cell.material().equalsIgnoreCase(type.replacement()))
            return false;
        // Clearing leaves air alone, but stone TNT fills the holes too
        if (cell.air() && type.replacement().equals("AIR"))
            return false;
        if (cell.blastResistance() >= blastLimit)
            return false;
        if (protectedBlocks.contains(cell.material().toUpperCase(Locale.ROOT)))
            return false;
        return !(keepUsable && cell.usable());
    }
}
