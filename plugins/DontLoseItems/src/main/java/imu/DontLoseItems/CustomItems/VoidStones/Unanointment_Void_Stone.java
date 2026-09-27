package imu.DontLoseItems.CustomItems.VoidStones;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import imu.DontLoseItems.Enums.VOID_STONE_TIER;
import imu.DontLoseItems.Enums.VOID_STONE_TYPE;
import imu.DontLoseItems.main.DontLoseItems;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ImusUtilities;

public class Unanointment_Void_Stone extends Void_Stone
{
	
		

//	private Enchantment[] _valid_armor_ench = 
//		{
//		    Enchantment.PROTECTION_ENVIRONMENTAL,
//		    Enchantment.PROTECTION_FIRE,
//		    Enchantment.PROTECTION_PROJECTILE,
//		    Enchantment.PROTECTION_EXPLOSIONS,
//		    Enchantment.THORNS,
//		    Enchantment.DEPTH_STRIDER,
//		    Enchantment.FROST_WALKER,
//		    Enchantment.WATER_WORKER,
//		    Enchantment.,
//		    Enchantment.UNBREAKING,
//		    Enchantment.MENDING
//		};
//
//		private Enchantment[] _valid_tool_ench = 
//		{
//		    Enchantment.DIG_SPEED,
//		    Enchantment.SILK_TOUCH,
//		    Enchantment.LOOT_BONUS_BLOCKS,
//		    Enchantment.DURABILITY,
//		    Enchantment.MENDING,
//		    Enchantment.UNBREAKING,
//		    Enchantment.LOOT_BONUS_MOBS
//		};
	private final int _minimumLevel = 2;
	private final int _allowEnchants = 1;
	private HashSet<Enchantment> _nonValidEnchants;
		
	public Unanointment_Void_Stone()
	{
		super("&5UNANOINTMENT &0VOID &7STONE", VOID_STONE_TYPE.UNANOINTMENT, VOID_STONE_TIER.NORMAL);
		
		_nonValidEnchants = new HashSet<>();
		_nonValidEnchants.add(Enchantment.BINDING_CURSE);
		_nonValidEnchants.add(Enchantment.VANISHING_CURSE);
	}
	
	
	
	private ItemStack SetBaseLore(ItemStack stack)
	{
		List<String> lores = new ArrayList<>();
		
		lores.add("");
		lores.add("&9This unanointed stone can be used to");
		lores.add("&4remove &eone &9enchantment and &2add &9a new");
		lores.add("&eone &9that does not already exist on the item.");
//		lores.add("");
//		lores.add("&9The item must have at least two enchantments.");
		lores.add("");
		lores.add("&9To apply the effect, combine the stone with an item");
		lores.add("&9in a &7Anvil");
		lores.add("");

		Metods._ins.addLore(stack, lores);
		return stack;

	}
	
	
	@Override
	public ItemStack UseItem(ItemStack stack, VOID_STONE_TIER tier)
	{
		if(stack.getEnchantments().size() == 0) return stack;
		
		Enchantment[] current = new Enchantment[stack.getEnchantments().size()];
		
		
		int i = 0;
		HashSet<Enchantment> nopEnchants = new HashSet<>();
		for(Enchantment ench : stack.getEnchantments().keySet())
		{
			current[i++] = ench;
			nopEnchants.add(ench);
		}
		
		Enchantment selected = current[ThreadLocalRandom.current().nextInt(current.length)];
		
		Enchantment[] enchs = Enchantment.values().clone();
		
		enchs = ImusUtilities.ShuffleArray(enchs);
		
		stack.removeEnchantment(selected);
		
		boolean hasImusEnchant = DontLoseItems.HasServerImusEnchants();
		for(i = 0; i < enchs.length; i++)
		{
			boolean done = false;
			Enchantment ench = enchs[i];
			
			if(_nonValidEnchants.contains(ench)) continue;
			
			if(nopEnchants.contains(ench)) continue;
			
			int level;
			
	        if (hasImusEnchant) 
	        {
	            level = 1; 
	        } else 
	        {
	            level = ThreadLocalRandom.current().nextInt(ench.getMaxLevel()) + _minimumLevel;
	        }
			//int level = ThreadLocalRandom.current().nextInt(ench.getMaxLevel())+_minimumLevel;
			
			if(level == 0) level = 1;
			
			if(level > ench.getMaxLevel()) level = ench.getMaxLevel();
			
			//if(!ench.canEnchantItem(stack) ) continue;
			
			
			int counter = 0;
			
			for(Enchantment en : stack.getEnchantments().keySet())
			{
				if(en.conflictsWith(ench))
				{
					counter++;
				}
				
				if(counter >= 2) {done = true; break;}
				
			}
			
			if(done) 
			{
				continue;
			}

			stack.addUnsafeEnchantment(ench, level);
			break;
			
		}
		
		
		return stack;
	}

	@Override
	public ItemStack GetVoidStoneWithTier(VOID_STONE_TIER tier)
	{
		ItemStack stack = GetItemStack();
		stack = SetBaseLore(stack);
		
		return stack;
	}
	
	
	
	
	

	

}
