package imu.DontLoseItems.CustomEnd;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

import javax.swing.text.Utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import imu.DontLoseItems.CustomEnd.EndCustomEvents.EndEvent;
import imu.DontLoseItems.CustomEnd.EndCustomEvents.EndEvent_EndStoneToDeprisScrap;
import imu.DontLoseItems.CustomEnd.EndCustomEvents.EndEvent_EndStoneToDeprisScrapOrSilverFish;
import imu.DontLoseItems.CustomEnd.EndCustomEvents.EndEvent_EndStoneToDiamond;
import imu.DontLoseItems.CustomEnd.EndCustomEvents.EndEvent_EndStoneToDiamondOrSilverFish;
import imu.DontLoseItems.CustomEnd.EndCustomEvents.EndEvent_PlayergroundVanish;
import imu.DontLoseItems.CustomEnd.EndCustomEvents.EndEvent_PlayergroundVanishHard;
import imu.DontLoseItems.CustomEnd.EndCustomEvents.EndEvent_RandomEntityTypeEnderman;
import imu.DontLoseItems.CustomEnd.EndCustomEvents.EndEvent_RandomPotionEffect;
import imu.DontLoseItems.CustomEnd.EndCustomEvents.EndEvent_SpecialCreepers;
import imu.DontLoseItems.CustomEnd.EndCustomEvents.EndEvent_TntEverywhere;
import imu.DontLoseItems.CustomEnd.EndCustomEvents.EndEvent_TntWalk;
import imu.DontLoseItems.CustomEnd.EndCustomEvents.EndEvent_UnstableGround;
import imu.DontLoseItems.main.DontLoseItems;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;
import imu.iAPI.Other.Tuple;
import imu.iAPI.Utilities.ImusUtilities;

public class UnstableEnd implements Listener
{
	public static UnstableEnd Instance;
	private double _state = 0;
	private final double MAX_STATE = 1000;
	private final int START_DELAY_EVENT = 20 * 10;
	public BossBar BOSS_BAR;
	private LinkedList<EndEvent> _allEvents;
	private LinkedList<EndEvent> _activeEvents;

	public int _totalRolls = 3;

	private Cooldowns _cds;

	private final String EVENT_ID = "EventID";

	private boolean isEventActive = false;

	private HashMap<UUID, Integer> _chestLootBases = new HashMap<>();
	private int _totalTicks = 0;

	public UnstableEnd()
	{
		Instance = this;
		Bukkit.getServer().getPluginManager().registerEvents(this, DontLoseItems.Instance);
		_cds = new Cooldowns();
		BOSS_BAR = Bukkit.createBossBar("Unstable Void", BarColor.PURPLE, BarStyle.SEGMENTED_20, BarFlag.DARKEN_SKY);
		BOSS_BAR.setTitle(ChatColor.BLACK + "| " + ChatColor.DARK_PURPLE + "Unstable Void" + ChatColor.BLACK + " |");
		InitEvents();
		InitLoopTimer();

	}

	public void InitEvents()
	{
		_allEvents = new LinkedList<>();
		_activeEvents = new LinkedList<>();

		_allEvents.add(new EndEvent_RandomEntityTypeEnderman());
		_allEvents.add(new EndEvent_EndStoneToDiamond());
		_allEvents.add(new EndEvent_EndStoneToDeprisScrap());
		_allEvents.add(new EndEvent_PlayergroundVanish());
		_allEvents.add(new EndEvent_UnstableGround());
		_allEvents.add(new EndEvent_RandomPotionEffect());
		_allEvents.add(new EndEvent_TntEverywhere());
		_allEvents.add(new EndEvent_SpecialCreepers());
		_allEvents.add(new EndEvent_EndStoneToDeprisScrapOrSilverFish());
		_allEvents.add(new EndEvent_EndStoneToDiamondOrSilverFish());
		_allEvents.add(new EndEvent_PlayergroundVanishHard());
		_allEvents.add(new EndEvent_TntWalk());

	}

	public void OnDisabled()
	{
		if (GetCurrentEvent() != null)
			GetCurrentEvent().OnEventEnd();

		BOSS_BAR.removeAll();
		_chestLootBases.clear();
		_allEvents.clear();
		_activeEvents.clear();
		// HandlerList.unregisterAll(this);
	}

	public void RefreshPorgress()
	{
		BOSS_BAR.setProgress(_state * 0.001);
	}

	public void SetState(double amount)
	{
		_state = amount;
		if (_state < 0)
			_state = 0;
		if (_state > MAX_STATE)
			_state = MAX_STATE;
	}

	public int GetPlayerBaseRollAmount(Player player)
	{
		// System.out.println("base is: "+_chestLootBases.get(player.getUniqueId()));
		if (_chestLootBases.containsKey(player.getUniqueId()))
			return _chestLootBases.get(player.getUniqueId());
		return 0;
	}

	public void AddPlayerChestlootBase(Player player, int amount)
	{
		int value = 0;
		if (_chestLootBases.containsKey(player.getUniqueId()))
		{
			value = _chestLootBases.get(player.getUniqueId());
		}

		if ((value + amount) < 0)
		{
			_chestLootBases.remove(player.getUniqueId());
			return;
		}
		_chestLootBases.put(player.getUniqueId(), value + amount);
	}

	public void RemovePlayerChestLootBase(Player player)
	{
		_chestLootBases.remove(player.getUniqueId());
	}

	public boolean IsMax()
	{
		return _state >= MAX_STATE;
	}

	public double GetStateAmount()
	{
		return _state;
	}

	public void AddState(double amount)
	{
		SetState(GetStateAmount() + amount);
	}

	public void AddState(UnstableIncrease increase)
	{
		SetState(GetStateAmount() + increase.Amount);
	}

	public void OnLoop()
	{

		if (_activeEvents.size() <= 0)
			return;

		GetCurrentEvent().OnOneTickLoop();

		if (_totalTicks % 20 == 0)
			CheckPlayers();

		OnEventChange();

		_totalTicks++;
	}

	private void CheckPlayers()
	{
		EndEvent event = GetCurrentEvent();
		if (event == null)
			return;

		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (EndEvents.Instance.IsPlayerUnstableArea(player))
			{
				if (GetCurrentEvent().HasPlayer(player))
					continue;

				// joined some where ot event
				event.AddPlayer(player);
				event.OnPlayerJoinMiddleOfEvent(player);
				continue;
			}

			if (!event.HasPlayer(player))
				return;

			event.RemovePlayer(player);
			GetCurrentEvent().OnPlayerLeftMiddleOfEvent(player);
		}
	}

	@EventHandler
	public void OnPlayerQuit(PlayerQuitEvent e)
	{
		if (GetCurrentEvent() == null)
			return;

		if (!GetCurrentEvent().HasPlayer(e.getPlayer()))
			return;

		GetCurrentEvent().RemovePlayer(e.getPlayer());
		GetCurrentEvent().OnPlayerLeftMiddleOfEvent(e.getPlayer());
	}


	private LinkedList<Tuple<Block, Integer>> _placedBlocked = new LinkedList<>();
	private Material[] _placedMaterialOrder = { Material.COPPER_BLOCK, Material.IRON_BLOCK, Material.GOLD_BLOCK,
			Material.DIAMOND_BLOCK, Material.BEDROCK,

	};

	@EventHandler
	public void OnBlockPlace(BlockPlaceEvent e)
	{
		if (e.isCancelled())
			return;
		
		if(!DontLoseItems.IsEnd(e.getPlayer())) return;
		
		if(e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		
		if(!IsEventRuning()) return;
		
		if (!GetCurrentEvent().HasPlayer(e.getPlayer()))
			return;
		
		Block block = e.getBlockPlaced();
		Player player = e.getPlayer();

		if (block.getType().isSolid())
		{
			if (block.getType().isInteractable())
			{
				player.sendMessage(Metods.msgC("&cUnable &9to place &6inventory &9item in &5Event"));
				e.setCancelled(true);
				
			} 
			else
			{
				_placedBlocked.addLast(new Tuple<>(block, _placedMaterialOrder.length - 1));
			}
		}

	
	}

	@EventHandler
	public void OnBlockBreak(BlockBreakEvent e)
	{
		if (e.isCancelled())
			return;
		
		if(!DontLoseItems.IsEnd(e.getPlayer())) return;
		
		if(!IsEventRuning()) return;
		
		if (!GetCurrentEvent().HasPlayer(e.getPlayer()))
			return;
		
		
		
		Block brokenBlock = e.getBlock();

		Iterator<Tuple<Block, Integer>> iterator = _placedBlocked.iterator();
		while (iterator.hasNext())
		{
			Tuple<Block, Integer> entry = iterator.next();
			Block block = entry.GetKey();

			if (block.equals(brokenBlock))
			{
				iterator.remove();
				break;
			}
		}
	}

	private void InitLoopTimer()
	{
		new BukkitRunnable() {
			@Override
			public void run()
			{
				Iterator<Tuple<Block, Integer>> iterator = _placedBlocked.iterator();

				while (iterator.hasNext())
				{
					Tuple<Block, Integer> entry = iterator.next();
					Block block = entry.GetKey();
					int state = entry.GetValue();

					if (state < 0)
					{
						block.breakNaturally();
						iterator.remove();
					} 
					else
					{
						Material nextMaterial = _placedMaterialOrder[state];
						for (Player player : Bukkit.getOnlinePlayers())
						{
							if(!DontLoseItems.IsEnd(player)) continue;
							
							if (player.getWorld().equals(block.getWorld())
									&& player.getLocation().distance(block.getLocation()) < 30)
							{
								ImusUtilities.SetFakeBlock(player, nextMaterial, block.getLocation());
							}
						}
						entry.SetValue(state - 1);
					}
				}
			}
		}.runTaskTimer(DontLoseItems.Instance, 20L, 20L); // Runs every second (20 ticks)

	}

	@EventHandler
	public void OnPlayerJoin(PlayerJoinEvent e)
	{

		if (GetCurrentEvent() == null)
			return;

		if (!EndEvents.Instance.IsPlayerUnstableArea(e.getPlayer()))
			return;

		if (GetCurrentEvent().HasPlayer(e.getPlayer()))
			return;

		// GetCurrentEvent().AddPlayer(e.getPlayer());
		GetCurrentEvent().OnPlayerJoinMiddleOfEvent(e.getPlayer());
	}

	private void AllTimeEventsHasBeenEnded()
	{
		isEventActive = false;
		SetState(0);
		RefreshPorgress();
		_totalTicks = 0;
	}

	public void OnEventChange()
	{
		if (!_cds.isCooldownReady(EVENT_ID))
			return;

		OnEventDeActive(_activeEvents.removeFirst());

		if (_activeEvents.size() <= 0)
		{
			AllTimeEventsHasBeenEnded();
			return;
		}
		OnTriggerEvent(_activeEvents.getFirst());

	}

	private void SetCooldown(EndEvent event)
	{
		_cds.setCooldownInSeconds(EVENT_ID, event.GetDuration());
	}

	private void OnEventDeActive(EndEvent event)
	{
		event.OnEventEnd();
		event.ClearPlayers();
		event.UnRegisterBukkitEvents();

	}

	public boolean IsEventRuning()
	{
		return GetCurrentEvent() != null;
	}

	public EndEvent GetCurrentEvent()
	{
		if (_activeEvents.size() == 0)
			return null;

		return _activeEvents.getFirst();
	}

	private void OnTriggerEvent(EndEvent event)
	{
		SetCooldown(event);

		isEventActive = true;
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (!EndEvents.Instance.IsPlayerUnstableArea(player))
				continue;

			event.AddPlayer(player);
			// FIXME tests
			// event.PrintToPlayer(player);
			event.TitleToPlayer(player);
		}
		event.OnEventStart();
		event.RegisterBukkitEvents();

	}

	public void OnTrigger()
	{
		if (isEventActive)
			return;

		isEventActive = true;

		for (Player player : Bukkit.getServer().getOnlinePlayers())
		{
			if (!EndEvents.Instance.IsPlayerUnstableArea(player))
				continue;

			player.sendTitle(ChatColor.GREEN + "Event Starting in 10s!", ChatColor.YELLOW + "Prepare yourself...", 10,
					70, 20);
		}

		Bukkit.getScheduler().runTaskLater(DontLoseItems.Instance, new Runnable() {
			@Override
			public void run()
			{
				EndEvent[] events = _allEvents.toArray(new EndEvent[_allEvents.size()]);

				ImusUtilities.ShuffleArray(events);

				for (int i = 0; i < events.length; i++)
				{

					if (i >= _totalRolls)
						break;

					EndEvent event = events[i];
					_activeEvents.add(event);
				}

				if (_activeEvents.size() == 0)
				{
					AllTimeEventsHasBeenEnded();
					return;
				}

				OnTriggerEvent(_activeEvents.getFirst());
			}
		}, START_DELAY_EVENT);
		// OnTriggerEvent(_activeEvents.getFirst());

	}
}
