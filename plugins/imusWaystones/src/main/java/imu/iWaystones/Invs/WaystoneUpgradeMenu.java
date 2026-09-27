package imu.iWaystones.Invs;

import java.util.ArrayList;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iAPI.Interfaces.IButton;
import imu.iAPI.InvUtil.InventoryReaderStack;
import imu.iAPI.Other.CustomInvLayout;
import imu.iAPI.Other.Metods;
import imu.iAPI.Utilities.ItemUtils;
import imu.iWaystone.Interfaces.IModDataInv;
import imu.iWaystone.Interfaces.IModDataValues;
import imu.iWaystone.Upgrades.BaseUpgrade;
import imu.iWaystone.Upgrades.PlayerUpgradePanel;
import imu.iWaystone.Upgrades.UpgradeBottomBuild;
import imu.iWaystone.Upgrades.UpgradeCastTime;
import imu.iWaystone.Upgrades.UpgradeCooldown;
import imu.iWaystone.Upgrades.UpgradeDimension;
import imu.iWaystone.Upgrades.UpgradeFoundation;
import imu.iWaystone.Upgrades.UpgradeXPusage;
import imu.iWaystone.Waystones.Waystone;
import imu.iWaystones.Enums.ConvUpgradeModData;
import imu.iWaystones.Enums.UpgradeType;
import imu.iWaystones.Main.ImusWaystones;
import imu.iWaystones.Managers.WaystoneManager;
import imu.iWaystones.Other.ConvUpgrade;

public class WaystoneUpgradeMenu extends CustomInvLayout implements IModDataInv
{
	private ImusWaystones _main;
	private Waystone _waystone;
	private WaystoneManager _wManager;

	PlayerUpgradePanel _panel;
	boolean upgrading = false;
	private String _namePrefix = "&5Upgrading: &r";
	public WaystoneUpgradeMenu(Plugin main, Player player, Waystone waystone) 
	{
		super(main, player,"&5Upgrading: &r"+waystone.GetName(), 4 * 9);
		
		_main = ImusWaystones._instance;
		_waystone = waystone;
		_wManager = _main.GetWaystoneManager();
	}

	enum BUTTON implements IButton
	{
		NONE,
		UPGRADE,
		BACK,
		SET_NAME,
		EXTRACT_UPGRADES,
	}
//	protected void Rename()
//	{
//		ProtocolManager 
//	}
	void LoadUpgradePanel()
	{
		_panel = _waystone.GetPlayerUpgradePanel(_player.getUniqueId());
		_panel.LoadToolTips();
		SetUpgradesAsync();
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
		ImusWaystones._instance.GetWaystoneManager().UnRegisterInv(_waystone, this);
		if(!CheckIfValid()) return;
	}
	
	@Override
	public void SetModData(IModDataValues modValue, String value) 
	{
		ConvUpgradeModData v = (ConvUpgradeModData)modValue;

		switch (v) 
		{
		case RENAME:

			_player.sendMessage(Metods.msgC("&3Waystone name changed to &r"+value));
			
			_wManager.GetWaystone(_waystone.GetUUID()).SetName(value);
			_wManager.SaveWaystone(_wManager.GetWaystone(_waystone.GetUUID()), true);			
			RenameWindow(_namePrefix+value);

			break;
	
		}
	}
	
	@Override
	public void openThis() 
	{
		super.openThis();
		ImusWaystones._instance.GetWaystoneManager().RegisterInv(_waystone, this);
		setupButtons();
		
		
	}
	@Override
	public void closeThis()
	{
		_player.closeInventory();
	}
	
	
	@Override
	public void onClickInsideInv(InventoryClickEvent e) {
		BUTTON button = GetButtonPress(e);
		switch (button) 
		{
		case BACK:
			if(!CheckIfValid()) return;
			_player.closeInventory();
			new WaystoneMenuInv(_wManager.GetWaystone(_waystone.GetUUID()), _player).openThis();
			break;
		case NONE:
			break;
		case UPGRADE:
			if(!CheckIfValid()) return;
			if(upgrading) return;
			UpgradeAsync(_panel.GetUpgrade(UpgradeType.valueOf(Metods._ins.getPersistenData(e.getCurrentItem(),"upgrade" , PersistentDataType.STRING))), e.getClick());			
			break;
		case SET_NAME:
			if(!CheckIfValid()) return;
			_player.closeInventory();
			Metods._ins.ConversationWithPlayer(_player, new ConvUpgrade(ConvUpgradeModData.RENAME, this, "&3Give waystone &2new &3name?"));
			break;
		case EXTRACT_UPGRADES:
			if(e.getClick() != ClickType.SHIFT_RIGHT) return;
			
			if(!_panel.HasUpgrade())
			{
				_player.sendMessage(Metods.msgC("&cThere isn't any upgrades to extract!"));
				return;
			}
			Metods._ins.InventoryAddItemOrDrop(_panel.GetUpgradeItem(), _player);
			_player.sendMessage(Metods.msgC("&5Upgrades has been extracted"));
			_panel.ResetUpgrades();
			_panel.SaveUpgradesToDatabase(_player.getUniqueId());
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
	@Override
	public void setupButtons() {
		
		for(int i = 0; i < _size; i++) {setupButton(BUTTON.NONE, Material.BLACK_STAINED_GLASS_PANE, _waystone.IsMaxOut() ? "&5MAX OUT!" : " ", i);}

		setupButton(BUTTON.BACK, Material.RED_WOOL, "&bBACK", _size-9);
		
		if(!_waystone.IsMaxOut())
		{
			ItemStack stack = new ItemStack(Material.GRINDSTONE);
			Metods.setDisplayName(stack, "&bExtract Upgrades");
			ArrayList<String> lores = new ArrayList<>();
			lores.add("&ePress &5Shift &bM2 &eto Extract");
			lores.add(" ");
			lores.add("&9Moves Upgrades to Item");
			lores.add("&9Which can be inserted other waystone");
			lores.add("&9to apply items upgrades");
			Metods._ins.addLore(stack, lores);
			SetButton(stack, BUTTON.EXTRACT_UPGRADES);
			SetITEM(_size-5, stack);
		}

		if(_waystone.GetOwnerUUID().equals(_player.getUniqueId())|| _player.isOp()) setupButton(BUTTON.SET_NAME, Material.NAME_TAG, "&2Rename Waystone", _size-6);
		
		//setupButton(BUTTON.UPGRADE_CASTTIME, Material.LAPIS_BLOCK, "&bCast Time", 1);
		LoadUpgradePanel();
		SetUpgradesAsync();
		
	}
	
	void SetUpgradesAsync()
	{
		if(_waystone.IsMaxOut()) return;
		
		String forceStr = "&4===> &e&k#&7(&eSHIFT &bM2&7)&e&k# &eto &6FORCE &5UPGRADE!";
		ItemStack stack = null;
		
		if(new UpgradeCastTime().IsEnabled())
		{
			stack = SetButton(_panel.get_castTime()._displayItem.clone(), BUTTON.UPGRADE);
			Metods._ins.setPersistenData(stack, "upgrade", PersistentDataType.STRING, UpgradeType.CAST_TIME.toString());
			Metods._ins.addLore(stack, " ", false);
			Metods._ins.addLore(stack, "&9Cast time: &1"+_waystone.GetValue(_panel.get_castTime()), false);
			if(_player.getGameMode() == GameMode.CREATIVE) Metods._ins.addLore(stack, forceStr, true);
			_inv.setItem(1, stack);
		}
		else
		{
			stack = new ItemStack(Material.BARRIER);
			ItemUtils.SetDisplayName(stack, "&9Disabled");
			SetButton(stack, BUTTON.NONE);
			_inv.setItem(1,stack);
		}
		
		
		if(new UpgradeXPusage().IsEnabled())
		{
			stack = SetButton(_panel.get_xpUsage()._displayItem.clone(), BUTTON.UPGRADE);
			Metods._ins.setPersistenData(stack, "upgrade", PersistentDataType.STRING, UpgradeType.XP_USAGE.toString());
			Metods._ins.addLore(stack, " ", false);
			Metods._ins.addLore(stack, "&9Xp usage: &1"+_waystone.GetValue(_panel.get_xpUsage()), false);
			if(_player.getGameMode() == GameMode.CREATIVE) Metods._ins.addLore(stack, forceStr, true);
			_inv.setItem(3,stack);
		}
		else
		{
			stack = new ItemStack(Material.BARRIER);
			ItemUtils.SetDisplayName(stack, "&9Disabled");
			SetButton(stack, BUTTON.NONE);
			_inv.setItem(3,stack);
		}
		
		if(new UpgradeDimension().IsEnabled())
		{
			stack = SetButton(_panel.get_dimension()._displayItem.clone(), BUTTON.UPGRADE);
			Metods._ins.setPersistenData(stack, "upgrade", PersistentDataType.STRING, UpgradeType.DIMENSION.toString());
			Metods._ins.addLore(stack, " ", false);
			if(_player.getGameMode() == GameMode.CREATIVE) Metods._ins.addLore(stack, forceStr, true);
			_inv.setItem(5,stack);
		}
		else
		{
			stack = new ItemStack(Material.BARRIER);
			ItemUtils.SetDisplayName(stack, "&9Disabled");
			SetButton(stack, BUTTON.NONE);
			_inv.setItem(5,stack);
		}
		
		if(new UpgradeCooldown().IsEnabled())
		{
			stack = _panel.get_cooldown()._displayItem.clone();
			Metods._ins.setPersistenData(stack, "upgrade", PersistentDataType.STRING, UpgradeType.COOLDOWN.toString());
			Metods._ins.addLore(stack, " ", false);
			Metods._ins.addLore(stack, "&6CD With reduction: "+Metods.FormatTime((long)(_panel.GetCooldown() * 1000)), false);
			Metods._ins.addLore(stack, "&3Base Cooldown: "+Metods.FormatTime((long)(_panel.get_foundation().GetTier()._value * 1000)), false);
			if(_player.getGameMode() == GameMode.CREATIVE) Metods._ins.addLore(stack, forceStr, true);
			_inv.setItem(7,SetButton(stack, BUTTON.UPGRADE));
			
		}else
		{
			stack = new ItemStack(Material.BARRIER);
			ItemUtils.SetDisplayName(stack, "&9Disabled");
			SetButton(stack, BUTTON.NONE);
			_inv.setItem(7,stack);
		}
		
		if(new UpgradeFoundation().IsEnabled())
		{
			stack = _panel.get_foundation()._displayItem.clone();
			Metods._ins.setPersistenData(stack, "upgrade", PersistentDataType.STRING, UpgradeType.FOUNDATION.toString());
			Metods._ins.addLore(stack, " ", false);
			//Metods._ins.addLore(stack, "&9Base foundation: &1"+_panel.get_foundation().GetCombinedValue(0), false);
			if(_player.getGameMode() == GameMode.CREATIVE) Metods._ins.addLore(stack, forceStr, true);
			_inv.setItem(_size-4,SetButton(stack, BUTTON.UPGRADE));
		}
		else
		{
			stack = new ItemStack(Material.BARRIER);
			ItemUtils.SetDisplayName(stack, "&9Disabled");
			SetButton(stack, BUTTON.NONE);
			_inv.setItem(_size-4,stack);
		}
		

		
	}
	
	void UpgradeAsync(BaseUpgrade upgrade, ClickType clickType)
	{

		if(upgrading || upgrade.IsMaxTier()) return;
		
		upgrading = true;
		new BukkitRunnable() 
		{		
			
			boolean CheckItems()
			{
				InventoryReaderStack invReader = new InventoryReaderStack(_player.getInventory());
				
				for(ItemStack stack : upgrade.GetCost())
				{
					if(!invReader.HasEnough(stack, stack.getAmount()))
					{
						return false;
					}
				}
				
				for(ItemStack stack : upgrade.GetCost())
				{
					invReader.Reduce(stack, stack.getAmount());
				}
				
				return true;
			}
			@Override
			public void run() 
			{
				if(CheckItems() || (clickType == ClickType.SHIFT_RIGHT && _player.getGameMode() == GameMode.CREATIVE))
				{
					
					
					
					if(!(upgrade instanceof UpgradeBottomBuild))
					{
						upgrade.ButtonPressUpgradeTier(_player, _waystone, upgrade.GetCurrentTier());
						upgrade.IncreaseCurrentTier(1);
						upgrade.Tooltip();
						_wManager.GetWaystoneManagerSQL().SaveUpgradeAsync(_player.getUniqueId(), _waystone.GetUUID(), new BaseUpgrade[] {upgrade} );
						
					}else
					{
						_waystone.GetUpgradeBottomUpgrade();
						upgrade.ButtonPressUpgradeTier(_player, _waystone, upgrade.GetCurrentTier());

					}
					
					SetUpgradesAsync();					
				}

				upgrading = false;
			}
		}.runTaskAsynchronously(_main);
	}

	

	

	

}
