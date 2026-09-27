package imu.iAPI.LootTables;

import imu.iAPI.LootTables.Interfaces.ICallBack;
import imu.iAPI.Managers.Manager_Database;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Manager_ImusLootTable
{
    private HashMap<String, ImusLootTable> _lootTables = new HashMap<>();
    private static Manager_ImusLootTable _instance;
    public static Manager_ImusLootTable getInstance()
    {
        return _instance;
    }

    private final Controller_ImusLootTableSQL controller;
    public Manager_ImusLootTable(Plugin plugin, Manager_Database databaseManager)
    {
        _instance = this;
        controller = new Controller_ImusLootTableSQL(plugin, databaseManager);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                initTestTables();
            }
        }.runTaskLater(plugin, 20 * 10);

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                initTestCreateTable();
            }
        }.runTaskLater(plugin, 20 * 15);

       /* new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Bukkit.getLogger().info("=============> remove test loot table");
                    controller.removeLootTableByName("test");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }.runTaskLater(plugin, 20 * 60);*/
    }

    private void initTestCreateTable()
    {
        System.out.println("=============> init test create table");
        System.out.println("=============> init test create table");
        System.out.println("=============> init test create table");
        controller.createLootTableFromNameAsync("test", imusLootTable ->
        {
            System.out.println("==========================> test loot table created");
            System.out.println("==========================> test loot table created");
            System.out.println("==========================> test loot table created");
            imusLootTable.printLoot();
            _lootTables.put("test", imusLootTable);

        });
    }
    private void initTestTables()
    {
        System.out.println("=============> init test loot tables");
        System.out.println("=============> init test loot tables");
        System.out.println("=============> init test loot tables");
        ImusLootTable lootTable = new ImusLootTable();
        lootTable.add(new LootItemStack(Material.STONE, 100, 1, 64));
        lootTable.add(new LootItemStack(Material.DIRT, 100, 1, 64));
        lootTable.add(new LootItemStack(Material.COBBLESTONE, 100, 1, 64));
        lootTable.add(new LootItemStack(Material.GRASS_BLOCK, 100, 1, 64));
        lootTable.add(new LootItemStack(Material.GRAVEL, 100, 1, 64));
        lootTable.add(new LootItemStack(Material.SAND, 100, 1, 64));
        lootTable.add(new LootItemStack(Material.SANDSTONE, 100, 1, 64));

        lootTable.add(new LootItemStack(Material.DIAMOND, 100, 1, 64));
        lootTable.add(new LootItemStack(Material.DIAMOND_BLOCK, 100, 1, 64));

        controller.saveImusLootTableAsync("test", lootTable, () ->
        {
            System.out.println("==========================> test loot table saved");
            System.out.println("==========================> test loot table saved");
            System.out.println("==========================> test loot table saved");
            //_lootTables.put("test", lootTable);
        });
    }

    public void addLootTableAsync(String name, ImusLootTable lootTable)
    {
        controller.isLootTableExistAsync(name, (exist) ->
        {
            if(!exist)
            {
                controller.saveImusLootTableAsync(name, lootTable, () ->
                {
                    System.out.println("==========================> loot table saved");
                    System.out.println("==========================> loot table saved");
                    System.out.println("==========================> loot table saved");
                });
                _lootTables.put(name, lootTable);
            }


        });

    }


    public void updateItemStackAsync(ItemStack stack,Integer minAmount, Integer maxAmount, Integer weight, ICallBack callback)
    {
        controller.updateItemStackDetailsAsync(stack, minAmount, maxAmount, weight, callback);
    }

    public void removeItemStackAsync(ItemStack stack, ICallBack callback)
    {
        controller.removeItemStackIfExistsAsync(stack, callback);
    }
    public HashMap<String, ImusLootTable> get_lootTables()
    {
        return _lootTables;
    }

    public ImusLootTable get_lootTable(String name)
    {
        return _lootTables.get(name);
    }


}
