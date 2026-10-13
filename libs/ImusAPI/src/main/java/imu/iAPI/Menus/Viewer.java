package imu.iAPI.Menus;

import net.kyori.adventure.text.Component;

import java.util.UUID;

/**
 * Who looks at a menu: a player on the server, or a stand-in in tests.
 */
public interface Viewer
{
    UUID id();

    String name();

    boolean hasPermission(String permission);

    void message(Component text);

    /**
     * A message with &amp; colour codes.
     */
    default void message(String legacy)
    {
        message(Text.of(legacy));
    }
}
