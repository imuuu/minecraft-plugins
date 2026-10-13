package imu.waystones;

import imu.iAPI.Menus.Button;
import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Click;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Pager;
import imu.iAPI.Menus.Viewer;
import imu.iAPI.Utilities.TextUtils;
import org.bukkit.Material;

import java.util.List;

/**
 * A waystone's menu: the waystones the player can go to, the upgrades, and for admins its settings.
 */
final class WaystoneMenu extends Menu
{
    static final int PREVIOUS = 27, VISIBILITY = 28, UNBREAKABLE = 29, MAX_OUT = 30, UPGRADES = 31, ENABLED = 32,
            REMOVE = 33, NEXT = 35;

    private final WaystoneGame _game;
    private final Waystone _ws;
    private final Pager<Waystone> _pager;

    WaystoneMenu(WaystoneGame game, Waystone ws)
    {
        super(ws.name, 4);
        _game = game;
        _ws = ws;
        _pager = Pager.<Waystone>rows(0, 3).arrows(Icon.of(Material.ARROW, game.text("menu-previous")), Icon.of(Material.ARROW, game.text("menu-next")));
    }

    @Override
    protected void render(Canvas canvas)
    {
        Viewer viewer = canvas.viewer();
        canvas.fillRow(3, Icon.filler(Material.CYAN_STAINED_GLASS_PANE));

        String closed = closedReason(viewer);
        if (closed != null)
            canvas.set(13, Icon.of(Material.BARRIER, closed));
        else
            renderDestinations(canvas, viewer);

        canvas.set(UPGRADES, Icon.of(Material.BEACON, _game.text("menu-upgrades"), _game.text("menu-upgrades-lore")),
                click -> click.open(new UpgradeMenu(_game, _ws)));
        if (viewer.hasPermission(ImusWaystones.ADMIN))
            renderAdmin(canvas);
    }

    /**
     * Why the player can't leave from here now, or null.
     */
    private String closedReason(Viewer viewer)
    {
        if (!_ws.enabled)
            return _game.text("closed-disabled");
        if (!_ws.visibility.canDepart())
            return _game.text("closed-arrival-only");
        long left = _game.cooldownLeft(viewer, _ws);
        if (left > 0)
            return _game.text("closed-cooldown", "time", TextUtils.FormatTime(left));
        return null;
    }

    private void renderDestinations(Canvas canvas, Viewer viewer)
    {
        List<Waystone> list = _game.destinations(_ws, viewer.id());
        if (list.isEmpty())
        {
            canvas.set(13, Icon.of(Material.MAP, _game.text("no-destinations"), _game.text("no-destinations-lore")));
            return;
        }
        _pager.render(canvas, list, to -> Button.of(_game.icon(to).name(to.name).lore(
                _game.text("destination-owner", "owner", to.ownerName),
                _game.text("destination-world", "world", to.world),
                " ",
                _game.text("destination-click")), click ->
        {
            click.close();
            _game.travel(click.viewer(), _ws, to);
        }), PREVIOUS, NEXT);
    }

    private void renderAdmin(Canvas canvas)
    {
        canvas.set(VISIBILITY, toggle(Material.SPYGLASS, "admin-visibility", _game.text("visibility." + _ws.visibility.name())), click ->
        {
            _ws.visibility = _ws.visibility.next();
            _game.save(_ws);
        });
        canvas.set(UNBREAKABLE, toggle(Material.BEDROCK, "admin-unbreakable", onOff(_ws.unbreakable)), click ->
        {
            _ws.unbreakable = !_ws.unbreakable;
            _game.save(_ws);
        });
        canvas.set(MAX_OUT, toggle(Material.NETHER_STAR, "admin-max-out", onOff(_ws.maxedOut)), click ->
        {
            _ws.maxedOut = !_ws.maxedOut;
            _game.save(_ws);
        });
        canvas.set(ENABLED, toggle(Material.LEVER, "admin-enabled", onOff(_ws.enabled)), click ->
        {
            _ws.enabled = !_ws.enabled;
            _game.save(_ws);
        });
        canvas.set(REMOVE, Icon.of(Material.LAVA_BUCKET, _game.text("admin-remove"), _game.text("admin-remove-lore")), this::remove);
    }

    private void remove(Click click)
    {
        if (!click.isShift())
            return;
        click.close();
        _game.remove(_ws);
        _game.sound(click.viewer(), "entity.lightning_bolt.impact", 0.3f, 1f);
        _game.send(click.viewer(), "removed", "name", _ws.name);
    }

    private Icon toggle(Material material, String key, String value)
    {
        return Icon.of(material, _game.text(key), _game.text("admin-current", "value", value), _game.text("admin-click"));
    }

    private String onOff(boolean value)
    {
        return _game.text(value ? "on" : "off");
    }
}
