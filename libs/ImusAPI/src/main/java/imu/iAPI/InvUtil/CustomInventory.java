package imu.iAPI.InvUtil;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.Handelers.ButtonHandler;
import imu.iAPI.Interfaces.IBUTTONN;
import imu.iAPI.Interfaces.ICustomInventory;
import imu.iAPI.Interfaces.IGrid;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.Stack;
import java.util.UUID;

public abstract class CustomInventory implements ICustomInventory
{
    private Plugin _plugin;
    private Inventory _inv;
    private Player _player;
    private String _name;
    private int _size;

    private ButtonHandler _buttonHandler;
    private boolean _awaken = false;

    public CustomInventory(Plugin main, String name, int size)
    {
        _plugin = main;
        _size = adjustSize(size);

        _name = Metods.msgC(name);
        _buttonHandler = new ButtonHandler(_plugin, this);
        _inv = _plugin.getServer().createInventory(null, _size, _name);
    }


    @Override
    public void onOpen()
    {
        _buttonHandler.setInventoryLock(setInventoryLock());
        _buttonHandler.onHandlerOpen();
        ImusAPI._instance.RegisterCustomInventory(this);
        if (!_awaken)
        {
            onAwake();
            _awaken = true;
        }
    }

    @Override
    public void onClose()
    {
        ImusAPI._instance.UnregisterCustomInventory(this);

    }

    public abstract INVENTORY_AREA setInventoryLock();

    @Override
    public ButtonHandler getButtonHandler()
    {
        return _buttonHandler;
    }

    @Override
    public Inventory getInventory()
    {
        return _inv;
    }

    public IBUTTONN getEmptyButton(int slot, Material material)
    {
        return new Button(slot, ItemUtils.SetDisplayNameEmpty(new ItemStack(material)));
    }

    @Override
    public int getSize()
    {
        return _size;
    }

    @Override
    public Player getPlayer()
    {
        return _player;
    }

    @Override
    public Plugin getPlugin()
    {
        return _plugin;
    }

    private int adjustSize(int size)
    {
        if (size <= 0)
        {
            size = 1;
        }

        int result = (size + 8) / 9 * 9;
        if (result > 54)
        {
            return 54;
        }
        return result;
    }

    @Override
    public void open(Player player)
    {
        _player = player;
        player.openInventory(_inv);
        onOpen();
    }

    @Override
    public boolean onDropItem(ItemStack stack, int slot)
    {
        if (getButton(slot) != null) return false;

        Button button = new Button(slot, stack);
        addButton(button);

        return true;
    }

    @Override
    public IBUTTONN onDropItemSet(ItemStack stack, int slot)
    {
        return null;
    }

    @Override
    public boolean onDragItem(ItemStack stack, int slot)
    {
        if (getButton(slot) != null) return false;

        Button button = new Button(slot, stack);
        addButton(button);

        return true;
    }

    @Override
    public IBUTTONN onDragItemSet(ItemStack stack, int slot)
    {
        return null;
    }

    @Override
    public boolean onPickupAll(IBUTTONN button, int slot)
    {
        return false;
    }

    protected IBUTTONN SetButton(ItemStack stack, int slot)
    {
        IBUTTONN button = new Button(slot, stack.clone());
        _buttonHandler.addButton(button);
        updateButton(slot);
        return button;
    }

    //>>>> Page System
    private Stack<ICustomInventory> _pageStack = new Stack<>();

    @Override
    public void setPageStack(Stack<ICustomInventory> pageStack)
    {
        _pageStack = pageStack;
    }

    public Stack<ICustomInventory> getPageStack()
    {
        return _pageStack;
    }

    public void openPage(ICustomInventory newPage)
    {
        if (this != newPage)
        {
            _pageStack.push(this);
            closeCurrentAndOpenNewPage(newPage);
        }
    }

    public void back()
    {
        if (!_pageStack.isEmpty())
        {
            ICustomInventory previousPage = _pageStack.pop();
            closeCurrentAndOpenNewPage(previousPage);
        }
    }

    private void closeCurrentAndOpenNewPage(ICustomInventory newPage)
    {
        newPage.setPageStack(_pageStack);
        newPage.open(getPlayer());
        clearTouches();
    }

    protected void addDefaultBackButton(int slot)
    {
        ItemStack stack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemUtils.SetDisplayName(stack, "&bGo Back");
        Button button = new Button(getSize() - 9, stack, inventoryClickEvent ->
        {
            back();
        });
        addButton(button);
    }
    //<<<< Page System

    //BUTTON HANDLING
    protected void addButton(IBUTTONN button)
    {
        _buttonHandler.addButton(button);
    }

    protected IBUTTONN getButton(int position)
    {
        return _buttonHandler.getButton(position);
    }

    protected IBUTTONN getButton(UUID uuid)
    {
        return _buttonHandler.getButton(uuid);
    }

    protected IBUTTONN removeButton(IBUTTONN button)
    {
        return _buttonHandler.removeButton(button.getPosition());
    }

    protected IBUTTONN removeButton(int position)
    {
        return _buttonHandler.removeButton(position);
    }

    protected void clearButtons()
    {
        _buttonHandler.clearButtons();
    }

    protected void updateButtons(boolean clearEmpties)
    {
        _buttonHandler.updateButtons(clearEmpties);
    }

    protected void updateButton(int position)
    {
        _buttonHandler.updateButton(position);
    }

    protected void updateButton(IBUTTONN button)
    {
        _buttonHandler.updateButton(button.getPosition());
    }

    //>>>> TOUCH HANDLING
    protected void addTouch(int position)
    {
        _buttonHandler.addTouch(position);
    }

    protected void addTouch(IBUTTONN button)
    {
        _buttonHandler.addTouch(button);
    }

    protected void removeTouch(int position)
    {
        _buttonHandler.removeTouch(position);
    }

    protected void removeTouch(IBUTTONN button)
    {
        _buttonHandler.removeTouch(button);
    }

    protected boolean isTouched(int position)
    {
        return _buttonHandler.isTouched(position);
    }

    protected boolean isTouched(IBUTTONN button)
    {
        return _buttonHandler.isTouched(button);
    }

    protected void clearTouches()
    {
        _buttonHandler.clearTouches();
    }

    protected void dropTouches()
    {
        _buttonHandler.dropTouches();
    }
    //<<<< TOUCH SYSTEM

    //>>>> SnapShot system

    protected void takeSnapshot(String snapshotName)
    {
        _buttonHandler.takeSnapshot(snapshotName);
    }

    protected void restoreSnapshot(String snapshotName)
    {
        _buttonHandler.restoreSnapshot(snapshotName);
    }

    protected IBUTTONN getButtonFromSnapshot(String snapshotName, int slotId)
    {
        return _buttonHandler.getButtonFromSnapshot(snapshotName, slotId);
    }

    protected Set<String> ListSnapshots()
    {
        return _buttonHandler.listSnapshots();
    }

    protected boolean hasSnapshot(String snapshotName)
    {
        return _buttonHandler.hasSnapshot(snapshotName);
    }

    protected void removeSnapshot(String snapshotName)
    {
        _buttonHandler.removeSnapshot(snapshotName);
    }

    protected void addGrid(IGrid grid)
    {
    	_buttonHandler.addGrid(grid);
    }

    protected void removeGrid(IGrid grid)
    {
    	_buttonHandler.removeGrid(grid);
    }

    protected void clearGrids()
    {
    	_buttonHandler.clearGrids();
    }
    //<<<< SnapShot system
    //EXAMPLE
//	private void InitButtons()
//	{
//		Button button = new Button(0, new ItemStack(Material.SMOOTH_STONE), inventoryClickEvent -> 
//		{
//	        ExamplePress((Button)GetButton(0), inventoryClickEvent);
//	    });
//		
    //OR
//		Button button = new Button(0, new ItemStack(Material.SMOOTH_STONE), this::ExamplePress);
//		
//		AddButton(button);
//		
//		UpdateButtons(true);
//	}
//	
//	private void ExamplePress(Button button, InventoryClickEvent event)
//	{
//		button.GetItemStack();
//		Player player = (Player) event.getWhoClicked();
//		player.sendMessage("ExamplePress");

    //if want to add tooltip or something
//      UpdateButton(button);
//	}


}
