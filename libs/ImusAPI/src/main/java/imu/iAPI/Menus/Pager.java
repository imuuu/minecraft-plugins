package imu.iAPI.Menus;

import java.util.List;
import java.util.function.Function;

/**
 * Pages of items in a set of slots, with arrows to the previous and next page. Keep one in the menu so the page
 * stays when the menu is drawn again.
 *
 * <pre>
 * private final Pager&lt;Shop&gt; _pager = Pager.rows(0, 5);
 *
 * protected void render(Canvas canvas)
 * {
 *     _pager.render(canvas, shops, shop -&gt; Button.of(icon(shop), click -&gt; open(shop)), 45, 53);
 * }
 * </pre>
 */
public final class Pager<T>
{
    private final int[] _slots;
    private int _page;

    public Pager(int... slots)
    {
        _slots = slots.clone();
    }

    /**
     * Whole rows, the first one 0.
     */
    public static <T> Pager<T> rows(int firstRow, int rowCount)
    {
        int[] slots = new int[rowCount * 9];
        for (int i = 0; i < slots.length; i++)
            slots[i] = firstRow * 9 + i;
        return new Pager<>(slots);
    }

    public int page()
    {
        return _page;
    }

    public void setPage(int page)
    {
        _page = Math.max(0, page);
    }

    public int pageSize()
    {
        return _slots.length;
    }

    public int pageCount(int itemCount)
    {
        return Math.max(1, (itemCount + _slots.length - 1) / _slots.length);
    }

    /**
     * Draws the page's items, and an arrow in each arrow slot when there is a page that way.
     *
     * @param previousSlot slot of the previous page arrow, -1 for none
     * @param nextSlot     slot of the next page arrow, -1 for none
     */
    public void render(Canvas canvas, List<T> items, Function<T, Button> button, int previousSlot, int nextSlot)
    {
        int pages = pageCount(items.size());
        _page = Math.min(_page, pages - 1);

        int first = _page * _slots.length;
        for (int i = 0; i < _slots.length; i++)
        {
            int index = first + i;
            if (index < items.size())
                canvas.set(_slots[i], button.apply(items.get(index)));
            else
                canvas.clear(_slots[i]);
        }

        String pageText = "&7Page " + (_page + 1) + "/" + pages;
        if (previousSlot >= 0 && _page > 0)
            canvas.set(previousSlot, Icon.of("arrow", "&e<- Previous page", pageText), click -> _page--);
        if (nextSlot >= 0 && _page < pages - 1)
            canvas.set(nextSlot, Icon.of("arrow", "&eNext page ->", pageText), click -> _page++);
    }
}
