package imu.GS.Managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import imu.GS.ENUMs.SQL_TABLES;
import imu.GS.ENUMs.TagSubCmds;
import imu.GS.Main.Main;
import imu.GS.ShopUtl.ShopNormal;
import imu.GS.ShopUtl.Shop;
import imu.GS.ShopUtl.ShopItemBase;

public class TagManager 
{
	Main _main;
	private HashMap<Material, HashSet<String>> _tags_material = new HashMap<>();
	// Replaced as a whole by the async loader, read on the main thread
	private volatile Set<String> _tags_shopItems = ConcurrentHashMap.newKeySet();
	
	public TagManager(Main main)
	{
		_main = main;
	}
	
	
	public String[] GetAllMaterialTags()
	{
		Set<String> newSet = new HashSet<>();
		for(HashSet<String> set : _tags_material.values())
		{
			for(String tag : set)
			{
				newSet.add(tag);
			}
		}
		
		return newSet.stream().toArray(String[] :: new);
	}
	public void UpdateMaterialTabList()
	{
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				_main.get_tab_cmd1().SetRule("/gs tag materials "+TagSubCmds.increase_price, 4,  Arrays.asList(GetAllMaterialTags()));
				_main.get_tab_cmd1().SetRule("/gs tag materials "+TagSubCmds.set_price, 4,  Arrays.asList(GetAllMaterialTags()));
			}
		}.runTask(_main);
		
		
		
	}
	
	public void UpdateShopitemTabList()
	{
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				_main.get_tab_cmd1().SetRule("/gs tag shopitems "+TagSubCmds.set_price, 4,  Arrays.asList(_tags_shopItems.toArray(String[] :: new)));
				_main.get_tab_cmd1().SetRule("/gs tag shopitems "+TagSubCmds.increase_price, 4,  Arrays.asList(_tags_shopItems.toArray(String[] :: new)));		
			}
		}.runTask(_main);
		
	}
	
	public ArrayList<Material> GetAllMaterialsWithTag(String tagName)
	{
		ArrayList<Material> mats = new ArrayList<>();
		for(Map.Entry<Material, HashSet<String>> entry : _tags_material.entrySet())
		{
			if(entry.getValue().contains(tagName.toLowerCase())) mats.add(entry.getKey());
		}
		
		return mats;
	}
	
	public BukkitTask LoadAllShopItemTagsNamesAsync()
	{
		return new BukkitRunnable() {
			
			@Override
			public void run()
			{
				Set<String> tags = ConcurrentHashMap.newKeySet();
				final String quarry = "SELECT tag_name FROM "+SQL_TABLES.tags_shopitems;
				try (Connection con = _main.GetSQL().GetConnection();
					 PreparedStatement ps = con.prepareStatement(quarry);
					 ResultSet rs = ps.executeQuery())
				{
					while(rs.next())
					{
						String tag = rs.getString(1);
						if(tag != null) tags.add(tag.toLowerCase());
					}
				} catch (Exception e) {
					System.out.println("Couldnt load tag names from database!");
					e.printStackTrace();
					return;
				}
				_tags_shopItems = tags;
				UpdateShopitemTabList();
			}
		}.runTaskAsynchronously(_main);
		
		
	}
	
	public void LoadMaterialTags()
	{
		try(Connection con = _main.GetSQL().GetConnection()) 
		{
			PreparedStatement ps = con.prepareStatement("SELECT * FROM "+SQL_TABLES.tags_material);
			ResultSet rs = ps.executeQuery();
			if(rs.isBeforeFirst())
			{
				while(rs.next())
				{
					Material mat = Material.getMaterial(rs.getString(2));
					String tagName = rs.getString(3).toLowerCase();
					if(!_tags_material.containsKey(mat)) _tags_material.put(mat, new HashSet<>());
					_tags_material.get(mat).add(tagName);
					//System.out.println("adding tag "+tagName+" to material: "+mat);
				}
			}
			
			UpdateMaterialTabList();
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public BukkitTask LoadMaterialTagsAsync()
	{
		return new BukkitRunnable() {			
			@Override
			public void run() 
			{
				LoadMaterialTags();
			}
		}.runTaskAsynchronously(_main);
		
		
	}
	
	
	void ExecuteAsync(String sql, String... params)
	{
		new BukkitRunnable() {
			@Override
			public void run()
			{
				try(Connection con = _main.GetSQL().GetConnection();
					PreparedStatement ps = con.prepareStatement(sql))
				{
					for(int i = 0; i < params.length; i++) ps.setString(i+1, params[i]);
					ps.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(_main);
	}

	public void SaveTagAsync(Material mat, String tagName)
	{
		// Tags are stored and compared in lower case
		String tag = tagName.toLowerCase();
		if(!AddTag(mat, tag)) return;

		ExecuteAsync("INSERT INTO "+SQL_TABLES.tags_material.toString()+" (material_name, tag_name) VALUES (?,?)", mat.name(), tag);
		UpdateMaterialTabList();
	}

	public void SaveTagsAsync(ShopItemBase sib)
	{
		// Captured on the calling (main) thread
		final String uuid = sib.GetUUID().toString();
		final ArrayList<String> tags = new ArrayList<>(sib.GetTags());

		new BukkitRunnable() {
			@Override
			public void run()
			{
				// Shop saves rewrite these rows too; don't interleave with one
				synchronized (_main.GetShopManagerSQL().GetWriteLock())
				{
					try (Connection con = _main.GetSQL().GetConnection())
					{
						con.setAutoCommit(false);
						try(PreparedStatement delete = con.prepareStatement("DELETE FROM "+SQL_TABLES.tags_shopitems.toString()+" WHERE sib_uuid=?");
							PreparedStatement insert = con.prepareStatement("INSERT INTO "+SQL_TABLES.tags_shopitems.toString()+" (sib_uuid, tag_name) VALUES (?,?)"))
						{
							delete.setString(1, uuid);
							delete.executeUpdate();

							for(String tagName : tags)
							{
								insert.setString(1, uuid);
								insert.setString(2, tagName);
								insert.addBatch();
							}
							insert.executeBatch();
							con.commit();
						}
						catch (SQLException e)
						{
							con.rollback();
							throw e;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}.runTaskAsynchronously(_main);
	}

	boolean AddTag(Material mat, String tagName)
	{
		if(!_tags_material.containsKey(mat))
		{
			_tags_material.put(mat, new HashSet<>());
		}

		for(String tag : _tags_material.get(mat)) { if(tag.equalsIgnoreCase(tagName)) {return false;}}
		_tags_material.get(mat).add(tagName.toLowerCase());
		return true;
	}
	
	public boolean FindAndAddTag(UUID sib_uuid, String tagName)
	{
//		if(!_tags_shopItems.containsKey(sib.GetUUID())) _tags_shopItems.put(sib.GetUUID(), new ArrayList<String>());
//		for(String tag : _tags_shopItems.get(sib.GetUUID())) { if(tag.equalsIgnoreCase(tagName)) {return false;}}
//		_tags_shopItems.get(sib.GetUUID()).add(tagName);
		
		for(Shop shop : _main.get_shopManager().GetShops())
		{
			
			if(!(shop instanceof ShopNormal)) continue;
			
			ShopNormal ShopNormal = (ShopNormal)shop;
			
			for(ShopItemBase[] shopItemPages : ShopNormal.get_items())
			{
				for(ShopItemBase shopItem : shopItemPages)
				{
					if(shopItem.GetUUID().equals(sib_uuid))
					{
						if(!shopItem.AddTag(tagName)) return false;
						return true;
					}
				}
			}
		}
		return true;		
	}
	
	
	// In-memory changes happen on the calling (main) thread, only the database write is async.
	// Deletes compare in lower case so rows saved in mixed case by older versions are removed too.

	public void RemoveTagAsync(Material mat, String tagName)
	{
		String tag = tagName.toLowerCase();
		HashSet<String> tags = _tags_material.get(mat);
		if(tags != null) tags.remove(tag);

		ExecuteAsync("DELETE FROM "+SQL_TABLES.tags_material.toString()+" WHERE material_name=? AND LOWER(tag_name)=?", mat.name(), tag);
		UpdateMaterialTabList();
	}

	public void RemoveTagAsync(ShopItemBase sib, String tagName)
	{
		String tag = tagName.toLowerCase();
		sib.RemoveTag(tag);
		ExecuteAsync("DELETE FROM "+SQL_TABLES.tags_shopitems.toString()+" WHERE sib_uuid=? AND LOWER(tag_name)=?", sib.GetUUID().toString(), tag);
	}

	public void RemoveAllAsync(Material mat)
	{
		_tags_material.put(mat, new HashSet<>());
		ExecuteAsync("DELETE FROM "+SQL_TABLES.tags_material.toString()+" WHERE material_name=?", mat.name());
		UpdateMaterialTabList();
	}

	public void RemoveAllAsync(ShopItemBase sib)
	{
		sib.ClearTags();
		ExecuteAsync("DELETE FROM "+SQL_TABLES.tags_shopitems.toString()+" WHERE sib_uuid=?", sib.GetUUID().toString());
	}
	
	public boolean HasTag(Material mat, String tagName)
	{
		if(_tags_material.containsKey(mat))
			if(_tags_material.get(mat).contains(tagName.toLowerCase())) return true;
		return false;
	}
	
	public boolean HasTag(ItemStack stack, String tagName)
	{
		return HasTag(stack.getType(), tagName);
	}
	
	public boolean HasTag(ShopItemBase sib, String tagName)
	{
		if(sib.HasTag(tagName)) return true;
		return false;
	}
	
	
}
