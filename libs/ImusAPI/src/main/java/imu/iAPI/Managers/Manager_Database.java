package imu.iAPI.Managers;


import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import imu.iAPI.Interfaces.IHasSqlSource;
import imu.iAPI.SqlTables.LootTableSystem.TableItemStack;
import imu.iAPI.SqlTables.LootTableSystem.TableLootItems;
import imu.iAPI.SqlTables.LootTableSystem.TableLootTable;

import java.sql.SQLException;

public class Manager_Database
{
    private Dao<TableLootTable, Integer> _tableLootTableDao;
    private Dao<TableLootItems, Integer> _tableLootItemsDao;
    private Dao<TableItemStack, Integer> _tableItemStackDao;

    public Manager_Database(IHasSqlSource source) throws SQLException
    {
        ConnectionSource connectionSource = source.getSource();

        TableUtils.createTableIfNotExists(connectionSource, TableLootTable.class);
        TableUtils.createTableIfNotExists(connectionSource, TableLootItems.class);
        TableUtils.createTableIfNotExists(connectionSource, TableItemStack.class);

        _tableLootTableDao = DaoManager.createDao(connectionSource, TableLootTable.class);
        _tableLootItemsDao = DaoManager.createDao(connectionSource, TableLootItems.class);
        _tableItemStackDao = DaoManager.createDao(connectionSource, TableItemStack.class);
    }

    public Dao<TableLootTable, Integer> get_tableLootTableDao()
    {
        return _tableLootTableDao;
    }

    public Dao<TableLootItems, Integer> get_tableLootItemsDao()
    {
        return _tableLootItemsDao;
    }

    public Dao<TableItemStack, Integer> get_tableItemStackDao()
    {
        return _tableItemStackDao;
    }
}
