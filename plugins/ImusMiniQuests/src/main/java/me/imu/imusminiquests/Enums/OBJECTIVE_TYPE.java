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
    FISH("Catch", false),
    /** Targets are item materials of the crafted result. Counts every item made, shift-click too. */
    CRAFT("Craft", false),
    /** Targets are item materials taken out of a furnace, blast furnace or smoker. */
    SMELT("Smelt", false),
    /** Targets are item materials enchanted at an enchanting table. */
    ENCHANT("Enchant", false),
    /** Targets are entity types of the animals bred. */
    BREED("Breed", true),
    /** Targets are item materials bought from villagers and wandering traders. */
    TRADE("Trade for", false),
    /** Targets are item materials eaten or drunk. */
    EAT("Eat", false),
    /** Targets are entity types tamed (wolves, cats, horses, parrots...). */
    TAME("Tame", true),
    /** Targets are entity types sheared (sheep, mooshrooms, snow golems...). */
    SHEAR("Shear", true);

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
