package imu.iMiniGames.Invs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;
import imu.iMiniGames.Main.ImusMiniGames;
import imu.iMiniGames.Managers.CombatManager;
import imu.iMiniGames.Other.SpleefDataCard;
import net.md_5.bungee.api.ChatColor;

public class SpleefGamePlanerChoosePlayerINV extends CustomInvLayout
{
	CombatManager _combatManager;
	
	String pd_buttonType = "img.GPCAIbt";
	String pd_uuid = "img.AIuuid";
	
	
	int _tooltip_starts = 0;
	int _current_page = 0;
	SpleefDataCard _card;
	HashMap<UUID, ItemStack> _playerHeads = new HashMap<>();
	ImusMiniGames _main;
	public SpleefGamePlanerChoosePlayerINV(ImusMiniGames main, Player player, SpleefDataCard card) 
	{
		super(main, player, ChatColor.DARK_AQUA + "===== Available Players =====", 3*9);
		_main = main;
		_combatManager = main.get_combatManager();
		
		_tooltip_starts = _size-9;
		_card = card;
		openThis();
		refresh();
	}
	
	public enum BUTTON
	{
		NONE,
		PLAYER_HEAD,
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
		ArrayList<Player> players = new ArrayList<>( _main.getServer().getOnlinePlayers());
		players.remove(_player);
		for(int i = 0; i < 9 ; ++i)
		{
			int idx = i +start;
			
			
			if(players.size() > idx)
			{
				Player p = players.get(idx);
				if(p != null)
				{
					ItemStack head = new ItemStack(Material.PLAYER_HEAD); //Metods._ins.getPlayerHead(p);
					Metods.setDisplayName(head, ChatColor.translateAlternateColorCodes('&', "&6&l"+p.getName()));					
					ItemStack optionItem = new ItemStack(Material.BLACK_CONCRETE);
					Metods.setDisplayName(optionItem, ChatColor.DARK_RED + "Not wanna be invited!");
					if(!_main.isPlayerBlocked(p))
					{
						optionItem.setType(Material.RED_CONCRETE);
						Metods.setDisplayName(optionItem, ChatColor.RED + "Not selected");
						if(_card.isInvitePlayer(p) && _card.getInvitePlayer(p))
						{
							Metods.setDisplayName(optionItem, ChatColor.GREEN + "SELECTED");
							optionItem.setType(Material.GREEN_CONCRETE);
						}
						
						
						setButton(head,BUTTON.PLAYER_HEAD);
						setButton(optionItem,BUTTON.PLAYER_HEAD);
						
						Metods._ins.setPersistenData(head, pd_uuid, PersistentDataType.STRING, p.getUniqueId().toString());
						Metods._ins.setPersistenData(optionItem, pd_uuid, PersistentDataType.STRING, p.getUniqueId().toString());
						
						
					}else
					{
						_card.removeInvitePlayer(p);
					}
					_inv.setItem(i+9, optionItem);
					_inv.setItem(i, head);
					
					continue;
				}
			}
			_inv.setItem(i, Metods.setDisplayName(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), " "));
			_inv.setItem(i+9, Metods.setDisplayName(new ItemStack(Material.BLACK_STAINED_GLASS_PANE), " "));
		}
		
		setupButton(BUTTON.GO_LEFT, Material.BIRCH_SIGN, ChatColor.AQUA + "<<", _tooltip_starts+3);
		setupButton(BUTTON.GO_RIGHT, Material.BIRCH_SIGN, ChatColor.AQUA + ">>", _size-4);
		setupButton(BUTTON.BACK, Material.RED_STAINED_GLASS_PANE, ChatColor.AQUA + "GO BACK", _tooltip_starts);
		
		
		
	}
	
	int totalPages()
	{
		int pages =(int) Math.round(((_main.getServer().getOnlinePlayers().size()-1)/(_tooltip_starts))+0.5);
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
		Metods._ins.setPersistenData(stack, pd_buttonType, PersistentDataType.STRING, b.toString());
	}
	
	BUTTON getButton(ItemStack stack)
	{
		String button = Metods._ins.getPersistenData(stack, pd_buttonType, PersistentDataType.STRING);
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
	public void onClickInsideInv(InventoryClickEvent e) {
		ItemStack stack = e.getCurrentItem();
		
		BUTTON button = getButton(stack);

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
			new SpleefGamePlaner(_main, _player, _card);
			break;
		case PLAYER_HEAD:
			UUID uuid  = UUID.fromString(Metods._ins.getPersistenData(stack, pd_uuid, PersistentDataType.STRING));

			Player p = Bukkit.getPlayer(uuid);
			if(p != null)
			{
				if(_card.isInvitePlayer(p) && _card.getInvitePlayer(p))
				{
					_card.removeInvitePlayer(p);
				}else
				{
					_card.addInvitePlayer(p, true);
				}
			}
			refresh();
			break;
		default:
			break;
		}
	}

	@Override
	public void setupButtons() {
	
	}

	@Override
	public void invClosed(InventoryCloseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
