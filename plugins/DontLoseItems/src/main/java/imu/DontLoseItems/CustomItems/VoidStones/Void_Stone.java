package imu.DontLoseItems.CustomItems.VoidStones;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import imu.DontLoseItems.CustomItems.CustomItem;
import imu.DontLoseItems.Enums.VOID_STONE_TIER;
import imu.DontLoseItems.Enums.VOID_STONE_TYPE;
import imu.DontLoseItems.Managers.Manager_VoidStones;
import imu.iAPI.Other.Metods;

public abstract class Void_Stone extends CustomItem
{
	private VOID_STONE_TYPE _type;
	private VOID_STONE_TIER	_tier;
	
	public Void_Stone(String name, VOID_STONE_TYPE type, VOID_STONE_TIER tier)
	{
		super(new ItemStack(Material.STONE), name);
		
		Set_tier(tier);
		Set_type(type);
		
	}

	@Override
	public ItemStack GetItemStack()
	{
		ItemStack stack = super.GetItemStack();
		
		Manager_VoidStones.SetVoidStoneData(stack, _type, _tier);
		Metods._ins.AddGlow(stack);
		return stack;
	}
	public abstract ItemStack GetVoidStoneWithTier(VOID_STONE_TIER tier);
	public abstract ItemStack UseItem(ItemStack stack, VOID_STONE_TIER tier);


	public VOID_STONE_TYPE Get_type()
	{
		return _type;
	}

	public void Set_type(VOID_STONE_TYPE _type)
	{
		this._type = _type;
	}

	public VOID_STONE_TIER Get_tier()
	{
		return _tier;
	}

	public void Set_tier(VOID_STONE_TIER _tier)
	{
		this._tier = _tier;
	}
	
	

}
