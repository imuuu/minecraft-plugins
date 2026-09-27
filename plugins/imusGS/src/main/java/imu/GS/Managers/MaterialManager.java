package imu.GS.Managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import imu.GS.ENUMs.SQL_TABLES;
import imu.GS.Main.Main;
import imu.GS.Other.MaterialOverflow;
import imu.GS.Other.MaterialSmartData;
import imu.GS.ShopUtl.ShopNormal;
import imu.GS.ShopUtl.Shop;
import imu.GS.ShopUtl.ShopItemBase;
import imu.GS.ShopUtl.ItemPrice.PriceMaterial;

public class MaterialManager extends Manager
{
	public static MaterialManager _ins;
	ShopManagerSQL _shopManagerSQL;
	ShopManager _shopManager;
	private HashMap<Material, PriceMaterial> _material_prices = new HashMap<>();
	
	public MaterialManager(Main main)
	{
		super(main);
		_ins = this;
		_shopManager = main.get_shopManager();
		_shopManagerSQL = main.GetShopManagerSQL();
	}
	
	@Override
	public void INIT() 
	{

		
	}
	public void CreateTables()
	{
		
		try(Connection con =_main.GetSQL().GetConnection()) 
		{
			PreparedStatement ps;
			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.price_materials.toString()+" ("
					+ "material VARCHAR(50), "
					+ "price FLOAT(20), "
					+ "smart_multiplier FLOAT(20) DEFAULT -1.0, "				
					+ "PRIMARY KEY(material)"
					+ ");");
			ps.executeUpdate();
			
			
			ps = con.prepareStatement("CREATE TABLE IF NOT EXISTS "+SQL_TABLES.material_overflow.toString()+" ("
					+ "material VARCHAR(50), "
					+ "softcap INT(50), "
					+ "batch_size INT(20), "
					+ "min_price FLOAT(20), "
					+ "drop_percent FLOAT(20), "
					+ "PRIMARY KEY(material)"
					+ ");");
			ps.executeUpdate();
			
			
//			ps = con.prepareStatement("ALTER TABLE "+SQL_TABLES.price_materials.toString()+" ADD COLUMN IF NOT EXISTS smart_multiplier FLOAT(20) DEFAULT -1.0;");
//			ps.executeUpdate();
//			
			//_main.getLogger().info("====> material_prices");
			
			ps.close();
			
			
		} 
		catch (Exception e) 
		{
			PrintERROR("CreateTables", "Couldnt Create tables");
			e.printStackTrace();
		}
	}
	
	public PriceMaterial GetPriceMaterialAndCheck(ItemStack stack)
	{
		//System.err.println("Checking mat price: "+stack.getType());
		PriceMaterial priceMaterial = new PriceMaterial(stack.getType());
		priceMaterial.SetPrice(0);
		if(stack == null || stack.getType() == Material.AIR) return priceMaterial;
		
		PriceMaterial priceMat = _material_prices.get(stack.getType());
		
		//if(priceMat == null) return priceMaterial;
		
		priceMaterial = (PriceMaterial)priceMat.clone();
		
		double addedEnchantPrice = _main.GetShopEnchantManager().CalculateEnchantPrice(stack);
		
		double newPrice = (priceMaterial.HasSmartData() ? priceMaterial.GetSmartData().GetPrice() : priceMaterial.GetPrice()) + addedEnchantPrice;
		
		if(newPrice < 0.0) newPrice = 0.0;
		
		priceMaterial.SetPrice(newPrice * _shopManager.GetDurabilityReduction(stack));
		
		return priceMaterial;
	}
	
	void SetMaterialOverflow(MaterialOverflow mOverflow)
	{
		_material_prices.get(mOverflow.get_mat()).SetOverflow(mOverflow);
	}
	
	void RemoveMaterialOverflow(Material mat)
	{
		_material_prices.get(mat).SetOverflow(null);
	}
	
	void PutMaterialPrice(Material mat, double price)
	{		
		PriceMaterial pm = new PriceMaterial(mat);
		pm.SetPrice(price);
		_material_prices.put(mat, pm);
	}
	
	void PutMaterialPrice(Material mat, PriceMaterial pm)
	{		
		_material_prices.put(mat, pm);
	}
	
	public PriceMaterial GetPriceMaterial(Material mat)
	{
		return _material_prices.get(mat);
	}
	
	public boolean HasPriceMaterial(Material mat)
	{
		return _material_prices.containsKey(mat);
	}
	
	public void SaveMaterialPrice(List<Material> mats, double prices)
	{
		new BukkitRunnable() 
    	{
			@Override
			public void run() 
			{				
				_shopManagerSQL.SaveMaterialPrice(mats, prices);
			}
		}.runTaskAsynchronously(_main);	
		
		new BukkitRunnable() {
			
			double price = prices;
			@Override
			public void run() 
			{
				if(price < 0) 
				{
					price = 0;
				}else
				{
					price = Math.round(price * 100.00) / 100.00;
				}
				for(Material mat : mats)
				{
					PutMaterialPrice(mat, price);

					for(Shop shop : _shopManager.GetShops())
					{
						if(!(shop instanceof ShopNormal)) continue;
						
						ShopNormal normalShop = (ShopNormal) shop;
						
						boolean removeCustomers = false;
						
						for(ShopItemBase[] pages : normalShop.get_items())
						{
							for(ShopItemBase sib : pages)
							{
								if(sib == null) continue;
								
								if(sib.GetItemPrice() instanceof PriceMaterial && sib.GetRealItem().getType() == mat)
								{
									sib.SetItemPrice(GetPriceMaterial(mat));
									removeCustomers = true;
								}
							}
						}
						if(removeCustomers) shop.RemoveCustomerALL();
					}	
				}	
			}
		}.runTask(_main);
		
	}
	
	public void SaveMaterialOverflowAsync(Iterable<MaterialOverflow> overflows)
	{
		LinkedList<String> statements = new LinkedList<>();
		
		for(MaterialOverflow of : overflows)
		{
			statements.add("REPLACE INTO "+SQL_TABLES.material_overflow+" (material, softcap, batch_size, min_price, drop_percent) VALUES("
					+ "\""+of.get_mat()+"\","
					+ of.get_softCap()+","
					+ of.Get_batchSize()+","
					+ of.Get_minPrice()+","
					+ of.get_dropProsent()
					+ ");");
			
			SetMaterialOverflow(of);
		}
		
		_main.GetSQL().ExecuteStatementsAsync(statements);
	}
	public void LoadMaterialOverflows()
	{
		final String quarry = "SELECT * FROM "+SQL_TABLES.material_overflow.toString()+";";
		try (Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement(quarry);
			ResultSet rs = ps.executeQuery();
			
			while(rs.next())
			{
				int i = 1;
				Material mat = Material.getMaterial(rs.getString(i++));
				int softCap = rs.getInt(i++);
				int batch_size = rs.getInt(i++);
				double min_price = rs.getFloat(i++);
				double drop_percent = rs.getFloat(i++);
				
				SetMaterialOverflow(new MaterialOverflow(mat, softCap, drop_percent, batch_size, min_price));
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
	}
	
	public void SaveMaterialSmartDataAsync(Iterable<Material> mats, double multi)
	{
		LinkedList<String> statements = new LinkedList<>();
		
		for(Material mat : mats)
		{
//			statements.add("REPLACE INTO " + SQL_TABLES.price_materials+" (material, price, smart_multiplier) VALUES("
//					+ "\""+mat.name()+"\","
//					+ _material_prices.get(mat).GetPrice()+","
//					+ multi
//					+ ");");
			
			statements.add("UPDATE " + SQL_TABLES.price_materials+" SET smart_multiplier =" +multi+" WHERE material=\""+mat.name()+"\";");
			
			if(multi < 0)
			{
				_material_prices.get(mat).SetSmartData(null);
				continue;
			}
			MaterialSmartData data = new MaterialSmartData(mat, multi);
			data.Calculate();
			_material_prices.get(mat).SetSmartData(data);
		}
		
		_main.GetSQL().ExecuteStatementsAsync(statements);
	}
	
	public void RemoveMaterialOverflow(Iterable<Material> mats)
	{
		LinkedList<String> statements = new LinkedList<>();
		
		for(Material mat : mats)
		{
			statements.add("DELETE FROM " + SQL_TABLES.material_overflow+" WHERE material=\""+mat.toString()+"\";");
			RemoveMaterialOverflow(mat);
		}
		_main.GetSQL().ExecuteStatementsAsync(statements);
	}
	void LoadMaterialPrices()
	{	
		try (Connection con = _main.GetSQL().GetConnection())
		{
			
			PreparedStatement ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.price_materials.toString()+";");
			ResultSet rs = ps.executeQuery();
			int i = 1;
			final String statementINSERT ="INSERT INTO "+SQL_TABLES.price_materials.toString()+" (material, price) VALUES(?,?);";
			if(!rs.isBeforeFirst())
			{
				//empty!
				PreparedStatement ps2 = con.prepareStatement(statementINSERT);
				for(Material mat : Material.values())
				{
					i = 1;

					ps2.setString(i++, mat.name());
					ps2.setFloat(i++, 0.0f);
					ps2.addBatch();
					PutMaterialPrice(mat, 0.0);
				}
				
				ps2.executeBatch();
				ps2.close();
			}
			else
			{
				while(rs.next())
				{
					i = 1;
							
					Material mat = Material.getMaterial(rs.getString(i++));
					PriceMaterial pm = new PriceMaterial(mat);		
					pm.SetPrice(rs.getFloat(i++));
					double smart_multiplier = rs.getFloat(i++);
					
					if(smart_multiplier > -1) 
					{
						pm.SetSmartData(new MaterialSmartData(mat, smart_multiplier));
					}
					PutMaterialPrice(mat, pm);
				}

				
			}
			
			PreparedStatement ps3 = con.prepareStatement(statementINSERT);
			boolean hasFoundNewMats = false;
			for(Material mat : Material.values())
			{
				if(!HasPriceMaterial(mat))
				{
					hasFoundNewMats = true;
					i = 1;
					ps3.setString(i++, mat.name());
					ps3.setFloat(i++, 0.0f);
					ps3.addBatch();
					PutMaterialPrice(mat, 0.0);
					Bukkit.getLogger().info(Main.Instance._pluginNamePrefix+"New Material found:"+mat+" ");
				}
			}
			
			if(hasFoundNewMats)
			{
				ps3.executeBatch();		
			}
			ps3.close();
			
			
			
			rs.close();
			ps.close();
			
			for(PriceMaterial pm : _material_prices.values())
			{
				if(!pm.HasSmartData()) continue;			
				pm.GetSmartData().Calculate();
			}
			
			PrintINFO("LoadMaterialPrices", "Material prices loaded");
		}
		catch (SQLException e) 
		{
		
			PrintERROR("LoadMaterialPrices", "ERROR");
			e.printStackTrace();

		}
		
		
	}

	
	
}
