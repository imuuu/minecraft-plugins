package me.imu.imuschallenges.Interfaces;

import me.imu.imuschallenges.Database.Tables.TablePlayerShopStats;

@FunctionalInterface
public interface ShopStatsCallback
{
    public void onShopStatsRetrieved(TablePlayerShopStats shopStats);
}
