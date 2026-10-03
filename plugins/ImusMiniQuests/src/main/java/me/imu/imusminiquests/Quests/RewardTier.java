package me.imu.imusminiquests.Quests;

import java.util.List;

/**
 * One rarity tier of the shared random rewards in rewards.yml, such as common or epic.
 *
 * @param name     the key in rewards.yml
 * @param display  coloured name, e.g. "&5Epic" with colours translated
 * @param weight   how often this tier is picked, against the other tiers
 * @param announce tell the whole server when someone gets a reward from this tier
 * @param rewards  the entries, picked by their own weights
 */
public record RewardTier(String name, String display, int weight, boolean announce, List<QuestReward> rewards)
{
}
