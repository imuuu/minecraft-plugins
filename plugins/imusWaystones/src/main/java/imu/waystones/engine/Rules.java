package imu.waystones.engine;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * How waystone teleports work, and what upgrades cost and do. Built from config.yml.
 *
 * @param castSeconds          seconds to stand still before a teleport, without upgrades
 * @param castSecondsPerTier   seconds every Cast Time tier takes off
 * @param minCastSeconds       a cast never gets shorter than this
 * @param foundationCooldowns  the cooldown in seconds at each Foundation tier, from tier 0
 * @param cooldownPercentPerTier % of the cooldown every Cooldown tier takes off
 * @param xpLevels             XP levels a teleport costs without upgrades; 0 = free
 * @param xpLevelsPerTier      levels every XP Usage tier takes off
 * @param ladders              what each next tier costs: ladders.get(kind).get(tier) takes tier to tier + 1
 */
public record Rules(double castSeconds, double castSecondsPerTier, double minCastSeconds, List<Double> foundationCooldowns,
                    double cooldownPercentPerTier, int xpLevels, int xpLevelsPerTier, Map<UpgradeKind, List<List<Cost>>> ladders)
{
    public Rules
    {
        foundationCooldowns = foundationCooldowns.isEmpty() ? List.of(0.0) : List.copyOf(foundationCooldowns);
        EnumMap<UpgradeKind, List<List<Cost>>> copy = new EnumMap<>(UpgradeKind.class);
        ladders.forEach((kind, steps) -> copy.put(kind, steps.stream().map(List::copyOf).toList()));
        ladders = copy;
    }

    /**
     * The highest tier of the upgrade. Foundation can't go past its cooldown list.
     */
    public int maxTier(UpgradeKind kind)
    {
        int steps = ladders.getOrDefault(kind, List.of()).size();
        return kind == UpgradeKind.FOUNDATION ? Math.min(steps, foundationCooldowns.size() - 1) : steps;
    }

    /**
     * What the next tier costs, or null at the top.
     */
    public List<Cost> nextCost(UpgradeKind kind, int tier)
    {
        if (tier >= maxTier(kind))
            return null;
        return ladders.get(kind).get(Math.max(0, tier));
    }

    public boolean usesXp()
    {
        return xpLevels > 0;
    }

    private int tier(Panel panel, UpgradeKind kind)
    {
        return Math.min(panel.tier(kind), maxTier(kind));
    }

    public double castSeconds(Panel panel)
    {
        return Math.max(minCastSeconds, castSeconds - castSecondsPerTier * tier(panel, UpgradeKind.CAST_TIME));
    }

    public double cooldownSeconds(Panel panel)
    {
        double base = foundationCooldowns.get(tier(panel, UpgradeKind.FOUNDATION));
        double cut = Math.min(100, cooldownPercentPerTier * tier(panel, UpgradeKind.COOLDOWN));
        return Math.max(0, base * (1 - cut / 100));
    }

    public int xpLevels(Panel panel)
    {
        if (!usesXp())
            return 0;
        return Math.max(0, xpLevels - xpLevelsPerTier * tier(panel, UpgradeKind.XP_USAGE));
    }

    public boolean reaches(Panel panel, Dimension from, Dimension to)
    {
        return panel.tier(UpgradeKind.DIMENSION) >= Dimension.tierNeeded(from, to);
    }
}
