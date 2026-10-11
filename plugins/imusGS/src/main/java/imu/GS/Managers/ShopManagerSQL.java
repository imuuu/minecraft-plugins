package imu.GS.Managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import imu.GS.ENUMs.ItemPriceType;
import imu.GS.ENUMs.SQL_TABLES;
import imu.GS.ENUMs.ShopItemType;
import imu.GS.ENUMs.TransactionAction;
import imu.GS.Main.Main;
import imu.GS.Other.CustomPriceData;
import imu.GS.Other.LogData;
import imu.GS.ShopUtl.ShopItemBase;
import imu.GS.ShopUtl.ShopItemModData;
import imu.GS.ShopUtl.ShopNormal;
import imu.GS.ShopUtl.ItemPrice.PriceCustom;
import imu.GS.ShopUtl.ItemPrice.PriceOwn;
import imu.GS.ShopUtl.ItemPrice.PriceUnique;
import imu.GS.ShopUtl.ShopItems.ShopItemSeller;
import imu.GS.ShopUtl.ShopItems.ShopItemStockable;
import imu.GS.ShopUtl.ShopItems.ShopItemUnique;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;
import imu.iAPI.Other.Tuple;

public class ShopManagerSQL
{
	Main _main;
	ShopManager _shopManager;

	// Every write that touches shop item rows holds this lock, so two saves of the same shop can't interleave
	private final Object _writeLock = new Object();

	// Tables holding per-shopitem rows, with the column that references the shopitem uuid
	private static final String[][] ITEM_TABLES = {
			{SQL_TABLES.shopitem_moddata.toString(), "uuid"},
			{SQL_TABLES.shopitem_locations.toString(), "uuid"},
			{SQL_TABLES.shopitem_permissions.toString(), "uuid"},
			{SQL_TABLES.shopitem_worlds.toString(), "uuid"},
			{SQL_TABLES.price_customs.toString(), "uuid"},
			{SQL_TABLES.price_values.toString(), "uuid"},
			{SQL_TABLES.custom_price.toString(), "uuid"},
			{SQL_TABLES.tags_shopitems.toString(), "sib_uuid"},
			{SQL_TABLES.shopitems.toString(), "uuid"},
	};

	public ShopManagerSQL(Main main, ShopManager shopManager)
	{
		_main = main;
		_shopManager = shopManager;
	}

	public Object GetWriteLock()
	{
		return _writeLock;
	}

	public void LoadTables()
	{

		PreparedStatement ps;
		try(Connection con =_main.GetSQL().GetConnection())
		{
			_main.getLogger().info("===LOADING TABLES===");
			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.shops.toString()+" ("
					+ "uuid CHAR(36) NOT NULL, "
					+ "name VARCHAR(100) NOT NULL, "
					+ "display_name VARCHAR(100), "
					+ "pages INT(100), "
					+ "shop_type INT(100), "
					+ "sellM FLOAT(20), "
					+ "buyM FLOAT(20), "
					+ "expire_percent FLOAT(20),"
					+ "expire_cooldown INT(100),"
					+ "locked INT(1),"
					+ "customer_can_sell INT(1),"
					+ "absolutePositions INT(1), PRIMARY KEY(uuid));");
			ps.executeUpdate();

			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.shopitems.toString()+" ("
					+ "uuid CHAR(36), "
					+ "shop_uuid CHAR(36) NOT NULL, "
					+ "type VARCHAR(200) NOT NULL, "
					+ "item_display_name VARCHAR(100),"
					+ "amount INT(100),"
					+ "page INT(100), "
					+ "slot INT(27),"

					+ "price_type 		VARCHAR(20),"
					+ "type_data TEXT(16000),"
					+ "itemstack TEXT(16000),"
					+ "PRIMARY KEY(uuid));");

			ps.executeUpdate();

			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.shopitem_permissions.toString()+" ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "uuid CHAR(36), "
					+ "name VARCHAR(50), "
					+ "PRIMARY KEY(id));");

			ps.executeUpdate();

			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.shopitem_worlds.toString()+" ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "uuid CHAR(36), "
					+ "name VARCHAR(50), "
					+ "PRIMARY KEY(id));");

			ps.executeUpdate();

			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.shopitem_locations.toString()+" ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "uuid CHAR(36), "
					+ "distance		INT(20),"
					+ "dis_world	VARCHAR(40),"
					+ "dis_locX		INT(20),"
					+ "dis_locY		INT(20),"
					+ "dis_locZ		INT(20),"

					+ "PRIMARY KEY(id));");

			ps.executeUpdate();

			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.price_customs.toString()+" ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "uuid CHAR(36), "
					+ "amount INT(20), "
					+ "itemstack TEXT(16000),"
					+ "PRIMARY KEY(id));");

			ps.executeUpdate();

			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.price_values.toString()+" ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "uuid CHAR(36), "
					+ "name VARCHAR(36), "
					+ "amount FLOAT(20), "
					+ "mark VARCHAR(10), "
					+ "PRIMARY KEY(id));");

			ps.executeUpdate();

			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.custom_price.toString()+" ("
					+ "uuid CHAR(36), "
					+ "minimum_stack_amount FLOAT(20), "
					+ "PRIMARY KEY(uuid));");

			ps.executeUpdate();

			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.tags_material.toString()+" ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "material_name VARCHAR(50) NOT NULL, "
					+ "tag_name VARCHAR(30),"
					+ "PRIMARY KEY(id));");
			ps.executeUpdate();

			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.tags_shopitems.toString()+" ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "sib_uuid CHAR(36) NOT NULL, "
					+ "tag_name VARCHAR(30),"
					+ "PRIMARY KEY(id));");
			ps.executeUpdate();

			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.uniques.toString()+" ("
					+ "uuid CHAR(36), "
					+ "item_display_name VARCHAR(100),"
					+ "price FLOAT(20), "
					+ "itemstack TEXT(16000),"
					+ "PRIMARY KEY(uuid));");
			ps.executeUpdate();

			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.log_transaction.toString()+" ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "datetime DATETIME DEFAULT(NOW()), "
					+ "player_uuid CHAR(36),"
					+ "player_name VARCHAR(20),"
					+ "action VARCHAR(10),"
					+ "shop_uuid CHAR(36),"
					+ "shopname VARCHAR(100) NOT NULL,"
					+ "shopitem_uuid CHAR(36),"
					+ "amount INT(10),"
					+ "price FLOAT(20), "
					+ "cal_total_price FLOAT(20), "
					+ "custom_price_view VARCHAR(10000), "
					+ "itemstack_displayname VARCHAR(100), "
					+ "itemstack_enchants VARCHAR(1000), "
					+ "itemstack TEXT(16000),"
					+ "PRIMARY KEY(id));");
			ps.executeUpdate();

			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.shopitem_moddata.toString()+" ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "uuid CHAR(36) NOT NULL, "
					+ "max_amount 		INT(20),"
					+ "fill_amount 		INT(20),"
					+ "fill_delay 		INT(20),"

					+ "selltime_start 	INT(20),"
					+ "selltime_end		INT(20),"
					+ "PRIMARY KEY(id));");

			ps.executeUpdate();


			ps.close();
		} catch (Exception e)
		{
			_main.getLogger().info("Couldnt load tables correctly");
			e.printStackTrace();
		}
		_main.getLogger().info("===TABLE LOADING FINNISHED===");
	}

	static String Truncate(String str, int maxLength)
	{
		if(str == null || str.length() <= maxLength) return str;
		return str.substring(0, maxLength);
	}

	// ==================== TRANSACTION LOG ====================

	public void LogPurchaseAsync(Player player, List<LogData> logs, int iterations)
	{
		// While the plugin is disabling (inventories closed on shutdown) the scheduler is gone, so write directly
		if(!_main.isEnabled())
		{
			try
			{
				WriteLogs(player, logs);
			}
			catch (Exception e)
			{
				_main.getLogger().log(Level.SEVERE, "Couldn't write purchase logs of "+player.getName()+" on shutdown", e);
			}
			return;
		}

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				try
				{
					WriteLogs(player, logs);
				}
				catch (Exception e)
				{
					if(iterations < 6)
					{
						Bukkit.getLogger().info("ShopManagerSQL:LogPurchaseAsync Could't log the data to database. Will be doing it later");
						int seconds = (ThreadLocalRandom.current().nextInt(60))+20;

						new BukkitRunnable() {

							@Override
							public void run()
							{
								LogPurchaseAsync(player, logs, iterations+1);
							}
						}.runTaskLater(_main, 20 * seconds * 5);

					}else
					{
						Bukkit.getLogger().info("ShopManagerSQL:LogPurchaseAsync ERROR");
						e.printStackTrace();
					}
				}
			}
		}.runTaskAsynchronously(_main);
	}

	void WriteLogs(Player player, List<LogData> logs) throws SQLException
	{
		if(logs.isEmpty()) return;

		String[] ench_str = new String[logs.size()];
		for(int i = 0; i < logs.size(); ++i)
		{
			LinkedList<String> enchants = new LinkedList<>();
			for(Map.Entry<Enchantment, Integer> entry : Metods._ins.GetEnchantsWithLevels(logs.get(i).Get_shopitem().GetRealItem()).entrySet())
			{
				enchants.add(entry.getKey().getKey().getKey().toUpperCase()+":"+entry.getValue());
			}
			ench_str[i] = enchants.isEmpty() ? " " : Metods._ins.CombineArrayToOneString(enchants, ";");
		}

		try(Connection con = _main.GetSQL().GetConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO "+SQL_TABLES.log_transaction.toString()+" "
					+ "(player_uuid, player_name, action, shop_uuid, shopname, shopitem_uuid, amount, price, cal_total_price,custom_price_view, itemstack_displayname, itemstack_enchants,itemstack) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"))
		{
			int count = 0;
			for(LogData data : logs)
			{
				int i = 1;
				ShopItemBase sib = data.Get_shopitem();
				ps.setString(i++, player.getUniqueId().toString());
				ps.setString(i++, Truncate(player.getName(), 20));
				ps.setString(i++, sib instanceof ShopItemSeller ? TransactionAction.SELL.toString() : TransactionAction.BUY.toString() );
				ps.setString(i++, sib.GetShop().GetUUID().toString());
				ps.setString(i++, Truncate(sib.GetShop().GetName(), 100));
				ps.setString(i++, sib.GetUUID().toString());
				ps.setInt(i++, data.Get_amount());
				ps.setFloat(i++, (float)sib.GetItemPrice().GetPrice());
				ps.setFloat(i++, (float) Metods.Round((data.Get_price() * (sib instanceof ShopItemSeller ? 1 : -1))));
				ps.setString(i++, Truncate(sib.GetItemPrice() instanceof PriceCustom ? ((PriceCustom)sib.GetItemPrice()).GetViewStringOfItems(data.Get_amount()) : "", 10000));
				ps.setString(i++, Truncate(ImusAPI._metods.GetItemDisplayName(sib.GetRealItem()), 100));
				ps.setString(i++, Truncate(ench_str[count], 1000));
				ps.setString(i++, ImusAPI._metods.EncodeItemStack(sib.GetRealItem()));
				count++;
				ps.addBatch();
			}

			ps.executeBatch();
		}
	}

	// ==================== LOADING ====================

	/**
	 * Loads every shop with its items. Runs off the main thread during startup; the returned shops
	 * are not yet visible to the rest of the plugin.
	 * An item row that fails to load is left untouched in the database (see ShopNormal.MarkItemUnloaded),
	 * so a later save can't wipe it.
	 */
	List<ShopNormal> LoadShopsAndItems()
	{
		List<ShopNormal> shops = new ArrayList<>();
		try(Connection con = _main.GetSQL().GetConnection())
		{
			LoadShops(con, shops);

			for(ShopNormal shop : shops)
			{
				try
				{
					LoadShopItems(con, shop);
				}
				catch (Exception e)
				{
					// Unknown which rows exist => never let this shop overwrite the database
					shop.SetLoadFailed(true);
					_main.getLogger().log(Level.SEVERE, "Couldn't load items of shop "+shop.GetName()+". Saving of this shop is disabled until the plugin loads it cleanly.", e);
				}
			}
		}
		catch (SQLException e)
		{
			_main.getLogger().log(Level.SEVERE, "Couldn't load shops from the database!", e);
		}
		return shops;
	}

	void LoadShops(Connection con, List<ShopNormal> shops) throws SQLException
	{
		try(PreparedStatement ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.shops.toString());
			ResultSet rs = ps.executeQuery())
		{
			while(rs.next())
			{
				UUID uuid = UUID.fromString(rs.getString("uuid"));
				String name = rs.getString("name");
				ShopNormal shop = new ShopNormal(_main, uuid, rs.getString("display_name"), rs.getInt("pages"));
				shop.set_sellM(rs.getFloat("sellM"));
				shop.set_buyM(rs.getFloat("buyM"));
				shop.set_expire_percent(rs.getFloat("expire_percent"));
				shop.set_expire_cooldown_m(rs.getInt("expire_cooldown"));
				shop.SetLocked(rs.getInt("locked") != 0);
				shop.SetCustomersCanOnlyBuy(rs.getInt("customer_can_sell") != 0);
				shop.SetAbsolutePosBool(rs.getInt("absolutePositions") != 0);
				shops.add(shop);
				_main.getLogger().info("Shop loaded named: "+ name);
			}
		}

		if(shops.isEmpty()) _main.getLogger().info("No shops found!");
	}

	void LoadShopItems(Connection con, ShopNormal shop) throws SQLException
	{
		try(PreparedStatement ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.shopitems.toString()+" WHERE shop_uuid=?"))
		{
			ps.setString(1, shop.GetUUID().toString());
			try(ResultSet rs = ps.executeQuery())
			{
				while(rs.next())
				{
					// A row whose uuid can't be read can't be protected from the next save
					UUID uuid = UUID.fromString(rs.getString("uuid"));
					try
					{
						LoadShopItem(con, shop, uuid, rs);
					}
					catch (Exception e)
					{
						shop.MarkItemUnloaded(uuid);
						_main.getLogger().log(Level.WARNING, "Couldn't load shop item "+uuid+" ("+rs.getString("item_display_name")+") of shop "+shop.GetName()+". Its database rows are kept as they are.", e);
					}
				}
			}
		}
	}

	void LoadShopItem(Connection con, ShopNormal shop, UUID uuid, ResultSet rs) throws Exception
	{
		ShopItemType siType = ShopItemType.valueOf(rs.getString("type"));
		int amount = rs.getInt("amount");
		int page = rs.getInt("page");
		int slot = rs.getInt("slot");
		ItemPriceType priceType = ItemPriceType.valueOf(rs.getString("price_type"));
		String typeData = rs.getString("type_data");
		String encodedStack = rs.getString("itemstack");
		ItemStack stack = encodedStack == null ? null : ImusAPI._metods.DecodeItemStack(encodedStack);

		if(stack == null) throw new IllegalStateException("itemstack couldn't be decoded");

		ShopItemSeller sis = siType == ShopItemType.STOCKABLE
				? new ShopItemStockable(_main, shop, stack, amount)
				: new ShopItemSeller(_main, shop, stack, amount);

		sis.SetUUID(uuid);

		switch (priceType) {
		case None:
			sis.SetItemPrice( _main.GetMaterialManager().GetPriceMaterialAndCheck(stack));
			break;
		case PriceCustom:
			sis.SetItemPrice( LoadPriceCustom(con, uuid));
			break;
		case PriceOwn:
			sis.SetItemPrice(new PriceOwn().SetPrice(LoadPriceValue(con, uuid)));
			break;
		case PriceUnique:
			UniqueManager uniqueManager = _shopManager.GetUniqueManager();
			if(uniqueManager.IsUnique(stack))
			{
				sis.SetItemPrice(new PriceUnique().SetPrice(uniqueManager.GetPriceItem(stack).GetPrice()));
			}
			else
			{
				_main.getLogger().warning("Shop item "+uuid+" of shop "+shop.GetName()+" was priced as a unique that no longer exists. Using its material price.");
				sis.SetItemPrice( _main.GetMaterialManager().GetPriceMaterialAndCheck(stack));
			}
			break;
		}

		if(typeData != null && !typeData.isEmpty() && !typeData.equals("null"))
			sis.ParseJsonData(JsonParser.parseString(typeData).getAsJsonObject());

		if(sis instanceof ShopItemStockable)
		{
			LoadModData(con, (ShopItemStockable)sis, shop);
		}

		// Never overwrite an item already loaded into the same slot
		if(page < 0 || slot < 0 || slot >= ShopNormal.SHOP_HOLDER_SIZE || shop.GetItem(page, slot) != null)
		{
			int[] free = shop.FindFreeSlot();
			page = free[0];
			slot = free[1];
		}
		shop.SetItem(sis, page, slot);
	}

	void LoadModData(Connection con, ShopItemStockable sis, ShopNormal shop) throws SQLException
	{
		String uuid = sis.GetUUID().toString();
		ShopItemModData modData = sis.GetModData();

		try(PreparedStatement ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.shopitem_moddata.toString()+" WHERE uuid=?"))
		{
			ps.setString(1, uuid);
			try(ResultSet rs = ps.executeQuery())
			{
				if(rs.next())
				{
					modData._maxAmount = rs.getInt("max_amount");
					modData._fillAmount = rs.getInt("fill_amount");
					modData._fillDelayMinutes = rs.getInt("fill_delay");
					modData._sellTimeStart = rs.getInt("selltime_start");
					modData._sellTimeEnd = rs.getInt("selltime_end");
				}
			}
		}

		try(PreparedStatement ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.shopitem_locations.toString()+" WHERE uuid=?"))
		{
			ps.setString(1, uuid);
			try(ResultSet rs = ps.executeQuery())
			{
				while(rs.next())
				{
					String worldName = rs.getString("dis_world");
					World world = worldName == null ? null : Bukkit.getWorld(worldName);
					if(world == null)
					{
						_main.getLogger().warning("Shop item "+uuid+" of shop "+shop.GetName()+" has a location in missing world "+worldName+". The location is dropped.");
						continue;
					}
					modData.AddLocation(rs.getInt("distance"), new Location(world, rs.getInt("dis_locX"), rs.getInt("dis_locY"), rs.getInt("dis_locZ")));
				}
			}
		}

		for(String permission : LoadStrings(con, SQL_TABLES.shopitem_permissions.toString(), "uuid", "name", uuid))
		{
			modData.AddPermission(permission);
		}

		for(String worldName : LoadStrings(con, SQL_TABLES.shopitem_worlds.toString(), "uuid", "name", uuid))
		{
			modData.AddWorldName(worldName);
		}

		for(String tag : LoadStrings(con, SQL_TABLES.tags_shopitems.toString(), "sib_uuid", "tag_name", uuid))
		{
			modData.AddTag(tag.toLowerCase());
		}

		sis.SetModData(modData);
	}

	List<String> LoadStrings(Connection con, String table, String keyColumn, String valueColumn, String key) throws SQLException
	{
		List<String> values = new ArrayList<>();
		try(PreparedStatement ps = con.prepareStatement("SELECT "+valueColumn+" FROM "+table+" WHERE "+keyColumn+"=?"))
		{
			ps.setString(1, key);
			try(ResultSet rs = ps.executeQuery())
			{
				while(rs.next()) values.add(rs.getString(1));
			}
		}
		return values;
	}

	double LoadPriceValue(Connection con, UUID uuid) throws SQLException
	{
		try(PreparedStatement ps = con.prepareStatement("SELECT amount FROM "+SQL_TABLES.price_values.toString()+" WHERE uuid=?"))
		{
			ps.setString(1, uuid.toString());
			try(ResultSet rs = ps.executeQuery())
			{
				return rs.next() ? rs.getFloat(1) : 0;
			}
		}
	}

	PriceCustom LoadPriceCustom(Connection con, UUID uuid) throws SQLException
	{
		ArrayList<CustomPriceData> datas = new ArrayList<>();
		try(PreparedStatement ps = con.prepareStatement("SELECT amount, itemstack FROM "+SQL_TABLES.price_customs.toString()+" WHERE uuid=?"))
		{
			ps.setString(1, uuid.toString());
			try(ResultSet rs = ps.executeQuery())
			{
				while(rs.next())
				{
					ItemStack stack = ImusAPI._metods.DecodeItemStack(rs.getString("itemstack"));
					if(stack == null) throw new IllegalStateException("custom price itemstack couldn't be decoded");
					stack.setAmount(1);
					datas.add(new CustomPriceData(stack, rs.getInt("amount")));
				}
			}
		}

		int minimumStackAmount = 1;
		try(PreparedStatement ps = con.prepareStatement("SELECT minimum_stack_amount FROM "+SQL_TABLES.custom_price.toString()+" WHERE uuid=?"))
		{
			ps.setString(1, uuid.toString());
			try(ResultSet rs = ps.executeQuery())
			{
				if(rs.next()) minimumStackAmount = rs.getInt(1);
			}
		}

		PriceCustom pc = new PriceCustom();
		pc.SetItemsAndPrice(datas.toArray(new CustomPriceData[0]), LoadPriceValue(con, uuid), minimumStackAmount);
		return pc;
	}

	void LoadUniques()
	{
		try (Connection con = _main.GetSQL().GetConnection();
			 PreparedStatement ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.uniques.toString());
			 ResultSet rs = ps.executeQuery())
		{
			int count = 0;
			while(rs.next())
			{
				String uuidStr = rs.getString("uuid");
				try
				{
					ItemStack stack = ImusAPI._metods.DecodeItemStack(rs.getString("itemstack"));
					if(stack == null) throw new IllegalStateException("itemstack couldn't be decoded");
					ShopItemUnique unique = new ShopItemUnique(_main, null, stack, 1);
					unique.SetUUID(UUID.fromString(uuidStr));
					unique.GetItemPrice().SetPrice(rs.getFloat("price"));
					_shopManager.GetUniqueManager().AddUniqueItem(unique,false);
					count++;
				}
				catch (Exception e)
				{
					_main.getLogger().log(Level.WARNING, "Couldn't load unique "+uuidStr, e);
				}
			}
			if(count == 0) _main.getLogger().info("Uniques not found!");
		} catch (SQLException e)
		{
			Bukkit.getLogger().info("ShopManagerSQL:LoadUniques:Loading Unique ERROR");
			e.printStackTrace();
		}
	}

	// ==================== SAVING ====================

	static final class LocationRow
	{
		int distance;
		String world;
		int x, y, z;
	}

	static final class ShopItemRow
	{
		UUID uuid;
		String type;
		String displayName;
		int amount, page, slot;
		ItemPriceType priceType;
		String typeData;
		String itemstack;

		Double priceValue;
		List<Integer> customAmounts; // PriceCustom items, same order as customEncoded
		List<String> customEncoded;
		int customMinStack;

		ShopItemModData modData; // copy, stockables only
		List<LocationRow> locations = new ArrayList<>();
		List<String> tags = new ArrayList<>();
	}

	static final class ShopSnapshot
	{
		UUID uuid;
		String name, displayName;
		int pages;
		double sellM, buyM, expirePercent;
		int expireCooldown;
		boolean locked, customersCanOnlyBuy, absolutePositions;
		List<ShopItemRow> items = new ArrayList<>();
		Set<UUID> preserved = new HashSet<>(); // rows the plugin couldn't load or serialize; left untouched
	}

	/**
	 * Copies everything a save needs out of the live shop. Must run on the main thread,
	 * the shop and its ItemStacks are only ever modified there.
	 */
	ShopSnapshot CaptureShop(ShopNormal shop)
	{
		ShopSnapshot snap = new ShopSnapshot();
		snap.uuid = shop.GetUUID();
		snap.name = shop.GetName();
		snap.displayName = shop.GetDisplayName();
		snap.pages = shop.get_items().size();
		snap.sellM = shop.get_sellM();
		snap.buyM = shop.get_buyM();
		snap.expirePercent = shop.get_expire_percent();
		snap.expireCooldown = shop.get_expire_cooldown_m();
		snap.locked = shop.HasLocked();
		snap.customersCanOnlyBuy = shop.GetCustomersCanOnlyBuy();
		snap.absolutePositions = shop.GetAbsolutePosBool();
		snap.preserved.addAll(shop.GetUnloadedItemUuids());

		for(ShopItemSeller[] page : shop.get_items())
		{
			for(ShopItemSeller sis : page)
			{
				if(sis == null) continue;

				ShopItemRow row = CaptureItem(sis, shop);
				if(row == null)
				{
					snap.preserved.add(sis.GetUUID());
					continue;
				}
				snap.items.add(row);
			}
		}
		return snap;
	}

	ShopItemRow CaptureItem(ShopItemSeller sis, ShopNormal shop)
	{
		ShopItemRow row = new ShopItemRow();
		row.uuid = sis.GetUUID();
		row.itemstack = ImusAPI._metods.EncodeItemStack(sis.GetRealItem());
		if(row.itemstack == null)
		{
			_main.getLogger().warning("Couldn't serialize shop item "+row.uuid+" of shop "+shop.GetName()+". Keeping its previous database rows.");
			return null;
		}
		row.type = sis.GetItemType().toString();
		row.displayName = Truncate(ImusAPI._metods.GetItemDisplayName(sis.GetRealItem()), 100);
		row.amount = sis.Get_amount();
		row.page = sis.GetPage();
		row.slot = sis.GetSlot();
		row.priceType = GetPriceType(sis);
		row.typeData = new Gson().toJson(sis.GetJsonData());
		row.tags.addAll(sis.GetTags());

		if(row.priceType == ItemPriceType.PriceOwn)
		{
			row.priceValue = sis.GetItemPrice().GetPrice();
		}

		if(row.priceType == ItemPriceType.PriceCustom)
		{
			PriceCustom pc = (PriceCustom)sis.GetItemPrice();
			row.customAmounts = new ArrayList<>();
			row.customEncoded = new ArrayList<>();
			if(pc.GetItems() != null)
			{
				for(CustomPriceData data : pc.GetItems())
				{
					String encoded = ImusAPI._metods.EncodeItemStack(data._stack);
					if(encoded == null)
					{
						_main.getLogger().warning("Couldn't serialize custom price of shop item "+row.uuid+" of shop "+shop.GetName()+". Keeping its previous database rows.");
						return null;
					}
					row.customAmounts.add(data._amount);
					row.customEncoded.add(encoded);
				}
			}
			row.customMinStack = pc.GetMinimumStackAmount();
			if(pc.GetPrice() >= 0) row.priceValue = pc.GetPrice();
		}

		if(sis instanceof ShopItemStockable)
		{
			ShopItemModData modData = ((ShopItemStockable)sis).GetModData();
			row.modData = modData.clone();
			if(modData._locations != null)
			{
				for(Tuple<Integer, Location> disLoc : modData._locations)
				{
					Location loc = disLoc.GetValue();
					if(loc == null || loc.getWorld() == null)
					{
						_main.getLogger().warning("Shop item "+row.uuid+" of shop "+shop.GetName()+" has a location without a world. It is not saved.");
						continue;
					}
					LocationRow locRow = new LocationRow();
					locRow.distance = disLoc.GetKey();
					locRow.world = loc.getWorld().getName();
					locRow.x = loc.getBlockX();
					locRow.y = loc.getBlockY();
					locRow.z = loc.getBlockZ();
					row.locations.add(locRow);
				}
			}
		}
		return row;
	}

	ItemPriceType GetPriceType(ShopItemBase sib)
	{
		if(sib.GetItemPrice().getClass().equals(PriceCustom.class)) return ItemPriceType.PriceCustom;

		if(sib.GetItemPrice().getClass().equals(PriceOwn.class)) return ItemPriceType.PriceOwn;

		if(sib.GetItemPrice().getClass().equals(PriceUnique.class)) return ItemPriceType.PriceUnique;

		return ItemPriceType.None;
	}

	/**
	 * Saves the shop in the background. The shop state is captured on the main thread first,
	 * so this can be called from any thread.
	 */
	public void SaveShopAsync(ShopNormal shop)
	{
		if(!Bukkit.isPrimaryThread())
		{
			Bukkit.getScheduler().runTask(_main, () -> SaveShopAsync(shop));
			return;
		}

		ShopSnapshot snap = CaptureForSave(shop);
		if(snap == null) return;

		Bukkit.getScheduler().runTaskAsynchronously(_main, () ->
		{
			if(WriteShop(snap)) _main.GetTagManager().LoadAllShopItemTagsNamesAsync();
		});
	}

	/** Saves every shop on the calling thread. Used on shutdown. */
	public void SaveShopsSync(Collection<ShopNormal> shops)
	{
		for(ShopNormal shop : shops)
		{
			ShopSnapshot snap = CaptureForSave(shop);
			if(snap != null) WriteShop(snap);
		}
	}

	ShopSnapshot CaptureForSave(ShopNormal shop)
	{
		// A shop removed while a delayed save was pending must not be written back
		if(_shopManager.GetShop(shop.GetUUID()) != shop) return null;

		if(shop.IsLoadFailed())
		{
			_main.getLogger().severe("Not saving shop "+shop.GetName()+": its items failed to load, saving would wipe them from the database.");
			return null;
		}
		return CaptureShop(shop);
	}

	boolean WriteShop(ShopSnapshot snap)
	{
		synchronized (_writeLock)
		{
			try(Connection con = _main.GetSQL().GetConnection())
			{
				con.setAutoCommit(false);
				try
				{
					// Replace every row of this shop's items, except the ones we must not touch
					Set<UUID> itemUuids = SelectShopItemUuids(con, snap.uuid);
					for(ShopItemRow row : snap.items) itemUuids.add(row.uuid);
					itemUuids.removeAll(snap.preserved);
					DeleteItemRows(con, itemUuids);

					WriteShopRow(con, snap);
					InsertItemRows(con, snap.uuid, snap.items);

					con.commit();
				}
				catch (Exception e)
				{
					con.rollback();
					throw e;
				}
			}
			catch (Exception e)
			{
				_main.getLogger().log(Level.SEVERE, "Couldn't save shop "+snap.name+". The database still has its previous state.", e);
				return false;
			}
		}
		return true;
	}

	Set<UUID> SelectShopItemUuids(Connection con, UUID shopUuid) throws SQLException
	{
		Set<UUID> uuids = new HashSet<>();
		try(PreparedStatement ps = con.prepareStatement("SELECT uuid FROM "+SQL_TABLES.shopitems.toString()+" WHERE shop_uuid=?"))
		{
			ps.setString(1, shopUuid.toString());
			try(ResultSet rs = ps.executeQuery())
			{
				while(rs.next())
				{
					try
					{
						uuids.add(UUID.fromString(rs.getString(1)));
					}
					catch (IllegalArgumentException e)
					{
						// unreadable uuid, the row can't belong to a loaded item; leave it
					}
				}
			}
		}
		return uuids;
	}

	void DeleteItemRows(Connection con, Collection<UUID> uuids) throws SQLException
	{
		if(uuids.isEmpty()) return;

		for(String[] table : ITEM_TABLES)
		{
			try(PreparedStatement ps = con.prepareStatement("DELETE FROM "+table[0]+" WHERE "+table[1]+"=?"))
			{
				for(UUID uuid : uuids)
				{
					ps.setString(1, uuid.toString());
					ps.addBatch();
				}
				ps.executeBatch();
			}
		}
	}

	void WriteShopRow(Connection con, ShopSnapshot snap) throws SQLException
	{
		try(PreparedStatement ps = con.prepareStatement("REPLACE INTO "+SQL_TABLES.shops.toString()+" "
				+ "(uuid, name, display_name, pages,shop_type, sellM, buyM, expire_percent, expire_cooldown, locked, customer_can_sell,absolutePositions)"
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?)"))
		{
			ps.setString(1, snap.uuid.toString());
			ps.setString(2, Truncate(snap.name, 100));
			ps.setString(3, Truncate(snap.displayName, 100));
			ps.setInt	(4, snap.pages);
			ps.setString(5, "1");
			ps.setFloat	(6, (float)snap.sellM);
			ps.setFloat	(7, (float)snap.buyM);
			ps.setFloat	(8, (float)snap.expirePercent);
			ps.setInt	(9, snap.expireCooldown);
			ps.setInt	(10, (snap.locked ? 1 : 0));
			ps.setInt	(11, (snap.customersCanOnlyBuy ? 1 : 0));
			ps.setInt	(12, (snap.absolutePositions ? 1 : 0));
			ps.executeUpdate();
		}
	}

	void InsertItemRows(Connection con, UUID shopUuid, List<ShopItemRow> rows) throws SQLException
	{
		if(rows.isEmpty()) return;

		try(PreparedStatement items = con.prepareStatement("INSERT INTO "+SQL_TABLES.shopitems.toString()+" (uuid, shop_uuid, type, item_display_name, amount, page, slot, price_type, type_data, itemstack) VALUES(?,?,?,?,?,?,?,?,?,?)");
			PreparedStatement priceValues = con.prepareStatement("INSERT INTO "+SQL_TABLES.price_values.toString()+" (uuid, name, amount, mark) VALUES (?,'',?,'$')");
			PreparedStatement customs = con.prepareStatement("INSERT INTO "+SQL_TABLES.price_customs.toString()+" (uuid, amount, itemstack) VALUES (?,?,?)");
			PreparedStatement customPrice = con.prepareStatement("INSERT INTO "+SQL_TABLES.custom_price.toString()+" (uuid, minimum_stack_amount) VALUES (?,?)");
			PreparedStatement modData = con.prepareStatement("INSERT INTO "+SQL_TABLES.shopitem_moddata.toString()+" (uuid, max_amount, fill_amount, fill_delay, selltime_start, selltime_end) VALUES(?,?,?,?,?,?)");
			PreparedStatement permissions = con.prepareStatement("INSERT INTO "+SQL_TABLES.shopitem_permissions.toString()+" (uuid, name) VALUES(?,?)");
			PreparedStatement worlds = con.prepareStatement("INSERT INTO "+SQL_TABLES.shopitem_worlds.toString()+" (uuid, name) VALUES(?,?)");
			PreparedStatement locations = con.prepareStatement("INSERT INTO "+SQL_TABLES.shopitem_locations.toString()+" (uuid, distance, dis_world, dis_locX, dis_locY, dis_locZ) VALUES(?,?,?,?,?,?)");
			PreparedStatement tags = con.prepareStatement("INSERT INTO "+SQL_TABLES.tags_shopitems.toString()+" (sib_uuid, tag_name) VALUES (?,?)"))
		{
			for(ShopItemRow row : rows)
			{
				String uuid = row.uuid.toString();
				int i = 1;
				items.setString(i++, uuid);
				items.setString(i++, shopUuid.toString());
				items.setString(i++, row.type);
				items.setString(i++, row.displayName);
				items.setInt(i++, row.amount);
				items.setInt(i++, row.page);
				items.setInt(i++, row.slot);
				items.setString(i++, row.priceType.toString());
				items.setString(i++, row.typeData);
				items.setString(i++, row.itemstack);
				items.addBatch();

				if(row.priceValue != null)
				{
					priceValues.setString(1, uuid);
					priceValues.setFloat(2, row.priceValue.floatValue());
					priceValues.addBatch();
				}

				if(row.customAmounts != null)
				{
					for(int c = 0; c < row.customAmounts.size(); c++)
					{
						customs.setString(1, uuid);
						customs.setInt(2, row.customAmounts.get(c));
						customs.setString(3, row.customEncoded.get(c));
						customs.addBatch();
					}
					customPrice.setString(1, uuid);
					customPrice.setInt(2, row.customMinStack);
					customPrice.addBatch();
				}

				for(String tag : row.tags)
				{
					tags.setString(1, uuid);
					tags.setString(2, Truncate(tag, 30));
					tags.addBatch();
				}

				if(row.modData == null) continue;

				i = 1;
				modData.setString(i++, uuid);
				modData.setInt(i++, row.modData._maxAmount);
				modData.setInt(i++, row.modData._fillAmount);
				modData.setInt(i++, row.modData._fillDelayMinutes);
				modData.setInt(i++, row.modData._sellTimeStart);
				modData.setInt(i++, row.modData._sellTimeEnd);
				modData.addBatch();

				if(row.modData._permissions != null)
				{
					for(String permission : row.modData._permissions)
					{
						permissions.setString(1, uuid);
						permissions.setString(2, Truncate(permission, 50));
						permissions.addBatch();
					}
				}

				if(row.modData._worldNames != null)
				{
					for(String worldName : row.modData._worldNames)
					{
						worlds.setString(1, uuid);
						worlds.setString(2, Truncate(worldName, 50));
						worlds.addBatch();
					}
				}

				for(LocationRow loc : row.locations)
				{
					i = 1;
					locations.setString(i++, uuid);
					locations.setInt(i++, loc.distance);
					locations.setString(i++, Truncate(loc.world, 40));
					locations.setInt(i++, loc.x);
					locations.setInt(i++, loc.y);
					locations.setInt(i++, loc.z);
					locations.addBatch();
				}
			}

			items.executeBatch();
			priceValues.executeBatch();
			customs.executeBatch();
			customPrice.executeBatch();
			modData.executeBatch();
			permissions.executeBatch();
			worlds.executeBatch();
			locations.executeBatch();
			tags.executeBatch();
		}
	}

	// ==================== DELETING ====================

	public void DeleteShopAsync(ShopNormal shop)
	{
		final UUID shopUuid = shop.GetUUID();
		final String shopName = shop.GetName();
		final Set<UUID> memoryUuids = new HashSet<>();
		for(ShopItemSeller[] page : shop.get_items())
		{
			for(ShopItemSeller sis : page)
			{
				if(sis != null) memoryUuids.add(sis.GetUUID());
			}
		}

		Bukkit.getScheduler().runTaskAsynchronously(_main, () ->
		{
			synchronized (_writeLock)
			{
				try(Connection con = _main.GetSQL().GetConnection())
				{
					con.setAutoCommit(false);
					try
					{
						Set<UUID> itemUuids = SelectShopItemUuids(con, shopUuid);
						itemUuids.addAll(memoryUuids);
						DeleteItemRows(con, itemUuids);

						try(PreparedStatement ps = con.prepareStatement("DELETE FROM "+SQL_TABLES.shops.toString()+" WHERE uuid=?"))
						{
							ps.setString(1, shopUuid.toString());
							ps.executeUpdate();
						}
						con.commit();
					}
					catch (Exception e)
					{
						con.rollback();
						throw e;
					}
				}
				catch (Exception e)
				{
					_main.getLogger().log(Level.SEVERE, "Couldn't delete shop "+shopName+" from the database", e);
				}
			}
		});
	}

	public void DeleteShopItemAsync(UUID shopItemUuid)
	{
		Bukkit.getScheduler().runTaskAsynchronously(_main, () ->
		{
			synchronized (_writeLock)
			{
				try(Connection con = _main.GetSQL().GetConnection())
				{
					con.setAutoCommit(false);
					try
					{
						DeleteItemRows(con, List.of(shopItemUuid));
						con.commit();
					}
					catch (Exception e)
					{
						con.rollback();
						throw e;
					}
				}
				catch (Exception e)
				{
					_main.getLogger().log(Level.SEVERE, "Couldn't delete shop item "+shopItemUuid+" from the database", e);
				}
			}
		});
	}

	// ==================== MATERIALS & UNIQUES ====================

	void SaveMaterialPrice(List<Material> mats, double price)
	{
		// Only the price column changes; smart_multiplier of existing rows is kept
		try (Connection con = _main.GetSQL().GetConnection();
			 PreparedStatement ps = con.prepareStatement("INSERT INTO "+SQL_TABLES.price_materials.toString()+" (material, price) VALUES(?,?) ON DUPLICATE KEY UPDATE price=VALUES(price)"))
		{
			for(Material mat : mats)
			{
				ps.setString(1, mat.name());
				ps.setFloat(2, (float)price);
				ps.addBatch();
			}

			ps.executeBatch();
		}
		catch (Exception e)
		{
			Bukkit.getLogger().info("ShopManagerSQL:SavematerialPrice:Couldnt Save Material total of ( "+mats.size()+" )  to database");
			e.printStackTrace();
		}

	}

	void SaveUniqueItemAsync(ShopItemUnique siu)
	{
		if(siu.GetRealItem().getType() == Material.AIR) return;

		// Captured here, on the calling (main) thread
		final String uuid = siu.GetUUID().toString();
		final String displayName = Truncate(ImusAPI._metods.GetItemDisplayName(siu.GetRealItem()), 100);
		final float price = (float)siu.GetItemPrice().GetPrice();
		final String itemstack = ImusAPI._metods.EncodeItemStack(siu.GetRealItem());

		Bukkit.getScheduler().runTaskAsynchronously(_main, () ->
		{
			try (Connection con = _main.GetSQL().GetConnection();
				 PreparedStatement ps = con.prepareStatement("REPLACE INTO "+SQL_TABLES.uniques.toString()+" (uuid, item_display_name, price, itemstack) VALUES (?, ?, ?, ?)"))
			{
				ps.setString(1, uuid);
				ps.setString(2, displayName);
				ps.setFloat(3, price);
				ps.setString(4, itemstack);
				ps.executeUpdate();
			}
			catch (Exception e)
			{
				_main.getLogger().log(Level.SEVERE, "Couldn't save unique "+displayName, e);
			}
		});
	}

	public void DeleteUniqueItemAsync(ShopItemBase sib)
	{
		final String uuid = sib.GetUUID().toString();
		Bukkit.getScheduler().runTaskAsynchronously(_main, () ->
		{
			try (Connection con = _main.GetSQL().GetConnection();
				 PreparedStatement ps = con.prepareStatement("DELETE FROM "+SQL_TABLES.uniques.toString()+" WHERE uuid=?"))
			{
				ps.setString(1, uuid);
				ps.executeUpdate();
			}
			catch (Exception e)
			{
				_main.getLogger().log(Level.SEVERE, "Couldn't delete unique "+uuid, e);
			}
		});
	}
}
