package imu.TokenTp.Managers;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import imu.TokenTp.CustomItems.ItemTeleToken;
import imu.TokenTp.CustomItems.ItemTeleTokenToken;
import imu.TokenTp.Enums.TeleState;
import imu.TokenTp.Enums.TeleTokenType;
import imu.TokenTp.Enums.TokenType;
import imu.TokenTp.Other.TeleChecks;
import imu.TokenTp.main.Main;
import imu.iAPI.Main.ImusAPI;
import imu.iAPI.Other.Cooldowns;
import imu.iAPI.Other.Metods;
import imu.iAPI.Other.Tuple;

public class TeleTokenManager 
{
	Main _main = null;
	Metods _itemM = null;
	
	int _teleport_time = 7;
	

	int _request_time = 8;
	int _accept_time = 8;
	int _token_time = 10;
	
	HashMap<String, String> _pdDataWords = new HashMap<>();
	
	TokenType[] token_types = {TokenType.TOKEN_TO_LOCATION,
								TokenType.TOKEN_TO_PLAYER};
	
	HashMap<UUID, Tuple<UUID, Boolean>> _anwsers = new HashMap<UUID,  Tuple<UUID, Boolean>>();
	HashMap<UUID, UUID> _request = new HashMap<UUID, UUID>();
	HashMap<UUID, Cooldowns> _cds = new HashMap<>();
	HashMap<UUID, TeleState> _teleActivated = new HashMap<>();

	
	String _cd_requestTimeSTR = "request";
	String _cd_acceptTimeSTR = "accept";
	String _cd_tokenTimeSTR = "token";
	
	public TeleTokenManager(Main main) 
	{
		_main = main;
		_itemM = ImusAPI._metods;
		setPdDataWords();
	}
	
	void setPdDataWords()
	{

		_pdDataWords.put("type", "teletoken_type");
		_pdDataWords.put("tokentype", "teletoken_tokenType");
		_pdDataWords.put("addressname", "teletoken_addressname");
		_pdDataWords.put("desc", "teletoken_desc");
		_pdDataWords.put("loc","teletoken_loc");
		
		
		_pdDataWords.put("world", "teletoken_locWorld");
		_pdDataWords.put("x", "teletoken_locX");
		_pdDataWords.put("y", "teletoken_locY");
		_pdDataWords.put("z", "teletoken_locZ");
		_pdDataWords.put("pitch", "teletoken_locPitch");
		_pdDataWords.put("yaw", "teletoken_locYaw");
		
	}
	
	/**
	 * 
	 * @param target
	 * @param teleporter
	 * @param bool
	 * @return return true if teleporwer uuid is in hashmap already!
	 */
	public Boolean makeRequestAnwser(UUID target, UUID teleporter, Boolean bool)
	{
		boolean b = false;
		if(_anwsers.containsKey(teleporter))
		{
			b = true;
		}
		Tuple<UUID, Boolean> tuple = new Tuple<UUID, Boolean>(target, bool);
		_anwsers.put(teleporter, tuple);
		
		return b;
	}
	public void set_teleport_time(int _teleport_time) {
		this._teleport_time = _teleport_time;
	}
	public int get_teleport_time()
	{
		return this._teleport_time;
	}

	public void set_request_time(int _request_time) {
		this._request_time = _request_time;
	}

	public void set_accept_time(int _accept_time) {
		this._accept_time = _accept_time;
	}

	public void set_token_time(int _token_time) {
		this._token_time = _token_time;
	}
	public int get_token_time()
	{
		return this._token_time;
	}
	public Boolean getRequestAnwser(UUID teleporter)
	{
		return _anwsers.get(teleporter).GetValue();
	}
	public int getRequestCDtime()
	{
		return _request_time;
	}
	public int getAcceptCDtime()
	{
		return _accept_time;
	}
	public UUID getRequestTarget(UUID teleporter)
	{
		return _anwsers.get(teleporter).GetKey();
	}
	public Boolean hasRequestTargetThis(UUID teleporter, UUID target)
	{
		if(_anwsers.containsKey(teleporter) && _anwsers.get(teleporter).GetKey() == target)
		{
			return true;
		}
			
		return false;
	}	
	public Boolean hasRequestAnwsered(UUID teleporter)
	{
		if(_anwsers.containsKey(teleporter))
		{
			if(_anwsers.get(teleporter).GetValue() == null)
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		return false;
	}
	
	Cooldowns getCdOfplayer(UUID player)
	{
		Cooldowns cds;
		if(_cds.containsKey(player))
		{
			cds = _cds.get(player);
		}else
		{
			cds = new Cooldowns();
		}
		return cds;
	}
	
	public void addTokenCD(UUID uuid)
	{
		Cooldowns cds = getCdOfplayer(uuid);
		cds.addCooldownInSeconds(_cd_tokenTimeSTR, _token_time);
		_cds.put(uuid, cds);
	}
	
	public Boolean isTokenCDready(UUID uuid)
	{
		Cooldowns cds = getCdOfplayer(uuid);
		if(cds.isCooldownReady(_cd_tokenTimeSTR))
			return true;
		
		return false;
	}
	
	public void setRequestCd(UUID teleporter)
	{		
		setRequestCd(teleporter, _request_time);
	}
	
	public void setRequestCd(UUID teleporter, int time)
	{		
		Cooldowns cds = getCdOfplayer(teleporter);
		cds.setCooldownInSeconds(_cd_requestTimeSTR, time);
		_cds.put(teleporter, cds);
	}
	
	public void setAcceptCd(UUID teleporter)
	{
		Cooldowns cds = getCdOfplayer(teleporter);
		cds.addCooldownInSeconds(_cd_acceptTimeSTR, _accept_time);
		_cds.put(teleporter, cds);
	}
	
	public boolean hasRequestCd(UUID teleporter)
	{
		if(getCdOfplayer(teleporter).isCooldownReady(_cd_requestTimeSTR))
		{
			return true;
		}
		return false;
	}
	
	/*
	 * return true if it will reset cds
	 */
	public Boolean resetIfNoCdsLeft(UUID uuid)
	{
		Cooldowns cds = getCdOfplayer(uuid);
		if((cds.isCooldownReady(_cd_requestTimeSTR) && cds.isCooldownReady(_cd_acceptTimeSTR)))
		{
			resetRequestAnwserEtc(uuid);
			return true;
		}
		return false;
	}
	
	public void resetRequestAnwserEtc(UUID uuid)
	{
		_anwsers.remove(uuid);
		_request.remove(uuid);
		_cds.remove(uuid);
		_teleActivated.remove(uuid);
	}
	
	public void setTeleState(UUID uuid, TeleState state)
	{
		_teleActivated.put(uuid, state);
	}
	public TeleState getTeleState(UUID uuid)
	{
		if(_teleActivated.containsKey(uuid))
		{
			return _teleActivated.get(uuid);
		}
		return TeleState.NONE;
	}
	public Boolean isTokenActiva(UUID uuid)
	{
		if(_teleActivated.containsKey(uuid))
		{
			return true;
		}
		return false;
	}
	
	public boolean isToken(ItemStack stack)
	{
		if(isTeleToken(stack))
		{
			for(TokenType t : token_types)
			{
				if(getTokenType(stack) == t)
				{
					return true;
				}
			}

		}
		
		return false;
	}
	
	public String getPDword(String wordKey)
	{
		return _pdDataWords.get(wordKey.toLowerCase());
	}
	
	public void setLocPd(ItemStack stack, Location loc)
	{
		if(loc == null)
		{
			_itemM.setPersistenData(stack, getPDword("loc"), PersistentDataType.INTEGER, 0);
		}else
		{
			_itemM.setPersistenData(stack, getPDword("loc"), PersistentDataType.INTEGER, 1);
		}
		
		_itemM.setPersistenData(stack, getPDword("world"), PersistentDataType.STRING, loc.getWorld().getName());
		_itemM.setPersistenData(stack, getPDword("x"), PersistentDataType.DOUBLE, loc.getX());
		_itemM.setPersistenData(stack, getPDword("y"), PersistentDataType.DOUBLE, loc.getY());
		_itemM.setPersistenData(stack, getPDword("z"), PersistentDataType.DOUBLE, loc.getZ());
		_itemM.setPersistenData(stack, getPDword("pitch"), PersistentDataType.DOUBLE, (double)loc.getPitch());
		_itemM.setPersistenData(stack, getPDword("yaw"), PersistentDataType.DOUBLE, (double)loc.getYaw());
	}
	
	public boolean hasLocation(ItemStack stack)
	{
		Integer i = _itemM.getPersistenData(stack, getPDword("loc"), PersistentDataType.INTEGER);
		if(i == null || i == 0)
			return false;
		
		return true;
	}
	
	public boolean isTeleToken(ItemStack stack)
	{
		String s = _itemM.getPersistenData(stack, _pdDataWords.get("type"), PersistentDataType.STRING);
		
		if(s == null)
		{
			return false;
		}
			
		
		return true;
	}
	
	public ItemStack makeToken(ItemStack card)
	{
		ItemTeleTokenToken token = new ItemTeleTokenToken(_main);
		token.setAllData(
				_itemM.getPersistenData(card, getPDword("desc"), PersistentDataType.STRING), 
				_itemM.getPersistenData(card, getPDword("addressname"), PersistentDataType.STRING), 
				getLocFromPd(card), 
				TeleTokenType.TOKEN,
				TokenType.valueOf(_itemM.getPersistenData(card, getPDword("tokentype"), PersistentDataType.STRING)));
		token.setTokenDesc();
		return token;
	}
	
	public ItemStack modifyTeleToken(ItemStack card, String desc, String dest)
	{
		ItemTeleToken teleToken = new ItemTeleToken(_main, card);
		
		if(desc != null)
		{
			teleToken.setDesc(desc);
		}
		
		if(dest != null)
		{
			teleToken.setAddress(dest, null);
		}
		return teleToken;
	}
	
	public ItemStack getTheCard(ItemStack stack1 , ItemStack stack2)
	{
		if(getTeleTokenType(stack1) == TeleTokenType.CARD)
		{
			return stack1;
		}
			
		
		if(getTeleTokenType(stack2) == TeleTokenType.CARD)
		{
			return stack2;
		}
			
		
		System.out.println("Couldnt get card! TeletokenManager.java line: 358");
		return null;
	}
	
	public TeleTokenType getTeleTokenType(ItemStack stack)
	{
		return TeleTokenType.valueOf(_itemM.getPersistenData(stack, _pdDataWords.get("type"), PersistentDataType.STRING));
	}
	
	public TokenType getTokenType(ItemStack stack)
	{
		return TokenType.valueOf(_itemM.getPersistenData(stack, _pdDataWords.get("tokentype"), PersistentDataType.STRING));
	}
	
	public Location getLocFromPd(ItemStack stack)
	{
		Location loc = null;
		
		if(!isTeleToken(stack))
			return null;

		if(_itemM.getPersistenData(stack, _pdDataWords.get("world"), PersistentDataType.STRING) == null)
			return null;

		
		loc = new Location(Bukkit.getWorld(_itemM.getPersistenData(stack, _pdDataWords.get("world"), PersistentDataType.STRING)), 
				_itemM.getPersistenData(stack, _pdDataWords.get("x"), PersistentDataType.DOUBLE), 
				_itemM.getPersistenData(stack, _pdDataWords.get("y"), PersistentDataType.DOUBLE), 
				_itemM.getPersistenData(stack, _pdDataWords.get("z"), PersistentDataType.DOUBLE));
		
		loc.setPitch(_itemM.getPersistenData(stack, _pdDataWords.get("pitch"), PersistentDataType.DOUBLE).floatValue());
		loc.setYaw(_itemM.getPersistenData(stack, _pdDataWords.get("yaw"), PersistentDataType.DOUBLE).floatValue());


		return loc;
	}
	
	public void startTeleport(Player p, Location loc)
	{
		setTeleState(p.getUniqueId(), TeleState.TELEPORTING);
		p.sendMessage(ChatColor.GOLD +""+ ChatColor.BOLD +"Teleporting started."+ChatColor.RED +""+ ChatColor.BOLD +" DO NOT MOVE!"+ChatColor.GOLD +""+ ChatColor.BOLD +" Cast time: "+ _teleport_time +" s");
		System.out.println("Player: "+p.getName()+" start to teleport: World: "+ loc.getWorld().getName() + " x: "+loc.getBlockX() + " y: "+loc.getBlockY()+" z: "+loc.getBlockZ());
		new BukkitRunnable() 
		{
			TeleChecks telecheck = new TeleChecks(p);
			
			@Override
			public void run() 
			{
				if(telecheck.canTeleport())
				{
					if(telecheck.drawAnimation(_teleport_time))
					{
						p.sendMessage(ChatColor.DARK_AQUA + "You have succesfully teleported to location!");
						System.out.println("Player: "+p.getName()+"'s teleport was success");
						p.teleport(loc);
						resetRequestAnwserEtc(p.getUniqueId());
						this.cancel();
					}
				}else
				{
					p.sendMessage(ChatColor.RED + "Teleport has been canceled!");
					resetRequestAnwserEtc(p.getUniqueId());
					this.cancel();
				}
				
			}
		}.runTaskTimer(_main, 0, 1);
	}
}
