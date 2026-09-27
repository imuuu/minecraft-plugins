package imu.DontLoseItems.CustomEnd.EndCustomEvents;

import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class EndEvent_RandomPotionEffect extends EndEvent
{
	private PotionEffect[] _potionEffectsNegatives;
	private PotionEffect[] _potionEffectsPositive;

	private final double _chanceToBePositive = 26;
	private final int _rangeTotalEffect = 4;

	private LinkedList<PotionEffect> _givenEffects = new LinkedList<>();

	public EndEvent_RandomPotionEffect()
	{
		super("Random potion effects", 60);
		ChestLootAmount = 1;
	}

	public void InitPotionEffects()
	{
		_potionEffectsNegatives = new PotionEffect[] {
				new PotionEffect(PotionEffectType.HUNGER, (int) (GetDuration() * 20), 1),
				new PotionEffect(PotionEffectType.WEAKNESS, (int) (GetDuration() * 20), 2),
				new PotionEffect(PotionEffectType.CONFUSION, (int) (GetDuration() * 20), 1), 
				new PotionEffect(PotionEffectType.SLOW_DIGGING, (int) (GetDuration() * 20), 2), 
				new PotionEffect(PotionEffectType.SLOW, (int) (GetDuration() * 20), 1), 
				new PotionEffect(PotionEffectType.POISON, (int) (GetDuration() * 20), 1), 
				};

		_potionEffectsPositive = new PotionEffect[] {
				new PotionEffect(PotionEffectType.ABSORPTION, 	(int) (GetDuration() * 20), 1),
				new PotionEffect(PotionEffectType.SPEED, 		(int) (GetDuration() * 20), 2),
				new PotionEffect(PotionEffectType.HEALTH_BOOST, (int) (GetDuration() * 20), 1),
				new PotionEffect(PotionEffectType.HEAL, 		(int) (GetDuration() * 20), 1), 
				new PotionEffect(PotionEffectType.FAST_DIGGING, (int) (GetDuration() * 20), 2), 
				new PotionEffect(PotionEffectType.REGENERATION, 	(int) (GetDuration() * 20), 1), 
				new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 	(int) (GetDuration() * 20), 1), 
				new PotionEffect(PotionEffectType.NIGHT_VISION, 	(int) (GetDuration() * 20), 1), 
				};
	}

	@Override
	public void OnEventStart()
	{
		InitPotionEffects();
		int range = ThreadLocalRandom.current().nextInt(_rangeTotalEffect)+1;
		for(int i = 0; i < range; ++i)
		{
			GenerateEffects();
		}
		
		for (Player player : GetPlayers())
		{
			AddEffects(player);
		}
	}

	@Override
	public void OnEventEnd()
	{
		AddChestLootBaseToAll(ChestLootAmount);
		for (Player player : GetPlayers())
		{
			RemoveEffects(player);
		}
		_givenEffects.clear();
	}

	private void RemoveEffects(Player player)
	{
		for (PotionEffect eff : _givenEffects)
		{
			player.removePotionEffect(eff.getType());
		}
	}

	private void GenerateEffects()
	{

		int size = 0;
		PotionEffect effect = null;
		if (ThreadLocalRandom.current().nextInt(100) < _chanceToBePositive)
		{
			size = ThreadLocalRandom.current().nextInt(_potionEffectsPositive.length);
			effect = _potionEffectsPositive[size];
			_givenEffects.add(effect);
			return;
		}

		size = ThreadLocalRandom.current().nextInt(_potionEffectsNegatives.length);
		effect = _potionEffectsNegatives[size];
		_givenEffects.add(effect);
	}

	private void AddEffects(Player player)
	{
		for (PotionEffect eff : _givenEffects)
		{
			player.addPotionEffect(eff);
		}
	}

	@Override
	public String GetEventName()
	{

		return "Random potion effects are applied";
		//return GetName();

	}

	@Override
	public String GetRewardInfo()
	{

		return "Chestloot base by &2+"+ChestLootAmount;
	}

	@Override
	public String GetDescription()
	{
		return "&6Random potion effects";
	}

	@Override
	public void OnPlayerLeftMiddleOfEvent(Player player)
	{
		RemoveEffects(player);
	}

	@Override
	public void OnPlayerJoinMiddleOfEvent(Player player)
	{
		AddEffects(player);

	}

	@Override
	public void OnOneTickLoop()
	{
		// TODO Auto-generated method stub

	}

}
