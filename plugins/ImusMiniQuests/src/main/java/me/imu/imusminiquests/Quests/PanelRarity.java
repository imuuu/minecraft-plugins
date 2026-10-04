package me.imu.imusminiquests.Quests;

/**
 * The rarity a quest panel rolls when it is made, like ScrollQuest's scrolls. A rarer panel asks
 * for more but gives more: its task is bigger, opening it gives extra luck and extra random
 * rewards.
 *
 * @param name               the key in config.yml rarities
 * @param display            coloured name, colours translated
 * @param weight             how often a new panel gets this rarity
 * @param amountMultiplier   the quest's amount is multiplied by this (1.5 = 100 logs becomes 150)
 * @param bonusLuck          added to the player's luck when the panel is opened
 * @param extraRandomRewards added to the quest's random rewards from rewards.yml
 */
public record PanelRarity(String name, String display, int weight, double amountMultiplier, double bonusLuck,
                          int extraRandomRewards)
{
    /** The panel's task size for a quest whose base amount is this. */
    public int scale(int baseAmount)
    {
        return Math.max(1, (int) Math.round(baseAmount * amountMultiplier));
    }
}
