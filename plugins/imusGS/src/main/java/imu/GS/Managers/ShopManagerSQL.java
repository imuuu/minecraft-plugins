package imu.GS.Managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import imu.GS.ENUMs.ItemPriceType;
import imu.GS.ENUMs.SQL_TABLES;
import imu.GS.ENUMs.ShopItemType;
import imu.GS.ENUMs.TransactionAction;
import imu.GS.Main.Main;
import imu.GS.Other.CustomPriceData;
import imu.GS.Other.LogData;
import imu.GS.ShopUtl.Shop;
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
	public ShopManagerSQL(Main main, ShopManager shopManager) 
	{
		_main = main;
		_shopManager = shopManager;
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
			
			//_main.getLogger().info("==> shops");
			
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
			
			//_main.getLogger().info("===> shopItems");
			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.shopitem_permissions.toString()+" ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "uuid CHAR(36), "					
					+ "name VARCHAR(50), "					
					+ "PRIMARY KEY(id));");

			ps.executeUpdate();
			//_main.getLogger().info("===> shopItems => permissions");
			
			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.shopitem_worlds.toString()+" ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "uuid CHAR(36), "					
					+ "name VARCHAR(50), "					
					+ "PRIMARY KEY(id));");

			ps.executeUpdate();
			//_main.getLogger().info("===> shopItems => worldNames");
			
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
			//_main.getLogger().info("===> shopItems => locations");
			
			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.price_customs.toString()+" ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "uuid CHAR(36), "				
					+ "amount INT(20), "				
					+ "itemstack TEXT(16000),"								
					+ "PRIMARY KEY(id));");

			ps.executeUpdate();
			//_main.getLogger().info("===> price type => custom");
			
			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.price_values.toString()+" ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "uuid CHAR(36), "
					+ "name VARCHAR(36), "				
					+ "amount FLOAT(20), "
					+ "mark VARCHAR(10), "					
					+ "PRIMARY KEY(id));");

			ps.executeUpdate();
			//_main.getLogger().info("===> price type => priceValues");
			
			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.custom_price.toString()+" ("
					+ "uuid CHAR(36), "				
					+ "minimum_stack_amount FLOAT(20), "							
					+ "PRIMARY KEY(uuid));");

			ps.executeUpdate();
			//_main.getLogger().info("===> CustomPrice");
			
			
			
			
			
			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.tags_material.toString()+" ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "material_name VARCHAR(50) NOT NULL, "
					+ "tag_name VARCHAR(30),"
					+ "PRIMARY KEY(id));");
			ps.executeUpdate();
			//_main.getLogger().info("=====> tags_materials");
			
			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.tags_shopitems.toString()+" ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "sib_uuid CHAR(36) NOT NULL, "
					+ "tag_name VARCHAR(30),"
					+ "PRIMARY KEY(id));");
			ps.executeUpdate();
			//_main.getLogger().info("=====> tags_shopitems");

			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.uniques.toString()+" ("
					+ "uuid CHAR(36), "	
					+ "item_display_name VARCHAR(100),"
					+ "price FLOAT(20), "
					+ "itemstack TEXT(16000),"
					+ "PRIMARY KEY(uuid));");
			ps.executeUpdate();
			//_main.getLogger().info("=====> uniques");
			
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
			//_main.getLogger().info("=====> log");
						
			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.shopitem_moddata.toString()+" ("
					+ "id INT NOT NULL AUTO_INCREMENT, "
					+ "uuid CHAR(36) NOT NULL, "
					+ "max_amount 		INT(20),"
					+ "fill_amount 		INT(20),"
					+ "fill_delay 		INT(20),"
					
					+ "selltime_start 	INT(20),"
					+ "selltime_end		INT(20),"
					+ "PRIMARY KEY(id));");
			
			//_main.getLogger().info("=====> modData");
			ps.executeUpdate();
			
			
			ps.close();
		} catch (Exception e) 
		{
			_main.getLogger().info("Couldnt load tables correctly");
			e.printStackTrace();
		}
		_main.getLogger().info("===TABLE LOADING FINNISHED===");
	}
	
	public BukkitTask LogPurchaseAsync(Player player, List<LogData> logs, int iterations)
	{
		//_main.getLogger().info("Loging..");
		return new BukkitRunnable() 
		{	
			@Override
			public void run() 
			{
				String[] ench_str = new String[logs.size()];
				for(int i = 0; i < logs.size(); ++i)
				{
					LinkedList<String> enchants = new LinkedList<>();
					for(Map.Entry<Enchantment, Integer> entry : Metods._ins.GetEnchantsWithLevels(logs.get(i).Get_shopitem().GetRealItem()).entrySet())
					{
						enchants.add(entry.getKey().getKey().getKey().toUpperCase()+":"+entry.getValue());
					}
					;
					ench_str[i] = enchants.isEmpty() ? " " : Metods._ins.CombineArrayToOneString(enchants, ";");
				}
				try(Connection con = _main.GetSQL().GetConnection())
				{
					
					PreparedStatement ps = con.prepareStatement("INSERT INTO "+SQL_TABLES.log_transaction.toString()+" "
							+ "(player_uuid, player_name, action, shop_uuid, shopname, shopitem_uuid, amount, price, cal_total_price,custom_price_view, itemstack_displayname, itemstack_enchants,itemstack) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
					
					int count = 0;
					for(LogData data : logs)
					{
						int i = 1;
						ShopItemBase sib = data.Get_shopitem();
						ps.setString(i++, player.getUniqueId().toString());
						ps.setString(i++, player.getName());
						ps.setString(i++, sib instanceof ShopItemSeller ? TransactionAction.SELL.toString() : TransactionAction.BUY.toString() );
						ps.setString(i++, sib.GetShop().GetUUID().toString());
						ps.setString(i++, sib.GetShop().GetName());
						ps.setString(i++, sib.GetUUID().toString());
						ps.setInt(i++, data.Get_amount());
						ps.setFloat(i++, (float)sib.GetItemPrice().GetPrice());
						ps.setFloat(i++, (float) Metods.Round((data.Get_price() * (sib instanceof ShopItemSeller ? 1 : -1))));
						ps.setString(i++, sib.GetItemPrice() instanceof PriceCustom ? ((PriceCustom)sib.GetItemPrice()).GetViewStringOfItems(data.Get_amount()) : "");
						ps.setString(i++, ImusAPI._metods.GetItemDisplayName(sib.GetRealItem()));
						ps.setString(i++, ench_str[count]);
						ps.setString(i++, ImusAPI._metods.EncodeItemStack(sib.GetRealItem()));
						count++;
						ps.addBatch();
					}
					
					ps.executeBatch();
					
					ps.close();
					
					
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
	
	void LoadShops()
	{
		
		try(Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.shops.toString()+"");
			ResultSet rs = ps.executeQuery();

			if(!rs.isBeforeFirst())
			{
				_main.getLogger().info("No shops found!");
				rs.close();
				ps.close();
				return;
			}
			while(rs.next())
			{
				int i = 1;
				UUID uuid = UUID.fromString(rs.getString(i++));
				String name = rs.getString(i++);
				String _displayName = rs.getString(i++);
				int pages = rs.getInt(i++);
				//int type  = rs.getInt(i++);
				i++;
				float sellM = rs.getFloat(i++);
				float buyM = rs.getFloat(i++);
				float expirePrercent = rs.getFloat(i++);
				int expireCooldown = rs.getInt(i++);
				boolean locked = (rs.getInt(i++) != 0);
				boolean customerCanOnlySell = (rs.getInt(i++) != 0);
				boolean absolutePos = (rs.getInt(i++) != 0);
				ShopNormal shop = new ShopNormal(_main, uuid,_displayName, pages);
				shop.set_sellM(sellM);
				shop.set_buyM(buyM);
				shop.set_expire_percent(expirePrercent);
				shop.set_expire_cooldown_m(expireCooldown);
				shop.SetLocked(locked);
				shop.SetCustomersCanOnlyBuy(customerCanOnlySell);
				shop.SetAbsolutePosBool(absolutePos);	
				_shopManager.GetShops().add(shop);
				_main.getLogger().info("Shop loaded named: "+ name);
						
				
			}
			
			
		
			rs.close();
			ps.close();
			
		} catch (SQLException e) 
		{
			Bukkit.getLogger().info("ShopManagerSQL:LoadShops: ERROR");
			e.printStackTrace();
		}
	}
	
	public void LoadShopItems()
	{
		try 
		{
			for(Shop sBase : _shopManager.GetShops())
			{
				if(!(sBase instanceof ShopNormal)) continue;
				
				ShopNormal shop = (ShopNormal)sBase;
				LoadShopItems(shop);
				LoadModDataForShopItems(shop);
			}
		} 
		catch (Exception e) 
		{
			Bukkit.getLogger().info("ShopManagerSQL:LoadShopsItems: ERROR");
			e.printStackTrace();
		}
		_shopManager.UpdateTabCompliters();
	}
	
	
	
	void LoadModDataForShopItems(ShopNormal shopBase)
	{
		try (Connection con = _main.GetSQL().GetConnection())
		{
			for(ShopItemBase[] sibs : shopBase.get_items())
			{
				for(ShopItemBase sib : sibs)
				{
					if(sib == null) continue;
					
					if(!(sib instanceof ShopItemStockable)) continue;
					
					UUID uuid = sib.GetUUID();
					
					PreparedStatement ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.shopitem_moddata.toString()+" WHERE uuid='"+uuid.toString()+"';");
					ResultSet rs = ps.executeQuery();
					ShopItemModData modData = ((ShopItemStockable)sib).GetModData();
					
					if(rs.isBeforeFirst())
					{
						rs.next();
						modData._maxAmount = rs.getInt(3);
						modData._fillAmount = rs.getInt(4);
						modData._fillDelayMinutes =rs.getInt(5);
						modData._sellTimeStart = rs.getInt(6);
						modData._sellTimeEnd = rs.getInt(7);
					}
					
					
					ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.shopitem_locations.toString()+" WHERE uuid='"+uuid.toString()+"';");
					rs = ps.executeQuery();
					
					if(rs.isBeforeFirst())
					{
						while(rs.next())
						{
							int l = 1;
							l++; l++; //id uuid
							int distance = rs.getInt(l++);
							Location loc = new Location(Bukkit.getWorld(rs.getString(l++)), rs.getInt(l++), rs.getInt(l++), rs.getInt(l++));
							modData.AddLocation(distance, loc);
							//modData.
							
						}
						_main.getLogger().info("distance added");
					}
					ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.shopitem_permissions.toString()+" WHERE uuid='"+uuid.toString()+"';");
					rs = ps.executeQuery();
					
					if(rs.isBeforeFirst())
					{
						while(rs.next())
						{
							modData.AddPermission(rs.getString(3));						
						}
						_main.getLogger().info("permission added");
					}
					
					ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.shopitem_worlds.toString()+" WHERE uuid='"+uuid.toString()+"';");
					rs = ps.executeQuery();
					if(rs.isBeforeFirst())
					{
						while(rs.next())
						{
							modData.AddWorldName(rs.getString(3));						
						}
						_main.getLogger().info("world added");
					}
					
					ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.tags_shopitems+" WHERE sib_uuid='"+uuid.toString()+"';");
					rs = ps.executeQuery();
					
					if(rs.isBeforeFirst())
					{
						while(rs.next())
						{
							modData.AddTag(rs.getString(3).toLowerCase());
						}
					}
					((ShopItemStockable)sib).SetModData(modData);
					rs.close();

				}
			}
		} 
		catch (Exception e) 
		{
			Bukkit.getLogger().info("ShopManagerSQL:LoadShopItemModData: ERROR");
			e.printStackTrace();
		}
	}
	
	void SaveMaterialPrice(List<Material> mats, double price)
	{
		
		try (Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement("REPLACE INTO "+SQL_TABLES.price_materials.toString()+" (material, price) VALUES(?,?);");
			for(Material mat : mats)
			{
				ps.setString(1, mat.name());
				ps.setFloat(2, (float)price);
				if(MaterialManager._ins.GetPriceMaterial(mat).HasSmartData()) ps.setFloat(3, (float)MaterialManager._ins.GetPriceMaterial(mat).GetSmartData().GetMultiplier());
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
	
	//@SuppressWarnings("deprecation")
	void LoadShopItems(ShopNormal shop) 
	{
		try(Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.shopitems.toString()+" WHERE shop_uuid='"+shop.GetUUID().toString()+"';");
			ResultSet rs2 = ps.executeQuery();
			
			if(rs2.isBeforeFirst())
			{
				while(rs2.next())
				{
					int i = 1;
					UUID uuid = UUID.fromString(rs2.getString(i++));			
					String shopName = rs2.getString(i++);
					ShopItemType siType = ShopItemType.valueOf(rs2.getString(i++));				
					i++;//String displayName = rs2.getString(i++);
					int amount = rs2.getInt(i++);
					int page = rs2.getInt(i++);
					int slot = rs2.getInt(i++);

					ItemPriceType priceType = ItemPriceType.valueOf(rs2.getString(i++));

					String typeData = rs2.getString(i++);
					ItemStack stack = ImusAPI._metods.DecodeItemStack(rs2.getString(i++));					
					
					if(stack == null)
					{
						System.out.println("Couldnt load item named: "+uuid+ " from shop: "+shopName);
						continue;
					}
					ShopItemSeller sis = null;
					switch (siType) 
					{
					case NORMAL:
						sis = new ShopItemSeller(_main,shop, stack, amount);
						break;
					case UNIQUE:
						sis = new ShopItemSeller(_main, shop, stack, amount);
						break;
					case STOCKABLE:
						sis = new ShopItemStockable(_main, shop, stack, amount);
						break;
					default:
						sis = new ShopItemSeller(_main,shop, stack, amount);
						break;
						}

					sis.SetUUID(uuid);
						
					switch (priceType) {
					case None:
						sis.SetItemPrice( _main.GetMaterialManager().GetPriceMaterialAndCheck(stack));
						break;
					case PriceCustom:
						sis.SetItemPrice( GetPriceCustom(uuid));
						break;
					case PriceOwn:
						sis.SetItemPrice(new PriceOwn().SetPrice(GetPriceValue(uuid)));
						break;
					case PriceUnique:
						sis.SetItemPrice(GetUniquePrice( _shopManager.GetUniqueManager().GetUniqueUUID(stack)));
						break;
					
					}
					
					if(!(typeData == null || typeData == ""))  //!Strings.isNullOrEmpty(typeData)
						sis.ParseJsonData(new JsonParser().parse(typeData).getAsJsonObject());
				
					//_main.getLogger().info("sis: "+sis.GetRealItem().getType()+ " price: "+sis.GetItemPrice());
					shop.SetItem(sis, page, slot);
				}
			}
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}

		
	}
	
	double GetPriceValue(UUID shopItemSellerUUID) 
	{
		try(Connection con = _main.GetSQL().GetConnection())
		{
			double priceValue = 0;
			PreparedStatement ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.price_values.toString()+" WHERE uuid='"+shopItemSellerUUID.toString()+"';");
			ResultSet rs = ps.executeQuery();
			if(rs.isBeforeFirst()) 
			{
				rs.next();
				priceValue = (double)rs.getFloat(4);
			}
			rs.close();
			ps.close();
			return priceValue;
			
		}
		catch(SQLException e)
		{
			return 9999999;
		}
		
		
		
	}
	
	void SavePriceValue(UUID shopItemSellerUUID, double value) 
	{		
		try(Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement("INSERT INTO "+SQL_TABLES.price_values.toString()+" "
					+ "(uuid, name, amount, mark) VALUES (?,?, ?, ?)");
			ps.setString(1, shopItemSellerUUID.toString());
			ps.setString(2, "");
			ps.setFloat(3, (float)value);
			ps.setString(4, "$");
			ps.executeUpdate();
			
			ps.close();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	PriceCustom GetPriceCustom(UUID uuid)
	{
		try(Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.price_customs.toString()+" WHERE uuid='"+uuid.toString()+"';");
			ResultSet rs = ps.executeQuery();
			PriceCustom pc = new PriceCustom();
			ArrayList<CustomPriceData> datas = new ArrayList<>();
			if(rs.isBeforeFirst())
			{		
				
				while(rs.next())
				{
					ItemStack stack = ImusAPI._metods.DecodeItemStack(rs.getString(4));
					stack.setAmount(1);
					datas.add(new CustomPriceData(stack, rs.getInt(3)));							
				}
				
			}
			ps.close();
			
			ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.custom_price.toString()+" WHERE uuid='"+uuid.toString()+"';");
			rs = ps.executeQuery();
			int minimumStackAmount = 1;
			if(rs.isBeforeFirst())
			{
				rs.next();
				minimumStackAmount = rs.getInt(2);
			}
			
			CustomPriceData[] array = new CustomPriceData[datas.size()];
			for(int i = 0; i < array.length; i++) {array[i] = datas.get(i);}
				
			pc.SetItemsAndPrice(array, GetPriceValue(uuid), minimumStackAmount);
			
			rs.close();
			ps.close();
			return pc;
			
		}
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		
		
		return null;
		

	}
	

	void SavePriceCustom(UUID shopItemUUID, PriceCustom pc)
	{
		try(Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement("INSERT INTO "+SQL_TABLES.price_customs.toString()+" "
					+ "(uuid, amount, itemstack) VALUES (?,?,?)");
			for(CustomPriceData item : pc.GetItems())
			{
				//_main.getLogger().info("saving custom item: "+item._stack.getType());
				
				ps.setString(1, shopItemUUID.toString());
				ps.setInt(2, item._amount);
				ps.setString(3, ImusAPI._metods.EncodeItemStack(item._stack));
				ps.addBatch();
				
			}
			
			ps.executeBatch();

			if(pc.GetPrice() >= 0)
			{
				SavePriceValue(shopItemUUID, pc.GetPrice());
			}
			
			ps = con.prepareStatement("REPLACE INTO "+SQL_TABLES.custom_price.toString()+" "
					+ "(uuid, minimum_stack_amount) VALUES (?,?)");
			ps.setString(1, shopItemUUID.toString());
			ps.setInt(2, pc.GetMinimumStackAmount());
			ps.executeUpdate();
			
			ps.close();
			
		} catch (SQLException e) 
		{
			e.printStackTrace();
		}

		
	}
	
	public BukkitTask DeleteShopAsync(ShopNormal shop)
	{
		return new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				final String quarry = "DELETE FROM "+SQL_TABLES.shops.toString()+" WHERE uuid='"+shop.GetUUID().toString()+"'";
				try (Connection con = _main.GetSQL().GetConnection())
				{	
					PreparedStatement ps = con.prepareStatement(quarry);
							
					ps.executeUpdate();
									
				} catch (Exception e) 
				{
					Bukkit.getLogger().info("ShopManagerSQL:DeleteShopAsync:TRY TO DELETE SHOP NAMED "+shop.GetName()+".. something went wrong!");
					e.printStackTrace();
				}
				
				
				DeleteAllShopItems(shop);
				
			}
		}.runTaskAsynchronously(_main);
		
		
	}
	
	public void DeleteShopItem(ArrayList<ShopItemBase> sibs, boolean closeAfter)
	{
		
		try (Connection con = _main.GetSQL().GetConnection())
		{
			Statement stms = con.createStatement();
//			PreparedStatement ps = con.prepareStatement(""
//					+ "DELETE FROM "+SQL_TABLES.shopitems.toString()+" WHERE uuid='"+sib.GetUUID()+"'");			
//			ps.executeUpdate();
			
//			String statement ="DELETE FROM "
//					+ SQL_TABLES.shopitems.toString()+ ", "
//					+SQL_TABLES.shopitem_locations.toString() +", "
//					+SQL_TABLES.shopitem_permissions.toString() +", "
//					+SQL_TABLES.shopitem_worlds.toString() +", "
//					+SQL_TABLES.price_customs.toString() +", "
//					+SQL_TABLES.tags_shopitems.toString() +", "
//					+ "WHERE uuid='"+sib.GetUUID()+"';";
//			
//			System.out.println("str: "+statement);
//			PreparedStatement ps = con.prepareStatement(statement);
			
			for(ShopItemBase sib : sibs)
			{
				if(sib == null) continue;
				
				stms.addBatch(""
						+ "DELETE FROM "+SQL_TABLES.shopitem_locations.toString()+" WHERE uuid='"+sib.GetUUID()+"';");			
				
				stms.addBatch(""
						+ "DELETE FROM "+SQL_TABLES.shopitem_permissions.toString()+" WHERE uuid='"+sib.GetUUID()+"';");			
				
				stms.addBatch(""
						+ "DELETE FROM "+SQL_TABLES.shopitem_worlds.toString()+" WHERE uuid='"+sib.GetUUID()+"';");			
				
				stms.addBatch(""
						+ "DELETE FROM "+SQL_TABLES.price_customs.toString()+" WHERE uuid='"+sib.GetUUID()+"';");			
				
				stms.addBatch(String.format("DELETE FROM "+SQL_TABLES.tags_shopitems.toString()+" WHERE sib_uuid='%s';",sib.GetUUID().toString()));
				
				stms.addBatch(String.format("DELETE FROM "+SQL_TABLES.price_values.toString()+" WHERE uuid='%s';",sib.GetUUID().toString()));
				
				stms.addBatch(String.format("DELETE FROM "+SQL_TABLES.price_customs.toString()+" WHERE uuid='%s';",sib.toString()));
				
				stms.addBatch("DELETE FROM "+SQL_TABLES.shopitem_moddata.toString()+ " WHERE uuid='"+sib.GetUUID().toString()+"';");
			}

			stms.executeBatch();
			stms.clearBatch();
			
			if(closeAfter)
			{
				stms.close();
			}
			
			
			//System.out.println("sib deleted: "+sib.GetDisplayItem().getType());
		} 
		catch (Exception e) 
		{
			Bukkit.getLogger().info("ShopManagerSQL:DeleteShopItem:111 Error happend deleting shopitem:");
			e.printStackTrace();
			return;
		}
		
		
	}
	void DeleteAllShopItems(ShopNormal shop)
	{
		
		try (Connection con = _main.GetSQL().GetConnection()) 
		{
			PreparedStatement ps = con.prepareStatement("DELETE FROM "+SQL_TABLES.shopitems+" WHERE shop_uuid='"+shop.GetUUID().toString()+"';");
			ps.executeUpdate();
		} catch (Exception e) 
		{
			Bukkit.getLogger().info("ShopManagerSQL:DeleteAllShopItems: ERROR");
		}
		
		ArrayList<ShopItemBase> sibs = new ArrayList<>();
		for (ShopItemSeller[] siss : shop.get_items()) 
		{
			for(int slot = 0; slot < siss.length; ++slot)
			{
				sibs.add(siss[slot]);
			}
		}
		
		DeleteShopItem(sibs, true);
	}
	public BukkitTask SaveShopAsync(ShopNormal shop)   
	{
		//boolean lock = shop.HasLocked();
		//shop.SetLocked(true);
		
		return new BukkitRunnable() { //ERROR ??
			
			@Override
			public void run() 
			{
				shop._temp_lock = true;
				
				DeleteAllShopItems(shop);
				
				final String quarry = "REPLACE INTO "+SQL_TABLES.shops.toString()+" "
						+ "(uuid, name, display_name, pages,shop_type, sellM, buyM, expire_percent, expire_cooldown, locked, customer_can_sell,absolutePositions)"
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?,?,?,?,?)";
				try(Connection con = _main.GetSQL().GetConnection()) 
				{
					PreparedStatement ps = con.prepareStatement(quarry);
					_main.getLogger().info("Saving shop: "+shop.GetName());
					
					ps.setString(1, shop.GetUUID().toString());
					ps.setString(2, shop.GetName());
					ps.setString(3, shop.GetDisplayName());
					ps.setInt	(4, shop.get_items().size());
					ps.setString(5, "1");
					ps.setFloat	(6, (float)shop.get_sellM());
					ps.setFloat	(7, (float)shop.get_buyM());
					ps.setFloat	(8, (float)shop.get_expire_percent());
					ps.setInt	(9, shop.get_expire_cooldown_m());		
					ps.setInt	(10, (shop.HasLocked() ? 1 : 0));		
					ps.setInt	(11, (shop.GetCustomersCanOnlyBuy() ? 1 : 0));		
					ps.setInt	(12, (shop.GetAbsolutePosBool() ? 1 : 0));		
					ps.executeUpdate();

					
				} catch (Exception e) 
				{
					Bukkit.getLogger().info("ShopManagerSQL:SaveShopAsync:Couldnt save shop data, probably SQL's shops table is missing");
					e.printStackTrace();
				}
				
				for (ShopItemSeller[] siss : shop.get_items()) 
				{
					for(int slot = 0; slot < siss.length; ++slot)
					{
						ShopItemSeller sis = siss[slot];
						
						if(sis == null)
						{												
							continue;
						}
						
						SaveShopItem(sis, false);
						
					}
				}
				
				_main.GetTagManager().LoadAllShopItemTagsNamesAsync();
				shop._temp_lock = false;
			}
		}.runTaskAsynchronously(_main);

	}
	
	
	ArrayList<String> GetINSERTModDataStatement(ShopItemStockable sis)
	{
		ArrayList<String> array = new ArrayList<String>();
		String modData = "INSERT INTO "+SQL_TABLES.shopitem_moddata.toString()+" (uuid, max_amount, fill_amount, fill_delay, selltime_start,selltime_end) VALUES("
				+  "'"+sis.GetUUID().toString()+"',"
				+ sis.GetModData()._maxAmount+","
				+ sis.GetModData()._fillAmount+","
				+ sis.GetModData()._fillDelayMinutes+","
				+ sis.GetModData()._sellTimeStart+","
				+ sis.GetModData()._sellTimeEnd+""
				+ ");";
		
		array.add(modData);
		
		if(sis.GetItemPrice() instanceof PriceOwn)
		{
			String priceOwn = "INSERT INTO "+SQL_TABLES.price_values.toString()+" "
					+ "(uuid, name, amount, mark) VALUES ("
					+ "'"+sis.GetUUID().toString()+"',"
					+ "'-',"
					+ (float)sis.GetItemPrice().GetPrice()+","
					+ "'$'"
					+ ");";
			array.add(priceOwn);
		}
		
		if(sis.GetModData()._permissions != null)
		{
			for(String permission : sis.GetModData()._permissions)
			{
				String permis ="INSERT INTO "+SQL_TABLES.shopitem_permissions.toString() +"(uuid, name) VALUES("
						+  "'"+sis.GetUUID().toString()+"',"
						+ "\""+permission+"\""
						+ ");";
				array.add(permis);
			}
		}

		if(sis.GetModData()._worldNames != null)
		{
			for(String worldName : sis.GetModData()._worldNames)
			{
				String world = "INSERT INTO "+SQL_TABLES.shopitem_worlds.toString()+" "
						+ "(uuid, name) VALUES ("
						+ "'"+sis.GetUUID().toString()+"',"
						+ "\""+worldName+"\","
						+ ");";
				array.add(world);
			}
		}
		
		if(sis.GetModData()._locations != null)
		{
			for(Tuple<Integer, Location> disLoc : sis.GetModData()._locations)
			{
				Location loc = disLoc.GetValue();
				String locSTR = "INSERT INTO "+SQL_TABLES.shopitem_locations.toString()+" "
						+ "(uuid, distance, dis_world, dis_locX, dis_locY, dis_locZ) VALUES ("
						+  "'"+sis.GetUUID().toString()+"',"
						+ disLoc.GetKey()+","
						+ "\""+loc.getWorld().getName()+"\","
						+ loc.getBlockX()+","
						+ loc.getBlockY()+","
						+ loc.getBlockZ()+""
						+ ");";
				
				array.add(locSTR);
			}
		}
		
		
		return array;
	}
	
	ArrayList<String> GetREPLACEShopItemStatements(ShopItemSeller sib)
	{
		ArrayList<String> statements = new ArrayList<String>();
		String shopItem = "REPLACE INTO "+SQL_TABLES.shopitems.toString()+" (uuid, shop_uuid, type, item_display_name, amount, page, slot, price_type, type_data, itemstack) VALUES("
				+"'"+sib.GetUUID().toString()+"',"
				+"'"+sib.GetShop().GetUUID().toString()+"',"
				+"'"+sib.GetItemType().toString()+"',"
				+"\""+ImusAPI._metods.GetItemDisplayName(sib.GetRealItem())+"\","
				+sib.Get_amount()+","
				+sib.GetPage()+","
				+sib.GetSlot()+","
				+"'"+GetPriceType(sib).toString()+"',"
				+"\""+new Gson().toJson(sib.GetJsonData())+"\","
				+"'"+ImusAPI._metods.EncodeItemStack(sib.GetRealItem())+"'"
				+");";
		

		statements.add(shopItem);
		if(sib instanceof ShopItemStockable) statements.addAll(GetINSERTModDataStatement((ShopItemStockable)sib));
		
		return statements;
	}
	ItemPriceType GetPriceType(ShopItemBase sib)
	{
		if(sib.GetItemPrice().getClass().equals(PriceCustom.class)) return ItemPriceType.PriceCustom;

		
		if(sib.GetItemPrice().getClass().equals(PriceOwn.class)) return ItemPriceType.PriceOwn;

		if(sib.GetItemPrice().getClass().equals(PriceUnique.class)) return ItemPriceType.PriceUnique;
		
		return ItemPriceType.None;
	}
	
	
	public void SaveShopItem(ShopItemSeller sis, boolean deleteAllData)
	{
		if(deleteAllData)
		{
			ArrayList<ShopItemBase> ar = new ArrayList<ShopItemBase>();
			ar.add(sis);
			DeleteShopItem(ar, false);
		}
		
		try(Connection con = _main.GetSQL().GetConnection())
		{		
			if(GetPriceType(sis) == ItemPriceType.PriceCustom) SavePriceCustom(sis.GetUUID(), ((PriceCustom)sis.GetItemPrice()));
			
			Statement stmt = con.createStatement();			
			
			for(String strStatements : GetREPLACEShopItemStatements(sis))
			{
				stmt.addBatch(strStatements);
			}
			
			stmt.executeBatch();

		} 
		catch (Exception e) 
		{
			Bukkit.getLogger().info("ShopManagerSQL:SaveShopItem:Saving shopitem: Couldnt add item NEW");
			e.printStackTrace();
		}
		
	}
	


	BukkitTask SaveUniqueItemAsync(ShopItemUnique siu)
	{
		return new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				
				if(siu.GetRealItem().getType() == Material.AIR) return;
				
				final String quarry = "REPLACE INTO uniques "
						+ "(uuid, item_display_name, price, itemstack)"
						+ "VALUES (?, ?, ?, ?)";
				try (Connection con = _main.GetSQL().GetConnection())
				{
					PreparedStatement ps = con.prepareStatement(quarry);
					int i = 1;
					ps.setString(i++, siu.GetUUID().toString());
					ps.setString(i++, ImusAPI._metods.GetItemDisplayName(siu.GetRealItem()));
					ps.setFloat(i++, (float)siu.GetItemPrice().GetPrice());
					ps.setString(i++, ImusAPI._metods.EncodeItemStack(siu.GetRealItem()));
					ps.executeUpdate();
				} 
				catch (Exception e) 
				{
					
					Bukkit.getLogger().info("ShopManagerSQL:SaveUniqueItemAsync:Couldnt save unique: "+ImusAPI._metods.GetItemDisplayName(siu.GetDisplayItem()));
					e.printStackTrace();
				}	
			}
		}.runTaskAsynchronously(_main);
		
	}
	
	public BukkitTask DeleteUniqueItemAsync(ShopItemBase sib)
	{
		return new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				final String quarry = "DELETE FROM uniques WHERE uuid='"+sib.GetUUID().toString()+"';";
				try (Connection con = _main.GetSQL().GetConnection())
				{				
					PreparedStatement ps = con.prepareStatement(quarry);
					ps.executeUpdate();
				} 
				catch (Exception e) 
				{
					Bukkit.getLogger().info("ShopManagerSQL:DeleteUniqueItemAsync:Couldnt save unique");
					e.printStackTrace();
				}	
			}
		}.runTaskAsynchronously(_main);
		
	}
	
	private PriceUnique GetUniquePrice(UUID uuid)
	{
		PriceUnique priceUnique  = new PriceUnique();
		try(Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement("SELECT * FROM uniques WHERE uuid='"+uuid.toString()+"';");
			ResultSet rs = ps.executeQuery();
			if(rs.isBeforeFirst())
			{
				rs.next();
				//_main.getLogger().info("rs: "+rs.getDouble(3));
				double price = (double)rs.getFloat(3);
				priceUnique.SetPrice(price);
			}
			
			rs.close();
			ps.close();
			
		} 
		catch (Exception e) 
		{
			Bukkit.getLogger().info("ShopManagerSQL:GetUniquePrice:Couldnt get unique price");
			e.printStackTrace();
		}	
		return priceUnique;
	}
	
	void LoadUniques()
	{
		
		try (Connection con = _main.GetSQL().GetConnection())
		{		
			PreparedStatement ps = con.prepareStatement("SELECT * FROM uniques");
			ResultSet rs = ps.executeQuery();
			if(!rs.isBeforeFirst())
			{
				//NO DATA
				_main.getLogger().info("Uniques not found!");
				return;
			}
			while(rs.next())
			{
				int i = 1;
				UUID uuid = UUID.fromString(rs.getString(i++));
				i++;
				double price = (double)rs.getFloat(i++);
				ItemStack stack = ImusAPI._metods.DecodeItemStack(rs.getString(i++));
				//_main.getLogger().info("Unique loaded: "+price);
				ShopItemUnique unique = new ShopItemUnique(_main, null, stack, 1);
				unique.SetUUID(uuid);
				unique.GetItemPrice().SetPrice(price);
				_shopManager.GetUniqueManager().AddUniqueItem(unique,false);
				
				
			}
			rs.close();
			ps.close();
		} catch (SQLException e) 
		{
			Bukkit.getLogger().info("ShopManagerSQL:LoadUniques:Loading Unique ERROR");
			e.printStackTrace();
		}
	}
}
