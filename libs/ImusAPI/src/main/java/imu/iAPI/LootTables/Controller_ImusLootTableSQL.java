package imu.iAPI.LootTables;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import imu.iAPI.Interfaces.ICallBackBoolean;
import imu.iAPI.LootTables.Interfaces.ICallBack;
import imu.iAPI.LootTables.Interfaces.ICallBackLootTable;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Managers.Manager_Database;
import imu.iAPI.SqlTables.LootTableSystem.TableItemStack;
import imu.iAPI.SqlTables.LootTableSystem.TableLootItems;
import imu.iAPI.SqlTables.LootTableSystem.TableLootTable;
import imu.iAPI.Utilities.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller_ImusLootTableSQL
{
    private final Plugin plugin;
    private final Manager_Database manager_database;

    private Dao<TableLootTable, Integer> _tableLootTableDao;
    private Dao<TableLootItems, Integer> _tableLootItemsDao;
    private Dao<TableItemStack, Integer> _tableItemStackDao;

    public Controller_ImusLootTableSQL(Plugin plugin, Manager_Database databaseManager)
    {
        this.plugin = plugin;
        this.manager_database = databaseManager;

        _tableLootTableDao = databaseManager.get_tableLootTableDao();
        _tableLootItemsDao = databaseManager.get_tableLootItemsDao();
        _tableItemStackDao = databaseManager.get_tableItemStackDao();
        //createTablesIfNeeded();
    }

   /* private void createTablesIfNeeded()
    {
        try
        {
            ConnectionSource connectionSource = ImusAPI._instance.getSource();
            TableUtils.createTableIfNotExists(connectionSource, TableItemStack.class);
            TableUtils.createTableIfNotExists(connectionSource, TableLootItems.class);
            TableUtils.createTableIfNotExists(connectionSource, TableLootTable.class);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }*/

    private void addOrUpdateLootTable(TableLootTable lootTable) throws SQLException
    {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("name", lootTable.getName());
        List<TableLootTable> existing = _tableLootTableDao.queryForFieldValues(queryMap);

        if (existing.isEmpty())
        {
            _tableLootTableDao.create(lootTable);
        }
        else
        {
            TableLootTable existingTable = existing.get(0);
            existingTable.setName(lootTable.getName());
            _tableLootTableDao.update(existingTable);
        }
    }

    public void isLootTableExistAsync(String lootTableName, ICallBackBoolean callback)
    {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        {
            try
            {
               /* ConnectionSource connectionSource = ImusAPI._instance.getSource();
                Dao<TableLootTable, Integer> dao = DaoManager.createDao(connectionSource, TableLootTable.class);*/

                Map<String, Object> queryMap = new HashMap<>();
                queryMap.put("name", lootTableName);
                List<TableLootTable> existing = _tableLootTableDao.queryForFieldValues(queryMap);
                boolean isExist = !existing.isEmpty();

                Bukkit.getScheduler().runTask(plugin, () ->
                {
                    if (callback != null)
                    {
                        callback.onCallBack(isExist);
                    }
                });
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        });
    }

    private void addLootTableAsync(TableLootTable lootTable, Runnable callback)
    {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
        {
            try
            {
                /*ConnectionSource connectionSource = ImusAPI._instance.getSource();
                Dao<TableLootTable, Integer> dao = DaoManager.createDao(connectionSource, TableLootTable.class);*/
                _tableLootTableDao.create(lootTable);
                if (callback != null)
                {
                    Bukkit.getScheduler().runTask(plugin, callback);
                }
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        });
    }

    private void addLootTable(TableLootTable lootTable) throws SQLException
    {
       /* ConnectionSource connectionSource = ImusAPI._instance.getSource();
        Dao<TableLootTable, Integer> dao = DaoManager.createDao(connectionSource, TableLootTable.class);*/
        _tableLootTableDao.create(lootTable);
    }

    public void saveImusLootTableAsync(String lootTableName, ImusLootTable lootTable, Runnable callback)
    {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try
            {
                // First, save or get the loot table
                TableLootTable tableLootTable = new TableLootTable();
                tableLootTable.setName(lootTableName);
                addOrUpdateLootTable(tableLootTable); // Save the loot table

                /*ConnectionSource connectionSource = ImusAPI._instance.getSource();
                Dao<TableLootItems, Integer> lootItemsDao = DaoManager.createDao(connectionSource, TableLootItems.class);*/

                for (ILootTableItem<?> item : lootTable.getItems())
                {
                    // Determine the type of the item and handle accordingly
                    if (item.get_value() instanceof ItemStack)
                    {
                        ItemStack stack = (ItemStack) item.get_value();
                        saveItemStack(tableLootTable, item, stack);
                    }
                    else if (item.get_value() instanceof String)
                    {
                        String stringValue = (String) item.get_value();
                        saveStringDetails(tableLootTable, item, stringValue);
                    }
                    // Add more types as needed
                }

                if (callback != null)
                {
                    Bukkit.getScheduler().runTask(plugin, callback);
                }
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        });
    }

    private void saveItemStack(TableLootTable lootTable, ILootTableItem<?> item, ItemStack stack) throws SQLException
    {
        String serializedItemStack = ItemUtils.EncodeItemStack(stack);
        //Dao<TableItemStack, Integer> itemStackDao = DaoManager.createDao(ImusAPI._instance.getSource(), TableItemStack.class);

        Map<String, Object> queryMapForItemStack = new HashMap<>();
        queryMapForItemStack.put("item", serializedItemStack);
        List<TableItemStack> existingItemStacks = _tableItemStackDao.queryForFieldValues(queryMapForItemStack);

        int itemStackId;
        if (existingItemStacks.isEmpty())
        {
            // ItemStack doesn't exist, create new record
            TableItemStack tableItemStack = new TableItemStack();
            tableItemStack.setDisplay_name(ItemUtils.GetDisplayName(stack));
            tableItemStack.setItem(serializedItemStack);
            _tableItemStackDao.create(tableItemStack);
            itemStackId = tableItemStack.getId();
        }
        else
        {
            Bukkit.getLogger().info("ItemStack already exists in the database. STACK: " + stack);
            return;
        }

        TableLootItems tableLootItem = new TableLootItems();
        tableLootItem.setLootTable(lootTable);
        tableLootItem.setType(LOOT_TYPE.ITEM_STACK);
        tableLootItem.setWeight(item.get_weight());
        tableLootItem.setMinAmount(item.get_minAmount());
        tableLootItem.setMaxAmount(item.get_maxAmount());
        tableLootItem.setLoot_item_id(itemStackId);
        _tableLootItemsDao.create(tableLootItem);
    }

    private void updateItemStack(ItemStack newStack, String oldSerializedItemStack) throws SQLException
    {
        Dao<TableItemStack, Integer> itemStackDao = DaoManager.createDao(ImusAPI._instance.getSource(), TableItemStack.class);
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("item", oldSerializedItemStack);
        List<TableItemStack> existingItemStacks = itemStackDao.queryForFieldValues(queryMap);

        if (!existingItemStacks.isEmpty())
        {
            // Found the existing ItemStack, now update it
            TableItemStack existingItemStack = existingItemStacks.get(0);
            existingItemStack.setDisplay_name(ItemUtils.GetDisplayName(newStack));
            existingItemStack.setItem(ItemUtils.EncodeItemStack(newStack)); // Serialize the new ItemStack
            itemStackDao.update(existingItemStack);
        }
        else
        {
            Bukkit.getLogger().info("ItemStack to be updated not found in the database.");
        }
    }

    private boolean updateItemStackDetails(ItemStack stack, Integer minAmount, Integer maxAmount, Integer weight, ICallBack callback) throws SQLException
    {
        String serializedItemStack = ItemUtils.EncodeItemStack(stack);
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("item", serializedItemStack);
        List<TableItemStack> existingItemStacks = _tableItemStackDao.queryForFieldValues(queryMap);

        // Check if the item stack exists
        if (!existingItemStacks.isEmpty())
        {
            TableItemStack existingItemStack = existingItemStacks.get(0);

            // Update the TableLootItems associated with this item stack
            Map<String, Object> lootItemsQueryMap = new HashMap<>();
            lootItemsQueryMap.put("loot_item_id", existingItemStack.getId());
            List<TableLootItems> relatedLootItems = _tableLootItemsDao.queryForFieldValues(lootItemsQueryMap);

            for (TableLootItems lootItem : relatedLootItems)
            {
                if (minAmount != null)
                {
                    lootItem.setMinAmount(minAmount);
                }
                if (maxAmount != null)
                {
                    lootItem.setMaxAmount(maxAmount);
                }
                if (weight != null)
                {
                    lootItem.setWeight(weight);
                }
                _tableLootItemsDao.update(lootItem);
            }


            return true;
        }
        else
        {
            Bukkit.getLogger().info("[imusAPI/Lootables]ItemStack not found in the database");
            //Bukkit.getScheduler().runTask(plugin, () -> callback.onComplete(false));
            return false;
        }
    }

    public void updateItemStackDetailsAsync(ItemStack stack, Integer minAmount, Integer maxAmount, Integer weight, ICallBack callback)
    {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success;
            try
            {
                success = updateItemStackDetails(stack, minAmount, maxAmount, weight, callback);

            } catch (SQLException e)
            {
                e.printStackTrace();
                success = false;

            }

            final boolean finalSuccess = success;
            Bukkit.getScheduler().runTask(plugin, () -> callback.onComplete(finalSuccess));
        });
    }

    private boolean removeItemStackIfExists(ItemStack stack) throws SQLException
    {
        String serializedItemStack = ItemUtils.EncodeItemStack(stack);
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("item", serializedItemStack);
        List<TableItemStack> existingItemStacks = _tableItemStackDao.queryForFieldValues(queryMap);

        if (!existingItemStacks.isEmpty())
        {
            TableItemStack existingItemStack = existingItemStacks.get(0);

            // Remove related loot items
            Map<String, Object> lootItemsQueryMap = new HashMap<>();
            lootItemsQueryMap.put("loot_item_id", existingItemStack.getId());
            List<TableLootItems> relatedLootItems = _tableLootItemsDao.queryForFieldValues(lootItemsQueryMap);

            for (TableLootItems lootItem : relatedLootItems)
            {
                _tableLootItemsDao.delete(lootItem);
            }

            _tableItemStackDao.delete(existingItemStack);

            return true;
        }
        else
        {
            return false;
        }
    }

    public void removeItemStackIfExistsAsync(ItemStack stack, ICallBack callback)
    {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success;
            try
            {
                success = removeItemStackIfExists(stack);
            } catch (SQLException e)
            {
                e.printStackTrace();
                success = false;
            }

            final boolean finalSuccess = success;
            Bukkit.getScheduler().runTask(plugin, () -> callback.onComplete(finalSuccess));
        });
    }


    public void removeLootTableByName(String lootTableName) throws SQLException
    {
        ConnectionSource connectionSource = ImusAPI._instance.getSource();
        Dao<TableLootTable, Integer> lootTableDao = DaoManager.createDao(connectionSource, TableLootTable.class);

        List<TableLootTable> lootTables = lootTableDao.queryForEq("name", lootTableName);
        if (lootTables.isEmpty())
        {
            Bukkit.getLogger().info("Loot table with name " + lootTableName + " not found.");
            return;
        }

        int lootTableId = lootTables.get(0).getId();
        removeLootTableById(lootTableId);
    }

    private void removeLootTableById(int lootTableId) throws SQLException
    {
        ConnectionSource connectionSource = ImusAPI._instance.getSource();
        Dao<TableLootItems, Integer> lootItemsDao = DaoManager.createDao(connectionSource, TableLootItems.class);
        Dao<TableItemStack, Integer> itemStackDao = DaoManager.createDao(connectionSource, TableItemStack.class);
        Dao<TableLootTable, Integer> lootTableDao = DaoManager.createDao(connectionSource, TableLootTable.class);

        List<TableLootItems> relatedLootItems = lootItemsDao.queryForEq("loot_table_id", lootTableId);

        for (TableLootItems lootItem : relatedLootItems)
        {
            itemStackDao.deleteById(lootItem.getLoot_item_id());
        }

        DeleteBuilder<TableLootItems, Integer> deleteLootItems = lootItemsDao.deleteBuilder();
        deleteLootItems.where().eq("loot_table_id", lootTableId);
        deleteLootItems.delete();
        lootTableDao.deleteById(lootTableId);
    }

    public void createLootTableFromNameAsync(String lootTableName, ICallBackLootTable callback)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                try
                {
                    ImusLootTable imusLootTable = createLootTableFromName(lootTableName);
                    if (callback != null)
                    {
                        Bukkit.getScheduler().runTask(plugin, () -> callback.onCallBack(imusLootTable));
                    }
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);


    }

    private ImusLootTable createLootTableFromName(String lootTableName) throws SQLException
    {
        ConnectionSource connectionSource = ImusAPI._instance.getSource();
        Dao<TableLootTable, Integer> lootTableDao = DaoManager.createDao(connectionSource, TableLootTable.class);
        Dao<TableLootItems, Integer> lootItemsDao = DaoManager.createDao(connectionSource, TableLootItems.class);
        Dao<TableItemStack, Integer> itemStackDao = DaoManager.createDao(connectionSource, TableItemStack.class);

        // Find the loot table by name
        List<TableLootTable> lootTables = lootTableDao.queryForEq("name", lootTableName);
        if (lootTables.isEmpty())
        {
            Bukkit.getLogger().info("Loot table with name " + lootTableName + " not found.");
            return null;
        }

        // Get the first matched loot table (assuming names are unique)
        TableLootTable tableLootTable = lootTables.get(0);

        // Retrieve all associated loot items
        List<TableLootItems> lootItems = lootItemsDao.queryForEq("loot_table_id", tableLootTable.getId());

        ImusLootTable imusLootTable = new ImusLootTable(); // Replace Object with your item type if needed
        for (TableLootItems lootItem : lootItems)
        {
            if (lootItem.getType() == LOOT_TYPE.ITEM_STACK)
            {
                TableItemStack itemStack = itemStackDao.queryForId(lootItem.getLoot_item_id());
                if (itemStack != null)
                {
                    ItemStack stack = ItemUtils.DecodeItemStack(itemStack.getItem());
                    LootItemStack lootItemStack = new LootItemStack(stack, lootItem.getWeight(), lootItem.getMinAmount(), lootItem.getMaxAmount());
                    imusLootTable.add(lootItemStack);
                }
            }

        }

        return imusLootTable;
    }


    private void saveStringDetails(TableLootTable tableLootTable, ILootTableItem<?> item,
                                   String stringValue) throws SQLException
    {
        // Logic to save String details to the database
        // ...
    }


    // Other methods (getLootTable, removeLootTable, etc.) would follow a similar pattern
}
