package imu.iWaystones.Managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import imu.iAPI.Other.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iAPI.Main.ImusAPI;
import imu.iWaystone.Upgrades.BaseUpgrade;
import imu.iWaystone.Upgrades.PlayerUpgradePanel;
import imu.iWaystone.Waystones.Waystone;
import imu.iWaystones.Enums.SQL_tables;
import imu.iWaystones.Enums.UpgradeType;
import imu.iWaystones.Enums.VISIBILITY_TYPE;
import imu.iWaystones.Main.ImusWaystones;

public class WaystoneManagerSQL
{
	public static WaystoneManagerSQL Instance;
	ImusWaystones _main = ImusWaystones._instance;
	WaystoneManager _waystoneManager;

	public WaystoneManagerSQL(WaystoneManager wm)
	{
		Instance = this;
		_waystoneManager = wm;
	}

	public void CreateTables()
	{
		
		try (Connection con = _main.GetSQL().GetConnection();)
		{
			PreparedStatement ps;

			_main.getLogger().info("===LOADING TABLES===");
			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS " + SQL_tables.waystones.toString() + 
					"("
					+ "id INT AUTO_INCREMENT, " 
					+ "uuid CHAR(36) NOT NULL UNIQUE, " 
					+ "name VARCHAR(100), " 
					+ "loc_world VARCHAR(20), "
					+ "loc_x INT NOT NULL, " 
					+ "loc_y INT NOT NULL, " 
					+ "loc_z INT NOT NULL, "
					+ "display_item TEXT(16000), " 
					+ "visibility_type ENUM('BY_TOUCH', 'TO_ALL') DEFAULT 'BY_TOUCH', " 
					+ "PRIMARY KEY(id) "
				    + ");");
			ps.executeUpdate();
			
//			ps = con.prepareStatement("ALTER TABLE " + SQL_tables.waystones.toString() + " "
//				    + "ADD COLUMN IF NOT EXISTS `max_out` BOOLEAN DEFAULT FALSE, "
//				    + "ADD COLUMN IF NOT EXISTS `unbreakable` BOOLEAN DEFAULT FALSE, "
//				    + "ADD COLUMN IF NOT EXISTS `enable` BOOLEAN DEFAULT TRUE;");
//				ps.executeUpdate();
//			
//			ps = con.prepareStatement("ALTER TABLE " + SQL_tables.waystones.toString() + " "
//				    + "MODIFY COLUMN visibility_type ENUM('BY_TOUCH', 'TO_ALL', 'ONE_WAY', 'TO_ALL_ONE_WAY') DEFAULT 'BY_TOUCH';");
//			ps.executeUpdate();



//			ps = con.prepareStatement("ALTER TABLE " + SQL_tables.waystones.toString() + " "
//					+ "ADD COLUMN IF NOT EXISTS visibility_type ENUM('BY_TOUCH', 'TO_ALL') DEFAULT 'BY_TOUCH';");
//			ps.executeUpdate();
			
//			ps = con.prepareStatement("ALTER TABLE " + SQL_tables.waystones.toString() + " "
//					+ "ADD COLUMN IF NOT EXISTS visibility_type ENUM('BY_TOUCH', 'TO_ALL') DEFAULT 'BY_TOUCH';");
//			ps.executeUpdate();
//
//
//			ps = con.prepareStatement(
//				    "ALTER TABLE " + SQL_tables.waystones.toString() + " "
//				    + "DROP PRIMARY KEY;");
//			ps.executeUpdate();
//
//			// Add the new primary key and set the `id` column to auto-increment
//			ps = con.prepareStatement(
//			    "ALTER TABLE " + SQL_tables.waystones.toString() + " "
//			    + "ADD COLUMN IF NOT EXISTS id INT AUTO_INCREMENT PRIMARY KEY FIRST;");
//			ps.executeUpdate();
//
//			// Add unique constraint to `uuid`
//			ps = con.prepareStatement(
//			    "ALTER TABLE " + SQL_tables.waystones.toString() + " "
//			    + "ADD UNIQUE (uuid);");
//			ps.executeUpdate();

			
			
			ps.close();

			_main.getLogger().info("==> Waystones");
			// _main.GetSQL().GetConnection().close();

			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS " + SQL_tables.discovered.toString() + "("
					+ "id INT NOT NULL AUTO_INCREMENT, " + "uuid_player CHAR(36) NOT NULL, "
					+ "uuid_ws CHAR(36) NOT NULL, " + "PRIMARY KEY(id));");
			ps.executeUpdate();
			ps.close();
			_main.getLogger().info("==> Discovered");

			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS " + SQL_tables.waystone_owners.toString() + "("
					+ "id INT NOT NULL AUTO_INCREMENT, " + "uuid_player CHAR(36) NOT NULL, "
					+ "player_name VARCHAR(50) NOT NULL, " + "uuid_ws CHAR(36) NOT NULL, " + "PRIMARY KEY(id));");
			ps.executeUpdate();
			ps.close();
			_main.getLogger().info("==> owners");

			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS " + SQL_tables.upgrades.toString() + "("
					+ "id INT NOT NULL AUTO_INCREMENT, " + "uuid_ws CHAR(36) NOT NULL, "
					+ "uuid_player CHAR(36) NOT NULL, " + "upgrade_name VARCHAR(20) NOT NULL, "
					+ "tier INT(10) NOT NULL, " + "PRIMARY KEY(id));");
			ps.executeUpdate();
			ps.close();
			_main.getLogger().info("==> upgrades");

		} catch (Exception e)
		{
			e.printStackTrace();
			_main.getLogger().info("===> TABLE LOADING ERROR===");
			return;
		}
		_main.getLogger().info("===TABLE LOADING FINNISHED===");
	}

	private Waystone LoadUpgrades(Waystone waystone)
	{
		final String quarry = "SELECT * FROM " + SQL_tables.upgrades.toString() + " " + "WHERE uuid_ws='"
				+ waystone.GetUUID().toString() + "';";
		try (Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement(quarry);
			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst())
			{
				return waystone;
			}
			while (rs.next())
			{
				UUID uuid_player = UUID.fromString(rs.getString(3));
				UpgradeType type = UpgradeType.valueOf(rs.getString(4));
				int tier = rs.getInt(5);
				BaseUpgrade upgrade = BaseUpgrade.GetNewUpgrade(type);
				upgrade.SetCurrentier(tier);
				waystone.SetPlayerUpgrade(uuid_player, upgrade);
			}
			return waystone;
		} catch (Exception e)
		{
			_main.getLogger().info("===> LOADING ERROR: LoadUpgrades ===");
			e.printStackTrace();
		}
		return waystone;
	}

	public void SaveUpgradesAsync(Waystone waystone)
	{
		new BukkitRunnable() {

			@Override
			public void run()
			{
				try(Connection con = _main.GetSQL().GetConnection())
				{
					SaveUpgrades(waystone, con);
				} catch (SQLException e)
				{
					e.printStackTrace();
				}
				
			}
		}.runTaskAsynchronously(_main);
	}

	private void SaveUpgrades(Waystone waystone, Connection con)
	{
		final String quarry = "DELETE FROM " + SQL_tables.upgrades.toString() + " " + "WHERE uuid_ws='"
				+ waystone.GetUUID().toString() + "';";

		try (PreparedStatement ps = con.prepareStatement(quarry))
		{
			ps.executeUpdate();
		} 
		catch (Exception e)
		{
			_main.getLogger().info("===> SAVING ERROR: SaveUpgrades ===");
		}

		for (Map.Entry<UUID, PlayerUpgradePanel> entry : waystone.GetPlayerUpgrades().entrySet())
		{
			for (BaseUpgrade upgrade : entry.getValue().GetUpgrades())
			{
//				
//				PreparedStatement ps = con.prepareStatement("REPLACE INTO "+SQL_tables.upgrades.toString()+" "
//						+ "(uuid_ws,uuid_player,upgrade_name, tier) VALUES (?,?,?,?);");
//				
//				int i = 1;
//				ps.setString(i++, waystone.GetUUID().toString());
//				ps.setString(i++, entry.getKey().toString());
//				ps.setString(i++, upgrade._id.toString());
//				ps.setInt(i++, upgrade.GetCurrentTier());
//				ps.executeUpdate();
//				ps.close();

				final String insertQuery = "REPLACE INTO " + SQL_tables.upgrades.toString()
						+ " (uuid_ws, uuid_player, upgrade_name, tier) VALUES (?, ?, ?, ?);";
				try (PreparedStatement insertPs = con.prepareStatement(insertQuery))
				{
					insertPs.setString(1, waystone.GetUUID().toString());
					insertPs.setString(2, entry.getKey().toString());
					insertPs.setString(3, upgrade._id.toString());
					insertPs.setInt(4, upgrade.GetCurrentTier());
					insertPs.executeUpdate();
				} 
				catch (SQLException e)
				{
					e.printStackTrace();
				}

			}

		}
	}

	public void SaveUpgradeAsync(UUID uuid_player, UUID uuid_ws, BaseUpgrade[] upgrades)
	{
		new BukkitRunnable() {

			@Override
			public void run()
			{

				try (Connection con = _main.GetSQL().GetConnection())
				{
					for (BaseUpgrade upgrade : upgrades)
					{

						String str1 = "DELETE FROM upgrades WHERE uuid_ws='" + uuid_ws.toString()
								+ "' AND uuid_player='" + uuid_player.toString() + "' AND upgrade_name='"
								+ upgrade._id.toString() + "';";
						String str22 = " INSERT INTO upgrades (uuid_ws, uuid_player, upgrade_name, tier) VALUES('"
								+ uuid_ws.toString() + "', '" + uuid_player.toString() + "', '" + upgrade._id.toString()
								+ "', '" + upgrade.GetCurrentTier() + "');";

						PreparedStatement ps = con.prepareStatement(str1);
						ps.executeUpdate();

						if (upgrade.GetCurrentTier() == 0)
						{
							ps.close();
							continue;
						}

						ps = con.prepareStatement(str22);
						ps.executeUpdate();
						ps.close();
					}

					con.close();

				} catch (Exception e)
				{
					_main.getLogger().info("===> SAVING ERROR2: SaveUpgradeAsync ===");

				}
			}
		}.runTaskAsynchronously(_main);

	}

	private void RemoveUpgrades(Waystone waystone)
	{
		final String quarry = "DELETE FROM " + SQL_tables.upgrades.toString() + " " + "WHERE uuid_ws='"
				+ waystone.GetUUID().toString() + "';";
		try (Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement(quarry);
			ps.executeUpdate();

		} catch (SQLException e)
		{

			_main.getLogger().info("===> REMOVING ERROR: RemoveUpgrade ===");
		}
	}

	private Waystone LoadWaystoneOwner(Waystone waystone)
	{
		final String quarry = "SELECT * FROM " + SQL_tables.waystone_owners.toString() + " " + "WHERE uuid_ws='"
				+ waystone.GetUUID().toString() + "';";
		try (Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement(quarry);
			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst())
			{

				return waystone;
			}
			while (rs.next())
			{
				waystone.SetOwner(rs.getString(3), UUID.fromString(rs.getString(2)));

				return waystone;
			}

		} catch (SQLException e)
		{

			_main.getLogger().info("===> LOADING ERROR: LoadWaystoneOwner ===");
			// e.printStackTrace();
		}
		return waystone;
	}

	private void SaveWaystoneOwner(UUID uuid_player, String player_name, UUID uuid_ws, Connection con)
	{
		final String quarry = "REPLACE INTO " + SQL_tables.waystone_owners.toString() + " "
				+ "(uuid_player,player_name,uuid_ws) VALUES (?,?,?);";

		try (PreparedStatement ps = con.prepareStatement(quarry))
		{
			int i = 1;
			ps.setString(i++, uuid_player.toString());
			ps.setString(i++, player_name);
			ps.setString(i++, uuid_ws.toString());
			ps.executeUpdate();

		} catch (SQLException e)
		{

			e.printStackTrace();
		}
	}

	private void RemoveWaystoneOwner(UUID uuid_player, UUID uuid_ws)
	{
		final String quarry = "DELETE FROM " + SQL_tables.waystone_owners.toString() + " " + "WHERE uuid_player='"
				+ uuid_player.toString() + "' AND uuid_ws='" + uuid_ws.toString() + "';";
		try (Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement(quarry);
			ps.executeUpdate();

		} catch (SQLException e)
		{

			e.printStackTrace();
		}
	}

	private void SaveWaystone(Waystone waystone) {
	    final String quarry = "REPLACE INTO " + SQL_tables.waystones.toString() + " "
	            + "(uuid, name, loc_world, loc_x, loc_y, loc_z, display_item, visibility_type, max_out, unbreakable, enable) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
	    try (Connection con = _main.GetSQL().GetConnection()) {
	        PreparedStatement ps = con.prepareStatement(quarry);
	        int i = 1;
	        ps.setString(i++, waystone.GetUUID().toString());
	        ps.setString(i++, waystone.GetName());
	        ps.setString(i++, waystone.GetLoc().getWorld().getName());
	        ps.setInt(i++, waystone.GetLoc().getBlockX());
	        ps.setInt(i++, waystone.GetLoc().getBlockY());
	        ps.setInt(i++, waystone.GetLoc().getBlockZ());
	        ps.setString(i++, ImusAPI._metods.EncodeItemStack(waystone.GetDisplayItem()));
	        ps.setString(i++, waystone.GetVisibilityType().toString());
	        ps.setBoolean(i++, waystone.IsMaxOut());
	        ps.setBoolean(i++, waystone.IsUnbreakable());
	        ps.setBoolean(i++, waystone.IsEnable());
	        ps.executeUpdate();

	        SaveWaystoneOwner(waystone.GetOwnerUUID(), waystone.GetOwnerName(), waystone.GetUUID(), con);
	        SaveUpgrades(waystone, con);

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}


	public void SaveWaystoneAsync(Waystone waystone)
	{
		new BukkitRunnable() {
			@Override
			public void run()
			{
				SaveWaystone(waystone);
			}
		}.runTaskAsynchronously(_main);
	}

	public void RemoveWaystoneAsync(Waystone waystone)
	{
		new BukkitRunnable() {
			@Override
			public void run()
			{
				RemoveWaystone(waystone);
			}
		}.runTaskAsynchronously(_main);
	}

	private void RemoveWaystone(Waystone waystone)
	{
		final String quarry = "DELETE FROM " + SQL_tables.waystones.toString() + " WHERE uuid='"
				+ waystone.GetUUID().toString() + "';";
		try (Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement(quarry);
			// System.out.println("try to remove: "+waystone.GetUUID());
			ps.executeUpdate();
			RemoveWaystoneOwner(waystone.GetOwnerUUID(), waystone.GetUUID());
			RemoveUpgrades(waystone);

		} catch (SQLException e)
		{

			Bukkit.getLogger()
					.info("Couldnt delete waystone from database! waystone UUID: " + waystone.GetUUID().toString());
			e.printStackTrace();
		}
	}

	public void LoadWaystones()
	{
		final String quarry = "SELECT * FROM " + SQL_tables.waystones.toString() + ";";
		try (Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement(quarry);
			_waystoneManager.GetWaystones().clear();

			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst())
			{
				return;
			}
			while (rs.next())
			{
				int i = 2;

				UUID uuid = UUID.fromString(rs.getString(i++));
				String name = rs.getString(i++);
				World world = Bukkit.getWorld(rs.getString(i++));
				int x = rs.getInt(i++);
				int y = rs.getInt(i++);
				int z = rs.getInt(i++);
				ItemStack displayItem = ImusAPI._metods.DecodeItemStack(rs.getString(i++));
				String visibilityTypeStr = rs.getString(i++);
				boolean maxOut = rs.getBoolean(i++);
		        boolean unbreakable = rs.getBoolean(i++);
		        boolean enable = rs.getBoolean(i++);

				Waystone newWaystone = new Waystone(new Location(world, x, y, z));
				newWaystone.SetName(name);
				newWaystone.SetDisplayitem(displayItem);
				newWaystone.SetUUID(uuid);
				newWaystone.SetVisibilityType(VISIBILITY_TYPE.valueOf(visibilityTypeStr));
				newWaystone.SetMaxOut(maxOut);
		        newWaystone.SetUnbreakable(unbreakable);
		        newWaystone.SetEnable(enable);
				newWaystone = LoadWaystoneOwner(newWaystone);
				newWaystone = LoadUpgrades(newWaystone);
				_waystoneManager.SaveWaystone(newWaystone, false);
			}

		} catch (SQLException e)
		{

			System.out.println("Couldnt load waystones!");
			e.printStackTrace();
		}
	}

	/**
	 * <strong>This is a lengthy operation and should be called async!</strong><br>
	 * Loads all waystones from the database
	 * @return A set of loaded waystones
	 */
	public Set<Waystone> getLoadWaystones() {

		try(Connection connection = _main.GetSQL().GetConnection()) {

			 PreparedStatement statement =connection.prepareStatement("SELECT * FROM " + SQL_tables.waystones + ";");
			ResultSet resultSet = statement.executeQuery();

			Set<Waystone> waystones = new HashSet<>();
			while(resultSet.next()) {
				UUID uuid = UUID.fromString(resultSet.getString("uuid"));
				String name = resultSet.getString("name");
				World world = Bukkit.getWorld(resultSet.getString("loc_world"));
				int x = resultSet.getInt("loc_x");
				int y = resultSet.getInt("loc_y");
				int z = resultSet.getInt("loc_z");
				ItemStack displayItem = ImusAPI._metods.DecodeItemStack(resultSet.getString("display_item"));
				VISIBILITY_TYPE visibilityType = VISIBILITY_TYPE.valueOf(resultSet.getString("visibility_type"));

				Waystone newWaystone = new Waystone(new Location(world, x, y, z));
				newWaystone.SetName(name);
				newWaystone.SetDisplayitem(displayItem);
				newWaystone.SetUUID(uuid);
				newWaystone.SetVisibilityType(visibilityType);

				LoadWaystoneOwner(newWaystone);
				LoadUpgrades(newWaystone);

				waystones.add(newWaystone);
			}
			return waystones;

		} catch (SQLException e) {
			_main.getLogger().severe("Couldn't load discovered waystones!");
			e.printStackTrace();
		}
		return Collections.emptySet();
	}

	/**
	 * <strong>This is a lengthy operation and should be called async!</strong><br>
	 * @return A set of discovered waystones
	 */
	public Set<Tuple<UUID, UUID>> getLoadDiscoveredWaystones() {

		try(Connection connection = _main.GetSQL().GetConnection()) {

			PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + SQL_tables.discovered + ";");
			ResultSet resultSet = statement.executeQuery();

			Set<Tuple<UUID, UUID>> discoveredWaystones = new HashSet<>();
			while(resultSet.next()) {
				UUID uuid_player = UUID.fromString(resultSet.getString("uuid_player"));
				UUID uuid_ws = UUID.fromString(resultSet.getString("uuid_ws"));
				discoveredWaystones.add(new Tuple<>(uuid_player, uuid_ws));
			}
			return discoveredWaystones;

		} catch (SQLException e) {
			_main.getLogger().severe("Couldn't load discovered waystones!");
			e.printStackTrace();
		}
		return Collections.emptySet();
	}
	public void LoadWaystonesAsync()
	{
		new BukkitRunnable() {
			@Override
			public void run()
			{
				LoadWaystones();
			}
		}.runTaskAsynchronously(_main);
	}

	public void SaveDiscoveredAsync(UUID uuid_player, UUID uuid_ws)
	{
		new BukkitRunnable() {

			@Override
			public void run()
			{
				SaveDiscovered(uuid_player, uuid_ws);
			}
		}.runTaskAsynchronously(_main);
	}

	private void SaveDiscovered(UUID uuid_player, UUID uuid_ws)
	{
		final String quarry = "INSERT INTO " + SQL_tables.discovered.toString() + " "
				+ "(uuid_player,uuid_ws) VALUES (?,?);";
		try (Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement(quarry);
			int i = 1;
			ps.setString(i++, uuid_player.toString());
			ps.setString(i++, uuid_ws.toString());
			ps.executeUpdate();

		} catch (SQLException e)
		{

			e.printStackTrace();
		}
	}

	public void LoadDiscoveredWaystonesAsync()
	{
		new BukkitRunnable() {

			@Override
			public void run()
			{
				LoadDiscoveredWaystones();
			}
		}.runTaskAsynchronously(_main);
	}

	public void LoadDiscoveredWaystones()
	{
		final String quarry = "SELECT * FROM " + SQL_tables.discovered.toString() + "";
		try (Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement(quarry);
			_waystoneManager.GetDiscovered().clear();
			System.out.println("LOADING waystones!");

			ResultSet rs = ps.executeQuery();
			if (!rs.isBeforeFirst())
			{

				return;
			}
			while (rs.next())
			{
				int i = 2;

				UUID uuid_player = UUID.fromString(rs.getString(i++));
				UUID uuid_ws = UUID.fromString(rs.getString(i++));
				_waystoneManager.AddDiscovered(uuid_player, uuid_ws, false);
			}

		} catch (SQLException e)
		{

			System.out.println("Couldnt load waystones!");
			// e.printStackTrace();
		}
	}

	public void RemoveDiscoveredAsync(UUID uuid_ws)
	{
		new BukkitRunnable() {

			@Override
			public void run()
			{
				RemoveDiscovered(uuid_ws);
			}
		}.runTaskAsynchronously(_main);
	}

	private void RemoveDiscovered(UUID uuid_ws)
	{
		final String quarry = "DELETE FROM " + SQL_tables.discovered.toString() + " WHERE uuid_ws='" + uuid_ws + "';";
		try (Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement(quarry);
			ps.executeUpdate();

			_main.GetSQL().GetConnection();
		} catch (SQLException e)
		{
			// e.printStackTrace();
			System.out.println("Couldnt remove discover!");
		}

	}

	public void RemoveDiscoveredFromPlayerAsync(UUID uuid_player, UUID uuid_ws)
	{
		new BukkitRunnable() {

			@Override
			public void run()
			{
				RemoveDiscoveredFromPlayer(uuid_player, uuid_ws);
			}
		}.runTaskAsynchronously(_main);
	}

	private void RemoveDiscoveredFromPlayer(UUID uuid_player, UUID uuid_ws)
	{
		final String quarry = "DELETE FROM " + SQL_tables.discovered.toString() + " WHERE uuid_player='"
				+ uuid_player.toString() + "' AND uuid_ws='" + uuid_ws + "' ;";
		try (Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement(quarry);
			ps.executeUpdate();

		} catch (SQLException e)
		{
			// e.printStackTrace();
			System.out.println("Couldnt remove player discover!");
		}
	}

}
