package imu.GS.Invs;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.GS.Main.Main;
import imu.GS.Managers.ShopManager;
import imu.GS.Managers.UniqueManager;
import imu.GS.ShopUtl.ShopItems.ShopItemUnique;
import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;

public class UniquesINV extends CustomInvLayout
{
	ArrayList<ShopItemUnique> _unique_items = null;
	
	ShopManager _shopManager = null;
	UniqueManager _uniqueManager;

	String pd_refId = "gs.uniquesRefID";
	String pd_modify = "";
	
	int unique_slots = 0;
	int current_page = 0;
	
	int makeSlot = -1;
	Main _main;
	public UniquesINV(Main main, Player player) 
	{
		super(main, player, ChatColor.DARK_PURPLE +"========== Uniques =========", 9*6);
		_main = main;
		_shopManager = main.get_shopManager();
		_uniqueManager = main.get_shopManager().GetUniqueManager();
		//_unique_items = _uniqueManager.GetUniques().values();
		unique_slots = _size-9;
		makeSlot = _size - 5;
		pd_modify = "modNUll";
		_denyItemMove = DENY_ITEM_MOVE.NONE;
		
	}
	
	@Override
	public void openThis() 
	{
		super.openThis();
	
		_unique_items = new ArrayList<ShopItemUnique>(_uniqueManager.GetUniques().values());

		Init();
		refreshItems();
	}
	protected enum BUTTON implements IButton
	{
		Go_Left(-1),
		Go_Right(1),
		UnigueItem(1000);
		
		int type;
		
		BUTTON(int i)
		{
			this.type = i;
		}
		public int getType()
		{
			return type;
		}		
	}
	
	int totalPages()
	{
		int pages =(int) Math.round(((_unique_items.size()-1)/(unique_slots))+0.5);
		return pages-1;
	}
	
	void chanceCurrentPage(int i)
	{
		current_page = current_page + i;
		if(current_page < 0)
		{
			current_page = 0;
		}
		if(current_page > totalPages())
		{
			current_page = totalPages();
		}
	}
	@EventHandler
	public void onInvOpen(InventoryOpenEvent e)
	{
		if(isThisInv(e))
		{
			//_shopManager.addInv(_player);
			refreshItems();
		}
	}

	@Override
	public void onClickInsideInv(InventoryClickEvent e) 
	{
		ItemStack droppedItem = e.getCursor();
		InventoryAction action = e.getAction();
		if(e.getSlot() != makeSlot)
		{
			e.setCancelled(true);
			ItemStack stack = e.getCurrentItem();
			if(stack == null)
				return;
			String bName = getButtonName(stack);
//			if(Strings.isNullOrEmpty(bName))
//				return;
			if(bName == null || bName == "")
				return;
			
			BUTTON button = BUTTON.valueOf(bName);
			switch(button)
			{
			case Go_Left:
				chanceCurrentPage(-1);
				refreshItems();
				return;
			case Go_Right:
				chanceCurrentPage(1);
				refreshItems();
				return;
			case UnigueItem:
				//OpenModifyInv(stack);
				new ShopItemPriceModifyINV(_main, _player, this, _unique_items.get(getRefID(stack))).openThis();
				return;
			default:
				break;
			
			}
			
		}
		
		if(action == InventoryAction.MOVE_TO_OTHER_INVENTORY)
		{
			droppedItem = e.getCurrentItem();
			if(!addNewItem(droppedItem))
			{
				_player.sendMessage(ChatColor.YELLOW + "That item is already unique!");
				e.setCancelled(true);
			}
		}
	}
	
	
	boolean addNewItem(ItemStack stack)
	{
		if(_uniqueManager.IsUnique(stack))
		{
			return false;
		}
				
		ItemStack newItem = stack.clone();
		stack.setAmount(0);
		
		//_shopManager.checkAndRemoveEliteMobSouldBound(newItem);
	
		ShopItemUnique siu = new ShopItemUnique(_main, null, stack, 1);
		_main.get_shopManager().GetUniqueManager().AddUniqueItem(siu, true);
		
		setInModify(newItem);
		
		//_shopManager.openUniqueINVmodify(_player, newItem,true);
		new ShopItemPriceModifyINV(_main, _player, this, siu).openThis();
		
		return true;
	}
	
	boolean rightAction(InventoryAction action)
	{
		InventoryAction[] actions = {InventoryAction.PLACE_ALL,InventoryAction.PLACE_ONE,InventoryAction.PLACE_SOME};
		for(InventoryAction ac : actions)
		{
			if(action == ac)
			{
				return true;
			}
		}
		return false;
	}

	void Init()
	{
		makeInv();		
	}
		
	void setRefID(ItemStack stack, int id)
	{
		ImusAPI._metods.setPersistenData(stack, pd_refId, PersistentDataType.INTEGER, id);
	}
	
	Integer getRefID(ItemStack stack)
	{
		return ImusAPI._metods.getPersistenData(stack, pd_refId, PersistentDataType.INTEGER);
	}
	
	void setInModify(ItemStack stack)
	{
		ImusAPI._metods.setPersistenData(stack, pd_modify, PersistentDataType.INTEGER, 1);
	}
	
	boolean isModified(ItemStack stack)
	{
		Integer i = ImusAPI._metods.getPersistenData(stack, pd_modify, PersistentDataType.INTEGER);
		if(i != null)
		{
			return true;
		}
		return false;
	}
	
	void removeInModify(ItemStack stack)
	{
		ImusAPI._metods.removePersistenData(stack, pd_modify);
	}
	
	void makeInv()
	{
		
		ItemStack optionLine = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
		Metods.setDisplayName(optionLine, " ");
		
		for(int i = _size-1; i > unique_slots-1; --i)
		{
//			if(makeSlot == i)
//				continue;
			_inv.setItem(i, optionLine);
		}
//		ItemStack marker = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE);
//		Metods.setDisplayName(marker, ChatColor.DARK_PURPLE +"Add new unique here >>");
//		_inv.setItem(makeSlot-1, marker);
//		Metods.setDisplayName(marker, ChatColor.DARK_PURPLE +"<< Add new unique here");
//		_inv.setItem(makeSlot+1, marker);
		
		ItemStack left_button = new ItemStack(Material.BIRCH_SIGN);
		ItemStack right_button = left_button.clone();
		Metods.setDisplayName(left_button, ChatColor.AQUA + "<<");
		Metods.setDisplayName(right_button, ChatColor.AQUA + ">>");
		
		//setButtonSwitch(left_button, LABELS.Go_Left.getType());
		SetButton(left_button, BUTTON.Go_Left);
		//setButtonSwitch(right_button, LABELS.Go_Right.getType());
		SetButton(right_button, BUTTON.Go_Right);
		
		_inv.setItem(unique_slots, left_button);
		_inv.setItem(_size-1, right_button);
			
	}
	
	void refreshItems()
	{
		_unique_items = new ArrayList<ShopItemUnique>(_uniqueManager.GetUniques().values());
		int start = 0 + current_page * (unique_slots);
		ItemStack empty = new ItemStack( Material.BLACK_STAINED_GLASS_PANE);
		ItemStack stack, copy;
		Metods.setDisplayName(empty, " ");
		
		for(int i = 0; i < unique_slots ; ++i)
		{
			int idx = i+start;
			if(idx < _unique_items.size())
			{
				
				stack = _unique_items.get(idx).GetRealItem();
				copy = stack.clone();
				SetTooltip(copy, _unique_items.get(idx).GetItemPrice().GetPrice());
				SetButton(copy, BUTTON.UnigueItem);
				setRefID(copy, idx);
				_inv.setItem(i, copy);
			}
			else
			{
				_inv.setItem(i,empty);
			}
		}
	}
	
	void SetTooltip(ItemStack stack, double price)
	{
		String modifyStr =ChatColor.YELLOW +"== Click to modify ==";
		
		if(isModified(stack))
		{
			 modifyStr = ChatColor.RED+"== BEING MODIFIED ==";
		}
		String[] tooltip_strs= {ChatColor.AQUA+ "======Unique======",
								ChatColor.DARK_PURPLE + "price : " + ChatColor.GOLD + price,
								modifyStr,
								ChatColor.AQUA+ "======Unique======"};
		for(String str : tooltip_strs)
		{
			ImusAPI._metods.addLore(stack, str, false);
		}
	}
	
	
	@Override
	public void invClosed(InventoryCloseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	public void setupButtons() {
		// TODO Auto-generated method stub
		
	}
	
}
