package imu.iAPI.Config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigEntryTest
{
    private static ConfigEntry entry(ConfigEntry.Type type)
    {
        return new ConfigEntry("path", "Name", null, type);
    }

    @Test
    void wholeNumbersAreCheckedAgainstTheirRange()
    {
        ConfigEntry seconds = entry(ConfigEntry.Type.INT).range(1, 600);
        assertEquals(20, seconds.parse(" 20 "));
        assertThrows(IllegalArgumentException.class, () -> seconds.parse("0"));
        assertThrows(IllegalArgumentException.class, () -> seconds.parse("601"));
        assertThrows(IllegalArgumentException.class, () -> seconds.parse("ten"));
        assertThrows(IllegalArgumentException.class, () -> seconds.parse("1.5"));
    }

    @Test
    void decimalsTakeACommaToo()
    {
        ConfigEntry share = entry(ConfigEntry.Type.DOUBLE).range(0, 1);
        assertEquals(0.25, share.parse("0,25"));
        assertEquals(0.25, share.parse("0.25"));
        assertThrows(IllegalArgumentException.class, () -> share.parse("NaN"));
        assertThrows(IllegalArgumentException.class, () -> share.parse("1.5"));
    }

    @Test
    void booleansKnowOnAndOff()
    {
        ConfigEntry toggle = entry(ConfigEntry.Type.BOOLEAN);
        assertEquals(true, toggle.parse("on"));
        assertEquals(true, toggle.parse("TRUE"));
        assertEquals(false, toggle.parse("no"));
        assertThrows(IllegalArgumentException.class, () -> toggle.parse("maybe"));
    }

    @Test
    void aChoiceIsOneOfItsOptions()
    {
        ConfigEntry mode = entry(ConfigEntry.Type.CHOICE).options(List.of("one-at-a-time", "every-shop"));
        assertEquals("every-shop", mode.parse("EVERY-SHOP"));
        assertThrows(IllegalArgumentException.class, () -> mode.parse("sometimes"));
    }

    @Test
    void textIsTrimmed()
    {
        assertEquals("fi", entry(ConfigEntry.Type.STRING).parse("  fi "));
    }
}
