package imu.DontLoseItems.Managers;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.persistence.PersistentDataType;

import imu.DontLoseItems.CustomItems.VoidStones.Reforge_Void_Stone;
import imu.DontLoseItems.CustomItems.VoidStones.Unanointment_Void_Stone;
import imu.DontLoseItems.CustomItems.VoidStones.Unstable_Void_Stone;
import imu.DontLoseItems.CustomItems.VoidStones.Void_Stone;
import imu.DontLoseItems.Enums.VOID_STONE_TIER;
import imu.DontLoseItems.Enums.VOID_STONE_TYPE;
import imu.iAPI.FastInventory.Fast_Inventory;
import imu.iAPI.FastInventory.Manager_FastInventories;
import imu.iAPI.Other.Metods;

public class Manager_VoidStones	implements Listener
{
	public static Manager_VoidStones Instance;
	
//	ENCHANT_ADD_TAKE, // adds +1/+2 or -1/-2
//	REFORCE, // destroy, lower tier, upper tier
//	UNANOINTMENT, // removes one and adds new one not exist
	
	//public Unstable_Void_Stone VoidStone;
	public final String PD_UNENCANTABLE = "VOID_UNENCHANTABLE";
	
	public static final String PD_VOID_STONE_TYPE = "VOID_STONE_TYPE";
	public static final String PD_VOID_STONE_TIER = "VOID_STONE_TIER";
	public static final String PD_VOID_STONE = "VOID_STONE";
	
	public static HashMap<Enchantment, Integer> MAX_ENCH_LEVEL = new HashMap<>();
	
	private Void_Stone[] _voidStones = {
			new Unstable_Void_Stone(),
			new Reforge_Void_Stone(),
			new Unanointment_Void_Stone(),
		};
	
	public Manager_VoidStones()
	{
		Instance = this;
		//VoidStone = new Unstable_Void_Stone();
		InitMaxLevels();
		

	}
	
	private void InitMaxLevels()
	{
		MAX_ENCH_LEVEL.put(Enchantment.MENDING, 1);
		MAX_ENCH_LEVEL.put(Enchantment.ARROW_INFINITE, 1);
		MAX_ENCH_LEVEL.put(Enchantment.BINDING_CURSE, 1);
		MAX_ENCH_LEVEL.put(Enchantment.SILK_TOUCH, 1);
		MAX_ENCH_LEVEL.put(Enchantment.VANISHING_CURSE, 1);
	}
	
	public void SetTestItems()
	{
		Fast_Inventory fastInv = new Fast_Inventory("Void_Stones", "&5VoidStones", null).Register();
		fastInv.AddStack(new Unstable_Void_Stone().GetVoidStoneWithTier(VOID_STONE_TIER.NORMAL));
		fastInv.AddStack(new Unstable_Void_Stone().GetVoidStoneWithTier(VOID_STONE_TIER.RARE));
		fastInv.AddStack(new Reforge_Void_Stone().GetVoidStoneWithTier(VOID_STONE_TIER.NORMAL));
		fastInv.AddStack(new Unanointment_Void_Stone().GetVoidStoneWithTier(VOID_STONE_TIER.NORMAL));

	}
	
	public static boolean IsVoidStone(ItemStack stack)
	{
		return Metods._ins.getPersistenData(stack, Manager_VoidStones.PD_VOID_STONE, PersistentDataType.INTEGER) != null;
	}
	
	public static VOID_STONE_TYPE GetVoidStoneType(ItemStack stack)
	{
		return VOID_STONE_TYPE.GetFromIndex(Metods._ins.getPersistenData(stack, Manager_VoidStones.PD_VOID_STONE_TYPE, PersistentDataType.INTEGER));
	}
	
	public static void SetVoidStoneData(ItemStack stack, VOID_STONE_TYPE type, VOID_STONE_TIER tier)
	{
		Metods._ins.setPersistenData(stack, PD_VOID_STONE, PersistentDataType.INTEGER, 1);
		Metods._ins.setPersistenData(stack, PD_VOID_STONE_TYPE, PersistentDataType.INTEGER, type.GetIndex());
		Metods._ins.setPersistenData(stack, PD_VOID_STONE_TIER, PersistentDataType.INTEGER, tier.GetIndex());
	}
	
	public Void_Stone GetVoidStoneByType(VOID_STONE_TYPE type)
	{
		for(Void_Stone stone : _voidStones)
		{
			if(stone.Get_type() == type) return stone;
		}
		
		return null;
	}
	public static VOID_STONE_TIER GetVoidStoneTier(ItemStack stack)
	{
		return VOID_STONE_TIER.GetFromIndex(Metods._ins.getPersistenData(stack, Manager_VoidStones.PD_VOID_STONE_TIER, PersistentDataType.INTEGER));	
	}
	
	public boolean IsUnenchantable(ItemStack stack)
	{
		return Metods._ins.getPersistenData(stack, PD_UNENCANTABLE, PersistentDataType.INTEGER) != null;
	}
	
	public ItemStack UseVoidStone(ItemStack stack, ItemStack voidStoneStack)
	{
		return UseVoidStone(stack, GetVoidStoneType(voidStoneStack), GetVoidStoneTier(voidStoneStack));
	}
	
	public ItemStack UseVoidStone(ItemStack stack, VOID_STONE_TYPE type, VOID_STONE_TIER tier)
	{
		return GetVoidStoneByType(type).UseItem(stack,tier);
	}
	
	public void SetEnchantableLores(ItemStack stack)
	{
		Metods._ins.setPersistenData(stack, PD_UNENCANTABLE, PersistentDataType.INTEGER, 1);
		Metods._ins.addLore(stack, " ", true);
		Metods._ins.addLore(stack, "&4&lUnsmitable", true);
		Metods._ins.addLore(stack, "&4&lUnenchantable", true);
	}
	@EventHandler
	public void OnAnvil(PrepareAnvilEvent e)
	{
		AnvilInventory inv = e.getInventory();
		
		ItemStack stack1 = inv.getItem(0);
		ItemStack stack2 = inv.getItem(1);
		if(stack1 == null || stack2 == null) return;
		
		ItemStack result = new ItemStack(Material.AIR);
		
		if(IsUnenchantable(stack1) && (stack2.getEnchantments().size() > 0 || stack2.getType() == Material.ENCHANTED_BOOK))
		{
			e.setResult(result);
			return;
		}
		
		if(IsUnenchantable(stack2) && (stack1.getEnchantments().size() > 0 || stack1.getType() == Material.ENCHANTED_BOOK))
		{
			e.setResult(result);
			return;
		}
		

	}
	@EventHandler
	public void OnBlockPlace(BlockPlaceEvent e)
	{
		if(e.isCancelled()) return;
		
		if(e.getItemInHand() == null) return;
		
		if(e.getItemInHand().getType() != Material.STONE) return;
		
		if(IsVoidStone(e.getItemInHand())) {e.setCancelled(true); return;}
		
	}
	@EventHandler
	public void AnvilEvent(PrepareAnvilEvent e)
	{

		AnvilInventory inv = e.getInventory();
		
		ItemStack stack1 = inv.getItem(0);
		ItemStack voidStone = inv.getItem(1);
		if(stack1 == null || voidStone == null) return;
		
		ItemStack result = new ItemStack(Material.AIR);
		
		if(IsUnenchantable(stack1) || IsUnenchantable(voidStone))
		{
			e.setResult(result);
			return;
		}
		
		if((Metods._ins.isTool(stack1) || Metods._ins.isArmor(stack1)) && GetVoidStoneType(voidStone) == VOID_STONE_TYPE.UNSTABLE)
		{
			int enchantCount = stack1.getEnchantments().size();
			
			if(enchantCount < 3) return;
			
			result = new ItemStack(stack1.getType());
			//result.setAmount(1);
			String[] lores = new String[stack1.getEnchantments().size()];
			for(int i = 0; i < enchantCount; i++)
			{
				lores[i] = "&b&kTTTTTTT";
			}
			Metods._ins.SetLores(result, lores, false);
			Metods.setDisplayName(result, Metods._ins.GetItemDisplayName(stack1));
			e.setResult(result);
			return;
		}
		
		if((Metods._ins.isTool(stack1) || Metods._ins.isArmor(stack1)) && GetVoidStoneType(voidStone) == VOID_STONE_TYPE.REFORCE)
		{
			if(!(Manager_HellTools.Instance.IsHellTool(stack1) || Manager_HellArmor.Instance.IsHellArmor(stack1))) return;
			
			result = new ItemStack(Material.BEDROCK);
			Metods.setDisplayName(result, "&e&kT &5Unstable Result &e&kT");

			e.setResult(result);
			return;
		}
		
		if((Metods._ins.isTool(stack1) || Metods._ins.isArmor(stack1)) && GetVoidStoneType(voidStone) == VOID_STONE_TYPE.UNANOINTMENT)
		{
			
			int enchantCount = stack1.getEnchantments().size();
			
			if(enchantCount == 0) return;
	
			result = new ItemStack(stack1.getType());
			//result.setAmount(1);
			String[] lores = new String[stack1.getEnchantments().size()];
			for(int i = 0; i < enchantCount; i++)
			{
				lores[i] = "&5&kTTTTTTT";
			}
			Metods._ins.SetLores(result, lores, false);
			Metods.setDisplayName(result, Metods._ins.GetItemDisplayName(stack1));
			e.setResult(result);
			return;
		}
		
		
		

	}
		
	@EventHandler
	public void AnvilClick(InventoryClickEvent e)
	{
		
		if(e.isCancelled()) return;
		
		if(!(e.getInventory() instanceof AnvilInventory)) return;
		
		if(e.getSlotType() != SlotType.RESULT) return;
		
		AnvilInventory inv = (AnvilInventory)e.getInventory();
				
		if(inv.getItem(0) == null || inv.getItem(1) == null) return;
		
		ItemStack stack1 = inv.getItem(0);
		ItemStack voidStone = inv.getItem(1);

		if(!(Metods._ins.isTool(stack1) || Metods._ins.isArmor(stack1))) return;
		
		
		if(GetVoidStoneType(voidStone) == VOID_STONE_TYPE.UNSTABLE)
		{
			//ItemStack stack = Unstable_Void_Stone.UseItem(stack1.clone(), GetVoidStoneTier(stack2));
			ItemStack stack = UseVoidStone(stack1.clone(), voidStone);
			
			SetEnchantableLores(stack);
			
			Metods._ins.InventoryAddItemOrDrop(stack, (Player)e.getWhoClicked());
			//inv.setItem(0, new ItemStack(Material.AIR));
			inv.getItem(0).setAmount(stack1.getAmount()-1);
			inv.getItem(1).setAmount(voidStone.getAmount()-1);
			inv.getItem(2).setAmount(0);
			//inv.setItem(1, new ItemStack(Material.AIR));
			return;
		}
		
		if(GetVoidStoneType(voidStone) == VOID_STONE_TYPE.REFORCE)
		{
			ItemStack stack = UseVoidStone(stack1.clone(), voidStone);
			if(stack == null) return;
			
			if(stack.getType() != Material.AIR)
			{
				Metods._ins.InventoryAddItemOrDrop(stack, (Player)e.getWhoClicked());
			}
			
			//inv.setItem(0, new ItemStack(Material.AIR));
			inv.getItem(0).setAmount(stack1.getAmount()-1);
			inv.getItem(1).setAmount(voidStone.getAmount()-1);
			inv.getItem(2).setAmount(0);
			return;
		}
		
		if(GetVoidStoneType(voidStone) == VOID_STONE_TYPE.UNANOINTMENT)
		{
			//ItemStack stack = Unstable_Void_Stone.UseItem(stack1.clone(), GetVoidStoneTier(stack2));
			ItemStack stack = UseVoidStone(stack1.clone(), voidStone);
			
			Metods._ins.InventoryAddItemOrDrop(stack, (Player)e.getWhoClicked());
			//inv.setItem(0, new ItemStack(Material.AIR));
			inv.getItem(0).setAmount(stack1.getAmount()-1);
			inv.getItem(1).setAmount(voidStone.getAmount()-1);
			inv.getItem(2).setAmount(0);
			//inv.setItem(1, new ItemStack(Material.AIR));
			return;
		}

		
		
	}
	
	
}
