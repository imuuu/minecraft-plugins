package imu.GS.engine.live;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * How far the server's players have got: every item they have had, and the highest level of every enchant.
 * Flash deals offer only what someone has already had, so the shop follows the server's progress.
 * Not thread safe; on the server it is only used on the main thread.
 */
public class ServerProgress
{
    private final Set<String> _items = new HashSet<>();
    private final Map<String, Integer> _enchants = new HashMap<>();
    private boolean _dirty;

    private static String key(String name)
    {
        return name.toLowerCase(Locale.ROOT);
    }

    /**
     * A player had the item, e.g. "netherite_chestplate". Returns true when it is new.
     */
    public boolean seeItem(String item)
    {
        boolean added = _items.add(key(item));
        _dirty |= added;
        return added;
    }

    /**
     * A player had the enchant at this level, e.g. "minecraft:sharpness" 4. Returns true when it is new or higher.
     */
    public boolean seeEnchant(String enchant, int level)
    {
        if (level <= 0)
            return false;
        Integer old = _enchants.get(key(enchant));
        if (old != null && old >= level)
            return false;
        _enchants.put(key(enchant), level);
        _dirty = true;
        return true;
    }

    public boolean hasSeen(String item)
    {
        return _items.contains(key(item));
    }

    /**
     * The highest level of the enchant players have had, 0 when nobody has had it.
     */
    public int seenLevel(String enchant)
    {
        return _enchants.getOrDefault(key(enchant), 0);
    }

    /**
     * The highest level a generated item may have the enchant at: what players have had, at most the normal max.
     * 0 when nobody has had it.
     */
    public static int levelCap(int normalMax, int seenLevel)
    {
        return Math.max(0, Math.min(normalMax, seenLevel));
    }

    /**
     * A generated item may go over the enchant's normal max only after players have had the max.
     */
    public static boolean mayGoOverMax(int normalMax, int seenLevel)
    {
        return seenLevel >= normalMax;
    }

    public Set<String> items()
    {
        return Collections.unmodifiableSet(_items);
    }

    public Map<String, Integer> enchants()
    {
        return Collections.unmodifiableMap(_enchants);
    }

    public void clear()
    {
        _items.clear();
        _enchants.clear();
        _dirty = true;
    }

    /**
     * True once since the last call when anything new was seen.
     */
    public boolean takeDirty()
    {
        boolean dirty = _dirty;
        _dirty = false;
        return dirty;
    }

    public void markDirty()
    {
        _dirty = true;
    }
}
