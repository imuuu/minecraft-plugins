package imu.imusEnchants.Events;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.Plugin;

import com.magmaguy.betterstructures.api.ChestFillEvent;

import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ItemUtils;
import imu.imusEnchants.Enchants.EnchantedItem;
import imu.imusEnchants.Enchants.NodeBooster;
import imu.imusEnchants.Enums.CALCULATION_MODE;
import imu.imusEnchants.Managers.ManagerEnchants;
import imu.imusEnchants.main.CONSTANTS;
import imu.imusEnchants.main.ImusEnchants;

public class Events implements Listener
{
	Plugin _plugin;

	public Events()
	{
		_plugin = ImusEnchants.Instance;

	}

	@EventHandler
	public void OnCraftItem(CraftItemEvent event)
	{

		ItemStack result = event.getCurrentItem();
		
		if (result == null)
			return;
		
		if (!ManagerEnchants.IsValidToEnchant(result))
			return;
		
		if(!(event.getViewers().get(0) instanceof Player)) return;
		
		if (event.isShiftClick())
		{
			Player player = (Player) event.getViewers().get(0);
			Inventory playerInv = player.getInventory();
			Bukkit.getScheduler().runTaskLater(ImusEnchants.Instance, new Runnable() {
	            @Override
	            public void run() {
	                for (ItemStack stack : playerInv.getContents()) 
	                {
	                    if (!EnchantedItem.IsPrecraftedEnchatable(stack)) continue;

	                    EnchantedItem.RemovePrecraftedEnchatable(stack);
	                    EnchantedItem eItem = new EnchantedItem(stack);
	                    eItem.SetTooltip();
	                }
	            }
	        }, 1L); // Delay of 1 tick
		}
		EnchantedItem.RemovePrecraftedEnchatable(result);
		EnchantedItem eItem = new EnchantedItem(result);
		eItem.SetTooltip();
		

	}
	
	@EventHandler
	public void OnCraftItem(PrepareItemCraftEvent event) 
	{
		
		
		if(!(event.getInventory() instanceof CraftingInventory)) return;
		
		Recipe recipe = event.getRecipe();
		
		if(recipe == null) return;
		
		
		if (!ManagerEnchants.IsValidToEnchant(recipe.getResult()))
			return;
		
		ItemStack stack = recipe.getResult();
		EnchantedItem.SetPrecraftTooltip(stack);
		CraftingInventory inv = event.getInventory();
		inv.setItem(0, stack);
		
		
	}

	@EventHandler
	public void OnPlayerInteract(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			Block clickedBlock = event.getClickedBlock();

			if (clickedBlock != null && clickedBlock.getType() == Material.ENCHANTING_TABLE)
			{
				Player player = event.getPlayer();
				event.setCancelled(true);
				ManagerEnchants.Instance.OpenEnchantingInventory(player);
			}
		}
	}

	@EventHandler
	public void OnPrepareAnvil(PrepareAnvilEvent event)
	{

		AnvilInventory inv = event.getInventory();

		if (!ItemUtils.IsTool(inv.getItem(0)))
			return;

		if (!ItemUtils.IsTool(inv.getItem(1)))
			return;

		ItemStack stack1 = inv.getItem(0);
		ItemStack stack2 = inv.getItem(1);

		if (EnchantedItem.HasSlots(stack1) || EnchantedItem.HasSlots(stack2))
		{
			event.setResult(new ItemStack(Material.AIR));
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onItemMend(PlayerItemMendEvent event)
	{
		int mendingLevel = event.getItem().getEnchantmentLevel(Enchantment.MENDING);
		if (mendingLevel > 0)
		{
			double repairFactor = CONSTANTS.MENDING_INCREASE_BY_LEVEL * mendingLevel;

			int repairAmount = (int) (event.getRepairAmount() * repairFactor);
			event.setRepairAmount(repairAmount);
		}
	}

	@EventHandler
	public void OnSmithing(PrepareSmithingEvent event)
	{
		SmithingInventory inv = event.getInventory();
		if (NodeBooster.IsBooster(inv.getItem(0)))
		{
			event.getViewers().get(0).sendMessage(Metods.msgC("&cBoosters can't be use in smithing table"));
			event.getViewers().get(0).closeInventory();
			return;
		}

		if (!ItemUtils.IsTool(inv.getItem(3)) && !ItemUtils.IsArmor(inv.getItem(3)))
			return;

		ItemStack stack = inv.getItem(3);

		if (!EnchantedItem.HasSlots(stack))
			return;

		EnchantedItem eItem = new EnchantedItem(stack);
		EnchantedItem.SetUpgraded(stack, true);
		eItem.SetTooltip();

		event.setResult(stack);
	}

	@EventHandler
	public void OnGrindstone(InventoryClickEvent event)
	{
		if (!(event.getInventory() instanceof GrindstoneInventory))
			return;

		if (event.getRawSlot() != 2)
			return;

		ItemStack item = event.getCurrentItem();
		if (!EnchantedItem.HasSlots(item))
			return;

		event.setCancelled(true);
		event.getWhoClicked().sendMessage(Metods.msgC("&cYou cannot use &6slotted &citem on a grindstone!"));
		event.getWhoClicked().closeInventory();
	}

	@EventHandler
	public void OnEntityShootBow(EntityShootBowEvent event)
	{
		if (CONSTANTS.CROSSBOW_CONSUME_ARROW_WITH_INFINITY)
			return;

		if (!(event.getEntity() instanceof Player))
			return;

		ItemStack bow = event.getBow();
		if (bow == null || bow.getType() != Material.CROSSBOW)
			return;

		event.setConsumeItem(false);
	}
	
	@SuppressWarnings("unused")
	@EventHandler(priority = EventPriority.HIGH)
	public void OnLootGenerate(LootGenerateEvent event) 
	{
		if(!CONSTANTS.ENABLE_MENDING_FOUND_ONLY_END && CONSTANTS.SET_FOUND_ENCHANTED_BOOKS_LEVEL_ONE) return;
		
	    List<ItemStack> loot = event.getLoot();
	    ProcessFoundItems(loot, event.getWorld());

	    Inventory inventory = event.getInventoryHolder().getInventory();
	    ProcessFoundItems(Arrays.asList(inventory.getContents()), event.getWorld());
	}
	
		//if better structure is enabled on the server this will be triggered
	@SuppressWarnings("unused")
	@EventHandler(priority = EventPriority.HIGH)
	public void OnBetterStructureLoot(ChestFillEvent e)
	{
		if(!CONSTANTS.ENABLE_MENDING_FOUND_ONLY_END && CONSTANTS.SET_FOUND_ENCHANTED_BOOKS_LEVEL_ONE) return;

		Inventory inv = e.getContainer().getInventory();
		inv = e.getContainer().getSnapshotInventory();
	
		ProcessFoundItems(Arrays.asList(inv.getContents()), e.getContainer().getWorld());

	}
	
	private void ProcessFoundItems(List<ItemStack> items, World world) 
	{
	    for (ItemStack item : items) 
	    {
	        if(item == null) continue;
	        
	        if(ManagerEnchants.IsValidToEnchant(item))
	        {
	        	EnchantedItem eItem = new EnchantedItem(item);
	        	eItem.SetTooltip();
	        }
	        
	        if (item.getType() == Material.ENCHANTED_BOOK) 
	        {
	        	System.out.println("enchanted book found: "+item);
	        	if(CONSTANTS.ENABLE_MENDING_FOUND_ONLY_END && ItemUtils.HasEnchant( item, Enchantment.MENDING) && world.getEnvironment() != Environment.THE_END )
	        	{
	        		item.setType(Material.DIAMOND);
	        		item.setAmount(3);
	        		continue;
	        	}
	        	
	        	if(!CONSTANTS.SET_FOUND_ENCHANTED_BOOKS_LEVEL_ONE) continue;
	        	
	        	EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
	            if (meta != null) 
	            {

	                Map<Enchantment, Integer> newEnchantments = new HashMap<>();
	                
	                //keep them one level
	                for (Map.Entry<Enchantment, Integer> entry : meta.getStoredEnchants().entrySet()) {
	                    newEnchantments.put(entry.getKey(), 1);
	                }

	                meta.getStoredEnchants().keySet().forEach(meta::removeStoredEnchant);
	                newEnchantments.forEach((enchant, level) -> meta.addStoredEnchant(enchant, level, true));

	                item.setItemMeta(meta);
	            }
	        }
	    }
	}


	
	@EventHandler
	public void OnCraftItemTool(PrepareItemCraftEvent event)
	{
		ItemStack[] matrix = event.getInventory().getMatrix();
		ItemStack firstItem = null;
		int sameMaterialCount = 0;
		int enchantedItems = 0;
		int slotSum = 0;
		int minSlots = Integer.MAX_VALUE;
		int maxSlots = Integer.MIN_VALUE;
		int totalItems = 0;
		for (ItemStack item : matrix)
		{
			if (item == null || item.getType() == Material.AIR)
			{
				continue;
			}

			totalItems++;
			if (!(ItemUtils.IsArmor(item) || ItemUtils.IsTool(item)))
				continue;

			if (firstItem == null)
			{
				firstItem = item;
				sameMaterialCount++;
			} else if (item.getType() == firstItem.getType())
			{
				sameMaterialCount++;
			}

			Integer slots = EnchantedItem.GetSlots(item);
			if (slots <= 0)
				continue;

			enchantedItems++;
			slotSum += slots;
			minSlots = Math.min(slots, minSlots);
			maxSlots = Math.max(slots, maxSlots);
		}

		if (sameMaterialCount == 2)
		{
			event.getInventory().setResult(new ItemStack(Material.AIR));
			return;
		}

		CALCULATION_MODE mode = CONSTANTS.CRAFTING_SLOT_CALCULATION;

		if (sameMaterialCount == 3 && totalItems == 3 && enchantedItems == 3)
		{
			ItemStack stack = new ItemStack(firstItem.getType());
			EnchantedItem eItem = new EnchantedItem(stack);

			switch (mode)
			{
			case MIN:
				eItem.SetSlots(minSlots);
				break;
			case MAX:
				eItem.SetSlots(maxSlots);
				break;
			case AVERAGE:
				int averageSlots = sameMaterialCount > 0 ? slotSum / sameMaterialCount : 0;
				eItem.SetSlots(averageSlots);
				break;
			}

			eItem.SetTooltip();
			event.getInventory().setResult(stack);
		}

	}

}
