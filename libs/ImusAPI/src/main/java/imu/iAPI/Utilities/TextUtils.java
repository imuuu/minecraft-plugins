package imu.iAPI.Utilities;

import java.util.List;
import org.bukkit.ChatColor;

/**
 * Text: colour codes, numbers and times as text, joining.
 */
public final class TextUtils
{
	private TextUtils() {}

	/**
	 * "&4%02dh &e: &3%02dm &e: &7%02ds", coloured: a time left or passed.
	 */
	public static String FormatTime(long millis)
	{
	    long secs = millis / 1000;
	    String str =String.format("&4%02dh &e: &3%02dm &e: &7%02ds", secs / 3600, (secs % 3600) / 60, secs % 60);
	    return Color(str);
	}

	/**
	 * Turns &amp; colour codes into the section sign codes chat and item names use. Null stays null.
	 */
	public static String Color(String s)
	{
		if(s == null) return s;
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	/**
	 * The items as text with the separator between them.
	 */
	public static <T> String Join(T[] array, String seperator)
	{
		String str = "";
		for(int i = 0; i < array.length; i++)
		{
			str += array[i];
			if(i < array.length-1)
				str += seperator;
		}
		return str;
	}

	public static <T> String Join(List<T> array, String seperator)
	{
		String str = "";
		for(int i = 0; i < array.size(); i++)
		{
			str += array.get(i);
			if(i < array.size()-1)
				str += seperator;
		}
		return str;
	}

	/**
	 * Rounded to two decimals, e.g. for prices.
	 */
	public static double Round(double value)
	{
		return Math.round(value * 100.00) / 100.00;
	}

	public static boolean IsNullOrEmpty(String value) 
	{
	    return value == null || value.isEmpty();
	}
}
