package imu.GS.engine.shop;

import imu.GS.engine.Fakes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestrictionsTest
{
    private final Fakes.Context _context = new Fakes.Context();

    @Test
    void sellTimeWithinADay()
    {
        Restrictions.SellTime day = new Restrictions.SellTime(0, 13000);
        assertTrue(day.contains(6000));
        assertFalse(day.contains(18000));
    }

    @Test
    void sellTimeOverMidnight()
    {
        Restrictions.SellTime night = new Restrictions.SellTime(13000, 1000);
        assertTrue(night.contains(18000));
        assertTrue(night.contains(500));
        assertFalse(night.contains(6000));
    }

    @Test
    void noRestrictionsAllowEveryone()
    {
        assertTrue(Restrictions.NONE.allows(_context));
    }

    @Test
    void everySetRestrictionMustPass()
    {
        Restrictions restrictions = new Restrictions(new Restrictions.SellTime(0, 13000),
                List.of(new Restrictions.Area("world", 0, 0, 0, 10)), List.of("vip"));

        _context.permissions.add("vip");
        assertTrue(restrictions.allows(_context));

        _context.time = 20000;
        assertFalse(restrictions.allows(_context), "right place and permission, wrong time");

        _context.time = 6000;
        _context.x = 50;
        assertFalse(restrictions.allows(_context), "right time and permission, too far");

        _context.x = 0;
        _context.permissions.clear();
        assertFalse(restrictions.allows(_context), "right time and place, no permission");
    }

    @Test
    void areaIsInItsOwnWorldOnly()
    {
        Restrictions.Area area = new Restrictions.Area("world_nether", 0, 0, 0, 10);
        assertFalse(area.contains(_context));
    }
}
