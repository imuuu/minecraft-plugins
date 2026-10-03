package me.imu.imusminiquests;

public final class CONSTANTS
{
    private CONSTANTS() {}

    // Permissions
    public static final String PERM_GIVE = "imq.give";
    public static final String PERM_LIST = "imq.list";
    public static final String PERM_COMPLETE = "imq.complete";
    public static final String PERM_RELOAD = "imq.reload";
    public static final String PERM_CONFIG = "imq.config";
    public static final String PERM_TAB_COMPLETER = "imq.tabcompleter";

    // Persistent data keys on the quest item, in this plugin's namespace
    public static final String KEY_QUEST_ID = "quest_id";
    public static final String KEY_PROGRESS = "progress";

    // Prefix of the chunk data key that marks a block a player placed
    public static final String KEY_PLACED_BLOCK_PREFIX = "placed_";
}
