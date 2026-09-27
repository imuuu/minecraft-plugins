package imu.imusEnchants.Inventories;

import java.util.Random;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.ENCHANTMENT_TIER;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.Enums.ITEM_CATEGORY;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Other.Metods;
import imu.iAPI.Other.XpUtil;
import imu.iAPI.Utilities.EnchantUtil;
import imu.iAPI.Utilities.InvUtil;
import imu.iAPI.Utilities.ItemUtils;
import imu.imusEnchants.Enchants.NodeBooster;
import imu.imusEnchants.main.CONSTANTS;
import imu.imusEnchants.main.ImusEnchants;

public class InventoryBuyEnchants extends CustomInventory
{
	private final ItemStack EMPTY_BLACK_SLOT;
	private static Random _random = new Random();
	
	public InventoryBuyEnchants()
	{
		super(ImusEnchants.Instance, "Buy Enchants", 6*9);
		EMPTY_BLACK_SLOT = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		ItemUtils.SetDisplayNameEmpty(EMPTY_BLACK_SLOT);
	}
	
	@Override
	public INVENTORY_AREA SetInventoryLock()
	{
		return null;
	}

	
	@Override
	public void OnOpen()
	{
		super.OnOpen();
		InitButtons();
		
	}
	
	private void ClearTable()
	{
		for(int i = 0; i < GetSize(); i++)
		{
			Button button = new Button(i, EMPTY_BLACK_SLOT);
			AddButton(button);
		}
	}
	
	
	
	private void InitButtons()
	{
		ItemStack stack; 
		
		ClearTable();
		
		AddDefaultBackButton(GetSize()-9);
		final String color1 = "&e";
		final String color2 = "&b";
		
		final String[] tier1_enchant_desc = new String[] 
		{
			"&3Guaranteed &eTier &91 &3Enchant",
			"&9Low &3chance to &eTier &d2",
			"&3Buyable at &2"+CONSTANTS.CAP_FIRST_1_ENCHANTS+"L"
		};
		
		final String[] tier2_enchant_desc = new String[] 
		{
			"&3Guaranteed &eTier &91 &3Enchant",
			"&6High &3chance to &eTier &d2",
			"&3Buyable at &2"+CONSTANTS.CAP_FIRST_2_ENCHANTS+"L"
		};
		
		final String[] tier3_enchant_desc = new String[] 
		{
			"&3Guaranteed  &eTier &d2 &3Enchant",
			"&6High &3chance to &eTier &63",
			"&3Buyable at &2"+CONSTANTS.CAP_FIRST_3_ENCHANTS+"L"
		};
		
		final String[] tier1_booster_desc = new String[] 
		{
			"&3Guaranteed &6Booster",
			"&3Booster has &5one &3power direcion",
			"&3Buyable at &2"+CONSTANTS.CAP_FIRST_1_BOOSTER+"L",
			"&cCost &2"+CONSTANTS.COST_FIRST_1_BOOSTER+"L"
		};
		
		final String[] tier2_booster_desc = new String[] 
		{
			"&3Guaranteed &6Booster",
			"&3Booster has &5two &3or &5more &3power direcion",
			"&3Buyable at &2"+CONSTANTS.CAP_FIRST_2_BOOSTER+"L",
			"&cCost &2"+CONSTANTS.COST_FIRST_2_BOOSTER+"L"
		};
		
		final String[] tier3_booster_desc = new String[] 
		{
			"&3Guaranteed &6Booster",
			"&3Booster has &5four &3power direcion",
			"&3Buyable at &2"+CONSTANTS.CAP_FIRST_3_BOOSTER+"L",
			"&cCost &2"+CONSTANTS.COST_FIRST_3_BOOSTER+"L"
		};
		
		//===============================================================
		Button button;
		//>>> COLUMN 1
		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"Tools"+color1+")");
		ItemUtils.AddLore(stack, tier1_enchant_desc);
		button = new Button(0, stack, inventoryClickEvent -> 
		{
			ButtonBuyTool(ENCHANTMENT_TIER.TIER_1);
	    });
		AddButton(button);
		
		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"Tools"+color1+")");
		ItemUtils.AddLore(stack, tier2_enchant_desc);
		button = new Button(9, stack, inventoryClickEvent -> 
		{
			ButtonBuyTool(ENCHANTMENT_TIER.TIER_2);
	    });
		AddButton(button);
		
		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"Tools"+color1+")");
		ItemUtils.AddLore(stack, tier3_enchant_desc);
		button = new Button(9+9, stack, inventoryClickEvent -> 
		{
			ButtonBuyTool(ENCHANTMENT_TIER.TIER_3);
	    });
		AddButton(button);
		//<<< COLUMN 1
		
		//===============================================================
		
		//>>> COLUMN 2
		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"Combat"+color1+")");
		ItemUtils.AddLore(stack, tier1_enchant_desc);
		button = new Button(2, stack, inventoryClickEvent -> 
		{
			ButtonBuyWeapon(ENCHANTMENT_TIER.TIER_1);
	    });
		AddButton(button);
		
		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"Combat"+color1+")");
		ItemUtils.AddLore(stack, tier2_enchant_desc);
		button = new Button(2+9, stack, inventoryClickEvent -> 
		{
			ButtonBuyWeapon(ENCHANTMENT_TIER.TIER_2);
	    });
		AddButton(button);
		
		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"Combat"+color1+")");
		ItemUtils.AddLore(stack, tier3_enchant_desc);
		button = new Button(2+9+9, stack, inventoryClickEvent -> 
		{
			ButtonBuyWeapon(ENCHANTMENT_TIER.TIER_3);
	    });
		AddButton(button);
		//<<< COLUMN 2
		
		//===============================================================
		
		//>>> COLUMN 3
		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"Armor"+color1+")");
		ItemUtils.AddLore(stack, tier1_enchant_desc);
		button = new Button(4, stack, inventoryClickEvent -> 
		{
			ButtonBuyArmor(ENCHANTMENT_TIER.TIER_1);
	    });
		AddButton(button);
		
		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"Armor"+color1+")");
		ItemUtils.AddLore(stack, tier2_enchant_desc);
		button = new Button(4+9, stack, inventoryClickEvent -> 
		{
			ButtonBuyArmor(ENCHANTMENT_TIER.TIER_2);
	    });
		AddButton(button);
		
		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"Armor"+color1+")");
		ItemUtils.AddLore(stack, tier3_enchant_desc);
		button = new Button(4+9+9, stack, inventoryClickEvent -> 
		{
			ButtonBuyArmor(ENCHANTMENT_TIER.TIER_3);
	    });
		AddButton(button);
		//<<< COLUMN 3
		
		//===============================================================
		
		//>>> COLUMN 4
//		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
//		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"All"+color1+")");
//		//ItemUtils.AddLore(stack, tier1_desc);
//		button = new Button(6, stack, inventoryClickEvent -> 
//		{
//			ButtonBuyAll(ENCHANTMENT_TIER.TIER_1);
//	    });
//		AddButton(button);
//		
//		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
//		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"All"+color1+")");
//		//ItemUtils.AddLore(stack, tier1_desc);
//		button = new Button(6+9, stack, inventoryClickEvent -> 
//		{
//			ButtonBuyAll(ENCHANTMENT_TIER.TIER_2);
//	    });
//		AddButton(button);
//		
//		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
//		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"All"+color1+")");
//		//ItemUtils.AddLore(stack, tier1_desc);
//		button = new Button(6+9+9, stack, inventoryClickEvent -> 
//		{
//			ButtonBuyAll(ENCHANTMENT_TIER.TIER_3);
//	    });
//		AddButton(button);
		//<<< COLUMN 4
		
		//===============================================================
		
		//>>> COLUMN 5
		
		stack = new ItemStack(CONSTANTS.BOOSTER_MATERIAL);
		ItemUtils.HideFlag(stack, ItemFlag.HIDE_POTION_EFFECTS);
		ItemUtils.AddGlow(stack);
		ItemUtils.SetDisplayName(stack, color1+"Buy Booster");
		ItemUtils.SetLores(stack, tier1_booster_desc, false);
		button = new Button(8, stack, inventoryClickEvent -> 
		{
			ButtonBuyBooster(ENCHANTMENT_TIER.TIER_1);
	    });
		AddButton(button);
		
		stack = new ItemStack(CONSTANTS.BOOSTER_MATERIAL);
		ItemUtils.HideFlag(stack, ItemFlag.HIDE_POTION_EFFECTS);
		ItemUtils.AddGlow(stack);
		ItemUtils.SetDisplayName(stack, color1+"Buy Booster");
		ItemUtils.SetLores(stack, tier2_booster_desc, false);
		button = new Button(8+9, stack, inventoryClickEvent -> 
		{
			ButtonBuyBooster(ENCHANTMENT_TIER.TIER_2);
	    });
		AddButton(button);
		
		stack = new ItemStack(CONSTANTS.BOOSTER_MATERIAL);
		ItemUtils.HideFlag(stack, ItemFlag.HIDE_POTION_EFFECTS);
		ItemUtils.AddGlow(stack);
		ItemUtils.SetDisplayName(stack, color1+"Buy Booster");
		ItemUtils.SetLores(stack, tier3_booster_desc, false);
		button = new Button(8+9+9, stack, inventoryClickEvent -> 
		{
			ButtonBuyBooster(ENCHANTMENT_TIER.TIER_3);
	    });
		AddButton(button);
		//<<< COLUMN 5
		
		
		UpdateButtons(true);
	}
	
	private void ButtonBuyBooster(ENCHANTMENT_TIER bookTier)
	{
		if(!PlayerHasEnoughLevelsBooster(bookTier)) return;
	    
		NodeBooster booster = null;
		
		switch(bookTier)
		{
		case TIER_1: booster = new NodeBooster(1,1);
			break;
		case TIER_2: booster = new NodeBooster(1,2 + _random.nextInt(3));
			break;
		case TIER_3: booster = new NodeBooster(1,4);
			break;
		default:
			break;
		
		}
		InvUtil.AddItemToInventoryOrDrop(GetPlayer(), booster.GetItemStack());
	}

	private void ReducePlayerLevel(int reduce)
	{
		if(GetPlayer().getLevel() <= 0)
		{
			XpUtil.SetPlayerLevel(GetPlayer(), 0);
			return;
		}
		XpUtil.SetPlayerLevel(GetPlayer(), GetPlayer().getLevel()-reduce);
	}
	
	private boolean PlayerHasEnoughLevels(ENCHANTMENT_TIER tier) 
	{
		if(GetPlayer().getGameMode() == GameMode.CREATIVE) return true;
		
	    int requiredLevel = CONSTANTS.GetCapEnchant(tier);
	    
	    boolean hasEnough = GetPlayer().getLevel() >= requiredLevel;
	    
	    if(!hasEnough)
	    {
	    	GetPlayer().sendMessage(Metods.msgC("&7Not Enough Levels"));
	    }
	    return hasEnough;
	}
	
	private boolean PlayerHasEnoughLevelsBooster(ENCHANTMENT_TIER tier) 
	{
		if(GetPlayer().getGameMode() == GameMode.CREATIVE) return true;
		
	    int requiredLevel = CONSTANTS.CAP_FIRST_1_BOOSTER;
	    
	    boolean hasEnough = GetPlayer().getLevel() >= requiredLevel;
	    
	    if(!hasEnough)
	    {
	    	GetPlayer().sendMessage(Metods.msgC("&7Not Enough Levels"));
	    }
	    return hasEnough;
	}
	
	
	private ENCHANTMENT_TIER GetEnchantTier(ENCHANTMENT_TIER bookTier) 
	{
	    switch (bookTier) {
	        case TIER_1:
	            return (RollChance(CONSTANTS.NORMAL_CHANCE_1_TO_BE_TIER_2)) ? ENCHANTMENT_TIER.TIER_2 : ENCHANTMENT_TIER.TIER_1;
	        
	        case TIER_2:
	            return (RollChance(CONSTANTS.HIGH_CHANCE_1_TO_BE_TIER_2)) ? ENCHANTMENT_TIER.TIER_2 : ENCHANTMENT_TIER.TIER_1;
	        case TIER_3:
	            return (RollChance(CONSTANTS.HIGH_CHANCE_2_TO_BE_TIER_3)) ? ENCHANTMENT_TIER.TIER_3 : ENCHANTMENT_TIER.TIER_2;

	        default:
	            return bookTier;
	    }
	}

	private boolean RollChance(double chance) 
	{
	    return _random.nextDouble() * 100 < chance;
	}

	private void ButtonBuyTool(ENCHANTMENT_TIER bookTier) 
	{
		if(!PlayerHasEnoughLevels(bookTier)) return;
	    
		ReducePlayerLevel(CONSTANTS.GetCostEnchant(bookTier));
		
		ENCHANTMENT_TIER enchantTier = GetEnchantTier(bookTier);
		
		Enchantment enchant = EnchantUtil.GetRandomEnchantment(ITEM_CATEGORY.TOOL, enchantTier);
		ItemStack book = EnchantUtil.GetEnchantedBook(enchant);
		InvUtil.AddItemToInventoryOrDrop(GetPlayer(), book);
		 
	}
	
	private void ButtonBuyWeapon(ENCHANTMENT_TIER bookTier)
	{
		if(!PlayerHasEnoughLevels(bookTier)) return;
		
		ReducePlayerLevel(CONSTANTS.GetCostEnchant(bookTier));
		
		ENCHANTMENT_TIER enchantTier = GetEnchantTier(bookTier);
		
		Enchantment enchant = EnchantUtil.GetRandomEnchantment(ITEM_CATEGORY.WEAPON, enchantTier);
		ItemStack book = EnchantUtil.GetEnchantedBook(enchant);
		InvUtil.AddItemToInventoryOrDrop(GetPlayer(), book);
	}
	
	private void ButtonBuyArmor(ENCHANTMENT_TIER bookTier)
	{
		if(!PlayerHasEnoughLevels(bookTier)) return;
		
		ReducePlayerLevel(CONSTANTS.GetCostEnchant(bookTier));
		
		ENCHANTMENT_TIER enchantTier = GetEnchantTier(bookTier);
		
		Enchantment enchant = EnchantUtil.GetRandomEnchantment(ITEM_CATEGORY.ARMOR,enchantTier);
		ItemStack book = EnchantUtil.GetEnchantedBook(enchant);
		InvUtil.AddItemToInventoryOrDrop(GetPlayer(), book);
	}
	
	private void ButtonBuyAll(ENCHANTMENT_TIER bookTier)
	{
		if(!PlayerHasEnoughLevels(bookTier)) return;
		
		ReducePlayerLevel(CONSTANTS.GetCostEnchant(bookTier));
		
		
	}

	
	
	

}
