package imu.iAPI.Interfaces;

import java.util.Stack;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public interface ICustomInventory
{
	public void onAwake();
	public void onOpen();
	
	public void onClose();
	
	public Inventory getInventory();

	public IButtonHandler getButtonHandler();
	
	public int getSize();
	
	public Player getPlayer();
	
	public void open(Player player);
	
	public Plugin getPlugin();
	
	public boolean onDropItem(ItemStack stack, int slot);
	public IBUTTONN onDropItemSet(ItemStack stack, int slot);
	
	public boolean onDragItem(ItemStack stack, int slot);
	public IBUTTONN onDragItemSet(ItemStack stack, int slot);
	
	public boolean onPickupAll(IBUTTONN button, int slot);
	
	public void back();
	
	public void setPageStack(Stack<ICustomInventory> pageStack);
	public Stack<ICustomInventory> getPageStack();
}
