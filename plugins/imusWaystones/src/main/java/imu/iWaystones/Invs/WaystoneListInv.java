package imu.iWaystones.Invs;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;
import imu.iWaystone.Waystones.Waystone;
import imu.iWaystones.Main.ImusWaystones;
import imu.iWaystones.Managers.WaystoneManager;

public class WaystoneListInv extends CustomInvLayout {

	private ImusWaystones _main;

	private WaystoneManager _wManager;
	private int _page = 0;
	private ArrayList<Waystone> _discoveredWaystones = new ArrayList<>();
	
	public WaystoneListInv(Player player) 
	{
		super(ImusWaystones._instance, player, "&5List of Waystones" , 4 * 9);
		_main = ImusWaystones._instance;

		_wManager = _main.GetWaystoneManager();
		
	}
	
	private void InitWaystones()
	{
		_discoveredWaystones.clear();
		for(UUID uuid_ws : _wManager.GetWaystones().keySet())
		{
			Waystone ws = _wManager.GetWaystone(uuid_ws);
			if(ws == null) continue;
				
			_discoveredWaystones.add(ws);
		}
	}
	
	enum BUTTON implements IButton
	{
		NONE,
		GO_LEFT,
		GO_RIGHT,
		WAYSTONE,
	}
	BUTTON GetButtonPress(InventoryClickEvent e)
	{
		ItemStack stack = e.getCurrentItem();
		if(stack == null)
			return BUTTON.NONE;
		
		String buttonName = getButtonName(e.getCurrentItem());
		if(buttonName == null)
			return BUTTON.NONE;
		
		return BUTTON.valueOf(buttonName);
	}
	
	@Override
	public void onClickInsideInv(InventoryClickEvent e) 
	{
		BUTTON button = GetButtonPress(e);
		//if(!CheckIfValid()) return;
		
		switch (button) 
		{
		case NONE:
			break;
		case GO_LEFT:
			_page = PageChance(_page, -1, _discoveredWaystones.size(), _size-9);
			setupButtons();
			break;
		case GO_RIGHT:
			_page = PageChance(_page, 1, _discoveredWaystones.size(), _size-9);
			setupButtons();
			break;

		case WAYSTONE:
			
			if(e.getClick() == ClickType.SHIFT_RIGHT)
			{
				_wManager.RemoveWaystone(GetWaystoneFromButton(e.getCurrentItem()));
				_player.sendMessage(Metods.msgC("&eWaystone &2has &cREMOVED"));
				//_player.closeInventory();
				Init();
				return;
			}
			Teleporting(e.getCurrentItem());			
			_player.closeInventory();
			break;
		
		}
	}
	

	private void Teleporting(ItemStack currentItem) 
	{
		Waystone ws = GetWaystoneFromButton(currentItem);
		
		if(ws == null)
		{
			_player.sendMessage(Metods.msgC("&cTarget waystone doesn't exist!"));
			//return;
		}
		
		if(!_wManager.IsValid(ws))
		{
			_player.sendMessage(Metods.msgC("&3Target Waystone &4isn't Valid!"));
		}
		
		
		_player.teleport(ws.GetLoc().add(new Vector(0, 1, 0)));
		
	
	}

	@Override
	public void openThis() 
	{
		super.openThis();
		
		InitWaystones();
		Init();
		
	}
	
	private void Init()
	{
		setupButtons();
	}
	
	@Override
	public void setupButtons() 
	{
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				for(int i = _size-9; i < _size; i++) {setupButton(BUTTON.NONE, Material.CYAN_STAINED_GLASS_PANE, " ", i);}
				setupButton(BUTTON.GO_LEFT, Material.BIRCH_SIGN, "&b<<", _size-9);
				setupButton(BUTTON.GO_RIGHT, Material.BIRCH_SIGN, "&b>>", _size-1);
				//setupButton(BUTTON.UPGRADE, Material.BIRCH_BOAT, "&eUPGRADE", _size-5);
				
			}
		}.runTaskAsynchronously(_main);
		
		LoadWaystones();
	}
	
	private Waystone GetWaystoneFromButton(ItemStack stack)
	{
		return _wManager.GetWaystone(UUID.fromString(Metods._ins.getPersistenData(stack, "iwm" ,PersistentDataType.STRING)));
	}
	
	private void LoadWaystones()
	{
		for(int i = 0; i < _size-9;i++)
		{
			int idx = i + ( _page * (_size-9));
			if(idx > _discoveredWaystones.size()-1) {_inv.setItem(i, new ItemStack(Material.BLUE_STAINED_GLASS_PANE));continue;}
			
			Waystone ws = _discoveredWaystones.get(idx);

			ItemStack stack = ws.GetDisplayItem().clone();
			Metods.setDisplayName(stack, ws.GetName());
			String[] lores = new String[5];
			lores[0] = "&7(&eClick&7)&2 to start &bTeleporting";
			lores[1] = "&7(&5Shift &bM2&7)&2 to &cRemove";
			lores[2] = " ";
			lores[3] = "&eOwner: &b"+ws.GetOwnerName();
			lores[4] = "&eLocation: &5"+ws.GetLoc().getWorld().getName() + " &2"+ws.GetLoc().getBlockX()+ " "+ ws.GetLoc().getBlockY() + " "+ws.GetLoc().getBlockZ();
			
			Metods._ins.SetLores(stack, lores, false);
			Metods._ins.setPersistenData(stack, "iwm", PersistentDataType.STRING, ws.GetUUID().toString());
			SetButton(stack, BUTTON.WAYSTONE);
			_inv.setItem(i, stack);
		}
		
	}
	
	boolean IsAbleToShow(Waystone waystone)
	{
		return true;
	}

	@Override
	public void invClosed(InventoryCloseEvent arg0)
	{
		
	}
	
	

}
