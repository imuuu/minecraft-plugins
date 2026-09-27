package imu.iMiniGames.Invs;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;
import imu.iMiniGames.Arenas.Arena;
import imu.iMiniGames.Arenas.SpleefArena;
import imu.iMiniGames.Main.ImusMiniGames;
import imu.iMiniGames.Managers.SpleefManager;
import imu.iMiniGames.Other.SpleefDataCard;
import net.md_5.bungee.api.ChatColor;

public class GamePlanerChooseArenaINV extends CustomInvLayout implements Listener
{
	SpleefManager _spleefManager;
	
	ArrayList<SpleefArena> _arenas;
	
	String pd_buttonType = "img.GPCAIbt";
	String pd_arena_name = "img.AIarenaName";
	
	
	int _tooltip_starts = 0;
	int _current_page = 0;
	SpleefDataCard _card;

	private Metods _itemM;
	
	public GamePlanerChooseArenaINV(ImusMiniGames main, Player player, SpleefDataCard card) 
	{
		super(main, player, ChatColor.DARK_AQUA + "====== Available Arenas =====", 2*9);
		
		//_main.getServer().getPluginManager().registerEvents(this,_main);
		_spleefManager = main.get_spleefManager();
		//_arenas = _spleefManager.getArenas();
		_itemM = ImusAPI._metods;
		for(Arena arena : _spleefManager.getArenas())
		{
			if(arena instanceof SpleefArena)
			{
				_arenas.add((SpleefArena)arena);
			}
		}
		
		_tooltip_starts = _size-9;
		_card = card;
		openThis();
		refresh();
	}
	
	public enum BUTTON
	{
		NONE,
		ARENA,
		GO_LEFT,
		GO_RIGHT,
		BACK;
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
			if(idx < _arenas.size())
			{
				Arena arena = _arenas.get(idx);
				
				ItemStack item_arena = setupButton(BUTTON.ARENA, Material.SNOW_BLOCK,ChatColor.GOLD + arena.get_name(),i);
				_itemM.addLore(item_arena, ChatColor.AQUA + "Desc: "+ChatColor.GOLD+arena.get_description(), true);
				_itemM.addLore(item_arena, ChatColor.AQUA + "Max players: "+ChatColor.GOLD+arena.get_maxPlayers(), true);
				
				_itemM.setPersistenData(item_arena, pd_arena_name, PersistentDataType.STRING, arena.get_name());
			}
			else
			{
				_inv.setItem(i, Metods.setDisplayName(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), " "));
			}
		}
		
		setupButton(BUTTON.GO_LEFT, Material.BIRCH_SIGN, ChatColor.AQUA + "<<", _tooltip_starts+3);
		setupButton(BUTTON.GO_RIGHT, Material.BIRCH_SIGN, ChatColor.AQUA + ">>", _size-4);
		setupButton(BUTTON.BACK, Material.RED_STAINED_GLASS_PANE, ChatColor.AQUA + "GO BACK", _tooltip_starts);
		
		
		
	}
	
	int totalPages()
	{
		int pages =(int) Math.round(((_arenas.size()-1)/(_tooltip_starts))+0.5);
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
	
	@Override
	public void onClickInsideInv(InventoryClickEvent e)
	{
		int rawSlot = e.getRawSlot();
		int slot = e.getSlot();
		
		if(isThisInv(e) && (rawSlot == slot))
		{			
			e.setCancelled(true);
			ItemStack stack = e.getCurrentItem();
			
			BUTTON button = getButton(stack);
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
			case ARENA:
				String ar_name = _itemM.getPersistenData(stack, pd_arena_name, PersistentDataType.STRING);
				_card.set_arena(_spleefManager.getArena(ar_name));
				new SpleefGamePlaner(ImusMiniGames.Instance, _player, _card);
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
	public void setupButtons()
	{
		// TODO Auto-generated method stub
		
	}

}
