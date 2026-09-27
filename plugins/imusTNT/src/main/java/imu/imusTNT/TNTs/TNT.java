package imu.imusTNT.TNTs;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.iAPI.Other.Metods;
import imu.imusTNT.enums.TNT_TYPE;

public abstract class TNT
{
	public String DisplayName;
	public TNT_TYPE Type;
	public Material Mat;
	public HashSet<Material> IgnoreMaterials;
	public TNT(String displayName, TNT_TYPE type)
	{
		this.DisplayName = displayName;
		this.Type = type;
		this.Mat= Material.TNT;
		IgnoreMaterials = new HashSet<>();
	}
	
	
	public void Add_IgnoreMat(Material mat)
	{
		IgnoreMaterials.add(mat);
	}
	
	public boolean IsIgnoreMat(Material mat)
	{
		if(IgnoreMaterials.contains(mat)) return true;
		
		return false;
	}
	public ItemStack GetItemStack()
	{
		ItemStack stack = new ItemStack(Mat);
		stack.setAmount(1);
		String[] lores = GetLores();
		
		Metods.setDisplayName(stack, DisplayName);
		Metods._ins.addLore(stack, lores);
		Metods._ins.setPersistenData(stack, TNT_Mananger.Instance.PD_TNT_TYPE, PersistentDataType.STRING, Type.toString());
		return stack;
	}
	
	
	public abstract String[] GetLores();
	
	public abstract void OnExplode(Entity entity,Location loc, List<Block> blocks);
	
	public abstract List<Block> GetBlocks(Location loc);
	
	public abstract void OnIgnite(Player player, Entity entity);
	
	//public abstract void 
	
}
