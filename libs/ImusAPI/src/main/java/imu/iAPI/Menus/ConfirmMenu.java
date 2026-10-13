package imu.iAPI.Menus;

import org.bukkit.Material;

import java.util.function.Consumer;

/**
 * Asks yes or no before something that can't be undone. Both answers go back to the menu that opened it; yes then
 * runs the action, which may open another menu.
 */
public class ConfirmMenu extends Menu
{
    public static final int YES = 11;
    public static final int SUBJECT = 13;
    public static final int NO = 15;

    private final Icon _subject;
    private final Consumer<Click> _onYes;

    /**
     * @param question the title, &amp; colour codes
     * @param subject  shown between the answers
     */
    public ConfirmMenu(String question, Icon subject, Consumer<Click> onYes)
    {
        super(question, 3);
        _subject = subject;
        _onYes = onYes;
    }

    @Override
    protected void render(Canvas canvas)
    {
        canvas.fill(Icon.filler(Material.GRAY_STAINED_GLASS_PANE));
        canvas.set(YES, Icon.of(Material.LIME_CONCRETE, "&a&lYes"), click ->
        {
            click.back();
            _onYes.accept(click);
        });
        canvas.set(SUBJECT, _subject);
        canvas.set(NO, Icon.of(Material.RED_CONCRETE, "&c&lNo"), Click::back);
    }
}
