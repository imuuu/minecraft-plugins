package imu.waystones;

import imu.iAPI.Menus.Button;
import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Click;
import imu.iAPI.Menus.ClickKind;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Pager;

import java.util.ArrayList;

/**
 * /waystones list: every waystone for admins. Click goes there at once, shift right click removes it.
 */
final class ListMenu extends Menu
{
    static final int PREVIOUS = 45, NEXT = 53;

    private final WaystoneGame _game;
    private final Pager<Waystone> _pager;

    ListMenu(WaystoneGame game)
    {
        super(game.text("list-title", "count", game.all().size()), 6);
        _game = game;
        _pager = Pager.<Waystone>rows(0, 5).arrows(Icon.of("arrow", game.text("menu-previous")), Icon.of("arrow", game.text("menu-next")));
    }

    @Override
    protected void render(Canvas canvas)
    {
        canvas.fillRow(5, Icon.filler("cyan_stained_glass_pane"));
        _pager.render(canvas, new ArrayList<>(_game.all()), ws -> Button.of(_game.icon(ws).name(ws.name).lore(
                _game.text("destination-owner", "owner", ws.ownerName),
                _game.text("list-location", "world", ws.world, "x", ws.x, "y", ws.y, "z", ws.z),
                _game.text("list-visibility", "visibility", _game.text("visibility." + ws.visibility.name())),
                " ",
                _game.text("list-click")), click -> click(click, ws)), PREVIOUS, NEXT);
    }

    private void click(Click click, Waystone ws)
    {
        if (click.kind() == ClickKind.SHIFT_RIGHT)
        {
            _game.remove(ws);
            _game.send(click.viewer(), "removed", "name", ws.name);
            return;
        }
        click.close();
        if (!_game.teleport(click.viewer(), ws))
            _game.send(click.viewer(), "world-not-loaded", "world", ws.world);
    }
}
