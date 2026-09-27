package imu.iMiniGames.Invs;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;
import imu.iMiniGames.Main.ImusMiniGames;
import imu.iMiniGames.Managers.SpleefManager;
import imu.iMiniGames.Other.SpleefDataCard;
import net.md_5.bungee.api.ChatColor;

public class GamePlanerChoosePotionEffectsINV extends CustomInvLayout implements Listener
{
	SpleefManager _spleefManager;
	
	ArrayList<PotionEffectType> _potionEffects = new ArrayList<>();
	
	String pd_buttonType = "img.GPCAIbt";
	String pd_potion_power = "img.GPpotionPower";
	String pd_potion_name = "img.GPpotionName";
	
	
	int _tooltip_starts = 0;
	int _current_page = 0;
	SpleefDataCard _card;
	
	int _powerMax = 5;
	int _powerMin = 0;

	private Metods _itemM;
	
	public GamePlanerChoosePotionEffectsINV(ImusMiniGames main, Player player, SpleefDataCard card) 
	{
		super(main, player, ChatColor.DARK_AQUA + "====== Available Effects =====", 4*9);
		
		_itemM = ImusAPI._metods;
		_spleefManager = main.get_spleefManager();
		addEffects();
		_tooltip_starts = _size-9;
		_card = card;
		openThis();
		refresh();
	}
	
	
	
	void addEffects()
	{
		for(Entry<PotionEffectType, Boolean> entry : _spleefManager.getPotionEffects().entrySet())
		{
			if(entry.getValue())
			{
				_potionEffects.add(entry.getKey());
			}
		}
	}
	
	public enum BUTTON
	{
		NONE,
		POTION_EFFECT,
		GO_LEFT,
		GO_RIGHT,
		BACK,
		CONFIRM,
		CLEAR;
	}
	
	void refresh_item(int slot, int increaseAmount)
	{
		ItemStack stack = _inv.getItem(slot);
		Integer power = _itemM.getPersistenData(stack, pd_potion_power, PersistentDataType.INTEGER);
		power += increaseAmount;
		_itemM.setPersistenData(stack, pd_potion_power, PersistentDataType.INTEGER, power);
		if(power > _powerMax)
		{
			_itemM.setPersistenData(stack, pd_potion_power, PersistentDataType.INTEGER, _powerMax);
			power = _powerMax;
		}
		if(power < _powerMin)
		{
			_itemM.setPersistenData(stack, pd_potion_power, PersistentDataType.INTEGER, _powerMin);
			power = _powerMin;
		}
		
		String str = ChatColor.AQUA + "Power: "+ChatColor.DARK_GREEN+power;
		if(power == 0)
		{
			str = ChatColor.AQUA + "Power: "+ChatColor.RED+"None";
		}
		_itemM.removeLore(stack, "Power:");
		_itemM.addLore(stack, str, true);
		
		PotionMeta meta = (PotionMeta)stack.getItemMeta();
		
		Color[] color_power = {Color.BLACK,Color.RED,Color.BLUE,Color.GREEN, Color.ORANGE ,Color.AQUA};

 		meta.setColor(color_power[power]);
 		stack.setItemMeta(meta);
		
	}
	
	void refresh()
	{
		ItemStack optionLine = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
		Metods.setDisplayName(optionLine, " ");
		
		for(int i = _size-1; i > _tooltip_starts-1; --i)
		{
			_inv.setItem(i, optionLine);
		}
		int start = 0 + _current_page * (_tooltip_starts);
		for(int i = 0; i < _tooltip_starts ; ++i)
		{
			int idx = i +start;
			if(idx < _potionEffects.size())
			{
				PotionEffectType pType= _potionEffects.get(idx);
				
				ItemStack item_arena = setupButton(BUTTON.POTION_EFFECT, Material.POTION,ChatColor.GOLD +pType.getName(),i);
				_itemM.addLore(item_arena, ChatColor.AQUA + "M1: "+ChatColor.GREEN + "Increase"+ChatColor.AQUA + " M2: "+ChatColor.RED + "Decrease", false);		
				_itemM.setPersistenData(item_arena, pd_potion_power, PersistentDataType.INTEGER, 0);
				_itemM.setPersistenData(item_arena, pd_potion_name, PersistentDataType.STRING, pType.getName());
				int power = 0;
				if(_card.get_invPotionEffects().containsKey(pType))
				{
					PotionEffect ef = _card.get_PotionEffect(pType);
					power = ef.getAmplifier();
				}
						
				refresh_item(i,power);
				
			}
			else
			{
				_inv.setItem(i, Metods.setDisplayName(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), " "));
			}
		}
		
		setupButton(BUTTON.GO_LEFT, Material.BIRCH_SIGN, ChatColor.AQUA + "<<", _tooltip_starts+3);
		setupButton(BUTTON.GO_RIGHT, Material.BIRCH_SIGN, ChatColor.AQUA + ">>", _size-4);
		setupButton(BUTTON.BACK, Material.RED_STAINED_GLASS_PANE, ChatColor.AQUA + "GO BACK", _tooltip_starts);
		setupButton(BUTTON.CONFIRM, Material.GREEN_STAINED_GLASS_PANE, ChatColor.AQUA + "CONFIRM", _size-1);
		setupButton(BUTTON.CLEAR, Material.LAVA_BUCKET, ChatColor.RED + "CLEAR ALL", _tooltip_starts+4);
		
		
		
	}
	
	int totalPages()
	{
		int pages =(int) Math.round(((_potionEffects.size()-1)/(_tooltip_starts))+0.5);
		return pages-1;
	}
	
	void chanceCurrentPage(int i)
	{
		_current_page = _current_page + i;
		if(_current_page < 0)
		{
			_current_page = 0;
		}
		if(_current_page > totalPages())
		{
			_current_page = totalPages();
		}
	}
	
	void setButton(ItemStack stack, BUTTON b)
	{
		_itemM.setPersistenData(stack, pd_buttonType, PersistentDataType.STRING, b.toString());
	}
	
	BUTTON getButton(ItemStack stack)
	{
		String button = _itemM.getPersistenData(stack, pd_buttonType, PersistentDataType.STRING);
		if(button != null)
			return BUTTON.valueOf(button);
		
		return BUTTON.NONE;
	}
	
	public ItemStack setupButton(BUTTON b, Material material, String displayName, int itemSlot)
	{
		ItemStack sbutton = new ItemStack(material);
		Metods.setDisplayName(sbutton, displayName);
		setButton(sbutton, b);
		_inv.setItem(itemSlot, sbutton);
		return _inv.getItem(itemSlot);
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
			
			BUTTON button = getButton(stack);
			ClickType cType = e.getClick();
			//int item_id = (_current_page * _tooltip_starts)+slot;
			switch (button) 
			{
			case NONE:
				
				break;
			case GO_LEFT:
				chanceCurrentPage(-1);
				refresh();
				break;
			case GO_RIGHT:
				chanceCurrentPage(1);
				refresh();
				break;
				
			case BACK:
				new SpleefGamePlaner(ImusMiniGames.Instance, _player, _card);
				break;
			case POTION_EFFECT:
				if(cType ==  ClickType.LEFT)
				{
					refresh_item(slot, 1);
				}
				if(cType == ClickType.RIGHT)
				{
					refresh_item(slot, -1);
				}
				break;
			case CONFIRM:

				_card.clearPotionEffect();
				for(int i = 0; i < _tooltip_starts ; ++i)
				{
					ItemStack pot = _inv.getItem(i);
					if(pot != null && getButton(pot) == BUTTON.POTION_EFFECT)
					{
						String name = _itemM.getPersistenData(pot, pd_potion_name, PersistentDataType.STRING);
						int power = _itemM.getPersistenData(pot, pd_potion_power, PersistentDataType.INTEGER);
						
						if(power == _powerMin)
							continue;
						
						_card.putPotionEffect(PotionEffectType.getByName(name), new PotionEffect(PotionEffectType.getByName(name), 1, power));
						//System.out.println("Added pot: "+name+" power "+power + " size "+_card.get_invPotionEffects().size());
					}
				}
				new SpleefGamePlaner(ImusMiniGames.Instance, _player, _card);
				break;
			case CLEAR:
				_card.clearPotionEffect();
				refresh();
				break;
			default:
				break;
			}
			
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



	@Override
	public void invClosed(InventoryCloseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onClickInsideInv(InventoryClickEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}



	@Override
	public void setupButtons()
	{
		// TODO Auto-generated method stub
		
	}

}
