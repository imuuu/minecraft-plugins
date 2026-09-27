package imu.iAPI.Buttons;

import java.util.UUID;
import java.util.function.Consumer;

import imu.iAPI.Interfaces.IButtonHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Interfaces.IBUTTONN;

public class Button implements IBUTTONN
{
	private UUID _uuid = UUID.randomUUID();

	private IButtonHandler _buttonHandler;
	private ItemStack _stack;
	private int _position;
	private boolean _lockPosition = true;
	private int _maxStackAmount = 1;
	protected Consumer<InventoryClickEvent> _onClickAction;
	
	protected ClickType _lastClickType = ClickType.UNKNOWN;

	private boolean _isStatic = false;
	private boolean _enableAction = true;
	public Button(int position, ItemStack stack, Consumer<InventoryClickEvent> onClickAction)
	{
		_stack = stack;
		_position = position;
		_onClickAction = onClickAction;

	}

	public Button(IBUTTONN button)
	{
		_stack = button.getItemStack();
		_position = button.getPosition();
		_onClickAction = button.getAction();
		_uuid = button.getUUID();
	}
	
	public Button(int position, ItemStack stack)
	{
		_stack = stack;
		_position = position;
	}

	public void setUUID(UUID uuid)
	{
		_uuid = uuid;
	}

	@Override
	public void setMaxStackAmount(int amount)
	{
		_maxStackAmount = amount;
	}

	@Override
	public int getMaxStackAmount()
	{
		return _maxStackAmount;
	}
	
	public void setAction(Consumer<InventoryClickEvent> onClickAction)
	{
		_onClickAction = onClickAction;
	}

	public Consumer<InventoryClickEvent> getAction()
	{
		return _onClickAction;
	}
	
//	@Override
//	public void OnClick(Player whoClicked, ClickType clickType)
//	{
//		
//	}

	@Override
	public int getPosition()
	{
		return _position;
	}

	@Override
	public void setPosition(int position)
	{
		_position = position;
	}

	public void setLockPosition(boolean lockPostion)
	{
		_lockPosition = lockPostion;
	}

	@Override
	public void setStatic(boolean isStatic)
	{
		_isStatic = isStatic;
	}

	@Override
	public boolean isStatic()
	{
		return _isStatic;
	}


	@Override
	public void setEnableAction(boolean enable)
	{
		_enableAction = enable;
	}

	@Override
	public boolean isActionEnabled()
	{
		return _enableAction;
	}

	public boolean isPositionLocked()
	{
		return _lockPosition;
	}
	
	public void action(InventoryClickEvent event)
	{
		if(!_enableAction)
			return;

		if (_onClickAction != null) 
		{
			_onClickAction.accept(event);
        }
	}

	@Override
	public UUID getUUID()
	{
		return _uuid;
	}

	@Override
	public ItemStack getItemStack()
	{
		return _stack;
	}
	
	@Override
	public void setItemStack(ItemStack stack)
	{
		_stack = stack;
	}

	@Override
	public void onUpdate()
	{
		
		
	}

	@Override
	public void setLastClickType(ClickType clickType)
	{
		_lastClickType = clickType;
	}

	@Override
	public ClickType getLastClickType()
	{
		return _lastClickType;
	}

	@Override
	public boolean onClick(InventoryClickEvent event)
	{
		return true;
	}

	@Override
	public void setButtonHandler(IButtonHandler buttonHandler)
	{
		_buttonHandler = buttonHandler;
	}

	@Override
	public IButtonHandler getButtonHandler()
	{
		return _buttonHandler;
	}


}
