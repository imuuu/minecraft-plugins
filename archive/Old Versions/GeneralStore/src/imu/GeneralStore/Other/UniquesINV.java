package imu.GeneralStore.Other;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.GeneralStore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class UniquesINV extends CustomInvLayout implements Listener
{
	ArrayList<ItemStack> unique_items = null;
	
	ShopManager _shopManager = null;

	String pd_refId = "gs.uniquesRefID";
	String pd_modify = "";
	
	int unique_slots = 0;
	int current_page = 0;
	
	int makeSlot = -1;
	
	public UniquesINV(Main main, Player player, String name) 
	{
		super(main, player, name, 9*6);
		main.getServer().getPluginManager().registerEvents(this, _main);
		_shopManager = _main.getShopManager();		
		unique_items = _shopManager.getUnique_items();
		unique_slots = _size-9;
		makeSlot = _size - 5;
		pd_modify = _shopManager.get_pd_modify();
		Init();
		
		_shopManager.uniqueInvs.put(player,this);

	}
	
	enum LABELS
	{
		Go_Left(-1),
		Go_Right(1),
		UnigueItem(1000);
		
		
		int type;
		
		LABELS(int i)
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
		int pages =(int) Math.round(((_shopManager.getUnique_items().size()-1)/(unique_slots))+0.5);
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
			_shopManager.addInv(_player);
			refreshItems();
		}
	}
	
	@EventHandler
	public void onInvClose(InventoryCloseEvent e)
	{
		if(isThisInv(e))
		{
			_shopManager.removeOpenedInv(_player);
		}
	}
	@EventHandler
	public void onInvClickEvent(InventoryClickEvent e) 
	{

		int rawSlot = e.getRawSlot();
		int slot = e.getSlot();
		
		ItemStack droppedItem = e.getCursor();
		InventoryAction action = e.getAction();
		
		if(isThisInv(e) && (rawSlot == slot) && slot != makeSlot)
		{			
			e.setCancelled(true);
			ItemStack stack = e.getCurrentItem();
			
			Integer switch_button = getButtonSwitch(stack);
			if(switch_button != null)
			{
				if(switch_button == LABELS.Go_Left.getType())
				{
					chanceCurrentPage(-1);
					refreshItems();
					return;
				}
				if(switch_button == LABELS.Go_Right.getType())
				{
					chanceCurrentPage(1);
					refreshItems();
					return;
				}
				
				if(switch_button == LABELS.UnigueItem.getType())
				{
					openModifyInv(stack);
					return;
				}
			}
		}

		if(isThisInv(e) && action == InventoryAction.MOVE_TO_OTHER_INVENTORY)
		{
			droppedItem = e.getCurrentItem();
			if(!addNewItem(droppedItem))
			{
				if( _shopManager.isUnique(droppedItem))
				{
					_player.sendMessage(ChatColor.YELLOW + "That item is already unique!");
				}else
				{
					_player.sendMessage(ChatColor.DARK_RED + "Please add only one item at the time");
				}
				
				e.setCancelled(true);
			}
		}
		
		if(isThisInv(e) && (rawSlot == slot) && slot == makeSlot && rightAction(action) && droppedItem != null)
		{
			if(!addNewItem(droppedItem))
			{
				if( _shopManager.isUnique(droppedItem))
				{
					_player.sendMessage(ChatColor.YELLOW + "That item is already unique!");
				}else
				{
					_player.sendMessage(ChatColor.DARK_RED + "Please add only one item at the time");
				}
				e.setCancelled(true);
			}			
		}	
	}
	
	boolean addNewItem(ItemStack stack)
	{
		if(stack.getAmount() > 1 || _shopManager.isUnique(stack))
		{
			return false;
		}
				
		ItemStack newItem = stack.clone();
		stack.setAmount(0);
		
		_shopManager.checkAndRemoveEliteMobSouldBound(newItem);
		
		Double[] newPrice = {0.0,0.0,0.0};		
		_shopManager.addUniqueItem(newItem, newPrice, false);	
		setInModify(newItem);
		_shopManager.openUniqueINVmodify(_player, newItem,true);
		
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
		itemM.setPersistenData(stack, pd_refId, PersistentDataType.INTEGER, id);
	}
	
	Integer getRefID(ItemStack stack)
	{
		return itemM.getPersistenData(stack, pd_refId, PersistentDataType.INTEGER);
	}
	
	void setInModify(ItemStack stack)
	{
		itemM.setPersistenData(stack, pd_modify, PersistentDataType.INTEGER, 1);
	}
	
	boolean isModified(ItemStack stack)
	{
		Integer i = itemM.getPersistenData(stack, pd_modify, PersistentDataType.INTEGER);
		if(i != null)
		{
			return true;
		}
		return false;
	}
	
	void removeInModify(ItemStack stack)
	{
		itemM.removePersistenData(stack, pd_modify);
	}
	
	void makeInv()
	{
		unique_items = _shopManager.getUnique_items();
		ItemStack optionLine = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
		itemM.setDisplayName(optionLine, " ");
		
		for(int i = _size-1; i > unique_slots-1; --i)
		{
			if(makeSlot == i)
				continue;
			_inv.setItem(i, optionLine);
		}
		ItemStack marker = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE);
		itemM.setDisplayName(marker, ChatColor.DARK_PURPLE +"Add new unique here >>");
		_inv.setItem(makeSlot-1, marker);
		itemM.setDisplayName(marker, ChatColor.DARK_PURPLE +"<< Add new unique here");
		_inv.setItem(makeSlot+1, marker);
		
		ItemStack left_button = new ItemStack(Material.BIRCH_SIGN);
		ItemStack right_button = left_button.clone();
		itemM.setDisplayName(left_button, ChatColor.AQUA + "<<");
		itemM.setDisplayName(right_button, ChatColor.AQUA + ">>");
		
		setButtonSwitch(left_button, LABELS.Go_Left.getType());
		setButtonSwitch(right_button, LABELS.Go_Right.getType());
		
		_inv.setItem(unique_slots, left_button);
		_inv.setItem(_size-1, right_button);
		
		refreshItems();
	}
	
	void refreshItems()
	{
		unique_items = _shopManager.getUnique_items();
		int start = 0 + current_page * (unique_slots);
		ItemStack empty = new ItemStack( Material.BLACK_STAINED_GLASS_PANE);
		ItemStack stack, copy;
		itemM.setDisplayName(empty, " ");
		
		for(int i = 0; i < unique_slots ; ++i)
		{
			int idx = i+start;
			if(idx < unique_items.size())
			{
				
				stack = unique_items.get(idx);
				copy = stack.clone();
				setTooltip(copy);
				setButtonSwitch(copy, LABELS.UnigueItem.getType());
				setRefID(copy, idx);
				_inv.setItem(i, copy);
			}
			else
			{
				_inv.setItem(i,empty);
			}
		}
	}
	
	void setTooltip(ItemStack stack)
	{
		Double[] p = _shopManager.getUniqueItemPrice(stack);
		String modifyStr =ChatColor.YELLOW +"== Click to modify ==";
		
		if(isModified(stack))
		{
			 modifyStr = ChatColor.RED+"== BEING MODIFIED ==";
		}
		String[] tooltip_strs= {ChatColor.AQUA+ "======Unique======",
								ChatColor.DARK_PURPLE + "percent : " + ChatColor.GOLD + p[2],
								ChatColor.DARK_PURPLE + "maxPrice: " + ChatColor.GOLD + p[1],
								ChatColor.DARK_PURPLE + "minPrice : " + ChatColor.GOLD + p[0],
								modifyStr,
								ChatColor.AQUA+ "======Unique======"};
		for(String str : tooltip_strs)
		{
			itemM.addLore(stack, str, false);
		}
	}
	
	void openModifyInv(ItemStack stack)
	{
		int idx = getRefID(stack);
		setInModify(_shopManager.unique_items.get(idx));
		_shopManager.openUniqueINVmodify(_player, _shopManager.unique_items.get(idx),false);
		
	}
	
}
