package me.imu.imusminiquests;

public final class CONSTANTS
{
    private CONSTANTS() {}

    // Permissions
    public static final String PERM_GIVE = "imq.give";
    public static final String PERM_MENU = "imq.menu";
    public static final String PERM_ECONOMY = "imq.economy";
    public static final String PERM_POINTS = "imq.points";
    public static final String PERM_POINTS_OTHERS = "imq.points.others";
    public static final String PERM_UNLOCKS = "imq.unlocks";
    public static final String PERM_UNLOCKS_EXEMPT = "imq.unlocks.exempt";
    public static final String PERM_UNLOCKS_ADMIN = "imq.unlocks.admin";
    public static final String PERM_LIST = "imq.list";
    public static final String PERM_COMPLETE = "imq.complete";
    public static final String PERM_RELOAD = "imq.reload";
    public static final String PERM_CONFIG = "imq.config";
    public static final String PERM_TAB_COMPLETER = "imq.tabcompleter";

    // Persistent data keys on the quest item, in this plugin's namespace
    public static final String KEY_QUEST_ID = "quest_id";
    public static final String KEY_PROGRESS = "progress";
    // On the player: how many quests they have claimed
    public static final String KEY_QUEST_POINTS = "quest_points";

    // Prefix of the chunk data key that marks a block a player placed
    public static final String KEY_PLACED_BLOCK_PREFIX = "placed_";
}
