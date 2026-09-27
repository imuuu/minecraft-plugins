package imu.GeneralStore.Other;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.GeneralStore.main.Main;
import net.md_5.bungee.api.ChatColor;

public class EnchantINV extends CustomInvLayout implements Listener
{
	
	EnchantsManager enchM = null;
	ItemMetods itemM = null;

	int unique_slots = 0;
	int current_page = 0;
	
	String pd_refId = "gs.EnchRefID";
	public EnchantINV(Main main, Player player, String name) 
	{
		super(main, player, name, 9*6);
		
		enchM = _main.getEnchManager();
		itemM = _main.getItemM();

		unique_slots = _size-9;

		_main.getServer().getPluginManager().registerEvents(this, _main);

	}
	
	enum LABELS
	{
		Go_Left(-1),
		Go_Right(1),
		ENC_ITEM(1000);
				
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
		int pages =(int) Math.round(((enchM.ench_items.size()-1)/(unique_slots))+0.5);
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
			makeInv();
		}
	}
	
	@EventHandler
	public void onInvClose(InventoryCloseEvent e)
	{
		if(isThisInv(e))
		{
			HandlerList.unregisterAll(this);
		}
	}
	@EventHandler
	public void onInvClickEvent(InventoryClickEvent e) 
	{
		int rawSlot = e.getRawSlot();
		int slot = e.getSlot();
		
		if(isThisInv(e) && (rawSlot == slot))
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
				
				if(switch_button == LABELS.ENC_ITEM.getType())
				{
					openModifyInv(stack);
					return;
				}
			}
		}	
	}
	

	
	void makeInv()
	{
		ItemStack optionLine = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
		itemM.setDisplayName(optionLine, " ");
		
		for(int i = _size-1; i > unique_slots-1; --i)
		{
			_inv.setItem(i, optionLine);
		}
				
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

		int start = 0 + current_page * (unique_slots);
		ItemStack empty = new ItemStack( Material.BLACK_STAINED_GLASS_PANE);
		ItemStack stack, copy;
		itemM.setDisplayName(empty, " ");
		
		for(int i = 0; i < unique_slots ; ++i)
		{
			int idx = i+start;
			if(idx < enchM.ench_items.size())
			{				
				stack = enchM.ench_items.get(idx);
				copy = stack.clone();
				setTooltip(copy);
				setButtonSwitch(copy, LABELS.ENC_ITEM.getType());
				setRefID(copy, idx);

				_inv.setItem(i, copy);
			}
			else
			{
				_inv.setItem(i,empty);
			}
		}
	}
	
	void setRefID(ItemStack stack, int id)
	{
		itemM.setPersistenData(stack, pd_refId, PersistentDataType.INTEGER, id);
	}
	
	Integer getRefID(ItemStack stack)
	{
		return itemM.getPersistenData(stack, pd_refId, PersistentDataType.INTEGER);
	}
	
	void setTooltip(ItemStack stack)
	{
		Double[] p = enchM.getEnchPriceData(stack);
		String modifyStr =ChatColor.YELLOW +"== Click to modify ==";
		
		if(enchM.isModify(stack))
		{
			 modifyStr = ChatColor.RED+"== BEING MODIFIED ==";
		}
		String[] tooltip_strs= {ChatColor.AQUA+ "==== Enchantment ====",
								ChatColor.DARK_PURPLE + "maxPrice: " + ChatColor.GOLD + p[3],
								ChatColor.DARK_PURPLE + "minPrice : " + ChatColor.GOLD + p[2],
								ChatColor.DARK_PURPLE + "maxLevel: " + ChatColor.GOLD + p[1],
								ChatColor.DARK_PURPLE + "minLevel : " + ChatColor.GOLD + p[0],
								modifyStr,
								ChatColor.AQUA+ "==== Enchantment ===="};
		for(String str : tooltip_strs)
		{
			itemM.addLore(stack, str, false);
		}
	}
	
	void openModifyInv(ItemStack stack)
	{
		ItemStack real =enchM.ench_items.get( getRefID(stack));
		enchM.setModifyData(real);
		enchM.openEnchantINVmodify(_player, real);
		
	}

	
}
