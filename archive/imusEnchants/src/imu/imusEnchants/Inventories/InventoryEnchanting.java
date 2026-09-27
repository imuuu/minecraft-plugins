package imu.imusEnchants.Inventories;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Buttons.Button;
import imu.iAPI.Enums.INVENTORY_AREA;
import imu.iAPI.Interfaces.IBUTTONN;
import imu.iAPI.InvUtil.CustomInventory;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ItemUtils;
import imu.iAPI.Utilities.ItemUtils.DisplayNamePosition;
import imu.imusEnchants.Enchants.EnchantedItem;
import imu.imusEnchants.Enchants.INode;
import imu.imusEnchants.Enchants.Node;
import imu.imusEnchants.Enchants.NodeSwapper;
import imu.imusEnchants.Enums.TOUCH_TYPE;
import imu.imusEnchants.Managers.ManagerEnchants;
import imu.imusEnchants.main.CONSTANTS;
import imu.imusEnchants.main.ImusEnchants;

public class InventoryEnchanting extends CustomInventory
{
	//private ImusEnchants _main = ImusEnchants.Instance;

	private int _enchantSlot = GetSize() - 5;
	private EnchantedItem _enchantedItem;

	private final ItemStack EMPTY_BLACK_SLOT;

	//private BukkitRunnable monitoringTask;

	private final String PD_LOCKED = "locked";
	public static final String PD_SWAPPER = "swapper";

	private long _timeID = 0;
	
	public InventoryEnchanting()
	{
		super(ImusEnchants.Instance, "&4==== &bEnchanting Table &4====", 6 * 9);

		EMPTY_BLACK_SLOT = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
		ItemUtils.SetDisplayNameEmpty(EMPTY_BLACK_SLOT);
	}

//	private void startMonitoring()
//	{
//		monitoringTask = new BukkitRunnable() {
//			@Override
//			public void run()
//			{
//				if (_enchantedItem == null)
//				{
//					System.out.println("EnchantedItem is null");
//				} else
//				{
//					System.out.println("EnchantedItem is not null");
//					ItemStack stack = _enchantedItem.GetItemStack();
//					if (stack == null)
//					{
//						System.out.println("EnchantedItem's ItemStack is null");
//					} else
//					{
//						System.out.println("EnchantedItem's ItemStack is not null: " + stack.getType());
//					}
//				}
//
//				if (GetButton(_enchantSlot) != null)
//				{
//					System.out.println("Enchant button is " + GetButton(_enchantSlot).GetItemStack().getType());
//					return;
//				}
//
//				System.out.println("Enchant button is NULL");
//			}
//		};
//		monitoringTask.runTaskTimer(_main, 0L, 10L); // Schedule to run every tick
//	}
//
//	private void stopMonitoring()
//	{
//		if (monitoringTask != null)
//		{
//			monitoringTask.cancel();
//		}
//	}

	@Override
	public INVENTORY_AREA SetInventoryLock()
	{
		return INVENTORY_AREA.NONE;
	}

	@Override
	public void OnOpen()
	{
		super.OnOpen();
		// startMonitoring();
		InitButtons();

	}

	@Override
	public void OnClose()
	{
		// stopMonitoring();
		super.OnClose();
	}

	private ItemStack GetEnchantItem()
	{
		return GetInventory().getItem(_enchantSlot);
	}

	private void ClearTable()
	{
		for (int i = 0; i < GetSize(); i++)
		{
			if (ManagerEnchants.REDSTRICTED_SLOTS.contains(i))
				continue;

			Button button = new Button(i, EMPTY_BLACK_SLOT);
			AddButton(button);
		}
	}

	private void InitButtons()
	{
		ItemStack stack;
		ItemStack emptyPurple = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);

		ItemUtils.SetDisplayNameEmpty(emptyPurple);

		ClearTable();

		Button button;
		// Empties
		button = new Button(GetSize() - 3, emptyPurple);
		AddButton(button);

		button = new Button(GetSize() - 7, emptyPurple);
		AddButton(button);

		button = new Button(GetSize() - 13, emptyPurple);
		AddButton(button);

		if (GetPlayer().isOp())
		{
			stack = new ItemStack(Material.NETHER_STAR);
			ItemUtils.SetDisplayName(stack, "&eREROLL Slots");
			ItemUtils.AddLore(stack, "&6Visible OPs only", true);
			button = new Button(GetSize() - 14, stack, inventoryClickEvent ->
			{
				ButtonOPreroll((Button) GetButton(GetSize() - 14), inventoryClickEvent);
			});
			AddButton(button);
		} else
		{
			button = new Button(GetSize() - 14, emptyPurple);
			AddButton(button);
		}

		button = new Button(GetSize() - 15, emptyPurple);
		AddButton(button);

		stack = new ItemStack(Material.BOOKSHELF);
		ItemUtils.SetDisplayName(stack, "&eBuy Enchants");
		button = new Button(GetSize() - 6, stack, inventoryClickEvent ->
		{
			ButtonOpenEnchantbuy((Button) GetButton(GetSize() - 6), inventoryClickEvent);
		});
		AddButton(button);

		stack = new ItemStack(Material.ENCHANTING_TABLE);
		ItemUtils.SetDisplayName(stack, "&eEnchant");
		button = new Button(GetSize() - 4, stack, inventoryClickEvent ->
		{
			ButtonEnchantItem((Button) GetButton(GetSize() - 4), inventoryClickEvent);
		});
		AddButton(button);

		// slot for enchant item
		RemoveButton(_enchantSlot);

		UpdateButtons(true);
	}

	@Override
	public boolean OnDragItem(ItemStack item, int slot)
	{
		return OnDropItem(item, slot);
	}

	@Override
	public IBUTTONN OnDragItemSet(ItemStack stack, int slot)
	{
		return OnDropItemSet(stack, slot);
	}

	@SuppressWarnings("unused")
	@Override
	public boolean OnDropItem(ItemStack stack, int slot)
	{
		if (slot == _enchantSlot)
		{
			if (!ManagerEnchants.IsValidToEnchant(stack))
			{
				GetPlayer().sendMessage(Metods.msgC("&cNot valid item to enchant!"));
				return false;
			}

			return true;
		}

		if (GetButton(slot) == null)
		{
			boolean dROP = ManagerEnchants.Instance.IsValidGUIitem(TOUCH_TYPE.DROP, _enchantedItem, stack);

			return dROP;
		}

		return false;
	}

	@Override
	public IBUTTONN OnDropItemSet(ItemStack stack, int slot)
	{
		System.out.println("DropItemSet: " + stack.getType());
		if (slot == _enchantSlot)
		{
			Button button = new Button(slot, stack);
			button.SetLockPosition(false);
			AddButton(button);
			AddTouch(slot);
			LoadItem(button, false);
			return button;
		}

		if (GetButton(slot) == null)
		{

			AddTouch(slot);
			IBUTTONN button = LoadNode(stack, slot, true);

			return button;
		}

		return null;
	}

	private void LoadItem(IBUTTONN button, boolean forceReveal)
	{
		ItemStack stack = button.GetItemStack();

		System.out.println("=====> LOADING ITEM: " + stack.getType());
		System.out.println("=====> LOADING ITEM: " + stack.getType());
		System.out.println("=====> LOADING ITEM: " + stack.getType());
		_enchantedItem = new EnchantedItem(stack, GetPlayer());

		_enchantedItem.Reveal(forceReveal);
		// button.SetItemStack(stack);
		LoadNodes(_enchantedItem);

		System.out.println("<======= LOADED: " + stack.getType());
		System.out.println("<======= LOADED: " + stack.getType());
		System.out.println("     ");
		System.out.println("     ");
		System.out.println("     ");

	}

	private IBUTTONN LoadNode(INode node, boolean insertNode)
	{
		return LoadNode(node.GetGUIitemLoad(_enchantedItem), node.GetFlatIndex(), insertNode);
	}

	private IBUTTONN LoadNode(ItemStack stack, int slot, boolean insertNode)
	{
		IBUTTONN button = null;
		
//		//TODO should be moved to use LoadGUIButton
//		if (material == CONSTANTS.ENCHANT_MATERIAL)
//			button = LoadEnchant(stack, slot);

		if (button == null)
		{
			INode nodee = ManagerEnchants.Instance.GetNode(stack, _enchantedItem);
			
			if (nodee != null)
				button = LoadGUIButton(nodee, stack, slot, insertNode);
		}

		return button;
	}

	private void LoadNodes(EnchantedItem enchantedItem)
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

				if (LoadNode(node, false) == null)
				{
					//Bukkit.getLogger().info("Couldn't load Node: " + node);
					RemoveButton(flatIndex);
					continue;
				}


				if(node.IsFrozen())
				{
					LockingButto(GetButton(flatIndex));
				}

			}
		}
		
		UpdateButtons(true);
		
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
	
	private void HanddleDropTouches()
	{
		if(_enchantedItem == null) return;
		
		for (INode node : _enchantedItem.GetUnlockedNodes())
		{
			if(node.IsFrozen()) continue;
			
			if(!IsTouched(node.GetFlatIndex())) continue;
			
//			Bukkit.getLogger().info("=====> DROP NODE"+node + " is it touched: "+IsTouched(node.GetFlatIndex()));	
			IBUTTONN button = GetButton(node.GetFlatIndex());
			
			if( button == null) continue;
			
			ItemStack stack = node.GetGUIitemUnLoad(_enchantedItem, button.GetItemStack());
			
			button.SetItemStack(stack);
			UpdateButton(button);
			
		}
		DropTouches();
	}
	@SuppressWarnings("unused")
	@Override
	public boolean OnPickupAll(IBUTTONN button, int slot)
	{

		if (slot == _enchantSlot)
		{
			EnchantedItem item = _enchantedItem;
			
			RemoveTouch(_enchantSlot);
			HanddleDropTouches();
			_enchantedItem = null;
			
			for (INode node : item.GetUnlockedNodes())
			{
				int nodeSlot = node.GetFlatIndex();

				Button b = new Button(nodeSlot, EMPTY_BLACK_SLOT);
				AddButton(b);
				UpdateButton(nodeSlot);
			}
			
			button.SetItemStack(new ItemStack(Material.AIR));
			
			RefreshTimeID();
			return true;
		}
		
		RemoveTouch(button);

		if (button != null)
		{
			if (_enchantedItem == null)
				return false;

			if (button.GetLastClickType() == ClickType.RIGHT
					&& ManagerEnchants.Instance.IsValidGUIitem(TOUCH_TYPE.PICK_UP, _enchantedItem, button))
			{
				if (ItemUtils.HasTag(button.GetItemStack(), PD_LOCKED))
				{
					RemoveNode(button);
					return false;
				}

				if (ItemUtils.HasTag(button.GetItemStack(), PD_SWAPPER))
				{
					ActivateSwapper(slot);				
					return false;
				}

			}

			if (!ManagerEnchants.Instance.IsValidGUIitem(TOUCH_TYPE.PICK_UP, _enchantedItem,
					GetInventory().getItem(slot)))
			{
				System.out.println("not valid material: " + GetInventory().getItem(slot).getType());
				return false;
			}
			
			if (ItemUtils.HasTag(button.GetItemStack(), PD_LOCKED))
			{
				return false;
			}
			
			INode currentNode = _enchantedItem.GetNodeBySlot(slot);
			ItemStack stack = currentNode.GetGUIitemUnLoad(_enchantedItem, GetInventory().getItem(slot));
			GetInventory().setItem(slot, stack);


			INode node = new Node();
			node.SetLock(false);
			_enchantedItem.SetNode(node, slot);

			button.SetItemStack(new ItemStack(Material.AIR)); // removes button
			return true;
		}

		return false;
	}

	private void RemoveNode(IBUTTONN button)
	{

		_enchantedItem.RemoveNode(button.GetPosition());

		UpdateEnchantedItem(true);

		RemoveButton(button);
		UpdateButton(button.GetPosition());
	}

	private void LockingButto(IBUTTONN button)
	{
		
		if (button == null)
			return;

		ItemStack stack = button.GetItemStack();
		ItemUtils.SetTag(stack, PD_LOCKED);
		// ItemUtils.AddLore(stack, "&c==== LOCKED ==== ", false);

		ItemUtils.AddTextToDisplayName(stack, " &e(&cLOCKED&e)", DisplayNamePosition.BACK);
		ItemUtils.AddLore(stack, "&3▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬", true);
		ItemUtils.AddLore(stack, "&cRemove by &eM2", true);
		
		button.SetLockPosition(true);
	}

	private IBUTTONN LoadGUIButton(INode node, ItemStack stack, int slot, boolean insertNode)
	{
		ItemStack loadedStack = node.GetGUIitemLoad(_enchantedItem);

		if (!loadedStack.getType().isAir())
		{
			stack = loadedStack;
		}

		if(insertNode) _enchantedItem.SetNode(node, slot);
		
		Button button = new Button(slot, stack);
		button.SetLockPosition(false);
		AddButton(button);
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
	
	private void ActivateSwapper(int slot)
	{
		final long id = _timeID;
		NodeSwapper node = (NodeSwapper)_enchantedItem.GetNodeBySlot(slot);
		node.Activate(_enchantedItem);
		
		INode swapped = node.GetSwappedNode();
		List<INode> detachedNodes = _enchantedItem.DetachNodes(
				nodee -> (!nodee.IsFrozen() && nodee.getClass() != Node.class), null);
		
		UpdateEnchantedItem(false);		
		ClearTable();
		
		UpdateButtons(false);
		_enchantedItem.ReattachNodes(detachedNodes);
		LoadNodes(_enchantedItem);
		
		if(swapped != null && CONSTANTS.SWAPPER_ANIMATION)
		{
			ItemStack redMark = new ItemStack(Material.RED_STAINED_GLASS_PANE);
			ItemUtils.SetDisplayNameEmpty(redMark);
			final int swappedSlot = Node.GetFlatIndex(swapped.GetX(), swapped.GetY());
			
			IBUTTONN swappedButton = GetButton(swappedSlot);
			ItemStack originalItem = null;
			if(swappedButton == null)
			{
				IBUTTONN b = new Button(swappedSlot, redMark);
				originalItem = new ItemStack(Material.AIR);
				AddButton(b);
				UpdateButton(b);
			}
			else
			{
				originalItem = swappedButton.GetItemStack();
				swappedButton.SetItemStack(redMark);
				UpdateButton(swappedButton);
			}

	        final ItemStack org = originalItem;
	        Bukkit.getScheduler().scheduleSyncDelayedTask(ImusEnchants.Instance, () -> 
	        {
	        	if(id != _timeID) 
	        	{
	        		Bukkit.getLogger().info("NOT SAME INV");
	        		return;
	        	}
	        	
	            IBUTTONN swapButton = GetButton(swappedSlot);
	            swapButton.SetItemStack(org);
	            
	            UpdateButton(swapButton);
	            if(org.getType().isAir())
	            {
	            	RemoveButton(swapButton);
	            }
	        }, CONSTANTS.DELAY_SWAPPER_ANIMATION); 
		}
	}

	private void ButtonOPreroll(Button button, InventoryClickEvent event)
	{
		if (_enchantedItem == null)
			return;

		IBUTTONN enchantItemButton = GetButton(_enchantSlot);
		RemoveButton(_enchantSlot);
		Button newButton = new Button(_enchantSlot, enchantItemButton.GetItemStack());
		newButton.SetLockPosition(false);
		AddButton(newButton);

		ClearTable();
		ClearTouches();
		AddTouch(_enchantSlot);
		LoadItem(newButton, true);

	}

	private void ButtonOpenEnchantbuy(Button button, InventoryClickEvent event)
	{
		// System.out.println("Open new inv");
		HanddleDropTouches();
		ClearTouches();
		OpenPage(new InventoryBuyEnchants());
	}

	private void UpdateEnchantedItem(boolean applyEnchants)
	{
		if (GetEnchantItem() == null || GetEnchantItem().getType().isAir())
			return;

		System.out.println("     ");

		IBUTTONN b = GetButton(_enchantSlot);

		if (applyEnchants)
		{
			_enchantedItem.SaveUnlockedNodes();
			_enchantedItem.ApplyEnchantsToItem();
			RefreshTimeID();
		}

		_enchantedItem.SaveUnlockedNodes();
		_enchantedItem.SetTooltip();

		b.SetItemStack(_enchantedItem.GetItemStack());
		UpdateButton(b);
	}

	private void ButtonEnchantItem(Button button, InventoryClickEvent event)
	{
		if (GetEnchantItem() == null || GetEnchantItem().getType().isAir())
			return;

		UpdateEnchantedItem(true);

		System.out.println("     ");


		ClearTable();
		ClearTouches();

		AddTouch(_enchantSlot);
		UpdateButtons(false);

		RefreshTimeID();
	}
	
	private void RefreshTimeID()
	{
		_timeID = System.currentTimeMillis();
	}

}
