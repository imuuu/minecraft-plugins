package imu.imustnt;

import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Viewer;
import imu.imustnt.engine.TntType;
import org.bukkit.Material;

import java.util.function.Function;

/**
 * The special TNTs for admins to take: click for one, right or shift click for a stack.
 */
class TntMenu extends Menu
{
    interface Giver
    {
        void give(Viewer viewer, TntType type, int amount);
    }

    private final Function<TntType, Icon> _icons;
    private final Giver _giver;

    /**
     * @param icons how each TNT looks
     */
    TntMenu(String title, Function<TntType, Icon> icons, Giver giver)
    {
        super(title, 1);
        _icons = icons;
        _giver = giver;
    }

    @Override
    protected void render(Canvas canvas)
    {
        canvas.fill(Icon.filler(Material.PURPLE_STAINED_GLASS_PANE));
        TntType[] types = TntType.values();
        for (int i = 0; i < types.length; i++)
        {
            TntType type = types[i];
            canvas.set(i, _icons.apply(type), click ->
                    _giver.give(click.viewer(), type, click.isRight() || click.isShift() ? 64 : 1));
        }
    }
}
