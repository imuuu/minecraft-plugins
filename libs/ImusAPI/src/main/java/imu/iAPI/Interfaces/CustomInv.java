package imu.iAPI.Interfaces;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public interface CustomInv 
{
	String pd_buttonType = "cButton";
	
	public ItemStack SetButton(ItemStack stack, IButton b);
	
	public String getButtonName(ItemStack stack);
	
	/**
	 * 
	 * @param b = BUTTON enum
	 * @param material = Material.
	 * @param displayName 
	 * @param itemSlot = if null doesnt set item to inv slot
	 * @return returns modified item
	 */
	public ItemStack setupButton(IButton b, Material material, String displayName, Integer itemSlot);
	
	void setupButtons();
	
	@EventHandler
	public void onClick(InventoryClickEvent e);
	
	/**
	 * Event when clicked inside this inv
	 * @param e = InventoryCloseEvent
	 */
	public void onClickInsideInv(InventoryClickEvent e);
	
	
	
	@EventHandler
	public void invClose(InventoryCloseEvent e);
	
	/**
	 * Event when this inv is closed
	 * @param e = InventoryCloseEvent
	 */
	public void invClosed(InventoryCloseEvent e);
	
}
