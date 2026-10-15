package imu.iAPI.Permissions;

import imu.iAPI.Menus.Viewer;

import java.util.List;

/**
 * What the permission menus need from the server: players, and their permissions. {@link PaperPermissions} on the
 * server, a fake in tests.
 */
public interface PermissionGame
{
    /**
     * The players close to the viewer, nearest first.
     */
    List<PermissionTarget> nearby(Viewer viewer);

    /**
     * An online player, or one who has played here before, by name; null when there is none.
     */
    PermissionTarget find(String name);

    /**
     * Whether the player has the permission now: given here, through another permission or by default.
     */
    boolean has(PermissionTarget target, PermissionInfo permission);

    /**
     * True when given in the menu, false when taken away there, null when left to the plugin's default.
     */
    Boolean grant(PermissionTarget target, String node);

    /**
     * Gives, takes away or resets (null) a permission: saves it and applies it at once if the player is online.
     */
    void setGrant(PermissionTarget target, String node, Boolean value, Viewer by);
}
