package imu.iAPI.Config;

import imu.iAPI.Menus.Button;
import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Pager;
import org.bukkit.Material;

import java.util.Collection;
import java.util.List;

/**
 * /ia config: every registered {@link ConfigMenu} the player may edit, one item each.
 */
public class ConfigListMenu extends Menu
{
    private final Collection<ConfigMenu> _menus;
    private final Pager<ConfigMenu> _pager = Pager.rows(0, 5);

    public ConfigListMenu()
    {
        this(ConfigMenu.getRegistered());
    }

    public ConfigListMenu(Collection<ConfigMenu> menus)
    {
        super("&9Plugin settings", 6);
        _menus = menus;
    }

    @Override
    protected void render(Canvas canvas)
    {
        List<ConfigMenu> editable = _menus.stream().filter(menu -> menu.canEdit(canvas.viewer())).toList();
        _pager.render(canvas, editable, menu -> Button.of(
                Icon.of(Material.COMPARATOR, "&6&l" + menu.getTitle(), "&7" + menu.getPluginName(), "&7" + menu.getEntries().size() + " settings"),
                click -> click.open(new ConfigEntriesMenu(menu))), 45, 53);
    }
}
