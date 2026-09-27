package imu.DontLoseItems.CustomItems.VoidStones;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.DontLoseItems.CustomItems.CustomItem;
import imu.DontLoseItems.Enums.VOID_STONE_TIER;
import imu.DontLoseItems.Enums.VOID_STONE_TYPE;
import imu.DontLoseItems.Managers.Manager_VoidStones;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ImusUtilities;

public class Unstable_Void_Stone extends Void_Stone
{

	public Unstable_Void_Stone()
	{
		super("&5UNSTABLE &0VOID &7STONE", VOID_STONE_TYPE.UNSTABLE, VOID_STONE_TIER.NONE);		
	}

	@SuppressWarnings("incomplete-switch")
	public ItemStack GetVoidStoneWithTier(VOID_STONE_TIER tier)
	{
		switch (tier)
		{
		case NORMAL: return GetNormal();
		case RARE: return GetRare();
		}
		return GetItemStack();
	}
	
	public int GetTierInceaseToEnchant(VOID_STONE_TIER tier)
	{
		switch (tier)
		{
		case NONE: 		return 0;
		case NORMAL: 	return 1;
		case RARE: 		return 2;
		}
		return 0;
	}
	
	private ItemStack SetBaseLore(ItemStack stack, int power)
	{
		List<String> lores = new ArrayList<>();
		
		lores.add("");
		lores.add("&9This unstable stone is able to enchant two random enchants");
		lores.add("&9with either a &2+"+power+" &9 or &4-"+power+"&9 level.");
		lores.add("");
		lores.add("&9Combine it in a &7Anvil");
		lores.add("&9with an enchanted item to apply the effect");
		lores.add("");
		lores.add("&9Item needs to have at least 3 enchants");
		lores.add("");
		lores.add("&9After use the item will be");
		lores.add("&4unenchantable &9and &4Anvil'able");
		Metods._ins.addLore(stack, lores);
		return stack;

	}
	private ItemStack GetNormal()
	{
		Set_tier(VOID_STONE_TIER.NORMAL);
		ItemStack stack = GetItemStack();
		stack = SetBaseLore(stack, 1);
		Metods._ins.AddGlow(stack);

		return stack;
	}
	
	private ItemStack GetRare()
	{
		Set_tier(VOID_STONE_TIER.RARE);
		ItemStack stack =  GetItemStack();

		stack = SetBaseLore(stack, 2);
		Metods._ins.AddGlow(stack);
		return stack;
	}
	
	private final int _enchantChance = 50;
	@Override
	public ItemStack UseItem(ItemStack stack,VOID_STONE_TIER tier)
	{
		if(tier == VOID_STONE_TIER.NONE) return stack;
		
		if(stack.getEnchantments().size() < 3)
		{
			System.out.println("not enough enchants");
			return stack;
		}
		
		boolean positive = ThreadLocalRandom.current().nextInt(100) < 50;
		HashMap<Enchantment, Integer> data = Metods._ins.GetEnchantsWithLevels(stack);
		
		Enchantment[] ench = new Enchantment[data.size()];
		
		int index = 0;
		for(var da : data.keySet())
		{
			ench[index++] = da;
		}
		ench = ImusUtilities.ShuffleArray(ench);
		Enchantment ench1 = ench[0];
		Enchantment ench2 = ench[1];
		
		stack.removeEnchantment(ench1);
		stack.removeEnchantment(ench2);
		
		reforge(stack, data, ench1, tier, ThreadLocalRandom.current().nextInt(100) < _enchantChance);
		reforge(stack, data, ench2, tier, ThreadLocalRandom.current().nextInt(100) < _enchantChance);
	
		
		return stack;
	}
	
	private void reforge(ItemStack stack, HashMap<Enchantment, Integer> data, Enchantment ench, VOID_STONE_TIER tier,boolean positive)
	{
		if(positive)
		{
			int value = data.get(ench)+GetTierInceaseToEnchant(tier);
			if(Manager_VoidStones.MAX_ENCH_LEVEL.containsKey(ench)) value = Manager_VoidStones.MAX_ENCH_LEVEL.get(ench);
			
			stack.addUnsafeEnchantment(ench, value);
			
		}
		else
		{
			int value = data.get(ench)-GetTierInceaseToEnchant(tier);
			if(value >= 1) stack.addEnchantment(ench, value);
			
		}
	}

	
	
	
	

	

}
