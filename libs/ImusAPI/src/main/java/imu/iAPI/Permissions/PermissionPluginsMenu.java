package imu.iAPI.Permissions;

import imu.iAPI.Menus.Button;
import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Pager;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * /ia perms: every plugin with permissions; clicking one picks a player.
 */
public class PermissionPluginsMenu extends Menu
{
    static final int PREVIOUS = 45;
    static final int LEVELS = 49;
    static final int NEXT = 53;

    private final PermissionGame _game;
    private final Pager<PluginPermissions> _pager = Pager.rows(0, 5);

    public PermissionPluginsMenu(PermissionGame game)
    {
        super("&9Permissions", 6);
        _game = game;
    }

    @Override
    protected void render(Canvas canvas)
    {
        canvas.fillRow(5, Icon.filler(Material.BLACK_STAINED_GLASS_PANE));
        List<PluginPermissions> plugins = Permissions.registered().stream().filter(plugin -> !plugin.isEmpty()).toList();
        _pager.render(canvas, plugins, plugin -> Button.of(icon(plugin),
                click -> click.open(new PermissionPlayersMenu(_game, plugin))), PREVIOUS, NEXT);
        canvas.set(LEVELS, PlayerPermissionsMenu.levelsIcon());
    }

    private static Icon icon(PluginPermissions plugin)
    {
        List<String> lore = new ArrayList<>();
        lore.add("&7" + plugin.permissions().size() + " permissions");
        for (PermissionLevel level : PermissionLevel.values())
        {
            int count = plugin.count(level);
            if (count > 0)
                lore.add("  " + level.coloured() + "&7: " + count);
        }
        int unset = plugin.count(null);
        if (unset > 0)
            lore.add("  &7Not set: " + unset);
        lore.add(" ");
        lore.add("&bClick &7to pick a player");
        return Icon.of(Material.WRITABLE_BOOK, "&6&l" + plugin.plugin(), lore.toArray(String[]::new));
    }
}
