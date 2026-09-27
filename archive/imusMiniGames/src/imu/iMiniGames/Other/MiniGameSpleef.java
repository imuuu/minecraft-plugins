package imu.iMiniGames.Other;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;
import imu.iAPI.Other.Tuple;
import imu.iMiniGames.Arenas.SpleefArena;
import imu.iMiniGames.Handlers.SpleefGameHandler;
import imu.iMiniGames.Main.ImusMiniGames;

public class MiniGameSpleef extends MiniGame implements Listener
{
	SpleefGameHandler _spleefHandler;
	SpleefGameCard _gameCard;
	SpleefDataCard _dataCard;
	Cooldowns _cd;
	
	double _layer_y = 0;
	
	boolean has_started = false;
	boolean has_ended = false;
	
	int _start_delay = 3;
	int _total_players = 0;
	int _anti_stand = 0;  // if 0, its disabled
	

	int _roundEnd_warning = 10;
	HashMap<Player, Tuple<Location, Integer>> _player_anti_stands = new HashMap<>();
	ArrayList<Block> _remove_blocks = new ArrayList<>();
	
	int _best_of = 1;
	
	Location mid_loc = null;
	double _max_distance = 0;
	BukkitTask run;
	public MiniGameSpleef(ImusMiniGames main, SpleefGameHandler spleefHandler, SpleefGameCard gameCard,String minigameName) 
	{
		super(main,minigameName);
		
		_spleefHandler = spleefHandler;
		_gameCard = gameCard;
		_dataCard = (SpleefDataCard)gameCard.getDataCard();
		SpleefArena arena = ((SpleefArena) gameCard.get_arena());
		_layer_y =arena.getPlatformCorner(0).getY();
		_main.getServer().getPluginManager().registerEvents(this, main);
		_best_of = _dataCard.get_bestOfAmount();

		mid_loc =arena.getPlatformCorner(1).toVector().getMidpoint(arena.getPlatformCorner(0).toVector()).toLocation(arena.getPlatformCorner(0).getWorld());
		_max_distance = arena.getPlatformCorner(1).distance(arena.getPlatformCorner(0));
		_spectator_loc = gameCard.get_arena().get_spectator_lobby();
		
	}
	
	void putPotionEffects(Player p)
	{
		if(!_dataCard.get_invPotionEffects().isEmpty())
		{
			for(Entry<PotionEffectType, PotionEffect> potion : _dataCard.get_invPotionEffects().entrySet())
			{
				PotionEffect ef =new PotionEffect(potion.getKey(), _roundTime* 20, potion.getValue().getAmplifier());
				p.addPotionEffect(ef);
				
			}
		}
	}
	
	void setupPlayerForStart(Player p)
	{
		p.setGameMode(GameMode.SURVIVAL);
		PlayerInventory inv = p.getInventory();
		inv.clear();
		
		new BukkitRunnable() {
			
			@Override
			public void run()
			{
				p.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
				p.setHealth(20);	
			}
		}.runTaskLater(ImusMiniGames.Instance, 10);
		
		
		p.setFoodLevel(20);
		p.setFireTicks(0);
		_spleefHandler.removePotionEffects(p);
		
		putPotionEffects(p);
		
		ItemStack shovel = new ItemStack(Material.IRON_SHOVEL);
		Metods.setDisplayName(shovel, ChatColor.GOLD + "SPLEEF SHOVEL");
		shovel.addEnchantment(Enchantment.VANISHING_CURSE, 1);
		shovel.addEnchantment(Enchantment.DIG_SPEED, 5);
		shovel.addEnchantment(Enchantment.DURABILITY, 3);
		inv.addItem(shovel);
		inv.addItem(shovel);
		inv.addItem(shovel);
	}
	
	public int get_anti_stand() {
		return _anti_stand;
	}

	public void set_anti_stand(int _anti_stand) {
		this._anti_stand = _anti_stand;
	}
	
	Player checkBestOf()
	{
		addLobbyPlayersToScore();
		
		//ArrayList<Player> winners = new ArrayList<>();
		Player winner = null;
		for(Entry<Player,MiniGamePlayerStats> stats : _players_ingame.entrySet())
		{
			if(stats.getValue().get_score() >= _best_of)
			{
				//winners.add(p.getKey());
				winner = stats.getKey();
				break;
			}
		}
		
		return winner;
	}
	
	@Override
	public void endGame()
	{
		has_ended = true;
				
		Player round_winner;
		if(_total_players > 1 || _total_players == 0)
		{
			round_winner = null;
		}else
		{
			 round_winner = (Player)_players_ingame.keySet().toArray()[0];
			 addPointsPlayer(round_winner, 1);
		}
			
		if(_best_of != 1)
		{
			Player real_winner = checkBestOf();
			if(real_winner == null)
			{
				startGame();
				return;
			}
		}
		
		String msg = ChatColor.DARK_PURPLE +""+ChatColor.BOLD+ "Spleef Game Has Ended";	
		for(UUID uuid : _gameCard.get_players_accept().keySet())
		{
			Player p = Bukkit.getPlayer(uuid);
			
			if(!p.isOnline())
			{
				continue;
			}

			p.sendMessage(ChatColor.DARK_PURPLE + "=================================");
			p.sendMessage(msg);
			
			_spleefHandler.removePotionEffects(p);
			p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 5));
			
			if(round_winner == null )
			{
				p.sendMessage(ChatColor.DARK_PURPLE + "=================================");
				if(_gameCard.get_bet() > 0)
				{
					p.sendMessage(ChatColor.RED + ""+ChatColor.BOLD + "It was DRAW! Money has send to the server! Thanks for playing!");
				}else
				{
					p.sendMessage(ChatColor.RED + ""+ChatColor.BOLD + "It was DRAW!");
				}
				p.sendMessage(ChatColor.DARK_PURPLE + "=================================");
			}

			_spleefHandler.gameEndForPlayer(_gameCard,p);
			
		}
		
		_players_off_game.clear();
		_player_anti_stands.clear();
		_players_ingame.clear();
		_remove_blocks.clear();
		_spleefHandler.matchEND(_gameCard, round_winner);
		HandlerList.unregisterAll(this);
	}
	
	void moveToLobbyPlayer(Player p)
	{
		p.getInventory().clear();
		p.teleport(_gameCard.get_arena().get_spectator_lobby());
		
		movePlayerToLobbyHash(p);
		
		p.setHealth(20);
		
		_spleefHandler.removePotionEffects(p);
		p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 40, 5));
		
		p.sendMessage(ChatColor.DARK_GRAY + "You have been moved to spetator lobby! Better luck next time!");
		
		_total_players--;
		checkIFendGame();
		
		//move to lobby
	}
	
	void checkIFendGame()
	{
		if(_total_players <= 1 || _gameCard.get_players_accept().size() < 2)
		{
			endGame();
		}
	}
	void playerLeft(Player p)
	{
		//_combatHandler.removePotionEffects(p);
		_spleefHandler.removePotionEffects(p);
		_total_players--;
		_players_ingame.remove(p);
		_gameCard.get_players_accept().remove(p.getUniqueId());

		checkIFendGame();
	}
	
	@EventHandler
	void onMove(PlayerMoveEvent event)
	{
		if(has_ended)
			return;
		
		Player p = event.getPlayer();
		if(_players_ingame.containsKey(p))
		{
			if(!has_started)
			{
				event.setCancelled(true);
				return;
			}
			if(p.getLocation().getBlockY() < _layer_y-3)
			{
				moveToLobbyPlayer(p);
				return;
			}
			if(p.getLocation().distance(mid_loc) > _max_distance)
			{
				p.sendMessage(ChatColor.RED + "You went too far from arena!");
				moveToLobbyPlayer(p);
				return;
			}
		}
	}
	
	@EventHandler
	void onFoodLevel(FoodLevelChangeEvent event)
	{
		if(has_ended)
			return;
		
		if(event.getEntity() instanceof Player)
		{
			Player p =(Player) event.getEntity();
			if(_players_ingame.containsKey(p))
			{
				event.setCancelled(true);
			}
		}
		
	}
	
	@EventHandler
	void onQuit(PlayerQuitEvent event)
	{
		Player p = event.getPlayer();
		System.out.println("player quit:");
		
		if(_players_ingame.containsKey(p) )
		{
			playerLeft(p);
			
		}
		
		if(_players_off_game.containsKey(p))
		{
			playerLeft(p);
		}
		
		if(_players_spectators.containsKey(p))
		{
			teleportSpectatorToBack(p);
		}
	}
	
	@EventHandler
	void onDrop(PlayerDropItemEvent event)
	{
		Player p = event.getPlayer();
		if(_players_ingame.containsKey(p))
		{
			event.setCancelled(true);		
		}
	}
	
	@EventHandler
	void onCMDwrite(PlayerCommandPreprocessEvent event)
	{
		if(_players_ingame.containsKey(event.getPlayer()) || _players_off_game.containsKey(event.getPlayer()))
		{
			event.getPlayer().sendMessage(ChatColor.RED + "You are in Spleef game! Can't use commands!");
			event.setCancelled(true);
		}
		
	}
	
	@EventHandler
	void onEntityDamage(EntityDamageEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			Player p = (Player)event.getEntity();
			if(_players_ingame.containsKey(p))
			{
				event.setCancelled(true);
//				if(p.getHealth() - event.getFinalDamage() <= 0.5)
//				{
//					p.setHealth(0.5);
//					event.setCancelled(true);
//				}
			}
		}
	}
	
	@EventHandler
	void onBlockBreak(BlockBreakEvent event)
	{
		Player p = event.getPlayer();
		if(!has_started)
		{
			if(_players_ingame.containsKey(p))
			{
				event.setCancelled(true);
			}
		}else
		{
			if(_players_ingame.containsKey(p))
			{
				event.setDropItems(false);
			}
		}
	
	}
	void anti_standBlocks(Player p, Material mat)
	{
		p.sendMessage(ChatColor.RED + "[ANTI_STAND]: You have stand too long same spot!");
		Location loc = p.getLocation();
		int z;
		for(int  x = loc.getBlockX()-1; x < loc.getBlockX()+2; ++x)
		{
			for( z = loc.getBlockZ()-1; z < loc.getBlockZ()+2; ++z)
			{
				Block b = p.getWorld().getBlockAt(x, loc.getBlockY()-1, z);
				b.setType(mat);
				_remove_blocks.add(b);
			}
			z = loc.getBlockZ()-1;
		}
	}
	
	void runnable()
	{
		run = new BukkitRunnable() 
		{
			
			@Override
			public void run() 
			{
				if(_cd.isCooldownReady("round") && !has_ended)
				{
					endGame();
					this.cancel();
				}
				
				if(has_ended)
				{
					cancel();
					return;
				}
				
				if(!_cd.isCooldownReady("start") && !has_started)
				{					
					String str = ChatColor.GREEN + ""+ChatColor.BOLD + "Game starts in: "+ChatColor.WHITE+(_cd.GetCdInSeconds("start")+1) + " s";
					_gameCard.sendMessageToALL(str);
				}
				else
				{
					if(!has_started)
					{
						String str = ChatColor.GREEN + ""+ChatColor.BOLD + "Game starts in: "+ChatColor.WHITE+"GO!";
						_gameCard.sendMessageToALL(str);
					}
					

					has_started = true;
					
					if(_cd.isCooldownReady("roundCloseToEnd"))
					{
						String str = ChatColor.RED + ""+ChatColor.BOLD + "Ends in "+ChatColor.WHITE+ (_cd.GetCdInSeconds("round")+1) + " s";
						_gameCard.sendMessageToALL(str);
					}
					
					if(!_remove_blocks.isEmpty())
					{
						for(Block b : _remove_blocks)
						{
							b.setType(Material.AIR);
						}
						_remove_blocks.clear();
					}
					
					if(_anti_stand > 0)
					{
						for(Player p : _players_ingame.keySet())
						{
							Tuple<Location, Integer> data = _player_anti_stands.get(p);
							Location p_loc = p.getLocation();
							Location last_loc = data.GetKey();

							if(p_loc.distance(last_loc) < 2)
							{
								_player_anti_stands.put(p, new Tuple<>(p_loc, data.GetValue()+1));
								if(data.GetValue() > _anti_stand-1)
								{
									anti_standBlocks(p, Material.RED_STAINED_GLASS);
									_player_anti_stands.put(p, new Tuple<>(p_loc, 0));
								}
							}
							else
							{
								_player_anti_stands.put(p, new Tuple<>(p_loc, 0));
							}
							
							
						}
					}
					
				}
	
			}
		}.runTaskTimer(_main, 20, 20);
	}

	@Override
	public void startGame() 
	{
		has_started = false;
		has_ended = false;
		_cd = new Cooldowns();
		_player_anti_stands.clear();
		_remove_blocks.clear();
		_total_players = _gameCard.get_players_accept().size();
		int count = 0;
		_round++;
		
		
		for(Player p: _players_ingame.keySet())
		{
			setupPlayerForStart(p);
			p.teleport(_gameCard.get_arena().getSpawnpointLoc(count));
			_player_anti_stands.put(p, new Tuple<Location, Integer>(new Location(p.getWorld(), 0, 0, 0),0));
			count++;
		}
		
		_cd.setCooldownInSeconds("round", _roundTime);
		_cd.setCooldownInSeconds("roundCloseToEnd", _roundTime-_roundEnd_warning);
		
		_cd.setCooldownInSeconds("start", _start_delay);
		((SpleefArena)_gameCard.get_arena()).fillWithSnowpPlatform();
		if(run != null)
		{
			run.cancel();
		}
		
		runnable();
		
		String str1 = ChatColor.YELLOW + "========================";
		
		
		_gameCard.sendMessageToALL(" ");
		_gameCard.sendMessageToALL(" ");
		_gameCard.sendMessageToALL(" ");
		_gameCard.sendMessageToALL(" ");
		_gameCard.sendMessageToALL(" ");
		_gameCard.sendMessageToALL(str1);
		_gameCard.sendMessageToALL(ChatColor.DARK_PURPLE + ""+ChatColor.BOLD + "ROUND: "+ChatColor.WHITE+_round 
		+ ChatColor.DARK_PURPLE + ""+ChatColor.BOLD + " ROUND LASTS "+ChatColor.WHITE+_roundTime+" s");
		_gameCard.sendMessageToALL(ChatColor.DARK_PURPLE + ""+ChatColor.BOLD + "Best of "+ChatColor.WHITE+_best_of);
		_gameCard.sendMessageToALL(str1);
		if(_gameCard.get_bet() > 0 && _round == 1)
		{
			_gameCard.sendMessageToALL(ChatColor.DARK_PURPLE + ""+ChatColor.BOLD + "Winner gets: "+ChatColor.DARK_GREEN+_gameCard.get_total_bet());
			_gameCard.sendMessageToALL(str1);
		}
		
		if(_round > 1)
		{
			for(Entry<Player,MiniGamePlayerStats> stats : _players_ingame.entrySet())
			{
				_gameCard.sendMessageToALL(ChatColor.AQUA + stats.getKey().getName() + ChatColor.GOLD+" score:  "+ChatColor.WHITE+stats.getValue().get_score()); 
			}
		}
		_gameCard.sendMessageToALL(" ");
	}

}
