package me.imu.imusminiquests.Inventories;

import imu.iAPI.Menus.Icon;
import imu.iAPI.Menus.Viewer;
import me.imu.imusminiquests.Quests.Quest;

import java.util.List;
import java.util.function.Consumer;

/**
 * What the quest menus need of the server. {@link PaperQuestMenuGame} is the real one; tests give a stand-in.
 */
public interface QuestMenuGame
{
    /**
     * Something one opening of a quest gave. take gives it to a player; null for money, which can't be taken.
     */
    record Prize(Icon icon, Consumer<Viewer> take) {}

    /**
     * What one opening gave, and whether command rewards were skipped.
     */
    record Opening(List<Prize> prizes, boolean commands) {}

    /**
     * A random reward tier and its chance in percent.
     */
    record TierChance(String display, double percent) {}

    List<Quest> quests();

    int totalDropWeight();

    /**
     * A quest picked by drop weight, or null when none can drop.
     */
    Quest rollDrop();

    /**
     * The quest's panel as players find it, at its base size.
     */
    Icon panelIcon(Quest quest);

    /**
     * Gives the player a panel of the quest, done already when finished.
     */
    void givePanel(Viewer viewer, Quest quest, boolean finished);

    /**
     * The most luck quest points give.
     */
    double maxLuck();

    /**
     * Opens the quest once without giving anything.
     */
    Opening open(Quest quest, double luck);

    /**
     * Random rewards one opening of the quest gives.
     */
    int randomRolls(Quest quest);

    List<TierChance> tiers(double luck);
}
