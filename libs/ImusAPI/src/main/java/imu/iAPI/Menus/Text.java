package imu.iAPI.Menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Text of menus: &amp; colour codes to components and back, and plain text for tests.
 */
public final class Text
{
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private Text() {}

    /**
     * "&amp;6Gold &amp;lbold" as a component.
     */
    public static Component of(String legacy)
    {
        return legacy == null ? null : LEGACY.deserialize(legacy);
    }

    public static List<Component> of(List<String> legacy)
    {
        List<Component> lines = new ArrayList<>(legacy.size());
        for (String line : legacy)
            lines.add(of(line));
        return lines;
    }

    /**
     * Back to &amp; colour codes, for APIs that take them.
     */
    public static String legacy(Component text)
    {
        return text == null ? null : LEGACY.serialize(text);
    }

    /**
     * The text without colours.
     */
    public static String plain(Component text)
    {
        return text == null ? null : PlainTextComponentSerializer.plainText().serialize(text);
    }
}
