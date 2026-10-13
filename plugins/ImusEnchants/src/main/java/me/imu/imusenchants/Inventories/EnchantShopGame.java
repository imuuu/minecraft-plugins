package me.imu.imusenchants.Inventories;

import imu.iAPI.Enums.ENCHANTMENT_TIER;
import imu.iAPI.Enums.ITEM_CATEGORY;
import imu.iAPI.Menus.Viewer;

/**
 * What the enchant shop needs of the server. {@link PaperEnchantShopGame} is the real one; tests give a stand-in.
 */
public interface EnchantShopGame
{
    int level(Viewer viewer);

    /**
     * Creative players buy without levels.
     */
    boolean isCreative(Viewer viewer);

    void setLevel(Viewer viewer, int level);

    /**
     * A random number from 0 up to 1.
     */
    double roll();

    /**
     * Gives a book of the category: sometimes a custom enchant, otherwise a vanilla one of the tier.
     *
     * @return false when no enchant of the kind is switched on
     */
    boolean giveBook(Viewer viewer, ENCHANTMENT_TIER tier, ITEM_CATEGORY category);

    /**
     * Gives a booster with that many power directions.
     */
    void giveBooster(Viewer viewer, int directions);

    /**
     * Opens the enchanting table again.
     */
    void openTable(Viewer viewer);
}
