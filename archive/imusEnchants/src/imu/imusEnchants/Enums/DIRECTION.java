package imu.imusEnchants.Enums;

import java.util.Arrays;
import java.util.Comparator;

public enum DIRECTION
{
	UP("↑", 1),
	DOWN("↓", 2),
	LEFT("←", 3), 
	RIGHT("→", 4);
	

	private final String _unicodeSymbol;
	private final int _sortOrder;

	DIRECTION(String unicodeSymbol, int sortOrder)
	{
		_unicodeSymbol = unicodeSymbol;
		_sortOrder = sortOrder;
	}

	public String GetUnicodeSymbol()
	{
		return _unicodeSymbol;
	}

	public int GetSortOrder()
	{
		return _sortOrder;
	}

	public static String GetStrDirection(DIRECTION[] diretions)
	{
	    DIRECTION[] sortedDirections = Arrays.copyOf(diretions, diretions.length);
	    Arrays.sort(sortedDirections, Comparator.comparingInt(DIRECTION::GetSortOrder));

	    StringBuilder strBuilder = new StringBuilder();
	    for (DIRECTION dir : sortedDirections) {
	        strBuilder.append(dir.GetUnicodeSymbol());
	        strBuilder.append(" "); 
	    }

	    return strBuilder.toString().trim(); 
	}
	
	 public static DIRECTION[] GetSortedDirections(DIRECTION[] directions) 
	 {
        DIRECTION[] sortedDirections = Arrays.copyOf(directions, directions.length);
        Arrays.sort(sortedDirections, Comparator.comparingInt(DIRECTION::GetSortOrder));
        return sortedDirections;
    }

}

//	Up-Left: ↖ (Unicode: U+2196)
//	Up-Right: ↗ (Unicode: U+2197)
//	Down-Left: ↙ (Unicode: U+2199)
//	Down-Right: ↘ (Unicode: U+2198)
//	North-East: ⇗ (Unicode: U+21D7)
//	North-West: ⇖ (Unicode: U+21D6)
//	South-East: ⇘ (Unicode: U+21D8)
//	South-West: ⇙ (Unicode: U+21D9)
