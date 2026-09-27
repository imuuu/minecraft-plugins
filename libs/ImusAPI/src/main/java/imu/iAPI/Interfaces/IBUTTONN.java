package imu.iAPI.Interfaces;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.function.Consumer;

public interface IBUTTONN
{
	public UUID getUUID();

	public ItemStack getItemStack();
	public void setItemStack(ItemStack stack);
	
	public void setMaxStackAmount(int amount);
	public int getMaxStackAmount();
	public int getPosition();
	public void setPosition(int position);
	public boolean isPositionLocked();
	public void setLockPosition(boolean lockPostion);

	public void setStatic(boolean isStatic);
	public boolean isStatic();

	public void setEnableAction(boolean enable);
	public boolean isActionEnabled();
	public void onUpdate();
	public void action(InventoryClickEvent event);

	public Consumer<InventoryClickEvent> getAction();
	
	public void setLastClickType(ClickType clickType);
	public ClickType getLastClickType();

	public boolean onClick(InventoryClickEvent event);

	public void setButtonHandler(IButtonHandler buttonHandler);
	public IButtonHandler getButtonHandler();
}
