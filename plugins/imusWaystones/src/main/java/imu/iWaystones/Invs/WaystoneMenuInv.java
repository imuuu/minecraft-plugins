package imu.iWaystones.Invs;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Material;

import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;


import imu.iAPI.Interfaces.IButton;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;

import imu.iWaystone.Upgrades.PlayerUpgradePanel;
import imu.iWaystone.Waystones.Waystone;
import imu.iWaystones.Enums.VISIBILITY_TYPE;
import imu.iWaystones.Main.ImusWaystones;
import imu.iWaystones.Managers.WaystoneManager;

public class WaystoneMenuInv extends CustomInvLayout {

	private ImusWaystones _main;
	private Waystone _waystone;
	private WaystoneManager _wManager;
	private int _page = 0;
	private ArrayList<Waystone> _discoveredWaystones = new ArrayList<>();
	
	private boolean _saveAfterClose = false;
	public WaystoneMenuInv(Waystone waystone, Player player) 
	{
		super(ImusWaystones._instance, player, waystone.GetName()+" "+(waystone.IsCooldown(player) ? "&ccd: "+ waystone.GetCooldown(player): "") , 4 * 9);
		_main = ImusWaystones._instance;
		_waystone = waystone;
		_wManager = _main.GetWaystoneManager();
		_waystone.ReadBuildUpgrade();
		
	}
	
	void LoadWaystones()
	{
		_discoveredWaystones.clear();
		
		if(!_waystone.IsEnable())
		{
			_player.sendMessage(Metods.msgC("&cWaystone is Disabled!"));
			return;
		}
		
		if(_waystone.GetVisibilityType() == VISIBILITY_TYPE.TO_ALL_ONE_WAY)
		{
			_player.sendMessage(Metods.msgC("&eThis Waystone is unable to teleport other Waystones"));
			return;
		}
		
		if(_waystone.IsCooldown(_player)) 
		{
			_player.sendMessage(Metods.msgC("&eYou have cooldown => "+_waystone.GetCooldown(_player)));
			return;
		}
		
		for(UUID uuid_ws : _wManager.GetWaystonesByVisibility(VISIBILITY_TYPE.TO_ALL))
		{
			LoadWaystone(uuid_ws);
		}
		
		for(UUID uuid_ws : _wManager.GetWaystonesByVisibility(VISIBILITY_TYPE.TO_ALL_ONE_WAY))
		{
			LoadWaystone(uuid_ws);
		}
		
		for(UUID uuid_ws : _wManager.GetDiscovered().get(_player.getUniqueId()))
		{
			LoadWaystone(uuid_ws);
		}
		
		
	}
	
	private void LoadWaystone(UUID uuid_ws)
	{
		PlayerUpgradePanel panel = _waystone.GetPlayerUpgradePanel(_player.getUniqueId());
		
		if(uuid_ws.equals(_waystone.GetUUID())) return;
			
		Waystone ws = _wManager.GetWaystone(uuid_ws);
		
		if(ws == null) return;
		
		if(_discoveredWaystones.contains(ws)) return;
		
		if(ws.GetVisibilityType() == VISIBILITY_TYPE.ONE_WAY) return;
		
		if(!ws.IsEnable()) return;
		
		if(ws.GetVisibilityType() == VISIBILITY_TYPE.TO_ALL || ws.GetVisibilityType() == VISIBILITY_TYPE.TO_ALL_ONE_WAY)
		{
			_discoveredWaystones.add(ws);
			return;
			
		}
		
		if(ws.GetLoc().getWorld().getEnvironment() == Environment.NETHER  && (_waystone.GetLoc().getWorld().getEnvironment() == Environment.NORMAL || _waystone.GetLoc().getWorld().getEnvironment() == Environment.THE_END) && !panel.get_dimension().IsNetherUnlocked())
		{
			return;
		}
		
		if(_waystone.GetLoc().getWorld().getEnvironment() == Environment.NETHER  && (ws.GetLoc().getWorld().getEnvironment() == Environment.NORMAL || ws.GetLoc().getWorld().getEnvironment() == Environment.THE_END) && !panel.get_dimension().IsNetherUnlocked())
		{
			return;
		}
			
		if(ws.GetLoc().getWorld().getEnvironment() == Environment.THE_END && (_waystone.GetLoc().getWorld().getEnvironment() == Environment.NORMAL || _waystone.GetLoc().getWorld().getEnvironment() == Environment.NETHER) && !panel.get_dimension().IsEndUnlocked())
		{
			return;
		}
		
		if(_waystone.GetLoc().getWorld().getEnvironment() == Environment.THE_END && (ws.GetLoc().getWorld().getEnvironment() == Environment.NORMAL || ws.GetLoc().getWorld().getEnvironment() == Environment.NETHER) && !panel.get_dimension().IsEndUnlocked())
		{
			return;
		}
		
		//if(!IsAbleToShow(ws)) return;
		_discoveredWaystones.add(ws);
	}
	
	enum BUTTON implements IButton
	{
		NONE,
		GO_LEFT,
		GO_RIGHT,
		UPGRADE,
		WAYSTONE,
		REMOVE,
		VISIBILITY_TYPE,
		UNBREAKABLE,
		SET_ENABLE,
		MAX_OUT,
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
	public void invClosed(InventoryCloseEvent e) 
	{
		if(_saveAfterClose)
		{
			_wManager.SaveWaystone(_wManager.GetWaystone(_waystone.GetUUID()), true);
		}
		ImusWaystones._instance.GetWaystoneManager().UnRegisterInv(_waystone, this);
	}

	@Override
	public void onClickInsideInv(InventoryClickEvent e) 
	{
		BUTTON button = GetButtonPress(e);
		if(!CheckIfValid()) return;
		
		switch (button) 
		{
		case NONE:
			break;
		case GO_LEFT:
			_page = PageChance(_page, -1, _discoveredWaystones.size(), _size-9);
			break;
		case GO_RIGHT:
			_page = PageChance(_page, 1, _discoveredWaystones.size(), _size-9);
			break;		
		case UPGRADE:
			_player.closeInventory();
			new WaystoneUpgradeMenu(_main, _player, _waystone).openThis();
			break;
		case WAYSTONE:
			Teleporting(e.getCurrentItem());			
			_player.closeInventory();
			break;
		case REMOVE:
			new BukkitRunnable() {
				
				@Override
				public void run()
				{
					_wManager.RemoveWaystone(_waystone);
				}
			}.runTaskLater(_main, 5);
			
			_player.closeInventory();
			break;
		case VISIBILITY_TYPE:
			
			_saveAfterClose = true;
			_waystone.RollVisibilityType();
			setupButtons();
			break;
		case UNBREAKABLE:
			_saveAfterClose = true;
			_waystone.SetUnbreakable(!_waystone.IsUnbreakable());
			setupButtons();
			break;
		case MAX_OUT:
			_saveAfterClose = true;
			_waystone.SetMaxOut(!_waystone.IsMaxOut());
			setupButtons();
			break;
		case SET_ENABLE:
			_saveAfterClose = true;
			_waystone.SetEnable(!_waystone.IsEnable());
			setupButtons();
			break;
		}
	}
	
	boolean CheckIfValid()
	{
		if(!_wManager.IsValid(_wManager.GetWaystone(_waystone.GetUUID())))
		{
			_player.sendMessage(Metods.msgC("&cThe waystone isn't valid!"));
			_wManager.RemoveWaystone(_waystone.GetUUID());
			_player.closeInventory();
			return false;
		}
		return true;
	}
	
	private void Teleporting(ItemStack currentItem) 
	{
		UUID uuid_ws = UUID.fromString(Metods._ins.getPersistenData(currentItem, "iwm", PersistentDataType.STRING));
		Waystone ws = _wManager.GetWaystone(uuid_ws);
		
		if(ws == null)
		{
			_player.sendMessage(Metods.msgC("&cTarget waystone doesn't exist!"));
			return;
		}
		
		if(!_wManager.IsValid(ws))
		{
			_player.sendMessage(Metods.msgC("&3Target Waystone &4isn't Valid!"));
			return;
		}
		
		if(!_waystone.HasEnoughExpToTeleport(_player))
		{
			_player.sendMessage(Metods.msgC("&3You don't have enough &2xp &3to teleport"));
			return;
		}
		
		_waystone.StartTeleporting(_player, ws);
	
	}

	@Override
	public void openThis() {
		super.openThis();
		ImusWaystones._instance.GetWaystoneManager().RegisterInv(_waystone, this);
		if(!CheckIfValid())
		{
			new BukkitRunnable() {
				
				@Override
				public void run() 
				{
					_player.closeInventory();
					_wManager.RemoveWaystone(_waystone);					
				}
			}.runTaskLater(_main, 1);
			return;
		}
		
		
		setupButtons();
		LoadWaystones();
	}
	
	@Override
	public void setupButtons() 
	{
		for(int i = _size-9; i < _size; i++) {setupButton(BUTTON.NONE, Material.CYAN_STAINED_GLASS_PANE, " ", i);}
		setupButton(BUTTON.GO_LEFT, Material.BIRCH_SIGN, "&b<<", _size-9);
		setupButton(BUTTON.GO_RIGHT, Material.BIRCH_SIGN, "&b>>", _size-1);
		
		setupButton(BUTTON.UPGRADE, Material.BIRCH_BOAT, "&eUPGRADE", _size-5);
		
		if(_player.getGameMode() == GameMode.CREATIVE)
		{
			 setupButton(BUTTON.REMOVE, Material.LAVA_BUCKET, "&eRemove This Waystone", _size-3);
			 
			 ItemStack stack = new ItemStack(Material.SPYGLASS);
			 Metods.setDisplayName(stack, "&eModify Visibility");
			 ArrayList<String> lores = new ArrayList<String>();
			 lores.add(" ");
			 lores.add("&9Current: &2"+_waystone.GetVisibilityType().toString());
			 _metods.addLore(stack, lores);
			 
			 SetButton(stack, BUTTON.VISIBILITY_TYPE);
			 SetITEM(_size-7, stack);
			 
			 stack = new ItemStack(Material.BEDROCK);
			 Metods.setDisplayName(stack, "&eUnbreakable");
			 lores = new ArrayList<String>();
			 lores.add(" ");
			 lores.add("&9Current: &2"+_waystone.IsUnbreakable());
			 _metods.addLore(stack, lores);
			 
			 SetButton(stack, BUTTON.UNBREAKABLE);
			 SetITEM(_size-8, stack);
			 
			 stack = new ItemStack(Material.NETHER_STAR);
			 Metods.setDisplayName(stack, "&eMax out");
			 lores = new ArrayList<String>();
			 lores.add(" ");
			 lores.add("&9Current: &2"+_waystone.IsMaxOut());
			 _metods.addLore(stack, lores);
			 
			 SetButton(stack, BUTTON.MAX_OUT);
			 SetITEM(_size-6, stack);
			 
			 stack = new ItemStack(Material.BARRIER);
			 Metods.setDisplayName(stack, "&eEnable");
			 lores = new ArrayList<String>();
			 lores.add(" ");
			 lores.add("&9Current: &2"+_waystone.IsEnable());
			 _metods.addLore(stack, lores);
			 
			 SetButton(stack, BUTTON.SET_ENABLE);
			 SetITEM(_size-2, stack);
		}
		
		LoadWaystonesAsync();
	}
	
	Waystone GetWaystoneFromButton(ItemStack stack)
	{
		return _wManager.GetWaystone(UUID.fromString(Metods._ins.getPersistenData(stack, "iwm" ,PersistentDataType.STRING)));
	}
	
	void LoadWaystonesAsync()
	{
		new BukkitRunnable() {
			
			@Override
			public void run() {
				for(int i = 0; i < _size-9;i++)
				{
					int idx = i + ( _page * (_size-9));
					if(idx > _discoveredWaystones.size()-1) {_inv.setItem(i, new ItemStack(Material.BLUE_STAINED_GLASS_PANE));continue;}
					
					Waystone ws = _discoveredWaystones.get(idx);

					ItemStack stack = ws.GetDisplayItem().clone();
					Metods.setDisplayName(stack, ws.GetName());
					Metods._ins.SetLores(stack, new String[] {"&7(&eClick&7)&2 to start &bTeleporting"}, false);
					Metods._ins.setPersistenData(stack, "iwm", PersistentDataType.STRING, ws.GetUUID().toString());
					SetButton(stack, BUTTON.WAYSTONE);
					_inv.setItem(i, stack);
				}
			}
		}.runTaskAsynchronously(_main);
		
	}
	
	boolean IsAbleToShow(Waystone waystone)
	{
		return true;
	}
	
	

}
