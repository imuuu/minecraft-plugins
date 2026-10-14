package imu.iAPI.Database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Copies every row of one database into another of the same plugin, e.g. SQLite to MySQL. The plugin has made its
 * tables in the target first; rows go into the tables and columns both have, ids and all.
 */
public final class DatabaseCopy
{
    private static final int BATCH = 500;
    private static final DateTimeFormatter SQLITE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DatabaseCopy() {}

    /**
     * @param copied false when nothing was copied (the target had data, or the source had nothing)
     * @param report lines for the console, one per table
     */
    public record Result(boolean copied, List<String> report) {}

    public static Result copy(Database source, Database target) throws SQLException
    {
        List<String> report = new ArrayList<>();
        try (Connection from = source.getConnection(); Connection to = target.getConnection())
        {
            Map<String, String> sourceTables = tables(from);
            Map<String, String> targetTables = tables(to);

            // Never mix data: the target must be empty
            for (String table : targetTables.values())
            {
                if (count(to, table) > 0)
                {
                    report.add(target.describe() + " has data already (" + table + "); nothing copied");
                    return new Result(false, report);
                }
            }

            boolean any = false;
            to.setAutoCommit(false);
            try
            {
                foreignKeys(to, target, false);
                for (Map.Entry<String, String> entry : targetTables.entrySet())
                {
                    String sourceTable = sourceTables.get(entry.getKey());
                    if (sourceTable == null)
                        continue;
                    int rows = copyTable(from, sourceTable, to, entry.getValue(), target.isSqlite());
                    any |= rows > 0;
                    report.add("Copied " + rows + " rows of " + entry.getValue() + ", " + count(to, entry.getValue()) + " there now, "
                            + count(from, sourceTable) + " in " + source.describe());
                }
                foreignKeys(to, target, true);
                to.commit();
            } catch (SQLException e)
            {
                to.rollback();
                throw e;
            } finally
            {
                to.setAutoCommit(true);
            }
            return new Result(any, report);
        }
    }

    /**
     * The tables, by lower case name (MySQL on Windows lower-cases them) to the name as it is.
     */
    private static Map<String, String> tables(Connection con) throws SQLException
    {
        Map<String, String> tables = new LinkedHashMap<>();
        DatabaseMetaData meta = con.getMetaData();
        try (ResultSet rs = meta.getTables(con.getCatalog(), null, "%", new String[] { "TABLE" }))
        {
            while (rs.next())
            {
                String name = rs.getString("TABLE_NAME");
                if (!name.startsWith("sqlite_"))
                    tables.put(name.toLowerCase(Locale.ROOT), name);
            }
        }
        return tables;
    }

    private static List<String> columns(Connection con, String table) throws SQLException
    {
        List<String> columns = new ArrayList<>();
        try (ResultSet rs = con.getMetaData().getColumns(con.getCatalog(), null, table, "%"))
        {
            while (rs.next())
                columns.add(rs.getString("COLUMN_NAME"));
        }
        return columns;
    }

    private static int count(Connection con, String table) throws SQLException
    {
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM `" + table + "`"))
        {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static int copyTable(Connection from, String sourceTable, Connection to, String targetTable, boolean toSqlite) throws SQLException
    {
        List<String> sourceColumns = columns(from, sourceTable);
        List<String> shared = new ArrayList<>();
        for (String column : columns(to, targetTable))
        {
            if (sourceColumns.stream().anyMatch(c -> c.equalsIgnoreCase(column)))
                shared.add(column);
        }
        if (shared.isEmpty())
            return 0;

        String names = String.join(", ", shared.stream().map(c -> "`" + c + "`").toList());
        String marks = String.join(", ", shared.stream().map(c -> "?").toList());
        int rows = 0;
        try (Statement read = from.createStatement();
             ResultSet rs = read.executeQuery("SELECT " + names + " FROM `" + sourceTable + "`");
             PreparedStatement write = to.prepareStatement("INSERT INTO `" + targetTable + "` (" + names + ") VALUES (" + marks + ")"))
        {
            while (rs.next())
            {
                for (int i = 1; i <= shared.size(); i++)
                    write.setObject(i, value(rs.getObject(i), toSqlite));
                write.addBatch();
                if (++rows % BATCH == 0)
                    write.executeBatch();
            }
            write.executeBatch();
        }
        return rows;
    }

    // Times go into SQLite as the text MySQL shows them, so they compare and sort the same
    private static Object value(Object value, boolean toSqlite)
    {
        if (!toSqlite)
            return value;
        if (value instanceof Timestamp timestamp)
            return timestamp.toLocalDateTime().format(SQLITE_TIME);
        if (value instanceof LocalDateTime time)
            return time.format(SQLITE_TIME);
        return value;
    }

    private static void foreignKeys(Connection con, Database database, boolean on) throws SQLException
    {
        try (Statement st = con.createStatement())
        {
            if (database.isSqlite())
                st.execute("PRAGMA defer_foreign_keys = " + (on ? "OFF" : "ON"));
            else
                st.execute("SET FOREIGN_KEY_CHECKS = " + (on ? 1 : 0));
        }
    }
}
