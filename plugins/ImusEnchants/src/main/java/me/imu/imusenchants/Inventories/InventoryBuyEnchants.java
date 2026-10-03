package me.imu.imusenchants.Inventories;

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

import me.imu.imusenchants.CONSTANTS;
import me.imu.imusenchants.CustomEnchants.CustomEnchant;
import me.imu.imusenchants.CustomEnchants.CustomEnchantBook;
import me.imu.imusenchants.CustomEnchants.CustomEnchantRegistry;
import me.imu.imusenchants.CustomEnchants.EnchantSettings;
import me.imu.imusenchants.Enchants.NodeBooster;
import me.imu.imusenchants.ImusEnchants;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class InventoryBuyEnchants extends CustomInventory
{
	private final ItemStack EMPTY_BLACK_SLOT;
	private static final Random _random = new Random();
	private final Set<Enchantment> _excludedEnchants = new HashSet<>();
	public InventoryBuyEnchants()
	{
		super(ImusEnchants.Instance, "&3Buy &eEnchants", 6*9);
		EMPTY_BLACK_SLOT = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		ItemUtils.SetDisplayNameEmpty(EMPTY_BLACK_SLOT);
		initExcludedEnchants();
	}
	
	@Override
	public INVENTORY_AREA setInventoryLock()
	{
		return null;
	}


	@Override
	public void onAwake()
	{

	}

	@Override
	public void onOpen()
	{
		super.onOpen();
		initButtons();
		
	}
	
	private void clearTable()
	{
		for(int i = 0; i < getSize(); i++)
		{
			Button button = new Button(i, EMPTY_BLACK_SLOT);
			addButton(button);
		}
	}



	private void initExcludedEnchants()
	{
		_excludedEnchants.clear();
		if(CONSTANTS.ENABLE_MENDING_FOUND_ONLY_END)
		{
			_excludedEnchants.add(Enchantment.MENDING);
		}

		_excludedEnchants.add(Enchantment.VANISHING_CURSE);
		_excludedEnchants.add(Enchantment.BINDING_CURSE);
		_excludedEnchants.addAll(EnchantSettings.GetDisabledVanilla());
	}
	
	private void initButtons()
	{
		ItemStack stack; 
		
		clearTable();
		
		addDefaultBackButton(getSize()-9);
		final String color1 = "&e";
		final String color2 = "&b";
		
		final String[] tier1_enchant_desc = new String[] 
		{
			"&3Guaranteed &eTier &91 &3Enchant",
			"&9Low &3chance to &eTier &d2",
			"&3Buyable at &2"+ CONSTANTS.CAP_FIRST_1_ENCHANTS+"L",
			"&3Small chance for a &dCustom Enchant"
		};
		
		final String[] tier2_enchant_desc = new String[] 
		{
			"&3Guaranteed &eTier &91 &3Enchant",
			"&6High &3chance to &eTier &d2",
			"&3Buyable at &2"+CONSTANTS.CAP_FIRST_2_ENCHANTS+"L",
			"&3Small chance for a &dCustom Enchant"
		};
		
		final String[] tier3_enchant_desc = new String[] 
		{
			"&3Guaranteed &eTier &d2 &3Enchant",
			"&6High &3chance to &eTier &63",
			"&3Buyable at &2"+CONSTANTS.CAP_FIRST_3_ENCHANTS+"L",
			"&3Small chance for a &dCustom Enchant"
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
		int _xOffset = 2;
		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"More like Tools"+color1+")");
		ItemUtils.AddLore(stack, tier1_enchant_desc);
		button = new Button(_xOffset + 0, stack, inventoryClickEvent ->
		{
			//ButtonBuyTool(ENCHANTMENT_TIER.TIER_1);
			buttonBuyMoreLikeTool(ENCHANTMENT_TIER.TIER_1);
	    });
		addButton(button);
		
		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"More like Tools"+color1+")");
		ItemUtils.AddLore(stack, tier2_enchant_desc);
		button = new Button(_xOffset + 9, stack, inventoryClickEvent ->
		{
			//ButtonBuyTool(ENCHANTMENT_TIER.TIER_2);
			buttonBuyMoreLikeTool(ENCHANTMENT_TIER.TIER_2);
	    });
		addButton(button);
		
		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"More like Tools"+color1+")");
		ItemUtils.AddLore(stack, tier3_enchant_desc);
		button = new Button(_xOffset + 9+9, stack, inventoryClickEvent ->
		{
			//ButtonBuyTool(ENCHANTMENT_TIER.TIER_3);
			buttonBuyMoreLikeTool(ENCHANTMENT_TIER.TIER_3);

	    });
		addButton(button);
		//<<< COLUMN 1
		
		//===============================================================
		
		//>>> COLUMN 2
		/*stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"Combat"+color1+")");
		ItemUtils.AddLore(stack, tier1_enchant_desc);
		button = new Button(_xOffset + 2, stack, inventoryClickEvent ->
		{
			ButtonBuyWeapon(ENCHANTMENT_TIER.TIER_1);
	    });
		AddButton(button);

		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"Combat"+color1+")");
		ItemUtils.AddLore(stack, tier2_enchant_desc);
		button = new Button(_xOffset + 2+9, stack, inventoryClickEvent ->
		{
			ButtonBuyWeapon(ENCHANTMENT_TIER.TIER_2);
	    });
		AddButton(button);

		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"Combat"+color1+")");
		ItemUtils.AddLore(stack, tier3_enchant_desc);
		button = new Button(_xOffset + 2+9+9, stack, inventoryClickEvent ->
		{
			ButtonBuyWeapon(ENCHANTMENT_TIER.TIER_3);
	    });
		AddButton(button);*/
		//<<< COLUMN 2
		
		//===============================================================
		
		//>>> COLUMN 3
		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"More like Armor"+color1+")");
		ItemUtils.AddLore(stack, tier1_enchant_desc);
		button = new Button(_xOffset + 4, stack, inventoryClickEvent ->
		{
			//ButtonBuyArmor(ENCHANTMENT_TIER.TIER_1);
			buttonBuyMoreLikeArmor(ENCHANTMENT_TIER.TIER_1);
	    });
		addButton(button);
		
		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"More like Armor"+color1+")");
		ItemUtils.AddLore(stack, tier2_enchant_desc);
		button = new Button(_xOffset + 4+9, stack, inventoryClickEvent ->
		{
			//ButtonBuyArmor(ENCHANTMENT_TIER.TIER_2);
			buttonBuyMoreLikeArmor(ENCHANTMENT_TIER.TIER_2);
	    });
		addButton(button);
		
		stack = new ItemStack(CONSTANTS.ENCHANT_MATERIAL);
		ItemUtils.SetDisplayName(stack, color1+"Buy Enchant ("+color2+"More like Armor"+color1+")");
		ItemUtils.AddLore(stack, tier3_enchant_desc);
		button = new Button(_xOffset + 4+9+9, stack, inventoryClickEvent ->
		{
			//ButtonBuyArmor(ENCHANTMENT_TIER.TIER_3);
			buttonBuyMoreLikeArmor(ENCHANTMENT_TIER.TIER_3);
	    });
		addButton(button);
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

		_xOffset = 4;
		stack = new ItemStack(CONSTANTS.BOOSTER_MATERIAL);
		ItemUtils.HideFlag(stack, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
		ItemUtils.AddGlow(stack);
		ItemUtils.SetDisplayName(stack, color1+"Buy Booster");
		ItemUtils.SetLores(stack, tier1_booster_desc, false);
		button = new Button(_xOffset, stack, inventoryClickEvent ->
		{
			buttonBuyBooster(ENCHANTMENT_TIER.TIER_1);
	    });
		addButton(button);
		
		stack = new ItemStack(CONSTANTS.BOOSTER_MATERIAL);
		ItemUtils.HideFlag(stack, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
		ItemUtils.AddGlow(stack);
		ItemUtils.SetDisplayName(stack, color1+"Buy Booster");
		ItemUtils.SetLores(stack, tier2_booster_desc, false);
		button = new Button(_xOffset+9, stack, inventoryClickEvent ->
		{
			buttonBuyBooster(ENCHANTMENT_TIER.TIER_2);
	    });
		addButton(button);
		
		stack = new ItemStack(CONSTANTS.BOOSTER_MATERIAL);
		ItemUtils.HideFlag(stack, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
		ItemUtils.AddGlow(stack);
		ItemUtils.SetDisplayName(stack, color1+"Buy Booster");
		ItemUtils.SetLores(stack, tier3_booster_desc, false);
		button = new Button(_xOffset+9+9, stack, inventoryClickEvent ->
		{
			buttonBuyBooster(ENCHANTMENT_TIER.TIER_3);
	    });
		addButton(button);
		//<<< COLUMN 5
		
		
		updateButtons(true);
	}
	
	private void buttonBuyBooster(ENCHANTMENT_TIER bookTier)
	{
		if(!playerHasEnoughLevelsBooster(bookTier)) return;

		reducePlayerLevel(CONSTANTS.GetCostBooster(bookTier));

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
		InvUtil.AddItemToInventoryOrDrop(getPlayer(), booster.GetItemStack());
	}

	private void reducePlayerLevel(int reduce)
	{
		// Creative players skip the level check, so this can be more than they have
		XpUtil.SetPlayerLevel(getPlayer(), Math.max(0, getPlayer().getLevel() - reduce));
	}
	
	private boolean playerHasEnoughLevelsEnchant(ENCHANTMENT_TIER tier)
	{
		if(getPlayer().getGameMode() == GameMode.CREATIVE) return true;
		
	    // The cost can be set above the level requirement in config.yml; never charge more than the player has
	    int requiredLevel = Math.max(CONSTANTS.GetCapEnchant(tier), CONSTANTS.GetCostEnchant(tier));
	    
	    boolean hasEnough = getPlayer().getLevel() >= requiredLevel;
	    
	    if(!hasEnough)
	    {
	    	getPlayer().sendMessage(Metods.msgC("&7Not Enough Levels"));
	    }
	    return hasEnough;
	}
	
	private boolean playerHasEnoughLevelsBooster(ENCHANTMENT_TIER tier)
	{
		if(getPlayer().getGameMode() == GameMode.CREATIVE) return true;
		
	    int requiredLevel = CONSTANTS.CAP_FIRST_1_BOOSTER;
	    switch (tier)
		{
			case TIER_1: requiredLevel = CONSTANTS.CAP_FIRST_1_BOOSTER;
				break;
			case TIER_2: requiredLevel = CONSTANTS.CAP_FIRST_2_BOOSTER;
				break;
			case TIER_3: requiredLevel = CONSTANTS.CAP_FIRST_3_BOOSTER;
				break;
		}
	    requiredLevel = Math.max(requiredLevel, CONSTANTS.GetCostBooster(tier));

	    boolean hasEnough = getPlayer().getLevel() >= requiredLevel;
	    
	    if(!hasEnough)
	    {
	    	getPlayer().sendMessage(Metods.msgC("&7Not Enough Levels"));
	    }
	    return hasEnough;
	}
	
	
	private ENCHANTMENT_TIER getEnchantTier(ENCHANTMENT_TIER bookTier)
	{
	    switch (bookTier) {
	        case TIER_1:
	            return (rollChance(CONSTANTS.NORMAL_CHANCE_1_TO_BE_TIER_2)) ? ENCHANTMENT_TIER.TIER_2 : ENCHANTMENT_TIER.TIER_1;
	        
	        case TIER_2:
	            return (rollChance(CONSTANTS.HIGH_CHANCE_1_TO_BE_TIER_2)) ? ENCHANTMENT_TIER.TIER_2 : ENCHANTMENT_TIER.TIER_1;
	        case TIER_3:
	            return (rollChance(CONSTANTS.HIGH_CHANCE_2_TO_BE_TIER_3)) ? ENCHANTMENT_TIER.TIER_3 : ENCHANTMENT_TIER.TIER_2;

	        default:
	            return bookTier;
	    }
	}

	private boolean rollChance(double chance)
	{
		return _random.nextDouble() < chance;
	}


	private void buttonBuyMoreLikeTool(ENCHANTMENT_TIER bookTier)
	{
		if(rollChance(CONSTANTS.BUY_MORE_LIKE_TOOL_CHANCE))
		{
			if(rollChance(CONSTANTS.BUY_MORE_LIKE_TOOL_TO_BE_TOOL_CHANCE))
			{
				buttonBuyTool(bookTier);
				return;
			}
			buttonBuyWeapon(bookTier);
			return;
		}
		buttonBuyArmor(bookTier);

	}

	private void buttonBuyMoreLikeArmor(ENCHANTMENT_TIER bookTier)
	{
		if(rollChance(CONSTANTS.BUY_MORE_LIKE_ARMOR_CHANCE))
		{
			buttonBuyArmor(bookTier);
			return;
		}

		//50% chance to be weapon
		if(rollChance(0.5))
		{
			buttonBuyWeapon(bookTier);
			return;
		}
		buttonBuyTool(bookTier);

	}

	private void buttonBuyTool(ENCHANTMENT_TIER bookTier)
	{
		buyBook(bookTier, ITEM_CATEGORY.TOOL);
	}

	private void buttonBuyWeapon(ENCHANTMENT_TIER bookTier)
	{
		buyBook(bookTier, ITEM_CATEGORY.WEAPON);
	}

	private void buttonBuyArmor(ENCHANTMENT_TIER bookTier)
	{
		buyBook(bookTier, ITEM_CATEGORY.ARMOR);
	}

	// The book is picked before the levels are taken: when every enchant of the category is
	// switched off in /ien admin there is nothing to give, and nothing is charged
	private void buyBook(ENCHANTMENT_TIER bookTier, ITEM_CATEGORY category)
	{
		if(!playerHasEnoughLevelsEnchant(bookTier)) return;

		ItemStack book = pickBook(bookTier, category);
		if(book == null)
		{
			getPlayer().sendMessage(Metods.msgC("&cNo enchants of this kind are available right now"));
			return;
		}

		reducePlayerLevel(CONSTANTS.GetCostEnchant(bookTier));
		InvUtil.AddItemToInventoryOrDrop(getPlayer(), book);
	}

	// Sometimes the bought book is a custom enchant of the category instead, always level I.
	// If no vanilla enchant of the category is left, a custom one is given if there is any.
	private ItemStack pickBook(ENCHANTMENT_TIER bookTier, ITEM_CATEGORY category)
	{
		CustomEnchant custom = rollChance(CONSTANTS.CUSTOM_BOOK_SHOP_CHANCE)
				? CustomEnchantRegistry.GetRandomForShop(category) : null;
		if(custom != null) return CustomEnchantBook.Create(custom, 1);

		Enchantment enchant = EnchantUtil.GetRandomEnchantment(category, getEnchantTier(bookTier), _excludedEnchants);
		if(enchant != null) return EnchantUtil.GetEnchantedBook(enchant);

		custom = CustomEnchantRegistry.GetRandomForShop(category);
		return custom == null ? null : CustomEnchantBook.Create(custom, 1);
	}

	private void buttonBuyAll(ENCHANTMENT_TIER bookTier)
	{
		if(!playerHasEnoughLevelsEnchant(bookTier)) return;
		
		reducePlayerLevel(CONSTANTS.GetCostEnchant(bookTier));
		
		
	}

	
	
	

}
