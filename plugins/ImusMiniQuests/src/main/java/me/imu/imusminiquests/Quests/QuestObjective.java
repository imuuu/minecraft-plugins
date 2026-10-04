package me.imu.imusminiquests.Quests;

import me.imu.imusminiquests.Enums.OBJECTIVE_TYPE;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * What a quest asks for: a type, the things that count and how many of them. With a time
 * limit (seconds, 0 = none) the whole amount has to be done within that time, or the count
 * starts over.
 */
public record QuestObjective(OBJECTIVE_TYPE type, TargetSet targets, int amount, String text, int timeLimitSeconds)
{
    /** The objective line for the quest's own amount. */
    public String describe()
    {
        return describe(amount);
    }

    /**
     * The objective line for a panel that needs this many (rarer panels need more). Uses the
     * configured text when there is one: %amount% in it is replaced, and so is the quest's own
     * amount written as a number ("Chop 100 logs" becomes "Chop 150 logs"). Otherwise builds one
     * like "Break 100 x #logs".
     */
    public String describe(int panelAmount)
    {
        if (text != null && !text.isBlank())
        {
            if (text.contains("%amount%")) return text.replace("%amount%", String.valueOf(panelAmount));
            Matcher matcher = Pattern.compile("\\b" + amount + "\\b").matcher(text);
            return matcher.find() ? matcher.replaceFirst(String.valueOf(panelAmount)) : text;
        }

        String what = targets.isAny() || targets.getRawTargets().isEmpty()
                ? "anything"
                : String.join(", ", targets.getRawTargets()).toLowerCase(Locale.ROOT).replace('_', ' ');
        return type.getVerb() + " " + panelAmount + " x " + what;
    }
}
