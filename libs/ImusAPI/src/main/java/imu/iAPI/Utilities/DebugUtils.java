package imu.iAPI.Utilities;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Map;

/**
 * Printing to the console while developing.
 */
public final class DebugUtils
{
	private DebugUtils() {}

	public static void PrintMap(HashMap<?, ?> map) {
		StringBuilder sb = new StringBuilder("========MAP========\n");
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object value = entry.getValue();
			// Check if the value is null
			if (value == null) {
				sb.append("Key: ").append(entry.getKey()).append(" value: null\n");
			} else if (value.getClass().isArray()) {
				sb.append("==KEY: ").append(entry.getKey()).append("\n");
				Object[] array = (Object[]) value;
				for (Object v : array) {
					sb.append("values: ").append(v).append("\n");
				}
			} else {
				sb.append("Key: ").append(entry.getKey()).append(" value: ").append(value).append("\n");
			}
		}
		sb.append("========MAP========");
		System.out.println(sb.toString());
	}
}
