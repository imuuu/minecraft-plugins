package me.imu.imusenchants.Events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import imu.iAPI.Utilities.ItemUtilToolsArmors;
import imu.iAPI.Utilities.ItemUtilToolsArmors.ArmorMaterial;
import imu.iAPI.Utilities.ItemUtilToolsArmors.ItemMaterial;
import imu.iAPI.Utilities.ItemUtilToolsArmors.ToolMaterial;
import imu.iAPI.Utilities.ItemUtils;
import me.imu.imusenchants.Enchants.EnchantedItem;
import me.imu.imusenchants.Enchants.NodeBooster;
import me.imu.imusenchants.CONSTANTS;

public class VillagerEvents implements Listener
{
	private final ItemMaterial[] ItemMats = new ItemMaterial[] 
			{ToolMaterial.DIAMOND, ArmorMaterial.DIAMOND};
	
	public MerchantRecipe CreateEnchantedItem(ItemStack stack)
	{
		ItemStack result = ItemUtilToolsArmors.GetRandomToolOrArmor(ItemMats);
		result.setAmount(1);
		EnchantedItem eItem = new EnchantedItem(result);
		eItem.SetTooltip();
		int slots = eItem.Get_slots();
		
		NodeBooster booster = new NodeBooster();
		booster.RandomizeDirection(3);

		ItemStack boosterStack = booster.GetItemStack();
		
		MerchantRecipe recipe = new MerchantRecipe(result, 2);

		recipe.addIngredient(new ItemStack(Material.DIAMOND, Math.round(slots * 0.4f)+1));
		recipe.addIngredient(boosterStack);
		return recipe;
	}
	
	public MerchantRecipe CreateBoosterItem()
	{
		NodeBooster booster = new NodeBooster();
		booster.RandomizeDirectionMax(3);
		ItemStack result = booster.GetItemStack();
		result.setAmount(1);
		MerchantRecipe recipe = new MerchantRecipe(result, 2);
		recipe.addIngredient(new ItemStack(Material.DIAMOND, booster.GetDirections().length * 3));
		
		return recipe;
	}
	
	
	@EventHandler
	public void OnVillagerAcquireTrade(VillagerAcquireTradeEvent event)
	{
		MerchantRecipe recipe = event.getRecipe();
		ItemStack result = recipe.getResult();

		if (ItemUtils.IsTool(result) || ItemUtils.IsArmor(result))
		{
			if(CONSTANTS.ENABLE_SELL_BOOSTERS_FOR_TOOLS_ARMOR_VILLAGERS)
			{
				NodeBooster booster = new NodeBooster();
				booster.RandomizeDirectionMax(4);

				ItemStack s = booster.GetItemStack();
				List<ItemStack> stacks = new ArrayList<>();
				stacks.add(s);
				stacks.add(new ItemStack(Material.DIAMOND));
				recipe.setIngredients(stacks);
			}
			
			if(event.getEntity() instanceof Villager)
			{
				if(!CONSTANTS.ENABLE_BUY_SLOT_ITEMS_VILLAGERS) return;
				
				Villager villager = (Villager) event.getEntity();
				List<MerchantRecipe> newRecipes = new ArrayList<>(villager.getRecipes());
				newRecipes.add(CreateEnchantedItem(result));
				villager.setRecipes(newRecipes);
			}
			
		}

		if (result.getType() == Material.ENCHANTED_BOOK)
		{
			if(CONSTANTS.DISABLE_ENCHANTED_BOOKS_VILLAGERS) event.setCancelled(true);
			
			if(!CONSTANTS.ENABLE_BUY_BOOSTERS_VILLAGERS) return;
			if(event.getEntity() instanceof Villager)
			{
				Villager villager = (Villager) event.getEntity();
				List<MerchantRecipe> newRecipes = new ArrayList<>(villager.getRecipes());
				newRecipes.add(CreateBoosterItem());
				villager.setRecipes(newRecipes);
			}
			
		}
	}
}
