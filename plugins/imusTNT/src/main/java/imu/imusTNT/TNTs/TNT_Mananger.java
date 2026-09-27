package imu.imusTNT.TNTs;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;

import imu.iAPI.Other.Metods;
import imu.imusTNT.enums.TNT_TYPE;
import imu.imusTNT.main.ImusTNT;

public class TNT_Mananger
{
	public static TNT_Mananger Instance;
	
	public final String PD_TNT_TYPE = "TNT_TYPE";
	public final String METADATA_TNT_TYPE = "tnt_type";
	public final String METADATA_PLACED_BY = "tnt_placed_by";
	public final String METADATA_EXPLODE = "explode";
	
	private ArrayList<TNT> _tnts;
	

	public TNT_Mananger()
	{
		Instance = this;
		_tnts = new ArrayList<>();
		Init();
	}
	
	public void OpenInv(Player player)
	{
		new TNT_Inv(player).openThis();
	}
	private void Init()
	{
		_tnts.add(new TNT_Chunk("&4Chunk TNT", TNT_TYPE.CHUNK_TNT));
		_tnts.add(new TNT_Chunk_Stone("&7Chunk Stone TNT", TNT_TYPE.CHUNK_STONE_TNT));
	}
	
	public ItemStack GetStack(TNT_TYPE tnt_type)
	{
		for(TNT tnt : _tnts)
		{
			if(tnt.Type == tnt_type) return tnt.GetItemStack();
		}
		
		return new ItemStack(Material.TNT);
	}
	public TNT GetTNT(TNT_TYPE tnt_type)
	{
		for(TNT tnt : _tnts)
		{
			if(tnt.Type == tnt_type) return tnt;
		}
		
		return null;
	}
	
	public TNT_TYPE GetTntType(ItemStack stack)
	{
		String str = Metods._ins.getPersistenData(stack, PD_TNT_TYPE, PersistentDataType.STRING);
		
		if(str == null) return TNT_TYPE.NONE;
		
		return TNT_TYPE.valueOf(str);
	}
	
	public TNT_TYPE GetTntType(Block block)
	{
		if(block.getMetadata(METADATA_TNT_TYPE).isEmpty()) return TNT_TYPE.NONE;
		String data = block.getMetadata(METADATA_TNT_TYPE).get(0).asString() ;
		
		return TNT_TYPE.valueOf(data);
	}
	
	public TNT_TYPE GetTntType(Entity entity)
	{
		if(entity.getMetadata(METADATA_TNT_TYPE).isEmpty()) return TNT_TYPE.NONE;
		String data = entity.getMetadata(METADATA_TNT_TYPE).get(0).asString() ;
		
		return TNT_TYPE.valueOf(data);
	}
	
	public Player GetTntPlacer(Entity entity)
	{
		if(entity.getMetadata(METADATA_TNT_TYPE).isEmpty()) return null;
		
		String data = entity.getMetadata(METADATA_PLACED_BY).get(0).asString() ;
		
		Player player = null;
		try
		{
			player = Bukkit.getPlayer(UUID.fromString(data));
		} 
		catch (Exception e){}
		
		
		return player;
	}
	
	public boolean IsCustomTNT(ItemStack stack)
	{
		if(GetTntType(stack) == TNT_TYPE.NONE) return false;
		
		return true;
	}
	public ArrayList<TNT> GetAllTnts()
	{
		return _tnts;
	}
	
	public void SetMetadata(Player player, ItemStack placedStack, Block block)
	{
		block.setMetadata(METADATA_TNT_TYPE, new FixedMetadataValue(ImusTNT.Instance, GetTntType(placedStack).toString()));
		block.setMetadata(METADATA_PLACED_BY, new FixedMetadataValue(ImusTNT.Instance, player.getUniqueId().toString()));
	}
	
	public boolean IsExploded(Entity entity)
	{
		if(entity.getMetadata(METADATA_EXPLODE).isEmpty()) return false;
		
		return true;
	}
	public void SetMetadataExplode(Entity entity)
	{
		entity.setMetadata(METADATA_EXPLODE,new FixedMetadataValue(ImusTNT.Instance, "true"));
	}
	public void SetMetadata(Player player, Block block, Entity entity)
	{
		entity.setMetadata(METADATA_TNT_TYPE,new FixedMetadataValue(ImusTNT.Instance, GetTntType(block).toString()));
		entity.setMetadata(METADATA_PLACED_BY,new FixedMetadataValue(ImusTNT.Instance,player.getUniqueId().toString()));
	}
	
	
	
	
}
