package imu.iAPI.Permissions;

import imu.iAPI.Menus.Button;
import imu.iAPI.Menus.Buttons;
import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Click;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Pager;
import imu.iAPI.Menus.Prompt;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * The players close by, to pick whose permissions of a plugin to change. Anyone else can be searched by name.
 */
public class PermissionPlayersMenu extends Menu
{
    static final int NOBODY = 22;
    static final int PREVIOUS = 45;
    static final int BACK = 48;
    static final int SEARCH = 49;
    static final int LEVELS = 50;
    static final int NEXT = 53;

    private final PermissionGame _game;
    private final PluginPermissions _plugin;
    private final Pager<PermissionTarget> _pager = Pager.rows(0, 5);
    private PermissionTarget _searched;

    public PermissionPlayersMenu(PermissionGame game, PluginPermissions plugin)
    {
        super("&9" + plugin.plugin() + ": pick a player", 6);
        _game = game;
        _plugin = plugin;
    }

    @Override
    protected void render(Canvas canvas)
    {
        canvas.fillRow(5, Icon.filler(Material.BLACK_STAINED_GLASS_PANE));

        List<PermissionTarget> players = new ArrayList<>();
        if (_searched != null)
            players.add(_searched);
        for (PermissionTarget target : _game.nearby(canvas.viewer()))
        {
            if (_searched == null || !_searched.id().equals(target.id()))
                players.add(target);
        }

        if (players.isEmpty())
            canvas.set(NOBODY, Icon.of(Material.BARRIER, "&7Nobody close by", "&7Search a player by name below"));
        _pager.render(canvas, players, target -> Button.of(icon(target), click -> open(click, target)), PREVIOUS, NEXT);

        canvas.set(SEARCH, Icon.of(Material.NAME_TAG, "&eSearch by name", "&7Online, or played here before"), this::search);
        canvas.set(LEVELS, PlayerPermissionsMenu.levelsIcon());
        Buttons.back(canvas, BACK);
    }

    private Icon icon(PermissionTarget target)
    {
        long on = _plugin.permissions().stream().filter(permission -> _game.has(target, permission)).count();
        Icon icon = target.icon().name("&e&l" + target.name());
        if (target == _searched)
            icon = icon.addLore("&6Searched");
        return icon.addLore("&7Has &a" + on + "&7/" + _plugin.permissions().size() + " &7permissions",
                "&bClick &7to change them");
    }

    private void open(Click click, PermissionTarget target)
    {
        click.open(new PlayerPermissionsMenu(_game, _plugin, target));
    }

    private void search(Click click)
    {
        click.prompt(Prompt.of("Player name", List.of("&7Online, or played here before"), "", 16, text ->
        {
            PermissionTarget target = _game.find(text.trim());
            if (target == null)
                return "Nobody called " + text.trim() + " has played here";
            _searched = target;
            _pager.setPage(0);
            return null;
        }));
    }
}
