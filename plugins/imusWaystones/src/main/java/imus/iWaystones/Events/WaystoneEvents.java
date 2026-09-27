package imus.iWaystones.Events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;
import imu.iWaystone.Waystones.Waystone;
import imu.iWaystones.Enums.VISIBILITY_TYPE;
import imu.iWaystones.Main.ImusWaystones;
import imu.iWaystones.Managers.WaystoneManager;

public class WaystoneEvents implements Listener
{
	ImusWaystones _main = ImusWaystones._instance;
	WaystoneManager _waystoneManager;
	Cooldowns _cds = new Cooldowns();

	public WaystoneEvents()
	{
		ImusWaystones._instance.getServer().getPluginManager().registerEvents(this, ImusWaystones._instance);
		_waystoneManager = ImusWaystones._instance.GetWaystoneManager();
	}

	@EventHandler
	public void OnBlockPlace(BlockPlaceEvent e)
	{
		Waystone wStone = _waystoneManager.TryToCreateWaystone(e.getBlock());
		if (wStone == null)
			return;

		wStone.SetOwner(e.getPlayer());

		HashMap<String, String> cmds = new HashMap<String, String>();
		cmds.put("( CONFIRM )",
				"/iw confirm " + wStone.GetLoc().getWorld().getName() + " " + wStone.GetLoc().toVector());
		// ImusAPI._metods.SendMessageCommands(e.getPlayer(), cmds, " // ");
		ImusAPI._metods.SendMessageCommands(e.getPlayer(), "Waystone creation started! ", cmds, "", "");
		_waystoneManager.SetPlayerConfirmation(e.getPlayer().getUniqueId(), wStone);
	}

	@EventHandler
	public void OnBlockInteract(PlayerInteractEvent e)
	{
		if (e.getClickedBlock() != null)
		{
			// System.out.println("interact: "+ e.getClickedBlock().getType() + " loc:
			// "+e.getClickedBlock().getLocation().toVector());
		}

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK || !_waystoneManager.IsWaystone(e.getClickedBlock()))
			return;

		e.setCancelled(true);

		if (_waystoneManager.IsTeleporting(e.getPlayer()))
		{
			return;
		}

		if (!_cds.isCooldownReady("click"))
			return;

		_cds.setCooldownInSeconds("click", 0.5f);

		Waystone ws = _waystoneManager.GetWaystone(e.getClickedBlock());

		if (!_waystoneManager.IsValid(ws))
		{
			_waystoneManager.RemoveWaystone(ws);
			return;
		}

		if (!_waystoneManager.HasDiscovered(e.getPlayer(), ws))
		{
			_waystoneManager.AddDiscovered(e.getPlayer().getUniqueId(), ws.GetUUID(), true);

			if (ws.GetVisibilityType() == VISIBILITY_TYPE.BY_TOUCH)
			{
				e.getPlayer().sendMessage(Metods.msgC("&3You have discovered new waystone named as " + ws.GetName()));
				return;
			}

		}

		if (ImusAPI._instance.GetOpenedInv(e.getPlayer()) != null)
			return;

		ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
		if (stack != null && _waystoneManager.IsUpgradeItemStack(stack))
		{
			ws.GetPlayerUpgradePanel(e.getPlayer().getUniqueId()).InsertUpgradeItem(stack);
			stack.setAmount(stack.getAmount() - 1);
			e.getPlayer().sendMessage(Metods.msgC("&6Upgrade data has been &5applied"));
			ws.GetPlayerUpgradePanel(e.getPlayer().getUniqueId()).SaveUpgradesToDatabase(e.getPlayer().getUniqueId());
			return;
		}
		_waystoneManager.OpenWaystone(e.getPlayer(), ws);

	}

	// since there are no changes to the event itself we can use monitor to be
	// certain if the event was cancelled
	@EventHandler(priority = EventPriority.MONITOR)
	public void OnWaystoneBreak(BlockBreakEvent e)
	{
		if (e.isCancelled())
			return;

		if (!_waystoneManager.IsWaystone(e.getBlock()))
			return;

		if (e.getPlayer().getGameMode() == GameMode.CREATIVE)
		{
			e.getPlayer().sendMessage(ChatColor.DARK_RED + "You can't break the waystone on Creative Mode!");
			e.setCancelled(true);
			return;
		}
		Waystone ws = _waystoneManager.GetWaystone(e.getBlock());

		if (ws.IsUnbreakable())
		{
			e.getPlayer().sendMessage(ChatColor.DARK_RED + "You can't break unbreakble waystone!");
			e.setCancelled(true);
			return;
		}

		destroyWaystone(e.getPlayer(), ws);
	}

	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event)
	{
		for (Block block : event.blockList())
		{
			if (_waystoneManager.IsWaystone(block))
			{
				Waystone ws = _waystoneManager.GetWaystone(block);
				if (ws != null && ws.IsUnbreakable())
				{
					event.blockList().remove(block);
					continue;
				}
				
				destroyWaystone(null, ws);
			}
		}
	}
	
	private void destroyWaystone(Player player, Waystone ws)
	{
		ws.SendMessageToOwner("&3Someone has &cDestroyed &3your &ewaystone &3named as " + ws.GetName());
		
		if(player == null)
		{
			Bukkit.getLogger().info(
					"WS: exploed " + ws.GetOwnerName() + "'s waystone named " + ws.GetName());
			_waystoneManager.RemoveWaystone(ws);
			return;
		}
		
		Bukkit.getLogger().info(
				"WS: " + player.getName() + " broke " + ws.GetOwnerName() + "'s waystone named " + ws.GetName());
		_waystoneManager.RemoveWaystone(ws);
		player.sendMessage(Metods.msgC("&3Waystone has been &cDestroyed"));
		player.playSound(ws.GetLoc(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.1f, 0.1f);
	}

}
