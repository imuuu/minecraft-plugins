package me.imu.imusminiquests.Enums;

/**
 * What a quest counts. A new type needs a constant here, a listener in ManagerQuestProgress that
 * calls addProgress, and a verb for the generated lore text.
 */
public enum OBJECTIVE_TYPE
{
    /** Targets are block materials. Crops only count when fully grown. */
    BREAK_BLOCK("Break", false),
    /** Targets are entity types. */
    KILL_ENTITY("Kill", true),
    /** Targets are item materials of what was caught. */
    FISH("Catch", false);

    private final String _verb;
    private final boolean _targetsEntities;

    OBJECTIVE_TYPE(String verb, boolean targetsEntities)
    {
        _verb = verb;
        _targetsEntities = targetsEntities;
    }

    public String getVerb() {return _verb;}

    public boolean targetsEntities() {return _targetsEntities;}
}
