package imu.iAPI.Config;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One editable value of a plugin's config.yml, shown as an item in a {@link ConfigMenu}.
 */
public class ConfigEntry
{
    public enum Type
    {
        INT, DOUBLE, BOOLEAN, STRING
    }

    private final String _path;
    private final String _name;
    private final Material _icon;
    private final Type _type;
    private double _min = -Double.MAX_VALUE;
    private double _max = Double.MAX_VALUE;
    private final List<String> _description = new ArrayList<>();
    private String _note;

    ConfigEntry(String path, String name, Material icon, Type type)
    {
        _path = path;
        _name = name;
        _icon = icon;
        _type = type;
    }

    ConfigEntry range(double min, double max)
    {
        _min = min;
        _max = max;
        return this;
    }

    /**
     * Adds a line of explanation under the name. Supports &amp; colour codes.
     */
    public ConfigEntry description(String line)
    {
        _description.add(line);
        return this;
    }

    /**
     * When a change takes effect, e.g. "now" or "on the next rotation". Shown in the item lore.
     */
    public ConfigEntry note(String note)
    {
        _note = note;
        return this;
    }

    public Object get(FileConfiguration config)
    {
        return switch (_type)
        {
            case INT -> config.getInt(_path);
            case DOUBLE -> config.getDouble(_path);
            case BOOLEAN -> config.getBoolean(_path);
            case STRING -> config.getString(_path, "");
        };
    }

    /**
     * @return the value in the plugin's bundled config.yml, or null when it has none
     */
    public Object getDefault(FileConfiguration config)
    {
        Configuration defaults = config.getDefaults();
        if (defaults == null || !defaults.contains(_path))
            return null;

        return switch (_type)
        {
            case INT -> defaults.getInt(_path);
            case DOUBLE -> defaults.getDouble(_path);
            case BOOLEAN -> defaults.getBoolean(_path);
            case STRING -> defaults.getString(_path);
        };
    }

    /**
     * Turns typed input into a value for this entry.
     *
     * @throws IllegalArgumentException with a message for the player when the input isn't valid
     */
    public Object parse(String input)
    {
        String text = input == null ? "" : input.trim();
        switch (_type)
        {
            case INT ->
            {
                int value;
                try
                {
                    value = Integer.parseInt(text);
                } catch (NumberFormatException e)
                {
                    throw new IllegalArgumentException("'" + text + "' is not a whole number");
                }
                checkRange(value);
                return value;
            }
            case DOUBLE ->
            {
                double value;
                try
                {
                    value = Double.parseDouble(text.replace(',', '.'));
                } catch (NumberFormatException e)
                {
                    throw new IllegalArgumentException("'" + text + "' is not a number");
                }
                if (Double.isNaN(value) || Double.isInfinite(value))
                    throw new IllegalArgumentException("'" + text + "' is not a number");
                checkRange(value);
                return value;
            }
            case BOOLEAN ->
            {
                return switch (text.toLowerCase(Locale.ROOT))
                {
                    case "true", "yes", "on" -> true;
                    case "false", "no", "off" -> false;
                    default -> throw new IllegalArgumentException("'" + text + "' is not true or false");
                };
            }
            default ->
            {
                return text;
            }
        }
    }

    private void checkRange(double value)
    {
        if (value < _min || value > _max)
            throw new IllegalArgumentException(format(value) + " is outside " + rangeText());
    }

    public boolean hasRange()
    {
        return _min != -Double.MAX_VALUE || _max != Double.MAX_VALUE;
    }

    public String rangeText()
    {
        return format(_min) + " - " + format(_max);
    }

    public String format(Object value)
    {
        if (value instanceof Double d)
        {
            if (_type == Type.INT || d == Math.rint(d) && Math.abs(d) < 1e15)
                return String.valueOf(d.longValue());
            return String.valueOf(d);
        }
        return String.valueOf(value);
    }

    public String getPath()
    {
        return _path;
    }

    public String getName()
    {
        return _name;
    }

    public Material getIcon()
    {
        return _icon;
    }

    public Type getType()
    {
        return _type;
    }

    public List<String> getDescription()
    {
        return _description;
    }

    public String getNote()
    {
        return _note;
    }
}
