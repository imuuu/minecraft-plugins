package imu.spawners.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpawnersTest
{
    private static final Luck LUCK = new Luck(14, 13, 100);

    @Test
    void aRollBelowTheChanceGetsTheSpawner()
    {
        Luck.Try hit = LUCK.roll(Luck.Stats.NEW, 13.9);
        assertTrue(hit.success());
        assertEquals(new Luck.Stats(0, 1, 1), hit.after());
        assertFalse(LUCK.roll(Luck.Stats.NEW, 14).success(), "the chance is 14%, rolls 0-13.99 hit");
    }

    @Test
    void missesRaiseTheChanceAndAHitSetsItBack()
    {
        Luck.Stats stats = Luck.Stats.NEW;
        stats = LUCK.roll(stats, 99).after();
        stats = LUCK.roll(stats, 99).after();
        assertEquals(40, LUCK.chance(stats));
        assertEquals(new Luck.Stats(26, 2, 0), stats);
        Luck.Try hit = LUCK.roll(stats, 39);
        assertTrue(hit.success());
        assertEquals(40, hit.chance());
        assertEquals(14, LUCK.chance(hit.after()));
    }

    @Test
    void theChanceStopsAtTheMax()
    {
        Luck capped = new Luck(14, 13, 50);
        Luck.Stats stats = Luck.Stats.NEW;
        for (int i = 0; i < 10; i++)
            stats = capped.roll(stats, 99.9).after();
        assertEquals(50, capped.chance(stats));
        assertEquals(36, stats.bonus(), "the bonus stops too, so a lower max later isn't hidden by a huge bonus");
    }

    @Test
    void oldSpawnerKeysAreRead()
    {
        assertEquals(new Spot("world_nether", -12, 40, 7), Spot.parse("world_nether;-12;40;7"));
        assertEquals("world;1;2;3", new Spot("world", 1, 2, 3).key());
        assertNull(Spot.parse("world;1;2"));
        assertNull(Spot.parse("world;a;2;3"));
        assertNull(Spot.parse(null));
    }

    @Test
    void entityNamesReadWell()
    {
        assertEquals("Cave Spider", Names.pretty("CAVE_SPIDER"));
        assertEquals("Zombie", Names.pretty("ZOMBIE"));
    }
}
