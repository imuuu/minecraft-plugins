package imu.iAPI.Config;

import imu.iAPI.Menus.Button;
import imu.iAPI.Menus.Buttons;
import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Click;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Pager;
import imu.iAPI.Menus.Prompt;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * The entries of a {@link ConfigMenu}. Left click edits a value, right click resets it to the default.
 */
public class ConfigEntriesMenu extends Menu
{
    private static final int PREVIOUS = 45;
    private static final int BACK = 49;
    private static final int NEXT = 53;

    private final ConfigMenu _menu;
    private final Pager<ConfigEntry> _pager = Pager.rows(0, 5);

    public ConfigEntriesMenu(ConfigMenu menu)
    {
        super("&9" + menu.getTitle(), 6);
        _menu = menu;
    }

    @Override
    protected void render(Canvas canvas)
    {
        canvas.fillRow(5, Icon.filler("black_stained_glass_pane"));
        FileConfiguration config = _menu.getConfig();
        _pager.render(canvas, _menu.getEntries(), entry -> new Button(icon(entry, config), click ->
        {
            if (click.isRight())
                resetToDefault(click, entry);
            else
                edit(click, entry);
        }), PREVIOUS, NEXT);
        Buttons.back(canvas, BACK);
    }

    static Icon icon(ConfigEntry entry, FileConfiguration config)
    {
        Object value = entry.get(config);
        Object defaultValue = entry.getDefault(config);

        String material = entry.getIcon() == null ? "paper" : entry.getIcon().name();
        if (entry.getType() == ConfigEntry.Type.BOOLEAN)
            material = Boolean.TRUE.equals(value) ? "lime_dye" : "gray_dye";

        List<String> lore = new ArrayList<>();
        for (String line : entry.getDescription())
            lore.add("&7" + line);
        lore.add(" ");
        lore.add("&9Value: &a" + entry.format(value));
        if (defaultValue != null)
            lore.add("&9Default: &7" + entry.format(defaultValue));
        if (entry.hasRange())
            lore.add("&9Allowed: &7" + entry.rangeText());
        if (entry.getType() == ConfigEntry.Type.CHOICE)
            lore.add("&9Options: &7" + String.join(", ", entry.getOptions()));
        if (entry.getNote() != null)
            lore.add("&9Takes effect: &e" + entry.getNote());
        lore.add(" ");
        lore.add(switch (entry.getType())
        {
            case BOOLEAN -> "&bLeft click &7to toggle";
            case CHOICE -> "&bLeft click &7for the next option";
            default -> "&bLeft click &7to change";
        });
        if (defaultValue != null)
            lore.add("&bRight click &7to reset to default");
        lore.add("&8" + entry.getPath());
        return Icon.of(material, "&6&l" + entry.getName(), lore.toArray(String[]::new));
    }

    private void edit(Click click, ConfigEntry entry)
    {
        FileConfiguration config = _menu.getConfig();
        switch (entry.getType())
        {
            case BOOLEAN -> _menu.setValue(click.viewer(), entry, !Boolean.TRUE.equals(entry.get(config)));
            case CHOICE -> _menu.setValue(click.viewer(), entry, entry.nextOption(config));
            default -> click.prompt(prompt(click, entry));
        }
    }

    private Prompt prompt(Click click, ConfigEntry entry)
    {
        String current = entry.format(entry.get(_menu.getConfig()));
        List<String> lines = new ArrayList<>(entry.getDescription());
        lines.add("&9Current value: &a" + current);
        if (entry.hasRange())
            lines.add("&9Allowed: &7" + entry.rangeText());
        if (entry.getNote() != null)
            lines.add("&9Takes effect: &e" + entry.getNote());
        return Prompt.of(entry.getName(), lines, current, 256, text ->
        {
            try
            {
                _menu.setValue(click.viewer(), entry, entry.parse(text));
                return null;
            } catch (IllegalArgumentException e)
            {
                return e.getMessage();
            }
        });
    }

    private void resetToDefault(Click click, ConfigEntry entry)
    {
        Object defaultValue = entry.getDefault(_menu.getConfig());
        if (defaultValue != null)
            _menu.setValue(click.viewer(), entry, defaultValue);
    }
}
