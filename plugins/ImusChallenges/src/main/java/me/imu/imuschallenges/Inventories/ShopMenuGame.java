package me.imu.imuschallenges.Inventories;

import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Viewer;

import java.util.List;
import java.util.function.Consumer;

/**
 * What the challenge shop menu needs of the server. {@link PaperShopMenuGame} is the real one; tests give a
 * stand-in.
 */
public interface ShopMenuGame
{
    /**
     * One side of the shop as the player sees it.
     *
     * @param closed      the player bought from it already, or their slots couldn't be read
     * @param boughtSlots slots the player can buy from; the rest are for sale
     * @param nextSlot    the number of the next slot for sale, counted past the free ones
     * @param slotPrice   what that slot costs
     */
    record Tier(boolean special, boolean closed, List<Icon> items, List<Integer> costs, int boughtSlots, int nextSlot, int slotPrice) {}

    /**
     * How long until the side gets new items, e.g. "2h 5m".
     */
    String resetsIn(boolean special);

    /**
     * Reads the player's slots and gives both sides, special first, on the main thread.
     */
    void load(Viewer viewer, Consumer<List<Tier>> done);

    /**
     * Buys the item for points; the points are taken later.
     *
     * @return true when the purchase went ahead and the menu should close
     */
    boolean buyItem(Viewer viewer, boolean special, int index);

    /**
     * Buys one more slot for money.
     *
     * @param nextSlot {@link Tier#nextSlot()}, which sets the price
     * @return true when it was bought and the menu should close
     */
    boolean buySlot(Viewer viewer, boolean special, int nextSlot);

    void opened(Viewer viewer);

    void closed(Viewer viewer);
}
