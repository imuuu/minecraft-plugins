package imu.iMiniGames.Other;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import imu.iMiniGames.Arenas.Arena;

public class PlanerDataCard 
{
	Player _owner;
	
	Arena _arena;
		
	HashMap<Integer, String> _invDataValues = new HashMap<>();
	
	HashMap<Player, Boolean> _invitePlayers = new HashMap<>();
	
	HashMap<PotionEffectType, PotionEffect> _invPotionEffects = new HashMap<>();
	HashMap<String, Integer> _attributes = new HashMap<>();
	
	int _best_of_amount = 1;
	int _best_of_max = 1;

	String _potions_names_str = "";
	
	public PlanerDataCard(Player owner) 
	{
		_owner = owner;
	}
	
	public void resetAttributes() {}
	
	public void addBestOfAmount(int amount)
	{
		_best_of_amount+=amount;
		
		if(_best_of_amount < 1)
			_best_of_amount = 1;
		
		if(_best_of_amount > _best_of_max)
			_best_of_amount = _best_of_max;
	}
	
	public String getAttributStringWithColor()
	{
		String str = "";
		for(Entry<String,Integer> entry : _attributes.entrySet())
		{
			str += ChatColor.translateAlternateColorCodes('&', "&9"+entry.getKey().replace("_", " ")+"&b : "+ (entry.getValue() == 0 ? "&cfalse" : "&atrue "));
		}
		
		return str;
				
	}
	public HashMap<String, Integer> get_attributes() 
	{
		return _attributes;
	}
	
	public HashMap<Player, Boolean> get_invitePlayers() 
	{
		return _invitePlayers;
	}
	public void addInvitePlayer(Player p, boolean b)
	{
		_invitePlayers.put(p, b);
	}
	public void removeInvitePlayer(Player p)
	{
		_invitePlayers.remove(p);
	}
	public Boolean getInvitePlayer(Player p)
	{
		return _invitePlayers.get(p);
	}
	public Boolean isInvitePlayer(Player p)
	{
		return _invitePlayers.containsKey(p);
	}
	
	public void clearInvitePlayers()
	{
		_invitePlayers.clear();
	}
	
	public int get_bestOfAmount()
	{
		return _best_of_amount;
	}
	
	public void set_bestOfMax(int max)
	{
		_best_of_max = max;
	}
	
	public void clearPotionEffect()
	{
		_invPotionEffects.clear();
		_potions_names_str = "";
	}
	
	public HashMap<PotionEffectType, PotionEffect> get_invPotionEffects() {
		return _invPotionEffects;
	}
	
	public void removePotionEffect(PotionEffectType t)
	{
		_invPotionEffects.remove(t);
	}
	
	public void putPotionEffect(PotionEffectType t, PotionEffect ef)
	{
		_invPotionEffects.put(t, ef);
		_potions_names_str += ChatColor.GOLD+ t.getName()+":"+ChatColor.LIGHT_PURPLE+ef.getAmplifier()+" ";
	}
	
	public PotionEffect get_PotionEffect(PotionEffectType t) 
	{
		return _invPotionEffects.get(t);
	}
	
	public HashMap<Integer, String> get_invDataValues()
	{
		return _invDataValues;
	}
	
	public void removeDataValue(int i)
	{
		_invDataValues.remove(i);
	}
	
	public void putDataValue(int i, String s)
	{
		_invDataValues.put(i, s);
	}
	
	public String getDataValue(int i)
	{
		return _invDataValues.get(i);
	}
	
	public Player get_owner() {
		return _owner;
	}
	public Arena get_arena() {
		return _arena;
	}
	public void set_arena(Arena _arena) {
		this._arena = _arena;
	}
	public String get_potions_names_str() {
		return _potions_names_str;
	}
}
