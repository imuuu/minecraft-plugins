package imu.waystones;

import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Viewer;
import imu.waystones.engine.Cost;
import imu.waystones.engine.Panel;
import imu.waystones.engine.Rules;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * What the waystone menus need of the server. {@link PaperWaystoneGame} is the real one; tests give a stand-in.
 */
interface WaystoneGame
{
    /**
     * A message of config.yml with its {placeholders} filled, &amp; colour codes.
     */
    String text(String key, Object... placeholders);

    /**
     * A list of lines under messages in config.yml.
     */
    List<String> textList(String key);

    Rules rules();

    Collection<Waystone> all();

    /**
     * The waystone, or null when it is gone.
     */
    Waystone get(UUID id);

    /**
     * Where the player can go from the waystone, in menu order.
     */
    List<Waystone> destinations(Waystone from, UUID player);

    /**
     * How long until the player can leave from the waystone again, in milliseconds.
     */
    long cooldownLeft(Viewer viewer, Waystone from);

    /**
     * The waystone's own icon item, without its name.
     */
    Icon icon(Waystone ws);

    void save(Waystone ws);

    void savePanel(Waystone ws, UUID player);

    void remove(Waystone ws);

    /**
     * Starts the trip from one waystone to another, or tells the player why not.
     */
    void travel(Viewer viewer, Waystone from, Waystone to);

    /**
     * Takes the player there at once.
     *
     * @return false when the waystone's world isn't loaded
     */
    boolean teleport(Viewer viewer, Waystone to);

    /**
     * Takes the items when the player has all of them.
     */
    boolean take(Viewer viewer, List<Cost> cost);

    /**
     * Gives the player an item holding the upgrades.
     */
    void giveUpgrades(Viewer viewer, Panel panel);

    /**
     * @param sound e.g. "entity.villager.no"
     */
    void sound(Viewer viewer, String sound, float volume, float pitch);

    /**
     * Sends a message of config.yml; an empty one isn't sent.
     */
    default void send(Viewer viewer, String key, Object... placeholders)
    {
        String text = text(key, placeholders);
        if (!text.isEmpty())
            viewer.message(text);
    }
}
