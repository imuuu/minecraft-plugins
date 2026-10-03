package me.imu.imusminiquests.Quests;

import me.imu.imusminiquests.Enums.OBJECTIVE_TYPE;

import java.util.Locale;

/**
 * What a quest asks for: a type, the things that count and how many of them.
 */
public record QuestObjective(OBJECTIVE_TYPE type, TargetSet targets, int amount, String text)
{
    /**
     * The objective line for the lore. Uses the configured text when there is one, otherwise
     * builds one like "Break 100 x #logs".
     */
    public String describe()
    {
        if (text != null && !text.isBlank()) return text;

        String what = targets.isAny() || targets.getRawTargets().isEmpty()
                ? "anything"
                : String.join(", ", targets.getRawTargets()).toLowerCase(Locale.ROOT).replace('_', ' ');
        return type.getVerb() + " " + amount + " x " + what;
    }
}
