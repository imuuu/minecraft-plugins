package me.imu.imuschallenges.Inventories;

import imu.iAPI.Menus.Canvas;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Menu;
import imu.iAPI.Menus.Viewer;
import me.imu.imuschallenges.CONSTANTS;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The challenge shop: a special side (a column on the left) and a normal side (a block on the right). A player
 * buys one item a side per refresh with challenge points, and more slots with money: click a slot for sale, then
 * right-click it to confirm.
 */
public class InventoryChallengeShop extends Menu
{
    static final int SPECIAL_START = 1 + 9;
    static final int NORMAL_START = 3 + 9 + 9;

    private final ShopMenuGame _game;
    private List<ShopMenuGame.Tier> _tiers;
    // Slots for sale clicked once, waiting for the right click
    private final Set<Integer> _armed = new HashSet<>();

    public InventoryChallengeShop(ShopMenuGame game)
    {
        super("&6Challenge Shop", 6);
        _game = game;
    }

    /**
     * The menu slot of a side's item.
     */
    static int slot(boolean special, int index)
    {
        return special
                ? SPECIAL_START + index * 9
                : NORMAL_START + index % CONSTANTS.NORMAL_SLOT_COLUMNS + index / CONSTANTS.NORMAL_SLOT_COLUMNS * 9;
    }

    private static int size(boolean special)
    {
        return special ? CONSTANTS.SPECIAL_SLOTS : CONSTANTS.NORMAL_SLOT_COLUMNS * CONSTANTS.NORMAL_SLOT_ROWS;
    }

    @Override
    protected void onOpen(Viewer viewer)
    {
        _game.opened(viewer);
        if (_tiers == null)
        {
            _game.load(viewer, tiers ->
            {
                _tiers = tiers;
                refresh();
            });
        }
    }

    @Override
    protected void onClose(Viewer viewer)
    {
        _game.closed(viewer);
    }

    @Override
    protected void render(Canvas canvas)
    {
        canvas.fill(Icon.filler(Material.BLACK_STAINED_GLASS_PANE));
        Icon special = Icon.of(Material.BLUE_STAINED_GLASS_PANE, "&6&lResets in &c&l" + _game.resetsIn(true));
        canvas.set(1, special);
        canvas.set(1 + 9 * 5, special);
        Icon normal = Icon.of(Material.BLUE_STAINED_GLASS_PANE, "&9&lResets in &c&l" + _game.resetsIn(false));
        for (int i = 0; i < CONSTANTS.NORMAL_SLOT_COLUMNS; i++)
        {
            canvas.set(NORMAL_START - 9 + i, normal);
            canvas.set(NORMAL_START - 9 + 9 * 3 + i, normal);
        }

        if (_tiers != null)
        {
            for (ShopMenuGame.Tier tier : _tiers)
                renderTier(canvas, tier);
        }
    }

    private void renderTier(Canvas canvas, ShopMenuGame.Tier tier)
    {
        boolean special = tier.special();
        if (tier.closed())
        {
            for (int i = 0; i < size(special); i++)
                canvas.set(slot(special, i), Icon.filler(Material.RED_STAINED_GLASS_PANE));
            return;
        }

        String tierName = special ? "&6Special" : "&2Normal";
        for (int i = 0; i < size(special); i++)
        {
            int slot = slot(special, i);
            int index = i;
            if (i >= tier.items().size())
                canvas.set(slot, Icon.filler(Material.PURPLE_STAINED_GLASS_PANE));
            else if (i < tier.boughtSlots())
            {
                Icon item = tier.items().get(i).addLore(" ", "&a&lCost: " + tier.costs().get(i) + " &4Challenge points");
                canvas.set(slot, item, click ->
                {
                    if (_game.buyItem(click.viewer(), special, index))
                        click.close();
                });
            }
            else
            {
                boolean armed = _armed.contains(slot);
                String name = "&c&lBuy " + tierName + " &9slot" + (armed ? " &6&k#&r &e(Confirm by &bM2)" : "");
                Icon forSale = Icon.of(Material.PURPLE_STAINED_GLASS_PANE, name, " ", "&aCost: " + tier.slotPrice() + " &2$").glow(armed);
                canvas.set(slot, forSale, click ->
                {
                    if (click.isLeft())
                        _armed.add(slot);
                    else if (click.isRight() && _armed.contains(slot) && _game.buySlot(click.viewer(), special, tier.nextSlot()))
                        click.close();
                });
            }
        }
    }

    /**
     * Both sides as loaded, for tests.
     */
    List<ShopMenuGame.Tier> tiers()
    {
        return _tiers == null ? List.of() : new ArrayList<>(_tiers);
    }
}
