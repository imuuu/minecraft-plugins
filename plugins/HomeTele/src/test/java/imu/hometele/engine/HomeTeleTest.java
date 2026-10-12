package imu.hometele.engine;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HomeTeleTest
{
    private static final UUID ALEX = UUID.randomUUID();
    private static final Spot HERE = new Spot("world", 10, 64, 10, 0, 0);
    private static final Spot THERE = new Spot("world", 200, 70, -50, 90, 0);

    @Test
    void withOneHomeNoNameIsNeeded()
    {
        HomeBook book = new HomeBook();
        assertEquals(HomeBook.SetResult.SET, book.set(ALEX, null, HERE, 1));
        assertEquals(HERE, book.get(ALEX, null));
        assertEquals(HERE, book.get(ALEX, "HOME"));
    }

    @Test
    void theLimitStopsNewHomesButNotMovingOne()
    {
        HomeBook book = new HomeBook();
        book.set(ALEX, "base", HERE, 2);
        book.set(ALEX, "farm", HERE, 2);
        assertEquals(HomeBook.SetResult.LIMIT_REACHED, book.set(ALEX, "mine", HERE, 2));
        assertEquals(HomeBook.SetResult.REPLACED, book.set(ALEX, "Base", THERE, 2));
        assertEquals(THERE, book.get(ALEX, "base"));
        assertEquals(List.of("base", "farm"), book.names(ALEX));
    }

    @Test
    void withSeveralHomesNoNameMeansTheOneCalledHome()
    {
        HomeBook book = new HomeBook();
        book.set(ALEX, "base", HERE, 3);
        book.set(ALEX, "farm", THERE, 3);
        assertNull(book.get(ALEX, null));
        book.set(ALEX, "home", THERE, 3);
        assertEquals(THERE, book.get(ALEX, null));
    }

    @Test
    void deletingTheLastHomeForgetsThePlayer()
    {
        HomeBook book = new HomeBook();
        book.set(ALEX, null, HERE, 1);
        assertFalse(book.delete(ALEX, "other"));
        assertTrue(book.delete(ALEX, "home"));
        assertTrue(book.owners().isEmpty());
    }

    @Test
    void cooldownsRunOut()
    {
        Cooldowns cooldowns = new Cooldowns();
        assertEquals(0, cooldowns.remaining(ALEX, "home", 300, 1_000), "never used");
        cooldowns.used(ALEX, "home", 1_000);
        assertEquals(300_000, cooldowns.remaining(ALEX, "home", 300, 1_000));
        assertEquals(0, cooldowns.remaining(ALEX, "sethome", 300, 1_000), "each action has its own");
        assertEquals(0, cooldowns.remaining(ALEX, "home", 300, 301_000));
    }

    @Test
    void aShorterCooldownSettingShortensTheWaitsAlreadyOn()
    {
        Cooldowns cooldowns = new Cooldowns();
        cooldowns.used(ALEX, "home", 0);
        assertEquals(290_000, cooldowns.remaining(ALEX, "home", 300, 10_000));
        assertEquals(0, cooldowns.remaining(ALEX, "home", 5, 10_000));
    }

    @Test
    void aWarmupIsDoneAfterItsTimeIfThePlayerStaysPut()
    {
        Warmup warmup = new Warmup(HERE, THERE, 0, 10_000, 1.0);
        assertEquals(Warmup.State.WAITING, warmup.check(new Spot("world", 10.5, 64, 10, 45, 10), 5_000));
        assertEquals(0.5, warmup.progress(5_000));
        assertEquals(Warmup.State.DONE, warmup.check(HERE, 10_000));
    }

    @Test
    void movingOrChangingWorldCancelsAWarmup()
    {
        Warmup warmup = new Warmup(HERE, THERE, 0, 10_000, 1.0);
        assertEquals(Warmup.State.MOVED, warmup.check(new Spot("world", 12, 64, 10, 0, 0), 1_000));
        assertEquals(Warmup.State.MOVED, warmup.check(new Spot("world_nether", 10, 64, 10, 0, 0), 1_000));
    }
}
