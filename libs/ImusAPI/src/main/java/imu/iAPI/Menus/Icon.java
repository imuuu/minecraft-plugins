package imu.iAPI.Menus;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * What a slot shows: a material, name, lore, amount and glint. It holds no ItemStack, so menus can be drawn and
 * clicked in unit tests; {@link Material} is a plain enum that works there too.
 * An icon can also carry a ready item of the server as its {@link #base()} (e.g. a plugin's custom item); a
 * name and lore given here then replace the base item's own.
 * <p>
 * Icons are immutable; every setter returns a changed copy.
 */
public final class Icon
{
    private final String _material;
    private final int _amount;
    private final Component _name;
    private final List<Component> _lore;
    private final boolean _glow;
    private final boolean _hideTooltip;
    private final Object _base;

    private Icon(String material, int amount, Component name, List<Component> lore, boolean glow, boolean hideTooltip, Object base)
    {
        _material = material;
        _amount = amount;
        _name = name;
        _lore = lore;
        _glow = glow;
        _hideTooltip = hideTooltip;
        _base = base;
    }

    public static Icon of(Material material)
    {
        return of(key(material));
    }

    /**
     * An icon with a name and lore lines, &amp; colour codes.
     */
    public static Icon of(Material material, String name, String... lore)
    {
        return of(key(material), name, lore);
    }

    /**
     * A pane or other item with no tooltip, to fill empty slots.
     */
    public static Icon filler(Material material)
    {
        return filler(key(material));
    }

    /**
     * By the item's id, "diamond" or "minecraft:diamond", e.g. one read from a config file.
     */
    public static Icon of(String material)
    {
        return new Icon(key(material), 1, null, List.of(), false, false, null);
    }

    /**
     * An icon with a name and lore lines, &amp; colour codes.
     */
    public static Icon of(String material, String name, String... lore)
    {
        return of(material).name(name).lore(lore);
    }

    /**
     * A pane or other item with no tooltip, to fill empty slots.
     */
    public static Icon filler(String material)
    {
        return of(material).hideTooltip(true);
    }

    private static String key(Material material)
    {
        return "minecraft:" + material.name().toLowerCase(Locale.ROOT);
    }

    private static String key(String material)
    {
        String key = material.toLowerCase(Locale.ROOT);
        return key.contains(":") ? key : "minecraft:" + key;
    }

    public Icon material(Material material)
    {
        return material(key(material));
    }

    public Icon material(String material)
    {
        return new Icon(key(material), _amount, _name, _lore, _glow, _hideTooltip, _base);
    }

    public Icon amount(int amount)
    {
        return new Icon(_material, amount, _name, _lore, _glow, _hideTooltip, _base);
    }

    public Icon name(Component name)
    {
        return new Icon(_material, _amount, name, _lore, _glow, _hideTooltip, _base);
    }

    public Icon name(String legacy)
    {
        return name(Text.of(legacy));
    }

    public Icon lore(List<Component> lore)
    {
        return new Icon(_material, _amount, _name, List.copyOf(lore), _glow, _hideTooltip, _base);
    }

    public Icon lore(String... legacy)
    {
        return lore(Text.of(List.of(legacy)));
    }

    /**
     * The lore with more lines at the end, &amp; colour codes.
     */
    public Icon addLore(String... legacy)
    {
        List<Component> lore = new ArrayList<>(_lore);
        lore.addAll(Text.of(List.of(legacy)));
        return lore(lore);
    }

    public Icon glow(boolean glow)
    {
        return new Icon(_material, _amount, _name, _lore, glow, _hideTooltip, _base);
    }

    public Icon hideTooltip(boolean hide)
    {
        return new Icon(_material, _amount, _name, _lore, _glow, hide, _base);
    }

    /**
     * A server item to show instead of a new one of {@link #material()}; the platform adapter knows its type.
     */
    public Icon base(Object item)
    {
        return new Icon(_material, _amount, _name, _lore, _glow, _hideTooltip, item);
    }

    public String material()
    {
        return _material;
    }

    public int amount()
    {
        return _amount;
    }

    /**
     * The name, or null to keep the item's own.
     */
    public Component name()
    {
        return _name;
    }

    public List<Component> lore()
    {
        return Collections.unmodifiableList(_lore);
    }

    public boolean glow()
    {
        return _glow;
    }

    public boolean hideTooltip()
    {
        return _hideTooltip;
    }

    public Object base()
    {
        return _base;
    }

    /**
     * The name without colours, or null.
     */
    public String plainName()
    {
        return Text.plain(_name);
    }

    public List<String> plainLore()
    {
        return _lore.stream().map(Text::plain).toList();
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;
        if (!(other instanceof Icon icon))
            return false;
        return _amount == icon._amount && _glow == icon._glow && _hideTooltip == icon._hideTooltip
                && _material.equals(icon._material) && Objects.equals(_name, icon._name)
                && _lore.equals(icon._lore) && Objects.equals(_base, icon._base);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(_material, _amount, _name, _lore, _glow, _hideTooltip, _base);
    }

    @Override
    public String toString()
    {
        return _material + (_amount != 1 ? " x" + _amount : "") + (_name != null ? " \"" + plainName() + "\"" : "");
    }
}
