package imu.iAPI.Utilities;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextUtilsTest
{
    @Test
    void colourCodesBecomeSectionSigns()
    {
        assertEquals("§aGreen §lbold", TextUtils.Color("&aGreen &lbold"));
        assertEquals("no codes", TextUtils.Color("no codes"));
        assertNull(TextUtils.Color(null));
    }

    @Test
    void pricesAreRoundedToCents()
    {
        assertEquals(12.35, TextUtils.Round(12.345678));
        assertEquals(3.0, TextUtils.Round(3));
    }

    @Test
    void joiningPutsTheSeparatorBetween()
    {
        assertEquals("a, b, c", TextUtils.Join(new String[] { "a", "b", "c" }, ", "));
        assertEquals("1-2", TextUtils.Join(List.of(1, 2), "-"));
        assertEquals("", TextUtils.Join(List.of(), "-"));
    }

    @Test
    void emptyTextIsNullOrEmpty()
    {
        assertTrue(TextUtils.IsNullOrEmpty(null));
        assertTrue(TextUtils.IsNullOrEmpty(""));
        assertFalse(TextUtils.IsNullOrEmpty(" "));
    }

    @Test
    void timeIsHoursMinutesAndSeconds()
    {
        String time = TextUtils.FormatTime(3_723_000);
        assertTrue(time.contains("01h") && time.contains("02m") && time.contains("03s"), time);
    }
}
