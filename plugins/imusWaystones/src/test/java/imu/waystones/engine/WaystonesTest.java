package imu.waystones.engine;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WaystonesTest
{
    private static final UUID ALEX = UUID.randomUUID();
    private static final UUID SAM = UUID.randomUUID();

    /** The old plugin's numbers */
    private static final Rules RULES = new Rules(30, 3, 1, List.of(5400.0, 2400.0, 1500.0, 60.0), 5, 0, 1, Map.of(
            UpgradeKind.CAST_TIME, steps(9, "LAPIS_BLOCK:10"),
            UpgradeKind.COOLDOWN, steps(10, "DIAMOND:1"),
            UpgradeKind.DIMENSION, List.of(Cost.parse("ENDER_PEARL:8 BLAZE_ROD:16"), Cost.parse("ENDER_EYE:32")),
            UpgradeKind.FOUNDATION, List.of(Cost.parse("GOLD_BLOCK"), Cost.parse("DIAMOND_BLOCK"), Cost.parse("NETHERITE_BLOCK"))));

    private static List<List<Cost>> steps(int count, String cost)
    {
        return java.util.Collections.nCopies(count, Cost.parse(cost));
    }

    private static Stone stone(String name, UUID owner, Dimension dimension, Visibility visibility)
    {
        return new Stone(UUID.randomUUID(), owner, name, dimension.name(), dimension, 0, 64, 0, visibility, true);
    }

    @Test
    void costsAreReadFromText()
    {
        assertEquals(List.of(new Cost("LAPIS_BLOCK", 40), new Cost("DIAMOND", 2)), Cost.parse("lapis_block:40  DIAMOND:2"));
        assertEquals(List.of(new Cost("NAME_TAG", 1)), Cost.parse("NAME_TAG"));
        assertTrue(Cost.parse("").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> Cost.parse("DIAMOND:x"));
        assertThrows(IllegalArgumentException.class, () -> Cost.parse("DIAMOND:0"));
    }

    @Test
    void castTimeAndCooldownComeFromTheTiers()
    {
        Panel panel = new Panel();
        assertEquals(30, RULES.castSeconds(panel));
        assertEquals(5400, RULES.cooldownSeconds(panel));
        panel.set(UpgradeKind.CAST_TIME, 9);
        panel.set(UpgradeKind.FOUNDATION, 3);
        panel.set(UpgradeKind.COOLDOWN, 10);
        assertEquals(3, RULES.castSeconds(panel));
        assertEquals(30, RULES.cooldownSeconds(panel), "the best foundation, halved");
        panel.set(UpgradeKind.FOUNDATION, 99);
        assertEquals(30, RULES.cooldownSeconds(panel), "a tier above the top counts as the top");
    }

    @Test
    void theLaddersTellTheNextCostAndTheTop()
    {
        assertEquals(9, RULES.maxTier(UpgradeKind.CAST_TIME));
        assertEquals(3, RULES.maxTier(UpgradeKind.FOUNDATION));
        assertEquals(Cost.parse("ENDER_EYE:32"), RULES.nextCost(UpgradeKind.DIMENSION, 1));
        assertNull(RULES.nextCost(UpgradeKind.DIMENSION, 2));
        assertEquals(0, RULES.maxTier(UpgradeKind.XP_USAGE), "no ladder, no upgrade");
    }

    @Test
    void xpIsFreeUnlessALevelCostIsSet()
    {
        Panel panel = new Panel();
        assertEquals(0, RULES.xpLevels(panel));
        Rules xp = new Rules(30, 3, 1, List.of(60.0), 5, 10, 1, Map.of(UpgradeKind.XP_USAGE, steps(10, "IRON_BLOCK")));
        assertEquals(10, xp.xpLevels(panel));
        panel.set(UpgradeKind.XP_USAGE, 4);
        assertEquals(6, xp.xpLevels(panel));
    }

    @Test
    void theNetherNeedsTierOneAndTheEndTierTwo()
    {
        assertEquals(0, Dimension.tierNeeded(Dimension.NETHER, Dimension.NETHER));
        assertEquals(1, Dimension.tierNeeded(Dimension.NORMAL, Dimension.NETHER));
        assertEquals(2, Dimension.tierNeeded(Dimension.NETHER, Dimension.END));
        assertEquals(2, Dimension.tierNeeded(Dimension.END, Dimension.NORMAL));
    }

    @Test
    void destinationsAreTheOnesForEveryoneThenTheTouchedOnes()
    {
        Stone home = stone("Home", ALEX, Dimension.NORMAL, Visibility.BY_TOUCH);
        Stone farm = stone("farm", SAM, Dimension.NORMAL, Visibility.BY_TOUCH);
        Stone hidden = stone("Hidden", SAM, Dimension.NORMAL, Visibility.BY_TOUCH);
        Stone spawn = stone("Spawn", SAM, Dimension.NORMAL, Visibility.TO_ALL);
        Stone arrivals = stone("Arrivals", SAM, Dimension.END, Visibility.TO_ALL_ONE_WAY);
        Stone exit = stone("Exit", SAM, Dimension.NORMAL, Visibility.ONE_WAY);
        Stone nether = stone("Nether base", SAM, Dimension.NETHER, Visibility.BY_TOUCH);
        Stone off = new Stone(UUID.randomUUID(), SAM, "Off", "NORMAL", Dimension.NORMAL, 0, 0, 0, Visibility.TO_ALL, false);
        List<Stone> all = List.of(home, farm, hidden, spawn, arrivals, exit, nether, off);
        Set<UUID> touched = Set.of(home.id(), farm.id(), exit.id(), nether.id());

        List<Stone> list = Destinations.from(home, all, touched, new Panel(), RULES);
        assertEquals(List.of(arrivals, spawn, farm), list, "for everyone even in the End; not itself, one-way, off or untouched");

        Panel netherTier = new Panel();
        netherTier.set(UpgradeKind.DIMENSION, 1);
        assertTrue(Destinations.from(home, all, touched, netherTier, RULES).contains(nether));
    }

    @Test
    void ownWaystonesNeedRoomBetweenThem()
    {
        Stone own = new Stone(UUID.randomUUID(), ALEX, "a", "world", Dimension.NORMAL, 100, 64, 100, Visibility.BY_TOUCH, true);
        Stone theirs = new Stone(UUID.randomUUID(), SAM, "b", "world", Dimension.NORMAL, 10, 64, 10, Visibility.BY_TOUCH, true);
        List<Stone> all = List.of(own, theirs);
        assertEquals(-1, Destinations.tooCloseToOwn(ALEX, "world", 0, 64, 0, List.of(theirs), 300), "someone else's doesn't count");
        assertEquals(100, Destinations.tooCloseToOwn(ALEX, "world", 100, 64, 0, all, 300), 0.001);
        assertEquals(-1, Destinations.tooCloseToOwn(ALEX, "world_nether", 100, 64, 100, all, 300), "another world doesn't count");
        assertEquals(-1, Destinations.tooCloseToOwn(ALEX, "world", 500, 64, 100, all, 300));
    }

    @Test
    void upgradeItemsOnlyRaiseTiers()
    {
        Panel panel = new Panel();
        panel.set(UpgradeKind.CAST_TIME, 5);
        Panel item = new Panel();
        item.set(UpgradeKind.CAST_TIME, 3);
        assertFalse(panel.raiseTo(item));
        item.set(UpgradeKind.COOLDOWN, 2);
        assertTrue(panel.raiseTo(item));
        assertEquals(Map.of(UpgradeKind.CAST_TIME, 5, UpgradeKind.COOLDOWN, 2), panel.tiers());
    }

    @Test
    void aWarmupWaitsUnlessThePlayerMoves()
    {
        Warmup warmup = new Warmup(UUID.randomUUID(), UUID.randomUUID(), "world", 0, 64, 0, 0, 3000, 0.5);
        assertEquals(Warmup.State.WAITING, warmup.check("world", 0.3, 64, 0, 1000));
        assertEquals(2, warmup.secondsLeft(1000));
        assertEquals(Warmup.State.DONE, warmup.check("world", 0, 64, 0, 3000));
        assertEquals(Warmup.State.MOVED, warmup.check("world", 1, 64, 0, 1000));
        assertEquals(Warmup.State.MOVED, warmup.check("world_nether", 0, 64, 0, 1000));
    }

    @Test
    void cooldownsAreForEachWaystoneAndFollowTheCurrentSetting()
    {
        Cooldowns cooldowns = new Cooldowns();
        UUID stone = UUID.randomUUID();
        cooldowns.used(ALEX, stone, 0);
        assertEquals(50_000, cooldowns.remaining(ALEX, stone, 60, 10_000));
        assertEquals(0, cooldowns.remaining(ALEX, UUID.randomUUID(), 60, 10_000));
        assertEquals(0, cooldowns.remaining(ALEX, stone, 5, 10_000), "an upgrade shortens the wait");
        cooldowns.forget(stone);
        assertEquals(0, cooldowns.remaining(ALEX, stone, 60, 10_000));
    }

    @Test
    void visibilityGoesRound()
    {
        assertEquals(Visibility.TO_ALL, Visibility.BY_TOUCH.next());
        assertEquals(Visibility.BY_TOUCH, Visibility.TO_ALL_ONE_WAY.next());
        assertEquals(Visibility.BY_TOUCH, Visibility.parse("nonsense"));
        assertFalse(Visibility.TO_ALL_ONE_WAY.canDepart());
        assertEquals(UpgradeKind.CAST_TIME, UpgradeKind.parse("cast-time"));
    }
}
