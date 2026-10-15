package imu.iAPI.Permissions;

import imu.iAPI.Menus.Icon;

import java.util.UUID;

/**
 * A player whose permissions the menu shows, online or not.
 *
 * @param icon their head, with what the menu should say about them (e.g. how far they are)
 */
public record PermissionTarget(UUID id, String name, boolean op, boolean online, Icon icon)
{
}
