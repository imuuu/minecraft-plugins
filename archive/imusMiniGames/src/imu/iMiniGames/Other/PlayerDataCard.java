package imu.iMiniGames.Other;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import imu.iMiniGames.Handlers.GameCard;
import imu.iMiniGames.Main.ImusMiniGames;

public class PlayerDataCard 
{
	ImusMiniGames _main;
	Player _player;
	UUID _uuid;	
	ItemStack[] _invContent;
	Location _location;
	int _foodLevel;
	double _health;
	GameMode _gamemode;
	int _xp;
	
	String _dataFolderName;
	
	Collection<PotionEffect> _potionEffects;
	int _fireTick = 0;

	boolean _isFlying;
	public PlayerDataCard(ImusMiniGames main, Player p, String dataFolderName)
	{
		_main = main;
		_player = p;
		_uuid = p.getUniqueId();
		_invContent = p.getInventory().getContents();
		_foodLevel = p.getFoodLevel();
		_health = p.getHealth();
		_location = p.getLocation();
		_xp = getExp(p);
		_gamemode=p.getGameMode();
		_dataFolderName = dataFolderName;
		_potionEffects = p.getActivePotionEffects();
		_fireTick = p.getFireTicks();
		
		_isFlying = p.isFlying();
	}
	
	public void saveDataToFile(boolean putTimeStamp)
	{
		new BukkitRunnable() {
			
			@Override
			public void run() 
			{
				ConfigMaker cm;
				FileConfiguration config = null;
				if(putTimeStamp)
				{
					String formatted = new SimpleDateFormat("yyyy-mm-dd-m").format(new Date(System.currentTimeMillis()));
					cm = new ConfigMaker(_main, "/PlayerData/"+_dataFolderName+"/"+formatted+"_"+_uuid+".yml");
					config = cm.getConfig();
				}
				else
				{
					cm = new ConfigMaker(_main, "/PlayerData/"+_dataFolderName+"/"+_uuid+".yml");
					config = cm.getConfig();
					
				}
				
				config.set("PlayerName", _player.getName());
				config.set("UUID", _uuid.toString());
				config.set("Health", _health);
				config.set("Foodlevel", _foodLevel);
				config.set("Gamemode",_gamemode.toString());
				config.set("Xp", _xp);
				config.set("Loc",_location);
				config.set("FireTick", _fireTick);
				config.set("Flying", _isFlying);
				
				if(_main.get_econ() != null)
				{
					config.set("Money", _main.get_econ().getBalance(_player));
				}
				
				for(int i = 0; i < _invContent.length; ++i)
				{
					ItemStack s = _invContent[i];
					if(s != null)
					{
						config.set("InvContent."+i, s );
					}else
					{
						config.set("InvContent."+i, "null");
					}
					
				}
				int count = 0;
				for(PotionEffect effect : _potionEffects)
				{
					config.set("PotionEffect."+count, effect.getType().getName()+":"+effect.getDuration()+":"+effect.getAmplifier());
					count++;
				}
				
				
				cm.saveConfig();
			}
		}.runTaskAsynchronously(_main);
	}
	
	public boolean isFile()
	{
		ConfigMaker cm = new ConfigMaker(_main, "/PlayerData/"+_dataFolderName+"/"+_uuid+".yml");
		return cm.isExists();
	}
	
	public void loadDataFileAndSetData()
	{
		ConfigMaker cm = new ConfigMaker(_main, "/PlayerData/"+_dataFolderName+"/"+_uuid+".yml");
		FileConfiguration config = cm.getConfig();
		
		_uuid = UUID.fromString(config.getString("UUID"));
		_health = config.getDouble("Health");
		_foodLevel=config.getInt("Foodlevel");
		_xp = config.getInt("Xp");
		_location=config.getLocation("Loc");
		_fireTick = config.getInt("FireTick");
		_gamemode = GameMode.valueOf(config.getString("Gamemode"));
		_isFlying = config.getBoolean("Flying");
		
		ItemStack[] stacks = new ItemStack[_invContent.length];
		for (String key : config.getConfigurationSection("InvContent.").getKeys(false)) 
		{
			if(config.getString("InvContent."+key).equalsIgnoreCase("null"))
			{
				stacks[Integer.parseInt(key)] = null;
			}else
			{
				stacks[Integer.parseInt(key)] = config.getItemStack("InvContent."+key);
			}
			
		}		
		_invContent = stacks;
		if(config.get("PotionEffect") != null)
		{
			Collection<PotionEffect> effects = new ArrayList<PotionEffect>();;
			for (String key : config.getConfigurationSection("PotionEffect.").getKeys(false)) 
			{
				String[] effect_str = config.getString("PotionEffect."+key).split(":");
				_main.get_itemM().printArray("effect", effect_str);
				PotionEffect effect = new PotionEffect(PotionEffectType.getByName(effect_str[0]), 
						Integer.parseInt(effect_str[1]), Integer.parseInt(effect_str[2]));
				effects.add(effect);
			}
			_potionEffects = effects;
		}else
		{
			System.out.println("no potion section found");
		}
		
		
	}
	
	public void setDataToPLAYER(Player player)
	{	
		if(player != null)
		{
			new BukkitRunnable() 
			{			
				@Override
				public void run() 
				{
					player.setHealth(_health);
					player.setFoodLevel(_foodLevel);
					changeExp(player, Integer.MAX_VALUE);
					changeExp(player, _xp);		
					player.getInventory().setContents(_invContent);
					player.setGameMode(_gamemode);
					player.addPotionEffects(_potionEffects);

					player.setFireTicks(_fireTick);
					if(player.getAllowFlight() && _isFlying)
					{
						player.setFlying(_isFlying);
					}
					_player.teleport(_location);
				}
			}.runTask(_main);
			
			
		}
		else
		{
			System.out.println("Couldn't find player with uuid PlayerDataCard:setDataToPLAYER");
		}
	}
	public void setDataToPLAYER(GameCard card, Player player)
	{	
		
		new BukkitRunnable() 
		{
			
			@Override
			public void run() 
			{
				if(player != null)
				{
					player.setHealth(_health);
					player.setFoodLevel(_foodLevel);
					changeExp(player, Integer.MAX_VALUE);
					changeExp(player, _xp);		
					
					player.setGameMode(_gamemode);
					player.addPotionEffects(_potionEffects);

					player.setFireTicks(_fireTick);
					if(player.getAllowFlight() && _isFlying)
					{
						player.setFlying(_isFlying);
					}
					
					if(card != null && card instanceof CombatGameCard && ((CombatDataCard) card.getDataCard()).isOwnGearKit)
					{
						
						player.getInventory().setContents(((CombatGameCard)card).checkAndApplyCombatConsumambles(player,_invContent));
					}else
					{
						player.getInventory().setContents(_invContent);
					}
					System.out.println("Minigame info: player: "+player.getName() + " has got all his levels/items/mode back!");
					_player.teleport(_location);	
				}
				else
				{
					System.out.println("Couldn't find player with uuid PlayerDataCard:setDataToPLAYER");
				}
			}
		}.runTask(_main);
		
	}
	
	public void removeDataFile()
	{
		ConfigMaker cm = new ConfigMaker(_main, "/PlayerData/"+_dataFolderName+"/"+_uuid+".yml");
		cm.removeConfig();
	}
	
	
	public Player get_player() {
		return _player;
	}

	public void set_player(Player _player) {
		this._player = _player;
	}

	public UUID get_uuid() {
		return _uuid;
	}

	public void set_uuid(UUID _uuid) {
		this._uuid = _uuid;
	}

	public ItemStack[] get_invContent() {
		return _invContent;
	}

	public void set_invContent(ItemStack[] _invContent) {
		this._invContent = _invContent;
	}

	public Location get_location() {
		return _location;
	}

	public void set_location(Location _location) {
		this._location = _location;
	}

	public int get_foodLevel() {
		return _foodLevel;
	}

	public void set_foodLevel(int _foodLevel) {
		this._foodLevel = _foodLevel;
	}

	public double get_health() {
		return _health;
	}

	public void set_health(double _health) {
		this._health = _health;
	}

	public int get_xp() {
		return _xp;
	}

	public void set_xp(int _xp) {
		this._xp = _xp;
	}

	//https://gist.github.com/Jikoo/30ec040443a4701b8980 ========
	public int getExp(Player player) 
	{
		return getExpFromLevel(player.getLevel())
				+ Math.round(getExpToNext(player.getLevel()) * player.getExp());
	}
	public int getExpFromLevel(int level) {
		if (level > 30) {
			return (int) (4.5 * level * level - 162.5 * level + 2220);
		}
		if (level > 15) {
			return (int) (2.5 * level * level - 40.5 * level + 360);
		}
		return level * level + 6 * level;
	}
	public  double getLevelFromExp(long exp) {
		if (exp > 1395) {
			return (Math.sqrt(72 * exp - 54215) + 325) / 18;
		}
		if (exp > 315) {
			return Math.sqrt(40 * exp - 7839) / 10 + 8.1;
		}
		if (exp > 0) {
			return Math.sqrt(exp + 9) - 3;
		}
		return 0;
	}
	
	private static int getExpToNext(int level) {
		if (level > 30) {
			return 9 * level - 158;
		}
		if (level > 15) {
			return 5 * level - 38;
		}
		return 2 * level + 7;
	}
	
	public void changeExp(Player player, int exp) {
		exp += getExp(player);

		if (exp < 0) {
			exp = 0;
		}

		double levelAndExp = getLevelFromExp(exp);

		int level = (int) levelAndExp;
		player.setLevel(level);
		player.setExp((float) (levelAndExp - level));
	}
	//======================

}
