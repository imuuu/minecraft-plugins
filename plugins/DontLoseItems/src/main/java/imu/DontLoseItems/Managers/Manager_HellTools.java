package imu.DontLoseItems.Managers;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import imu.DontLoseItems.CustomItems.Hell_Hoe_Controller;
import imu.DontLoseItems.CustomItems.Hell_Pickaxe_Controller;
import imu.DontLoseItems.CustomItems.Hell_ReflectShieldController;
import imu.DontLoseItems.CustomItems.Hell_Sword_Controller;
import imu.DontLoseItems.CustomItems.Hell_ThrowingAxe_Controller;
import imu.DontLoseItems.Enums.ITEM_RARITY;
import imu.DontLoseItems.main.DontLoseItems;
import imu.iAPI.Other.Metods;


public class Manager_HellTools implements Listener
{
	public static Manager_HellTools Instance;

	public Hell_Pickaxe_Controller HellPickController;
	public Hell_Sword_Controller HellSword_Controller;
	public Hell_ThrowingAxe_Controller HellAxe_Controller;
	public Hell_Hoe_Controller HellHoe_Controller;
	public Hell_ReflectShieldController Hell_ReflectShield_Controller;

	@SuppressWarnings("unused")
	private BukkitTask _task;
	
	public Manager_HellTools() 
	{
		Instance = this;
		HellPickController = new Hell_Pickaxe_Controller();
		HellSword_Controller = new Hell_Sword_Controller();
		HellAxe_Controller = new Hell_ThrowingAxe_Controller();
		HellHoe_Controller = new Hell_Hoe_Controller();
		Hell_ReflectShield_Controller = new Hell_ReflectShieldController();
		
		//AddItemsToTestInv();
		Runnable();
	}
	
//	private void AddItemsToTestInv()
//	{
//		Fast_Inventory fastInv = new Fast_Inventory("Hell Tools", "Hell Inv", new ArrayList<>());
//		fastInv.AddStack(CreateHellDoubleAxe(ITEM_RARITY.Legendary));
//		Manager_FastInventories.Instance.RegisterFastInventory(fastInv);
//	}

	private void Runnable()
	{
		_task = new BukkitRunnable() 
		{				
			@Override
			public void run() 
			{
				
				//ReflectArrowController.OnReflectArrowLoop();
				Hell_ReflectShield_Controller.OnReflectArrowLoop();
			}

			
		}.runTaskTimer(DontLoseItems.Instance, 20 * 3, 1);	
	}
	
	public ItemStack CreateHellPickaxe(ITEM_RARITY rarity)
	{
		return HellPickController.CreateItem(rarity);
	}
	public ItemStack CreateHellTripleSword(ITEM_RARITY rarity)
	{
		return HellSword_Controller.CreateItem(rarity);
	}
	
	public ItemStack CreateHellDoubleAxe(ITEM_RARITY rarity)
	{
		return HellAxe_Controller.CreateItem(rarity);
	}
	
	public ItemStack CreateHellHoe(ITEM_RARITY rarity)
	{
		return HellHoe_Controller.CreateItem(rarity);
	}
	
	public ItemStack CreateHellReflectShield(ITEM_RARITY rarity)
	{
		return Hell_ReflectShield_Controller.CreateItem(rarity);
	}
	
	public boolean IsTier_Pickaxe(ItemStack stack, ITEM_RARITY rarity)
	{
		return HellPickController.IsTier(stack, rarity);
	}
	
	public boolean IsTier_Axe(ItemStack stack, ITEM_RARITY rarity)
	{
		return HellAxe_Controller.IsTier(stack, rarity);
	}
	
	public boolean IsTier_Shield(ItemStack stack, ITEM_RARITY rarity)
	{
		return Hell_ReflectShield_Controller.IsTier(stack, rarity);
	}
	
	public boolean IsTier_Sword(ItemStack stack, ITEM_RARITY rarity)
	{
		return HellSword_Controller.IsTier(stack, rarity);
	}
	
	public boolean IsTier_Hoe(ItemStack stack, ITEM_RARITY rarity)
	{
		return HellHoe_Controller.IsTier(stack, rarity);
	}
	
	public ITEM_RARITY GetRarity(ItemStack stack)
	{
	    if(HellHoe_Controller.IsHellHoe(stack)) 
	        return HellHoe_Controller.GetRarity(stack);

	    if(HellPickController.IsHellPickaxe(stack)) 
	        return HellPickController.GetRarity(stack);

	    if(HellSword_Controller.IsHellSword(stack)) 
	        return HellSword_Controller.GetRarity(stack);

	    if(HellAxe_Controller.IsHellAxe(stack)) 
	        return HellAxe_Controller.GetRarity(stack);

	    if(Hell_ReflectShield_Controller.IsHellReflectShield(stack)) 
	        return Hell_ReflectShield_Controller.GetRarity(stack);

	    // Optionally, handle the case where the item is not a Hell tool
	    // return some default rarity or null

	    return null; // or any default value you want to return if none of the conditions above are met
	}

	
	public void OnDisable()
	{
		HellSword_Controller.OnDisable();
		HellAxe_Controller.OnDisable();
		HellHoe_Controller.OnDisable();
	}
	
	@EventHandler
	public void OnEntityDamageByEntityEvent(EntityDamageByEntityEvent e) 
	{
		if(e.isCancelled()) return;
		
		Hell_ReflectShieldController.Instance.OnBlockingArrow(e);
	}
	
	@EventHandler
	private void OnInteract(PlayerInteractEvent e)
	{
		
		if(e.getClickedBlock() != null && e.getClickedBlock().getType().isInteractable()) return;
		
		HellPickController.OnBlockInteract(e);
		HellSword_Controller.OnThrow(e);
		HellAxe_Controller.OnThrow(e);
		HellHoe_Controller.OnUse(e);
	}
	
	@EventHandler
	private void OnPlayerQuit(PlayerQuitEvent e)
	{
		HellAxe_Controller.OnPlayerQuit(e);
	}
	@EventHandler
	private void OnBlockBreak(BlockBreakEvent e)
	{
		if(e.isCancelled()) return;
		
		HellPickController.OnHellPickBlockBreak(e);
	}
	
	@EventHandler
	private void OnProjectile(ProjectileHitEvent e)
	{
		if(e.isCancelled()) return;
		
		HellAxe_Controller.OnProjectileHit(e);
	}
	
	public boolean IsHellTool(ItemStack stack)
	{
		return 		HellHoe_Controller.IsHellHoe(stack) 
				|| 	HellPickController.IsHellPickaxe(stack) 
				|| 	Hell_ReflectShield_Controller.IsHellReflectShield(stack) 
				|| 	HellSword_Controller.IsHellSword(stack) 
				|| 	HellAxe_Controller.IsHellAxe(stack);

	}
	
	public ItemStack IncreaseTier(ItemStack stack, int increase)
	{
		ITEM_RARITY current_rarity;
		ITEM_RARITY now_rarity; // = current_rarity.GetIncreaseRarity(increase);
		ItemStack newStack = null;
		
		//if used more rapidly then should be use if else	
		if(HellHoe_Controller.IsHellHoe(stack)) 
		{
			current_rarity = HellHoe_Controller.GetRarity(stack);
			now_rarity = current_rarity.GetIncreaseRarity(increase);
			newStack = CreateHellHoe(now_rarity);
		}
		
		if(HellPickController.IsHellPickaxe(stack)) 
		{
			current_rarity = HellPickController.GetRarity(stack);
			now_rarity = current_rarity.GetIncreaseRarity(increase);
			newStack = CreateHellPickaxe(now_rarity);
		}
		
		if(Hell_ReflectShield_Controller.IsHellReflectShield(stack)) 
		{
			current_rarity = Hell_ReflectShield_Controller.GetRarity(stack);
			now_rarity = current_rarity.GetIncreaseRarity(increase);
			newStack = CreateHellReflectShield(now_rarity);
		}
		
		if(HellSword_Controller.IsHellSword(stack)) 
		{
			current_rarity = HellSword_Controller.GetRarity(stack);
			now_rarity = current_rarity.GetIncreaseRarity(increase);
			newStack = CreateHellTripleSword(now_rarity);
		}
		
		if(HellAxe_Controller.IsHellAxe(stack)) 
		{
			current_rarity = HellAxe_Controller.GetRarity(stack);
			now_rarity = current_rarity.GetIncreaseRarity(increase);
			newStack = CreateHellDoubleAxe(now_rarity);
		}
		
		Metods.CloneEnchantments(stack, newStack);
		return newStack;
	}
	
	@EventHandler
	public void Smithing(PrepareAnvilEvent e)
	{

		AnvilInventory inv = e.getInventory();
		
		ItemStack stack1 = inv.getItem(0);
		ItemStack stack2 = inv.getItem(1);
		if(stack1 == null || stack2 == null) return;
		
		ItemStack result = null;
		if(IsTier_Axe(stack1,ITEM_RARITY.Legendary) && Manager_LegendaryUpgrades.Instance.IsUpgradeHellAxe(stack2))
		{
			result = CreateHellDoubleAxe(ITEM_RARITY.Void);
			Metods.CloneEnchantments(stack1, result);
			e.setResult(result);
			return;
		}
		
		if(IsTier_Hoe(stack1, ITEM_RARITY.Legendary) && Manager_LegendaryUpgrades.Instance.IsUpgradeHellHoe(stack2))
		{
			result = CreateHellHoe(ITEM_RARITY.Void);
			Metods.CloneEnchantments(stack1, result);
			e.setResult(result);
			return;
		}
		
		if(IsTier_Pickaxe(stack1, ITEM_RARITY.Legendary) && Manager_LegendaryUpgrades.Instance.IsUpgradeHellPickaxe(stack2))
		{
			result = CreateHellPickaxe(ITEM_RARITY.Void);
			Metods.CloneEnchantments(stack1, result);
			e.setResult(result);
			return;
		}
		
		if(IsTier_Shield(stack1, ITEM_RARITY.Legendary) && Manager_LegendaryUpgrades.Instance.IsUpgradeHellShield(stack2))
		{
			result = CreateHellReflectShield(ITEM_RARITY.Void);
			Metods.CloneEnchantments(stack1, result);
			e.setResult(result);
			return;
		}
		
		if(IsTier_Sword(stack1, ITEM_RARITY.Legendary) && Manager_LegendaryUpgrades.Instance.IsUpgradeHellSword(stack2))
		{
			result = CreateHellTripleSword(ITEM_RARITY.Void);
			Metods.CloneEnchantments(stack1, result);
			e.setResult(result);
			return;
		}
		
		

	}
	
	private void GiveVoidTool(Player player, ItemStack baseStack,ItemStack stack)
	{
		Metods.CloneEnchantments(baseStack, stack);
		Metods._ins.InventoryAddItemOrDrop(stack, player );
	}
	
	@EventHandler
	public void AnvilClick(InventoryClickEvent e)
	{
		
		if(e.isCancelled()) return;
		
		if(!(e.getInventory() instanceof AnvilInventory)) return;
		
		if(e.getSlotType() != SlotType.RESULT) return;
		
		AnvilInventory inv = (AnvilInventory)e.getInventory();
				
		if(inv.getItem(0) == null || inv.getItem(1) == null) return;
		
		boolean found = false;
		
		if(IsTier_Axe(e.getCurrentItem(),ITEM_RARITY.Void)) 		found = true;
		if(IsTier_Hoe(e.getCurrentItem(),ITEM_RARITY.Void)) 		found = true;
		if(IsTier_Pickaxe(e.getCurrentItem(),ITEM_RARITY.Void)) 	found = true;
		if(IsTier_Shield(e.getCurrentItem(),ITEM_RARITY.Void)) 		found = true;
		if(IsTier_Sword(e.getCurrentItem(),ITEM_RARITY.Void)) 		found = true;


		if(!found) return;

		GiveVoidTool((Player)e.getWhoClicked(), inv.getItem(0), e.getCurrentItem());

		inv.setItem(0, new ItemStack(Material.AIR));
		inv.setItem(1, new ItemStack(Material.AIR));
	}
	
	
}
