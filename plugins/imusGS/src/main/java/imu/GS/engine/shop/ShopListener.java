package imu.GS.engine.shop;

/**
 * Told about changes to a shop, e.g. by the open shop menus so they can redraw. Runs on the thread that changed
 * the shop, which is always the main thread on the server.
 */
public interface ShopListener
{
    /**
     * The amount or price of a listing changed; it is still in the same slot.
     */
    default void listingChanged(Shop shop, Listing listing) {}

    /**
     * Listings were added, removed or moved to other slots, or pages were added or removed.
     */
    default void layoutChanged(Shop shop) {}

    /**
     * The shop's name or settings changed.
     */
    default void settingsChanged(Shop shop) {}
}
