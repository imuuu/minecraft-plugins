package me.imu.imusenchants.Inventories;

import imu.iAPI.Config.ConfigMenu;
import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Viewer;

import java.util.List;

/**
 * What /ien admin needs of the server. {@link PaperEnchantAdminGame} is the real one; tests give a stand-in.
 */
public interface EnchantAdminGame
{
    /**
     * An enchant in the menu.
     *
     * @param key     unique, e.g. "vanilla:sharpness" or "custom:lifesteal"
     * @param book    how it shows while enabled
     * @param color   colour code of the name while enabled
     * @param details lore lines under the name
     */
    record Entry(String key, Icon book, String name, String color, List<String> details) {}

    List<Entry> vanilla();

    List<Entry> custom();

    boolean isEnabled(String key);

    void setEnabled(String key, boolean enabled);

    /**
     * Gives the enchant's book at its highest level.
     */
    void giveBook(Viewer viewer, String key);

    /**
     * A Slot Core as it shows in the menu.
     */
    Icon coreIcon();

    void giveCores(Viewer viewer, int amount);

    /**
     * ImusEnchants' config.yml settings, or null.
     */
    ConfigMenu settings();
}
