package imu.iAPI.Handelers;

import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.Interfaces.*;
import imu.iAPI.Utilities.InvUtil;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;


public class ButtonHandler implements Listener, IButtonHandler, ISnapshotHandler, ITouchHandler
{
    private Plugin _plugin;
    private Map<Integer, IBUTTONN> _buttons;

    private List<IGrid> _grids;

    private ICustomInventory _customInventory;

    private INVENTORY_AREA _inventoryLock = INVENTORY_AREA.NONE;

    private long _lastClickTime = 0;
    private final long COOLDOWN_IN_MILLIS = 100; // milliseconds

    private boolean _blockDrag = false;
    private boolean _blockDrop = false;

    public ButtonHandler(Plugin plugin, ICustomInventory customInventory)
    {
        _buttons = new HashMap<>();
        _grids = new ArrayList<>();
        _customInventory = customInventory;
        _plugin = plugin;

    }

    public void setInventoryLock(INVENTORY_AREA lock)
    {
        _inventoryLock = lock;
    }

    public void clearButtons()
    {
        if (_buttons == null) return;

        List<Integer> positionsToRemove = new ArrayList<>();
        for (Map.Entry<Integer, IBUTTONN> entry : _buttons.entrySet())
        {
            positionsToRemove.add(entry.getKey());
        }

        for (Integer position : positionsToRemove)
        {
            removeButton(position);
        }
    }

    public void addButton(IBUTTONN button)
    {
        addButton(button.getPosition(), button);
    }

    public void addButton(int position, IBUTTONN button)
    {
        button.setButtonHandler(this);
        _buttons.put(position, button);
    }

    public IBUTTONN removeButton(int position)
    {
        removeTouch(position);

        if (_buttons.get(position) != null && _buttons.get(position).isStatic())
        {
            return null;
        }

        return _buttons.remove(position);
    }

    public IBUTTONN removeButton(IBUTTONN button)
    {
        return removeButton(button.getPosition());
    }

    public IBUTTONN getButton(int position)
    {
        if (!_buttons.containsKey(position)) return null;

        return _buttons.get(position);
    }

    public IBUTTONN getButton(UUID uuid)
    {
        for (IBUTTONN button : _buttons.values())
        {
            if (button.getUUID().equals(uuid))
            {
                return button;
            }
        }
        return null;
    }

    public void onHandlerOpen()
    {
        _plugin.getServer().getPluginManager().registerEvents(this, _plugin);
    }

    public void onHandlerClose()
    {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event)
    {
        if (!event.getInventory().equals(_customInventory.getInventory()))
        {
            return;
        }

        dropTouches();
        _customInventory.onClose();
        onHandlerClose();
    }


    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event)
    {
        if (!event.getInventory().equals(_customInventory.getInventory()))
        {
            return;
        }

        IBUTTONN button = _buttons.get(event.getRawSlot());
        if (button != null)
        {
            button.setLastClickType(event.getClick());
        }

        INVENTORY_AREA area = getInventoryArea(event);
        if (_inventoryLock == INVENTORY_AREA.UPPER_LOWER_INV
                || area == _inventoryLock)
        {
            event.setCancelled(true);
        }

        if (area == INVENTORY_AREA.LOWER_INV)
        {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
            {
                event.setCancelled(true);
                return;
            }
        }

        //System.out.println("event action: " + event.getAction() + " AREA: " + area);
        if (area == INVENTORY_AREA.UPPER_INV)
        {
            switch (event.getAction())
            {
                case PLACE_ALL:
                case PLACE_ONE:
                case PLACE_SOME:
                case SWAP_WITH_CURSOR:
                {
                    event.setCancelled(true);
                    if (button != null || _blockDrop)
                    {
                        return;
                    }

                    final ItemStack droppedItem = event.getCursor();

                    boolean cancel = _customInventory.onDropItem(droppedItem, event.getSlot());

                    if (!cancel)
                    {
                        return;
                    }

                    final ItemStack newItem = event.getCursor().clone();
                    //event.getCursor().setAmount(0);

                    //next frame
                    _plugin.getServer().getScheduler().runTask(_plugin, () ->
                    {
                        _customInventory.getInventory().setItem(event.getSlot(), newItem);
                        IBUTTONN newButton = _customInventory.onDropItemSet(
                                newItem, event.getSlot());


                        if (newButton != null && newItem.getAmount() > newButton.getMaxStackAmount())
                        {
                            newButton.getItemStack().setAmount(newButton.getMaxStackAmount());
                            droppedItem.setAmount(droppedItem.getAmount() - newButton.getMaxStackAmount());
                        }
                        else
                        {
                            droppedItem.setAmount(0);
                        }


                        if (newButton != null) updateButton(newButton);

                    });


                    return;
                }
                case MOVE_TO_OTHER_INVENTORY:
                case PICKUP_ALL:
                {
                    handlePickupAll(event, button);
                    break;
                }
                case PICKUP_HALF:
                {
                    ItemStack itemInSlot = _customInventory.getInventory().getItem(event.getSlot());
                    if (itemInSlot != null)  //&& itemInSlot.getType().getMaxStackSize() == 1
                    {
                        handlePickupAll(event, button);
                    }
                    else
                    {
                        //System.out.println("this isnt supported yet pickup type:  "+event.getAction());
                        event.setCancelled(true);
                        return;
                    }
                    break;
                }
                case DROP_ONE_SLOT:
                case DROP_ALL_SLOT:
                case HOTBAR_SWAP:
                case COLLECT_TO_CURSOR:
                case CLONE_STACK:
                case HOTBAR_MOVE_AND_READD:
                {
                    event.setCancelled(true);
                    return;
                }

                default:
                    break;
            }
        }

        if (button == null) return;

        //System.out.println("==> There is button: "+event.getRawSlot());

        if (button.isPositionLocked())
        {
            //keeps buttons same place
            event.setCancelled(true);
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - _lastClickTime < COOLDOWN_IN_MILLIS)
        {
            event.setCancelled(true);
            return;
        }

        _lastClickTime = currentTime;



        if(!button.onClick(event))
        {
        	return;
        }

        button.action(event);

    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event)
    {
        if (!event.getInventory().equals(_customInventory.getInventory()))
        {
            return;
        }

        if (_blockDrag)
        {
            event.setCancelled(true);
            return;
        }

        for (Integer slot : event.getRawSlots())
        {
            if (slot < _customInventory.getSize())
            {
                if (getButton(slot) != null)
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        for (Integer slot : event.getRawSlots())
        {
            if (slot < _customInventory.getSize())
            {
                event.setCancelled(true);

                final ItemStack draggedItem = event.getOldCursor();
                boolean cancel = _customInventory.onDragItem(draggedItem, slot);

                if (!cancel)
                {
                    return;
                }
                event.setCancelled(false);
                final ItemStack newItem = event.getOldCursor().clone();
                event.getOldCursor().setAmount(0);
                final ItemStack currentDrag = event.getNewItems().get(slot);

                //next frame
                _plugin.getServer().getScheduler().runTask(_plugin, () ->
                {
                    _customInventory.getInventory().setItem(slot, newItem);
                    IBUTTONN newButton = _customInventory.onDragItemSet(newItem, slot);

                    if (newItem.getAmount() > newButton.getMaxStackAmount())
                    {
                        newButton.getItemStack().setAmount(newButton.getMaxStackAmount());
                        //UpdateButton(newButton);

                        int amount = currentDrag.getAmount() - newButton.getMaxStackAmount();
                        draggedItem.setAmount(amount);
                        InvUtil.AddItemToInventoryOrDrop(_customInventory.getPlayer(), draggedItem);
                    }

                    if (newButton != null) updateButton(newButton);

                });
            }
        }

    }

    public void handlePickupAll(InventoryClickEvent event, IBUTTONN button)
    {
        int slot = event.getSlot();
        boolean onPickUpAll = _customInventory.onPickupAll(button, slot);

        if (!onPickUpAll)
        {
            event.setCancelled(true);
            return;
        }

        IBUTTONN b = getButton(slot);

        if (b != null && b.getItemStack().getType() == Material.AIR)
        {
            removeButton(slot);
            return;
        }

        if (b == null) return;

        _plugin.getServer().getScheduler().runTask(_plugin, () ->
        {
            //System.out.println("Delay set item: "+slot + " ITEM: "+b.GetItemStack());
            _customInventory.getInventory().setItem(slot, b.getItemStack());
            _customInventory.getPlayer().updateInventory();
        });
    }


    public INVENTORY_AREA getInventoryArea(InventoryClickEvent e)
    {
        if (e.getRawSlot() != e.getSlot())
        {
            return INVENTORY_AREA.LOWER_INV;
        }

        if (e.getRawSlot() == e.getSlot())
        {
            return INVENTORY_AREA.UPPER_INV;
        }

        return INVENTORY_AREA.NONE;
    }

    @Override
    public void addGrid(IGrid grid)
    {
        grid.registerButtonHandler(this);
        grid.loadButtons();
        _grids.add(grid);
    }

    @Override
    public void removeGrid(IGrid grid)
    {
        grid.unregisterButtonHandler();
        _grids.remove(grid);
    }

    @Override
    public void clearGrids()
    {
        for (IGrid grid : _grids)
        {
            grid.unregisterButtonHandler();
        }
    }

    public void updateButtons(boolean clearEmpties)
    {

        if (clearEmpties)
        {
            Inventory inv = _customInventory.getInventory();
            for (int i = 0; i < _customInventory.getSize(); i++)
            {
                if (getButton(i) != null) continue;

                inv.setItem(i, new ItemStack(Material.AIR));
            }
        }

        for (IBUTTONN button : _buttons.values())
        {
            updateButton(button.getPosition());
        }
    }

    public void updateButton(IBUTTONN button)
    {
        button.onUpdate();
        _customInventory.
                getInventory().
                setItem(button.getPosition(), button.getItemStack());

    }

    public void updateButton(int position)
    {
        IBUTTONN button = getButton(position);
        if (button == null)
        {
            _customInventory.
                    getInventory().
                    setItem(position, new ItemStack(Material.AIR));
            return;
        }
        updateButton(button);
    }


    //TOUCH SYSTEM
    private Set<Integer> _touchedButtons = new HashSet<>();

    public void addTouch(int position)
    {
        _touchedButtons.add(position);
    }

    public void addTouch(IBUTTONN button)
    {
        if (button == null) return;
        addTouch(button.getPosition());
    }

    public void removeTouch(int position)
    {
        _touchedButtons.remove(position);
    }

    public void removeTouch(IBUTTONN button)
    {
        if (button == null) return;
        removeTouch(button.getPosition());
    }

    public boolean isTouched(int position)
    {
        return _touchedButtons.contains(position);
    }

    public boolean isTouched(IBUTTONN button)
    {
        if (button == null) return false;
        return isTouched(button.getPosition());
    }

    public void clearTouches()
    {
        _touchedButtons.clear();
    }

    public void dropTouches()
    {
        for (Integer position : _touchedButtons)
        {
            IBUTTONN button = getButton(position);
            if (button == null) continue;

            InvUtil.AddItemToInventoryOrDrop(_customInventory.getPlayer(), button.getItemStack());
        }
    }


    //SNAPSHOT SYSTEM, not tested
    private Map<String, Map<Integer, IBUTTONN>> _snapshots = new HashMap<>();

    public void takeSnapshot(String snapshotName)
    {
        Map<Integer, IBUTTONN> currentButtons = new HashMap<>(_buttons);
        _snapshots.put(snapshotName, currentButtons);
    }

    public void restoreSnapshot(String snapshotName)
    {
        Map<Integer, IBUTTONN> snapshot = _snapshots.get(snapshotName);
        if (snapshot != null)
        {
            _buttons.clear();
            _buttons.putAll(snapshot);
            updateButtons(true);
        }
    }

    public IBUTTONN getButtonFromSnapshot(String snapshotName, int slotId)
    {
        Map<Integer, IBUTTONN> snapshot = _snapshots.get(snapshotName);
        if (snapshot != null)
        {
            return snapshot.get(slotId);
        }
        return null;
    }

    public Set<String> listSnapshots()
    {
        return _snapshots.keySet();
    }

    public boolean hasSnapshot(String snapshotName)
    {
        return _snapshots.containsKey(snapshotName);
    }

    public void removeSnapshot(String snapshotName)
    {
        _snapshots.remove(snapshotName);
    }
}

