package imu.GS.Managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import imu.GS.ENUMs.GearValues;
import imu.GS.ENUMs.SQL_TABLES;
import imu.GS.Main.Main;
import imu.GS.Other.EnchantINFO;
import imu.GS.ShopUtl.ItemPrice.PriceMoney;
import imu.iAPI.Other.Metods;

public class ShopEnchantManager extends Manager
{
	
	
	private double _def_rawMultiplier = 0.3f;
	HashMap<Enchantment, EnchantINFO> _enchantInfos = new HashMap<>();
		

	private double _gear_netherite_mult = 1.40;
	private double _gear_diamond_mult = 1;
	private double _gear_gold_mult = 0.5;
	private double _gear_iron_mult = 0.4;
	private double _gear_stone_mult = 0.2;
	private double _gear_wood_mult = 0.1;
	
	private double _enchant_count_percent = 0.05; //%
	
	HashMap<Material, GearValues> _gearValues = new HashMap<>();
	public ShopEnchantManager(Main main) {
		super(main);
	}

	@Override
	public void INIT()
	{
		//this is async
		CreateTables();
		LoadEnchantInfos();
		LoadEnchantGearMultipliers();
		LoadEnchantOtherValues();
	}
	
	public enum ENCHANT_OTHER_VALUES
	{
		ENCHANT_COUNT_PERCENT
	}
	
	public EnchantINFO GetInfo(Enchantment ench)
	{
		if(!_enchantInfos.containsKey(ench)) _enchantInfos.put(ench, new EnchantINFO(ench, 0, ench.getMaxLevel(), _def_rawMultiplier));
		
		return _enchantInfos.get(ench);
		
	}

	public void CreateTables()
	{
		LinkedList<String> statements = new LinkedList<>();
		String strS = "CREATE TABLE IF NOT EXISTS "+SQL_TABLES.enchants.toString()+" ("
				+ "name VARCHAR(50) NOT NULL,"
				+ "minlvl INT(10),"
				+ "maxlvl INT(10),"
				+ "raw_mult FLOAT(20),"
				+ "price_type VARCHAR(10),"
				+ "PRIMARY KEY(name));";
		statements.add(strS);
		
		strS = "CREATE TABLE IF NOT EXISTS "+SQL_TABLES.enchant_price_money.toString()+" ("
				+ "name VARCHAR(50) NOT NULL,"
				+ "min FLOAT(20),"
				+ "max FLOAT(20),"
				+ "PRIMARY KEY(name));";
		statements.add(strS);
		
		
		strS = "CREATE TABLE IF NOT EXISTS "+SQL_TABLES.enchant_gear_values+ " ("
				+ "name VARCHAR(50) NOT NULL,"
				+"multiplier FLOAT(20),"
				+ "PRIMARY KEY(name));";
		statements.add(strS);
		
		strS = "CREATE TABLE IF NOT EXISTS "+SQL_TABLES.enchant_other_values+ " ("
				+"name VARCHAR(50) NOT NULL,"
				+"multiplier FLOAT(20),"
				+"PRIMARY KEY(name));";
		statements.add(strS);
		if(_main.GetSQL().ExecuteStatements(statements)) PrintINFO("CreateTables", "Loaded!");
				
	}
	
	public void CreateGearTypesAsync()
	{
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				_gearValues.clear();
				ItemStack stack;
				for(Material mat : Material.values())
				{
					stack = new ItemStack(mat);
					if(Metods._ins.isArmor(stack) || Metods._ins.isTool(stack))
					{

						if(mat.name().startsWith("NETHERITE")) {_gearValues.put(mat, GearValues.NETHERITE);continue;}
						if(mat.name().startsWith("DIAMOND")) {_gearValues.put(mat, GearValues.DIAMOND);continue;}
						if(mat.name().startsWith("GOLDEN")) {_gearValues.put(mat, GearValues.GOLD);continue;}
						if(mat.name().startsWith("IRON")) {_gearValues.put(mat, GearValues.IRON);continue;}
						if(mat.name().startsWith("STONE")) {_gearValues.put(mat, GearValues.STONE);continue;}
						if(mat.name().startsWith("WOODEN")) {_gearValues.put(mat, GearValues.WOOD);continue;}
					}
					
				}
			
				_gearValues.put(Material.SHIELD, GearValues.IRON);
				_gearValues.put(Material.SHEARS, GearValues.IRON);
				_gearValues.put(Material.FLINT_AND_STEEL, GearValues.IRON);
				_gearValues.put(Material.ELYTRA, GearValues.DIAMOND);
			}
			
		}.runTaskAsynchronously(_main);
	}
	
	
	
	public void LoadEnchantInfos()
	{
		try (Connection con = _main.GetSQL().GetConnection())
		{
			
			PreparedStatement ps = con.prepareStatement("SELECT enchants.name, enchants.minlvl, enchants.maxlvl, enchants.raw_mult,enchants.price_type, enchant_price_money.min, enchant_price_money.max FROM enchants "
					+ "INNER JOIN enchant_price_money ON enchants.name = enchant_price_money.name;");
			ResultSet rs = ps.executeQuery();
			if(!rs.isBeforeFirst())
			{
				PrintINFO("LoadEnchantInfos", "No Enchant prices found!");
				rs.close();
				ps.close();
				con.close();
				return;
			}
			
			int count = 0;
			while(rs.next())
			{
				int i = 1;
				EnchantINFO eInfo = new EnchantINFO(Enchantment.getByKey(NamespacedKey.minecraft(rs.getString(i++))),rs.getInt(i++) ,rs.getInt(i++), rs.getFloat(i++));
				i++; //pricetype
				eInfo.SetPrice(new PriceMoney().SetPrice(rs.getFloat(i++)), new PriceMoney().SetPrice(rs.getFloat(i++)));
				_enchantInfos.put(eInfo.GetEnchantment(), eInfo);
				count++;
			}
			
			rs.close();
			ps.close();
			
			PrintINFO("LoadEnchantInfos", "Enchants Loaded: "+count);
		} 
		catch 
		(Exception e) 
		{
			PrintERROR("LoadEnchantInfos", "ERROR loading");
			//e.printStackTrace();
		}
	}
	
	public void SaveEnchantOtherValuesAsync(ENCHANT_OTHER_VALUES value, double d)
	{
		LinkedList<String> statements = new LinkedList<>();
		statements.add("REPLACE INTO "+SQL_TABLES.enchant_other_values + " (name, multiplier) VALUES("
				+ "'"+value.toString()+"',"
				+ d
				+ ");");
		
		
		_main.GetSQL().ExecuteStatementsAsync(statements);
	}
	
	public void LoadEnchantOtherValues()
	{
		try (Connection con = _main.GetSQL().GetConnection())
		{
			
			PreparedStatement ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.enchant_other_values+";");
			ResultSet rs = ps.executeQuery();
			if(!rs.isBeforeFirst())
			{
				rs.close();
				ps.close();
				return;
			}
			while(rs.next())
			{
				ENCHANT_OTHER_VALUES value = ENCHANT_OTHER_VALUES.valueOf(rs.getString(1));
				double d = rs.getFloat(2);
				switch (value) 
				{
				case ENCHANT_COUNT_PERCENT:
					_enchant_count_percent = d;
					break;

				}
			}
			rs.close();
			ps.close();
			PrintINFO("LoadEnchantOtherValues", "Other values loaded");
		} 
		catch 
		(Exception e) 
		{
			PrintERROR("LoadEnchantOtherValues", "ERROR loading");
			e.printStackTrace();
		}
	}
	public void SaveEnchantInfoAsync(List<EnchantINFO> eInfos)
	{
		LinkedList<String> statements = new LinkedList<>();
		for(EnchantINFO eInfo : eInfos)
		{
			if(!(eInfo.GetMaxPrice() instanceof PriceMoney))
			{
				PrintERROR("SaveEnchantInfoAsync", "The price class isnt implemented to sql: class: "+eInfo.GetMaxPrice().getClass().getSimpleName());
				return;
			}
			statements.add("REPLACE INTO "+SQL_TABLES.enchants+" (name, minlvl, maxlvl, raw_mult, price_type) VALUES("
					+ "'"+eInfo.GetName()+"',"
					+ eInfo.GetMinLevel()+","
					+ eInfo.GetMaxLevel()+","
					+ eInfo.Get_rawMultiplier()+","
					+ "'"+eInfo.GetMaxPrice().getClass().getSimpleName()+"'"
					+ ");");
			
			
			statements.add("REPLACE INTO "+ SQL_TABLES.enchant_price_money+ " (name,min,max) VALUES("
					+ "'"+eInfo.GetName()+"',"
					+ eInfo.GetMinPrice().GetPrice()+","
					+ eInfo.GetMaxPrice().GetPrice()
					+ ");");
		}
		
		_main.GetSQL().ExecuteStatementsAsync(statements);
//		new BukkitRunnable() {
//			
//			@Override
//			public void run() 
//			{
//				ExecuteStatements(statements, "SaveEnchantInfoAsync");
//			}
//		}.runTaskAsynchronously(_main);
	}
	
	public void SaveEnchantGearMultiAsync(GearValues value, double multiplier)
	{
		LinkedList<String> statements = new LinkedList<>();
		statements.add("REPLACE INTO "+SQL_TABLES.enchant_gear_values + " (name, multiplier) VALUES("
				+ "'"+value.toString()+"',"
				+ multiplier
				+ ");");
//		new BukkitRunnable() {
//			
//			@Override
//			public void run() 
//			{
//				ExecuteStatements(statements, "SaveEnchantGearMultiAsync");
//			}
//		}.runTaskAsynchronously(_main);
		_main.GetSQL().ExecuteStatementsAsync(statements);
	}
	
	public void LoadEnchantGearMultipliers()
	{
		try (Connection con = _main.GetSQL().GetConnection())
		{
			PreparedStatement ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.enchant_gear_values+";");
			ResultSet rs = ps.executeQuery();
			if(!rs.isBeforeFirst())
			{
				rs.close();
				ps.close();
				return;
			}
			while(rs.next())
			{
				GearValues value = GearValues.valueOf(rs.getString(1));
				SetGearValue(value, rs.getFloat(2));
			}
			rs.close();
			ps.close();
			PrintINFO("LoadEnchantGearMultipliers", "Gear values loaded: ");
		} 
		catch 
		(Exception e) 
		{
			PrintERROR("LoadEnchantGearMultipliers", "ERROR loading");
			//e.printStackTrace();
		}
	}
	
	public double CalculateEnchantPrice(ItemStack stack)
	{
		double price = 0.0;
		double enchantCount = 0.0;
		for(Map.Entry<Enchantment, Integer> entry  : Metods._ins.GetEnchantsWithLevels(stack).entrySet())
		{			
			Enchantment ench = entry.getKey();
			int level = entry.getValue();
			EnchantINFO eInfo = GetInfo(ench);
			double priceValue = eInfo.GetPrice(level).GetPrice();
			price += stack.getType() == Material.ENCHANTED_BOOK ? (priceValue * eInfo.Get_rawMultiplier()) : priceValue;
			enchantCount++;
			//System.out.println("stack: "+stack.getType()+ " enchant: "+ench + " level: "+level+ " price: "+price);
		}
		
		if(enchantCount == 0.0) return price;
		
		if(price < 0) return price;
		
		double gearMulti = stack.getType() == Material.ENCHANTED_BOOK ? 1.0 : GetMultiplierForGear(stack);
		double enchCountPercent = Math.abs(1.0 + _enchant_count_percent * ((double)enchantCount-1.0));
				
		if(enchantCount == 0.0) return (price * gearMulti);

		return (price * gearMulti) * enchCountPercent; 
	}
	
	public double GetMultiplierForGear(ItemStack stack)
	{
		double mult = 0.0;
		GearValues value = _gearValues.get(stack.getType());
		if(value == null) return mult;
		return GetMultiplierForGear(value);

	}
	
	public double GetMultiplierForGear(GearValues value)
	{
		double mult = 0.0;
		switch (value)
		{
		case DIAMOND: return _gear_diamond_mult;
		case GOLD: return _gear_gold_mult;
		case IRON:return _gear_iron_mult;
		case NETHERITE:return _gear_netherite_mult;
		case STONE:return _gear_stone_mult;
		case WOOD:return _gear_wood_mult;
		}
		return mult;
	}
	
	public void SetGearValue(GearValues value, double multiplier)
	{
		switch (value)
		{
		case DIAMOND: 	_gear_diamond_mult 	= multiplier;break;
		case GOLD:		_gear_gold_mult		= multiplier;break;
		case IRON:		_gear_iron_mult		= multiplier;break;
		case NETHERITE:	_gear_netherite_mult= multiplier;break;
		case STONE:		_gear_stone_mult	= multiplier;break;
		case WOOD:		_gear_wood_mult		= multiplier;break;
		}
	}

	public double Get_enchant_count_percent() {
		return _enchant_count_percent;
	}

	public void Set_enchant_count_percent(double _enchant_count_multiplier) {
		this._enchant_count_percent = _enchant_count_multiplier;
	}
	
	
	
}
