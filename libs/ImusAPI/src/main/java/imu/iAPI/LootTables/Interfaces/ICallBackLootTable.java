package imu.iAPI.LootTables.Interfaces;

import imu.iAPI.LootTables.ImusLootTable;

@FunctionalInterface
public interface ICallBackLootTable
{
    public void onCallBack(ImusLootTable lootTable);
}
