package imu.iAPI.Permissions;

import imu.iAPI.Menus.Button;
import imu.iAPI.Menus.Buttons;
import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Click;
import imu.iAPI.Menus.ConfirmMenu;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Pager;
import imu.iAPI.Menus.Viewer;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * One player's permissions of one plugin. A click gives or takes a permission, a right click puts it back to the
 * plugin's default. High and very high ones ask first, and nobody can give a permission they don't have.
 */
public class PlayerPermissionsMenu extends Menu
{
    static final int PREVIOUS = 45;
    static final int RESET_ALL = 47;
    static final int BACK = 48;
    static final int PLAYER = 49;
    static final int LEVELS = 50;
    static final int NEXT = 53;

    private final PermissionGame _game;
    private final PluginPermissions _plugin;
    private final PermissionTarget _target;
    private final Pager<PermissionInfo> _pager = Pager.rows(0, 5);

    public PlayerPermissionsMenu(PermissionGame game, PluginPermissions plugin, PermissionTarget target)
    {
        super("&9" + plugin.plugin() + ": " + target.name(), 6);
        _game = game;
        _plugin = plugin;
        _target = target;
    }

    @Override
    protected void render(Canvas canvas)
    {
        canvas.fillRow(5, Icon.filler(Material.BLACK_STAINED_GLASS_PANE));
        List<PermissionInfo> permissions = _plugin.permissions().stream().map(this::live).toList();
        boolean locked = locked(canvas.viewer());
        _pager.render(canvas, permissions, permission -> Button.of(icon(permission, locked), click -> clicked(click, permission)),
                PREVIOUS, NEXT);

        long on = permissions.stream().filter(permission -> _game.has(_target, permission)).count();
        Icon player = _target.icon().name("&e&l" + _target.name()).lore(
                "&7Has &a" + on + "&7/" + permissions.size() + " &7of " + _plugin.plugin() + "'s permissions",
                _target.op() ? "&cOp: &7has every op permission by default" : "&7Not op",
                _target.online() ? "&aOnline" : "&7Offline, applies when they join");
        if (locked)
            player = player.addLore(" ", "&cOnly ops can change the permissions", "&cof ops and of players who change permissions");
        canvas.set(PLAYER, player);
        canvas.set(LEVELS, levelsIcon());
        if (!locked && permissions.stream().anyMatch(permission -> _game.grant(_target, permission.node()) != null))
            canvas.set(RESET_ALL, Buttons.confirm(Icon.of(Material.LAVA_BUCKET, "&cReset all",
                            "&7Everything given or taken here", "&7goes back to the plugin's defaults"),
                    "&cReset " + _target.name() + "'s permissions?", this::resetAll));
        Buttons.back(canvas, BACK);
    }

    // The default the plugin gives it now, not only the one plugin.yml starts with
    private PermissionInfo live(PermissionInfo permission)
    {
        return permission.withDefault(_game.defaultOf(permission));
    }

    /**
     * Ops, and players who change permissions themselves, are changed only by ops: nobody can take an admin's
     * permissions away from below. Everyone may change their own.
     */
    private boolean locked(Viewer viewer)
    {
        if (viewer.id().equals(_target.id()) || _game.isOp(viewer))
            return false;
        return _target.op() || _game.managesPermissions(_target);
    }

    private Icon icon(PermissionInfo permission, boolean locked)
    {
        boolean on = _game.has(_target, permission);
        Boolean grant = _game.grant(_target, permission.node());
        PermissionLevel level = permission.level();

        List<String> lore = new ArrayList<>();
        for (String line : wrap(permission.description(), 40))
            lore.add("&7" + line);
        lore.add(" ");
        lore.add("&9Danger: " + (level == null ? "&7not set" : level.coloured()));
        if (level != null)
            lore.add("&8" + level.meaning());
        lore.add("&9Has it: " + (on ? "&aYes" : "&cNo") + " &7" + source(permission, grant));
        if (!permission.children().isEmpty())
            lore.add("&9Gives also: &7" + shortList(permission.children(), 3));
        lore.add(" ");
        if (locked)
            lore.add("&8Only ops can change it for " + _target.name());
        else
            lore.add(on ? "&bClick &7to take it away" : "&bClick &7to give it");
        if (grant != null && !locked)
            lore.add("&bRight click &7to reset to the default (" + permission.defaultText() + ")");

        Icon icon = permission.icon() != null ? Icon.of(permission.icon()).glow(on)
                : Icon.of(on ? Material.LIME_DYE : Material.GRAY_DYE);
        String colour = level == null ? "&f" : level.colour();
        return icon.name((on ? "&a✔ " : "&c✘ ") + colour + permission.node()).lore(lore.toArray(String[]::new));
    }

    private static String source(PermissionInfo permission, Boolean grant)
    {
        if (grant == null)
            return "(default: " + permission.defaultText() + ")";
        return grant ? "(given here)" : "(taken away here)";
    }

    private void clicked(Click click, PermissionInfo permission)
    {
        if (locked(click.viewer()))
        {
            click.message("&cOnly ops can change the permissions of ops and of players who change permissions.");
            return;
        }
        Boolean grant = _game.grant(_target, permission.node());
        if (click.isRight())
        {
            if (grant != null)
                set(click, permission, null);
            return;
        }

        boolean give = !_game.has(_target, permission);
        // Back to the default when that already is what was asked, so the file only keeps real changes
        Boolean value = grant != null && give == permission.hasByDefault(_target.op()) ? null : give;
        if (give && !click.viewer().hasPermission(permission.node()))
        {
            click.message("&cYou can only give permissions you have yourself.");
            return;
        }
        if (give && permission.level() != null && permission.level().asksFirst())
        {
            click.open(new ConfirmMenu(permission.level().colour() + "Give " + permission.level().label().toLowerCase()
                    + " permission?", icon(permission, false), yes -> set(yes, permission, value)));
            return;
        }
        set(click, permission, value);
    }

    private void set(Click click, PermissionInfo permission, Boolean value)
    {
        _game.setGrant(_target, permission.node(), value, click.viewer());
        boolean on = _game.has(_target, permission);
        click.message((on ? "&a" + _target.name() + " has &e" : "&c" + _target.name() + " doesn't have &e")
                + permission.node() + (on ? " &anow" : " &cnow")
                + (value == null ? " &7(default: " + permission.defaultText() + ")" : ""));
    }

    private void resetAll(Click click)
    {
        if (locked(click.viewer()))
            return;
        int reset = 0;
        for (PermissionInfo permission : _plugin.permissions())
        {
            if (_game.grant(_target, permission.node()) != null)
            {
                _game.setGrant(_target, permission.node(), null, click.viewer());
                reset++;
            }
        }
        click.message("&e" + reset + " &7of " + _target.name() + "'s " + _plugin.plugin() + " permissions back to default");
    }

    static Icon levelsIcon()
    {
        List<String> lore = new ArrayList<>();
        for (PermissionLevel level : PermissionLevel.values())
        {
            lore.add(level.coloured() + "&7: " + level.meaning());
        }
        lore.add(" ");
        lore.add("&7High and very high ask before giving.");
        return Icon.of(Material.BOOK, "&eDanger levels", lore.toArray(String[]::new));
    }

    static String shortList(List<String> items, int max)
    {
        if (items.size() <= max)
            return String.join(", ", items);
        return String.join(", ", items.subList(0, max)) + " +" + (items.size() - max) + " more";
    }

    /**
     * Words into lines of at most width characters; a longer word gets a line of its own.
     */
    static List<String> wrap(String text, int width)
    {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.trim().split("\\s+"))
        {
            if (word.isEmpty())
                continue;
            if (!line.isEmpty() && line.length() + 1 + word.length() > width)
            {
                lines.add(line.toString());
                line.setLength(0);
            }
            if (!line.isEmpty())
                line.append(' ');
            line.append(word);
        }
        if (!line.isEmpty())
            lines.add(line.toString());
        return lines;
    }
}
