package me.imu.imusminiquests.Quests;

/**
 * A reward picked for one opening of a quest panel.
 *
 * @param reward the entry to give
 * @param tier   the rarity tier it came from, or null for the quest's own rewards
 */
public record RolledReward(QuestReward reward, RewardTier tier)
{
}
