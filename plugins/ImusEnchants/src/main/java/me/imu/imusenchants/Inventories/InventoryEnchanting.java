package me.imu.imusenchants.Inventories;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.Interfaces.IBUTTONN;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ItemUtils;
import imu.iAPI.Utilities.ItemUtils.DisplayNamePosition;
import me.imu.imusenchants.CONSTANTS;
import me.imu.imusenchants.Enchants.EnchantedItem;
import me.imu.imusenchants.Enchants.INode;
import me.imu.imusenchants.Enchants.Node;
import me.imu.imusenchants.Enchants.NodeSwapper;
import me.imu.imusenchants.Enums.TOUCH_TYPE;
import me.imu.imusenchants.ImusEnchants;
import me.imu.imusenchants.Managers.ManagerEnchants;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class InventoryEnchanting extends CustomInventory
{

    private final int _enchantSlot = getSize() - 5;
    private EnchantedItem _enchantedItem;

    private final ItemStack EMPTY_BLACK_SLOT;

    //private BukkitRunnable monitoringTask;

    private final String PD_LOCKED = "locked";
    public static final String PD_SWAPPER = "swapper";

    private long _timeID = 0;

    public InventoryEnchanting()
    {
        super(ImusEnchants.Instance, "&0===== &5Enchanting Table &0=====", 6 * 9);

        EMPTY_BLACK_SLOT = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemUtils.SetDisplayNameEmpty(EMPTY_BLACK_SLOT);
    }

    @Override
    public INVENTORY_AREA setInventoryLock()
    {
        return INVENTORY_AREA.NONE;
    }

    @Override
    public void onAwake()
    {

    }

    @Override
    public void onOpen()
    {
        super.onOpen();
        // startMonitoring();
        initButtons();

    }

    @Override
    public void onClose()
    {
        // stopMonitoring();
        super.onClose();
    }

    private ItemStack getEnchantItem()
    {
        return getInventory().getItem(_enchantSlot);
    }

    private void clearTable()
    {
        for (int i = 0; i < getSize(); i++)
        {
            if (ManagerEnchants.REDSTRICTED_SLOTS.contains(i))
                continue;

            Button button = new Button(i, EMPTY_BLACK_SLOT);
            addButton(button);
        }
    }

    private void initButtons()
    {
        ItemStack stack;
        ItemStack emptyPurple = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);

        ItemUtils.SetDisplayNameEmpty(emptyPurple);

        clearTable();

        Button button;
        // Empties
        button = new Button(getSize() - 3, emptyPurple);
        addButton(button);

        button = new Button(getSize() - 7, emptyPurple);
        addButton(button);

        button = new Button(getSize() - 13, emptyPurple);
        addButton(button);

        if (getPlayer().isOp())
        {
            stack = new ItemStack(Material.NETHER_STAR);
            ItemUtils.SetDisplayName(stack, "&eREROLL Slots");
            ItemUtils.AddLore(stack, "&6Visible OPs only", true);
            button = new Button(getSize() - 14, stack, inventoryClickEvent ->
            {
                buttonOPreroll((Button) getButton(getSize() - 14), inventoryClickEvent);
            });
            addButton(button);
        }
        else
        {
            button = new Button(getSize() - 14, emptyPurple);
            addButton(button);
        }

        button = new Button(getSize() - 15, emptyPurple);
        addButton(button);

        stack = new ItemStack(Material.BOOKSHELF);
        ItemUtils.SetDisplayName(stack, "&eBuy Enchants");
        button = new Button(getSize() - 6, stack, inventoryClickEvent ->
        {
            buttonOpenEnchantbuy((Button) getButton(getSize() - 6), inventoryClickEvent);
        });
        addButton(button);

        stack = new ItemStack(Material.ENCHANTING_TABLE);
        ItemUtils.SetDisplayName(stack, "&eEnchant");
        button = new Button(getSize() - 4, stack, inventoryClickEvent ->
        {
            buttonEnchantItem((Button) getButton(getSize() - 4), inventoryClickEvent);
        });
        addButton(button);

        // slot for enchant item
        removeButton(_enchantSlot);

        updateButtons(true);
    }

    @Override
    public boolean onDragItem(ItemStack item, int slot)
    {
        return onDropItem(item, slot);
    }

    @Override
    public IBUTTONN onDragItemSet(ItemStack stack, int slot)
    {
        return onDropItemSet(stack, slot);
    }

    @SuppressWarnings("unused")
    @Override
    public boolean onDropItem(ItemStack stack, int slot)
    {
        if (slot == _enchantSlot)
        {
            if (!ManagerEnchants.IsValidToEnchant(stack))
            {
                getPlayer().sendMessage(Metods.msgC("&cNot valid item to enchant!"));
                return false;
            }

            return true;
        }

        if (getButton(slot) == null)
        {
            boolean dROP = ManagerEnchants.Instance.IsValidGUIitem(TOUCH_TYPE.DROP, _enchantedItem, stack);

            return dROP;
        }

        return false;
    }

    @Override
    public IBUTTONN onDropItemSet(ItemStack stack, int slot)
    {
        //System.out.println("DropItemSet: " + stack.getType());
        if (slot == _enchantSlot)
        {
            Button button = new Button(slot, stack);
            button.setLockPosition(false);
            addButton(button);
            addTouch(slot);
            loadItem(button, false);
            return button;
        }

        if (getButton(slot) == null)
        {

            addTouch(slot);
            IBUTTONN button = loadNode(stack, slot, true);

            return button;
        }

        return null;
    }

    private void loadItem(IBUTTONN button, boolean forceReveal)
    {
        ItemStack stack = button.getItemStack();

        _enchantedItem = new EnchantedItem(stack, getPlayer());

        _enchantedItem.Reveal(forceReveal);
        loadNodes(_enchantedItem);

    }

    private IBUTTONN loadNode(INode node, boolean insertNode)
    {
        return loadNode(node.GetGUIitemLoad(_enchantedItem), node.GetFlatIndex(), insertNode);
    }

    private IBUTTONN loadNode(ItemStack stack, int slot, boolean insertNode)
    {
        IBUTTONN button = null;

        if (button == null)
        {
            INode nodee = ManagerEnchants.Instance.GetNode(stack, _enchantedItem);

            if (nodee != null)
                button = loadGUIButton(nodee, stack, slot, insertNode);
        }

        return button;
    }

    private void loadNodes(EnchantedItem enchantedItem)
    {
        for (int i = 0; i < CONSTANTS.ENCHANT_ROWS; i++)
        {
            for (int j = 0; j < CONSTANTS.ENCHANT_COLUMNS; j++)
            {
                INode node = enchantedItem.Get_nodes()[i][j];
                int flatIndex = i * CONSTANTS.ENCHANT_COLUMNS + j;

                // black glass
                if (node.IsLocked())
                    continue;

                if (loadNode(node, false) == null)
                {
                    //Bukkit.getLogger().info("Couldn't load Node: " + node);
                    removeButton(flatIndex);
                    continue;
                }


                if (node.IsFrozen())
                {
                    lockingButto(getButton(flatIndex));
                }

            }
        }

        updateButtons(true);

//		for (int i = 0; i < CONSTANTS.ENCHANT_ROWS; i++) 
//		{
//		    for (int j = 0; j < CONSTANTS.ENCHANT_COLUMNS; j++) 
//		    {
//		    	int flatIndex = i * CONSTANTS.ENCHANT_COLUMNS + j;
//		    	
//		    	
//		    	
//		    	ItemStack stack = GetInventory().getItem(flatIndex);
//		    	if(stack == null) continue;
//		    	
//		    	ItemUtils.SetDisplayName(stack, "x: "+i + " y: "+j);
//		    	GetInventory().setItem(flatIndex, stack);
//		    	
//		    }
//		}
//		

    }

    private void handdleDropTouches()
    {
        if (_enchantedItem == null) return;

        for (INode node : _enchantedItem.getUnlockedNodes())
        {
            if (node.IsFrozen()) continue;

            if (!isTouched(node.GetFlatIndex())) continue;

//			Bukkit.getLogger().info("=====> DROP NODE"+node + " is it touched: "+IsTouched(node.GetFlatIndex()));	
            IBUTTONN button = getButton(node.GetFlatIndex());

            if (button == null) continue;

            ItemStack stack = node.GetGUIitemUnLoad(_enchantedItem, button.getItemStack());

            button.setItemStack(stack);
            updateButton(button);

        }
        dropTouches();
    }

    @SuppressWarnings("unused")
    @Override
    public boolean onPickupAll(IBUTTONN button, int slot)
    {

        if (slot == _enchantSlot)
        {
            EnchantedItem item = _enchantedItem;

            removeTouch(_enchantSlot);
            handdleDropTouches();
            _enchantedItem = null;

            for (INode node : item.getUnlockedNodes())
            {
                int nodeSlot = node.GetFlatIndex();

                Button b = new Button(nodeSlot, EMPTY_BLACK_SLOT);
                addButton(b);
                updateButton(nodeSlot);
            }

            button.setItemStack(new ItemStack(Material.AIR));

            RefreshTimeID();
            return true;
        }

        removeTouch(button);

        if (button != null)
        {
            if (_enchantedItem == null)
                return false;

            if (button.getLastClickType() == ClickType.RIGHT
                    && ManagerEnchants.Instance.IsValidGUIitem(TOUCH_TYPE.PICK_UP, _enchantedItem, button))
            {
                if (ItemUtils.HasTag(button.getItemStack(), PD_LOCKED))
                {
                    removeNode(button);
                    return false;
                }

                if (ItemUtils.HasTag(button.getItemStack(), PD_SWAPPER))
                {
                    activateSwapper(slot);
                    return false;
                }

            }

            if (!ManagerEnchants.Instance.IsValidGUIitem(TOUCH_TYPE.PICK_UP, _enchantedItem,
                    getInventory().getItem(slot)))
            {
                System.out.println("not valid material: " + getInventory().getItem(slot).getType());
                return false;
            }

            if (ItemUtils.HasTag(button.getItemStack(), PD_LOCKED))
            {
                return false;
            }

            INode currentNode = _enchantedItem.GetNodeBySlot(slot);
            ItemStack stack = currentNode.GetGUIitemUnLoad(_enchantedItem, getInventory().getItem(slot));
            getInventory().setItem(slot, stack);


            INode node = new Node();
            node.SetLock(false);
            _enchantedItem.SetNode(node, slot);

            button.setItemStack(new ItemStack(Material.AIR)); // removes button
            return true;
        }

        return false;
    }

    private void removeNode(IBUTTONN button)
    {

        _enchantedItem.removeNode(button.getPosition());

        updateEnchantedItem(true);

        removeButton(button);
        updateButton(button.getPosition());
    }

    private void lockingButto(IBUTTONN button)
    {
        if (button == null)
            return;

        ItemStack stack = button.getItemStack();
        ItemUtils.SetTag(stack, PD_LOCKED);
        // ItemUtils.AddLore(stack, "&c==== LOCKED ==== ", false);

        ItemUtils.AddTextToDisplayName(stack, " &e(&cLOCKED&e)", DisplayNamePosition.BACK);
        ItemUtils.AddLore(stack, "&3▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
        ItemUtils.AddLore(stack, "&cRemove by &eM2", true);

        button.setLockPosition(true);
    }

    private IBUTTONN loadGUIButton(INode node, ItemStack stack, int slot, boolean insertNode)
    {
        ItemStack loadedStack = node.GetGUIitemLoad(_enchantedItem);

        if (!loadedStack.getType().isAir())
        {
            stack = loadedStack;
        }

        if (insertNode) _enchantedItem.SetNode(node, slot);

        Button button = new Button(slot, stack);
        button.setLockPosition(false);
        addButton(button);
        return button;
    }

//	private IBUTTONN LoadSwapper(ItemStack stack, int slot)
//	{
//		NodeEnchant nodeEnchant = new NodeEnchant(stack);	
//		_enchantedItem.SetNode(nodeEnchant, slot);
//
//		Button button = new Button(slot, stack);
//		button.SetLockPosition(false);
//		AddButton(button);		
//		return button;
//	}
//	
//	private IBUTTONN LoadEnchant(ItemStack stack, int slot)
//	{
//		NodeEnchant nodeEnchant = new NodeEnchant(stack);
//		_enchantedItem.SetNode(nodeEnchant, slot);
//
//		Button button = new Button(slot, stack);
//		button.SetLockPosition(false);
//		AddButton(button);
//		return button;
//	}

//	private IBUTTONN LoadBooster(ItemStack stack, int slot)
//	{
//		NodeBooster nodeBooster = new NodeBooster();
//		_enchantedItem.SetNode(nodeBooster, slot);
//
//		Button button = new Button(slot, stack);
//		button.SetLockPosition(false);
//		AddButton(button);
//		return button;
//	}

    private void activateSwapper(int slot)
    {
        final long id = _timeID;
        NodeSwapper node = (NodeSwapper) _enchantedItem.GetNodeBySlot(slot);
        node.Activate(_enchantedItem);

        INode swapped = node.GetSwappedNode();
        List<INode> detachedNodes = _enchantedItem.DetachNodes(
                nodee -> (!nodee.IsFrozen() && nodee.getClass() != Node.class), null);

        updateEnchantedItem(false);
        clearTable();

        updateButtons(false);
        _enchantedItem.ReattachNodes(detachedNodes);
        loadNodes(_enchantedItem);

        if (swapped != null && CONSTANTS.SWAPPER_ANIMATION)
        {
            ItemStack redMark = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemUtils.SetDisplayNameEmpty(redMark);
            final int swappedSlot = Node.GetFlatIndex(swapped.GetX(), swapped.GetY());

            IBUTTONN swappedButton = getButton(swappedSlot);
            ItemStack originalItem = null;
            if (swappedButton == null)
            {
                IBUTTONN b = new Button(swappedSlot, redMark);
                originalItem = new ItemStack(Material.AIR);
                addButton(b);
                updateButton(b);
            }
            else
            {
                originalItem = swappedButton.getItemStack();
                swappedButton.setItemStack(redMark);
                updateButton(swappedButton);
            }

            final ItemStack org = originalItem;
            Bukkit.getScheduler().scheduleSyncDelayedTask(ImusEnchants.Instance, () ->
            {
                if (id != _timeID)
                {
                    Bukkit.getLogger().info("NOT SAME INV");
                    return;
                }

                IBUTTONN swapButton = getButton(swappedSlot);
                swapButton.setItemStack(org);

                updateButton(swapButton);
                if (org.getType().isAir())
                {
                    removeButton(swapButton);
                }
            }, CONSTANTS.DELAY_SWAPPER_ANIMATION);
        }
    }

    private void buttonOPreroll(Button button, InventoryClickEvent event)
    {
        if (_enchantedItem == null)
            return;

        IBUTTONN enchantItemButton = getButton(_enchantSlot);
        removeButton(_enchantSlot);
        Button newButton = new Button(_enchantSlot, enchantItemButton.getItemStack());
        newButton.setLockPosition(false);
        addButton(newButton);

        clearTable();
        clearTouches();
        addTouch(_enchantSlot);
        loadItem(newButton, true);

    }

    private void buttonOpenEnchantbuy(Button button, InventoryClickEvent event)
    {
        // System.out.println("Open new inv");
        handdleDropTouches();
        clearTouches();
        openPage(new InventoryBuyEnchants());
    }

    private void updateEnchantedItem(boolean applyEnchants)
    {
        if (getEnchantItem() == null || getEnchantItem().getType().isAir())
            return;

        IBUTTONN b = getButton(_enchantSlot);

        if (applyEnchants)
        {
            _enchantedItem.SaveUnlockedNodes();
            _enchantedItem.ApplyEnchantsToItem();
            RefreshTimeID();
        }

        _enchantedItem.SaveUnlockedNodes();
        _enchantedItem.SetTooltip();

        b.setItemStack(_enchantedItem.GetItemStack());
        updateButton(b);
    }

    private void buttonEnchantItem(Button button, InventoryClickEvent event)
    {
        if (getEnchantItem() == null || getEnchantItem().getType().isAir())
            return;

        updateEnchantedItem(true);

        System.out.println("     ");


        clearTable();
        clearTouches();

        addTouch(_enchantSlot);
        updateButtons(false);

        RefreshTimeID();
    }

    private void RefreshTimeID()
    {
        _timeID = System.currentTimeMillis();
    }

}
