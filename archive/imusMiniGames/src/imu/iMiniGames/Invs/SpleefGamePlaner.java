package imu.iMiniGames.Invs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iAPI.Other.Metods;
import imu.iMiniGames.Arenas.SpleefArena;
import imu.iMiniGames.Invs.GamePlaner.BUTTON;
import imu.iMiniGames.Main.ImusMiniGames;
import imu.iMiniGames.Managers.PlanerManager;
import imu.iMiniGames.Other.SpleefDataCard;
import imu.iMiniGames.Other.SpleefGameCard;
import imu.iMiniGames.Prompts.ConvPromptSpleefGamePlaner;

public class SpleefGamePlaner extends GamePlaner
{

	SpleefDataCard _card;
	PlanerManager _pm;
	
	ArrayList<Integer> wrongs = new ArrayList<>();
	HashMap<UUID, ItemStack> _playerHeads = new HashMap<>();

	public SpleefGamePlaner(ImusMiniGames main, Player player, SpleefDataCard card) 
	{
		super(main, player, ChatColor.BLUE + ""+ChatColor.BOLD + "Spleef Planer");

		_card = card;
		_pm = main.get_planerManager();
		_card.set_bestOfMax(_main.get_spleefManager().get_maximum_best_of());
		loadPlayerHeads();
		reset();
	}
	
	void reset()
	{
		setupButtons();
		checkAnwsers();
	}
	
	void loadPlayerHeads()
	{
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				for(Player p : _main.getServer().getOnlinePlayers())
				{
					//_playerHeads.put(p.getUniqueId(),Metods._ins.getPlayerHead(p));
					_playerHeads.put(p.getUniqueId(),new ItemStack(Material.PLAYER_HEAD));
				}
				
			}
		}.runTaskAsynchronously(_main);
	}
	
	@Override
	public void setupButtons() 
	{
		ItemStack mod;
		setupButton(BUTTON.EXIT, Material.RED_STAINED_GLASS_PANE, ChatColor.RED + "EXIT", _size-9);
		mod = setupButton(BUTTON.CONFIRM, Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN + "CONFIRM", _size-1);
		Metods._ins.addLore(mod, ChatColor.YELLOW + "Confirm your game plan and start sending invites..", true);
		
		setupButton(BUTTON.RESET, Material.LAVA_BUCKET, ChatColor.RED +""+ChatColor.BOLD+ "RESET", 8);
		
		addLoreSetRemove(setupButton(BUTTON.SET_ARENA, Material.PAINTING, ChatColor.AQUA + "Set Arena", 0));
		String arenaName = _card.get_arena() != null ? _card.get_arena().get_arenaNameWithColor() : "Random";
		_card.putDataValue(0, arenaName);
		
		 mod = addLoreSetRemove(setupButton(BUTTON.ADD_BET,Material.GOLD_INGOT, ChatColor.AQUA + "Place Bet", 2));
		 Metods._ins.addLore(mod, ChatColor.YELLOW + "How much every player pays", true);
		
		 mod = addLoreSetRemove(setupButton(BUTTON.ADD_PLAYERS, Material.WITHER_SKELETON_SKULL, ChatColor.AQUA + "Add Players", 4));
		 Metods._ins.addLore(mod, ChatColor.YELLOW + "Add your friends with you!", true);
		 _card.get_invitePlayers().remove(_player);
		 if(!_card.get_invitePlayers().isEmpty())
		 {
			 int count = 1;
			 Metods._ins.addLore(mod, ChatColor.BLUE + "== Bellow has added players! ==", true);
			 for(Entry<Player, Boolean> entry : _card.get_invitePlayers().entrySet())
			 {
				 Player p = entry.getKey();
				 if(p != null)
				 {
					 Metods._ins.addLore(mod, ChatColor.translateAlternateColorCodes('&',"&2"+count+ ": &5"+p.getName()) ,true);
					 count++;
				 }
			 }
		 }
		 
		 mod = addLoreSetRemove(setupButton(BUTTON.POTION_EFFECTS, Material.EXPERIENCE_BOTTLE, ChatColor.AQUA + "Add Potion Effects", 6));
		 Metods._ins.addLore(mod, ChatColor.YELLOW + "Add Potion effects to your match!", true);
		 
		 if(!_card.get_invPotionEffects().isEmpty())
		 {
			 Metods._ins.addLore(mod, ChatColor.BLUE + "== Bellow has added effects! ==", true);
			 for(Entry<PotionEffectType, PotionEffect> entry : _card.get_invPotionEffects().entrySet())
			 {
				 PotionEffect ef = entry.getValue();
				 Metods._ins.addLore(mod, ChatColor.DARK_PURPLE + ef.getType().getName() + " "+ef.getAmplifier(),true);
			 }
		 }
		 
//		 mod = setupButton(BUTTON.ADD_BEST_OF_AMOUNT, Material.WRITABLE_BOOK, ChatColor.AQUA + "Add Best of (amount)", 10);
//		 Metods._ins.addLore(mod, ChatColor.AQUA + "M1: "+ChatColor.GREEN + "Increase"+ChatColor.AQUA + " M2: "+ChatColor.RED + "Decrease", false);	
//		 Metods._ins.addLore(mod, ChatColor.YELLOW + "Add how many wins person needs to win the small tournament!", true);
//		 Metods._ins.addLore(mod, ChatColor.AQUA + "Best of: " +ChatColor.DARK_GREEN + _card.get_bestOfAmount(), true);
//		 
		 mod = setupButton(BUTTON.ADD_BEST_OF_AMOUNT, Material.WRITABLE_BOOK, ChatColor.AQUA + "Add Needed wins", 10);
		 Metods._ins.addLore(mod, ChatColor.AQUA + "M1: "+ChatColor.GREEN + "Increase"+ChatColor.AQUA + " M2: "+ChatColor.RED + "Decrease", false);
		 Metods._ins.addLore(mod, ChatColor.YELLOW + "Add how many wins ", true);
		 Metods._ins.addLore(mod, ChatColor.YELLOW + "person needs to win!", true);	 
		 Metods._ins.addLore(mod, ChatColor.AQUA + "Person needs " + ChatColor.DARK_GREEN + ChatColor.BOLD+ _card.get_bestOfAmount()+ChatColor.AQUA+" win(s)", true);
		 
	}
	
	ItemStack addLoreSetRemove(ItemStack stack)
	{
		return Metods._ins.addLore(stack, ChatColor.AQUA + "M1: "+ChatColor.GREEN + "Set"+ChatColor.AQUA + " M2: "+ChatColor.RED + "Remove", false);		
	}
	
	void setPDvalue(ItemStack stack, String value)
	{
		_pm.setPDvalue(stack,value);
		String str = "Set to: ";
		
		Metods._ins.removeLore(stack, str);
		Metods._ins.addLore(stack, ChatColor.AQUA + str +ChatColor.DARK_GREEN + value, true);
		
	}
	void removePDvalue(ItemStack stack)
	{
		if(stack == null)
			return;
		
		_pm.removePDvalue(stack);
		String str = "Set to: ";
		
		Metods._ins.removeLore(stack, str);
		
	}
	
	void checkAnwsers()
	{
		if(!_card.get_invDataValues().isEmpty())
		{
			for(int i = 0; i < _inv.getContents().length; ++i)
			{
				String value = _card.getDataValue(i);
				if(value != null)
				{
					setPDvalue(_inv.getItem(i), value);
					if(wrongs.contains(i))
					{
						_inv.getItem(i).setType(Material.YELLOW_STAINED_GLASS);
					}
				}else
				{
					removePDvalue(_inv.getItem(i));
				}
			}
		}
			
	}
	
	SpleefGameCard confirm()
	{
				
		SpleefGameCard gameCard = new SpleefGameCard("SPLEEF","mg spleef");
		gameCard.set_maker(_player);
		wrongs.clear();
		
		for(int i = 0; i < _inv.getContents().length; ++i)
		{
			ItemStack s = _inv.getItem(i);
			if(s != null)
			{
				
				BUTTON button = getButton(s);

				String value = _pm.getPDvalue(s);
				
				if(button == BUTTON.SET_ARENA)
				{
					SpleefArena arena;
					if(value != null && value.equalsIgnoreCase("random"))
					{
						int arenas_size = _main.get_spleefManager().getArenas().size();
						if(arenas_size > 0)
						{
							//Random rand = new Random();
							int r = new Random().nextInt(arenas_size);
							arena = (SpleefArena)_main.get_spleefManager().getArena(r);
						}
						else
						{
							wrongs.add(i);
							_player.sendMessage(ChatColor.RED + "Didn't find any arenas");
							break;
						}
					}
					else
					{
						arena = (SpleefArena)_card.get_arena();						
					}
					
					if(arena != null)
					{
						gameCard.set_arena(arena);
					}
					else
					{
						_player.sendMessage(ChatColor.RED + "Didn't find arena with that name: " + value);
						wrongs.add(i);
					}
					break;
				}
			}
		}
	
		if(gameCard.get_arena() == null)
		{
			_player.sendMessage(ChatColor.RED+"Something went wrong! Coulnd't find arena");
			return null;
		}
		for(int i = 0; i < _inv.getContents().length; ++i)
		{
			ItemStack s = _inv.getItem(i);
			if(s != null)
			{
				
				BUTTON button = getButton(s);

				String value = _pm.getPDvalue(s);
				
				switch (button) 
				{
				case ADD_BET:
					if(value == null)
						continue;
					
					if(Metods._ins.isDigit(value))
					{
						double bet =Double.parseDouble(value);
						if(bet > 0)
						{
							gameCard.set_bet(bet);
						}else
						{
							_player.sendMessage(ChatColor.RED + "Bet need to be positive!");
							wrongs.add(i);
						}
						
					}else
					{
						_player.sendMessage(ChatColor.RED + "Please add only numbers");
						wrongs.add(i);
					}
					break;
				case ADD_PLAYERS:
					_card.addInvitePlayer(_player, true);
					if(!_card.get_invitePlayers().isEmpty() && _card.get_invitePlayers().size() > 1)
					{
						boolean found = false;
						
						 for(Entry<Player, Boolean> entry : _card.get_invitePlayers().entrySet())
						 {
							 Player p = entry.getKey();
							 if(p != null)
							 {
								 found = true;
								gameCard.putPlayer(p.getUniqueId());
									
								if(_main.isPlayerBlocked(p))
								{
									wrongs.add(i);
									_player.sendMessage(ChatColor.RED + p.getName() + " has blocked minigames! You can't in invite him/her!");
								}
							 }
						
						 } 
							 
						
						// adds player if not added
						gameCard.putPlayer(_player.getUniqueId());
						

						if(gameCard.get_players_accept().size() > gameCard.get_arena().get_maxPlayers())
						{
							_player.sendMessage(ChatColor.RED+ "Too many players for the arena!");
							wrongs.add(i);
						}
						
						
						if(!found)
						{
							_player.sendMessage(ChatColor.RED+ "Didnt find any players with given names");
							wrongs.add(i);
						}
						

						if(gameCard.get_players_accept().size() < 2)
						{
							_player.sendMessage(ChatColor.RED + "Not enough players! Need at least 2");
							wrongs.add(i);
						}
													
					}
					else
					{
						_player.sendMessage(ChatColor.RED + "You need at least 2 players");
						wrongs.add(i);
					}
					break;
				case SET_ARENA:					
					break;
				default:
					break;
					
				}
			}
		}
		
		if(!wrongs.isEmpty())
		{
			
			return null;
		}
		else
		{
			
			double total_bet = Math.round((gameCard.get_bet() * gameCard.get_players_accept().size() * (1.0-_main.get_spleefGameHandler().getBet_fee_percent()))*100)/100;
			gameCard.set_total_bet(total_bet);
			gameCard.setDataCard(_card);			
			return gameCard;
		}
	}
	void saveDataCard()
	{
		_main.get_spleefManager().savePlayerDataCard(_player, _card);
	}
	
	@Override
	public void onClickInsideInv(InventoryClickEvent e) {
		int slot = e.getSlot();
		ItemStack stack = e.getCurrentItem();
		//System.out.println("stack: "+stack);
		
		ConversationFactory cf = null;
		String question = null;
		Conversation conv = null;
		
		BUTTON button = getButton(stack);
		
		if(e.getClick() == ClickType.LEFT)
		{
			switch (button) {
			
			case NONE:				
				break;
			
			case EXIT:
				_player.closeInventory();
				break;
			case CONFIRM:
				SpleefGameCard gameCard = confirm();
				if(gameCard != null)
				{	
					_main.get_spleefGameHandler().savePlayerGameCard(_player, gameCard);						
					_main.get_spleefGameHandler().repearStartGame(_player, gameCard);
					//saveDataCard();
					_player.closeInventory();
					
				}
				else
				{
					//System.out.println("Card null");
					checkAnwsers();
				}
				break;
			case ADD_BET:
				cf = new ConversationFactory(_main);
				question = ChatColor.DARK_PURPLE + "Give your bet";
				conv = cf.withFirstPrompt(new ConvPromptSpleefGamePlaner(_main, _player, slot, question)).withLocalEcho(true).buildConversation(_player);
				conv.begin();
				_player.closeInventory();
				break;
			case ADD_PLAYERS:

				new SpleefGamePlanerChoosePlayerINV(_main, _player, _card);
				break;
			case RESET:
				_card = new SpleefDataCard(_player);
				reset();
				break;
				
			case SET_ARENA:
				new SpleefGamePlanerChooseArenaINV(_main, _player, _card);
				break;
			case POTION_EFFECTS:
				new SpleefGamePlanerPotionEffectsINV(_main, _player, _card);
				break;
			case ADD_BEST_OF_AMOUNT:
				_card.addBestOfAmount(1);
				setupButtons();
				checkAnwsers();
				break;
			default:
				break;
			}
		}
		else if(e.getClick() == ClickType.RIGHT)
		{
			switch (button) 
			{
			case ADD_BET:
				_card.removeDataValue(slot);
				if(wrongs.contains(slot))
					wrongs.remove(slot);
				//setupButtons();
				checkAnwsers();
				break;
			case ADD_PLAYERS:
				_card.clearInvitePlayers();
				setupButtons();
				break;

			case SET_ARENA:
				_card.set_arena(null);
				reset();
				break;
			case POTION_EFFECTS:
				_card.get_invPotionEffects().clear();
				reset();
				break;
			case ADD_BEST_OF_AMOUNT:
				_card.addBestOfAmount(-1);
				setupButtons();
				checkAnwsers();
				break;
			default:
				break;
			
			
			
			}
		}
		
	}

	@Override
	public void invClosed(InventoryCloseEvent e) 
	{
		super.invClosed(e);
		saveDataCard();
		e.getPlayer().sendMessage(ChatColor.GOLD + "Spleef plan has saved!");
	}
}
