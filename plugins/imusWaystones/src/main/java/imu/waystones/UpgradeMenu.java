package imu.waystones;

import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Click;
import imu.iAPI.Menus.ClickKind;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Prompt;
import imu.iAPI.Menus.Viewer;
import imu.iAPI.Utilities.TextUtils;
import imu.waystones.engine.Cost;
import imu.waystones.engine.Panel;
import imu.waystones.engine.Rules;
import imu.waystones.engine.UpgradeKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The player's own upgrades on a waystone: each costs items, and the next tier more.
 */
final class UpgradeMenu extends Menu
{
    static final Map<UpgradeKind, Integer> SLOTS = Map.of(UpgradeKind.CAST_TIME, 10, UpgradeKind.COOLDOWN, 12,
            UpgradeKind.DIMENSION, 14, UpgradeKind.XP_USAGE, 16, UpgradeKind.FOUNDATION, 22);
    private static final Map<UpgradeKind, String> ICONS = Map.of(UpgradeKind.CAST_TIME, "clock", UpgradeKind.COOLDOWN,
            "compass", UpgradeKind.DIMENSION, "ender_pearl", UpgradeKind.XP_USAGE, "experience_bottle",
            UpgradeKind.FOUNDATION, "diamond_block");
    static final int BACK = 27, RENAME = 30, EXTRACT = 32;

    private final WaystoneGame _game;
    private final Waystone _ws;

    UpgradeMenu(WaystoneGame game, Waystone ws)
    {
        super(game.text("upgrades-title", "name", ws.name), 4);
        _game = game;
        _ws = ws;
    }

    @Override
    protected void render(Canvas canvas)
    {
        Viewer viewer = canvas.viewer();
        Panel panel = _ws.panel(viewer.id());
        canvas.fill(Icon.filler("black_stained_glass_pane"));

        if (_ws.maxedOut)
            canvas.set(13, Icon.of("nether_star", _game.text("maxed-out"), _game.text("maxed-out-lore")));
        else
        {
            for (UpgradeKind kind : UpgradeKind.BUYABLE)
                canvas.set(SLOTS.get(kind), icon(kind, panel, viewer), click -> buy(click, kind));
            canvas.set(EXTRACT, Icon.of("grindstone", _game.text("extract"), _game.text("extract-lore")), this::extract);
        }

        if (canvas.canGoBack())
            canvas.set(BACK, Icon.of("red_wool", _game.text("menu-back")), Click::back);
        if (viewer.id().equals(_ws.owner) || viewer.hasPermission(ImusWaystones.ADMIN))
            canvas.set(RENAME, Icon.of("name_tag", _game.text("rename")), this::rename);
    }

    private Icon icon(UpgradeKind kind, Panel panel, Viewer viewer)
    {
        Rules rules = _game.rules();
        int max = rules.maxTier(kind);
        if (max == 0)
            return Icon.of("barrier", _game.text("upgrade-off", "upgrade", _game.text("upgrade." + kind.key() + ".name")));
        int tier = Math.min(panel.tier(kind), max);
        List<String> lore = new ArrayList<>(_game.textList("upgrade." + kind.key() + ".lore"));
        lore.add(" ");
        lore.add(_game.text("upgrade-now", "value", value(kind, panel)));
        lore.add(_game.text("upgrade-tier", "tier", tier, "max", max));
        List<Cost> next = rules.nextCost(kind, tier);
        if (next == null)
            lore.add(_game.text("upgrade-maxed"));
        else
        {
            lore.add(" ");
            lore.add(_game.text("upgrade-cost"));
            for (Cost cost : next)
                lore.add(_game.text("upgrade-cost-line", "amount", cost.amount(), "item", pretty(cost.material())));
            lore.add(" ");
            lore.add(_game.text("upgrade-click"));
            if (viewer.hasPermission(ImusWaystones.ADMIN))
                lore.add(_game.text("upgrade-admin-click"));
        }
        return Icon.of(ICONS.get(kind), _game.text("upgrade." + kind.key() + ".name"), lore.toArray(String[]::new));
    }

    /**
     * What the upgrade gives now, e.g. "12s" or "Nether".
     */
    private String value(UpgradeKind kind, Panel panel)
    {
        Rules rules = _game.rules();
        return switch (kind)
        {
            case CAST_TIME -> trim(rules.castSeconds(panel)) + "s";
            case COOLDOWN, FOUNDATION -> TextUtils.FormatTime((long) (rules.cooldownSeconds(panel) * 1000));
            case XP_USAGE -> rules.xpLevels(panel) + " lvl";
            case DIMENSION -> _game.text("dimension." + Math.min(2, panel.tier(UpgradeKind.DIMENSION)));
            default -> "";
        };
    }

    private void buy(Click click, UpgradeKind kind)
    {
        Viewer viewer = click.viewer();
        Panel panel = _ws.panel(viewer.id());
        int tier = panel.tier(kind);
        List<Cost> cost = _game.rules().nextCost(kind, tier);
        if (cost == null)
            return;
        boolean free = click.kind() == ClickKind.SHIFT_RIGHT && viewer.hasPermission(ImusWaystones.ADMIN);
        if (!free && !_game.take(viewer, cost))
        {
            _game.send(viewer, "cant-afford");
            _game.sound(viewer, "entity.villager.no", 0.6f, 1f);
            return;
        }
        panel.set(kind, tier + 1);
        _game.savePanel(_ws, viewer.id());
        _game.sound(viewer, "block.anvil.use", 0.5f, 1.4f);
    }

    private void extract(Click click)
    {
        if (click.kind() != ClickKind.SHIFT_RIGHT)
            return;
        Viewer viewer = click.viewer();
        Panel panel = _ws.panel(viewer.id());
        if (panel.isEmpty())
        {
            _game.send(viewer, "nothing-to-extract");
            return;
        }
        _game.giveUpgrades(viewer, panel);
        panel.clear();
        _game.savePanel(_ws, viewer.id());
        _game.send(viewer, "upgrades-extracted");
    }

    private void rename(Click click)
    {
        Viewer viewer = click.viewer();
        click.prompt(Prompt.of(_game.text("rename-title"), List.of(_game.text("rename-help")), _ws.name, 40, text ->
        {
            if (text.isBlank())
                return _game.text("rename-empty");
            _ws.name = text.trim();
            _game.save(_ws);
            _game.send(viewer, "renamed", "name", _ws.name);
            return null;
        }));
    }

    static String pretty(String material)
    {
        StringBuilder name = new StringBuilder();
        for (String word : material.toLowerCase().split("_"))
        {
            if (!name.isEmpty())
                name.append(' ');
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }

    private static String trim(double value)
    {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(Math.round(value * 10) / 10.0);
    }
}
