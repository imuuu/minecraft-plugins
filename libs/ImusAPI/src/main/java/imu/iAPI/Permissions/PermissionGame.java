package imu.iAPI.Permissions;

import imu.iAPI.Menus.Viewer;
import org.bukkit.permissions.PermissionDefault;

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
     * Who has the permission without being given it, now: a plugin may change it while running (ImusChallenges'
     * first advancement race), so plugin.yml's default is only where it starts.
     */
    default PermissionDefault defaultOf(PermissionInfo permission)
    {
        return permission.byDefault();
    }

    boolean isOp(Viewer viewer);

    /**
     * Whether the player may change permissions themselves; only ops change theirs.
     */
    default boolean managesPermissions(PermissionTarget target)
    {
        return has(target, PermissionInfo.of(PaperPermissions.USE_PERMISSION, "", PermissionLevel.VERY_HIGH));
    }

    /**
     * True when given in the menu, false when taken away there, null when left to the plugin's default.
     */
    Boolean grant(PermissionTarget target, String node);

    /**
     * Gives, takes away or resets (null) a permission: saves it and applies it at once if the player is online.
     */
    void setGrant(PermissionTarget target, String node, Boolean value, Viewer by);
}
