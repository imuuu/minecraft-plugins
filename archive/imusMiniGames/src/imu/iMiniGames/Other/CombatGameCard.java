package imu.iMiniGames.Other;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import imu.iMiniGames.Handlers.GameCard;

public class CombatGameCard extends GameCard
{

	public CombatGameCard(String tagName, String cmdName) {
		super(tagName, cmdName);
	}

	private HashMap<UUID, ArrayList<CombatConsumable>> _pConsumables = new HashMap<>(); 
	

	public void checkAndReduceCombatConsumable(UUID uuid,ItemStack stack, int reduced_amount)
	{
		ArrayList<CombatConsumable> cc = _pConsumables.get(uuid);
		if(cc != null)
		{
			ItemStack clone = stack.clone();
			clone.setAmount(1);
			for(CombatConsumable co : cc)
			{
				ItemStack co_clone = co.getStack().clone();
				co_clone.setAmount(1);
				if(co_clone.isSimilar(clone))
				{
					co.addAmount(reduced_amount);
					break;
				}
			}
		}
	}
	
	public ItemStack[] checkAndApplyCombatConsumambles(Player p, ItemStack[] content)
	{
		if(p != null)
		{
			ArrayList<CombatConsumable> cc = _pConsumables.get(p.getUniqueId());
			if(cc != null)
			{
				HashMap<Integer, Integer> values = new HashMap<>();
				int i = 0;
				for(ItemStack invStack : content)
				{					
					if(invStack == null)
					{
						i++;
						continue;
					}						

					ItemStack clone = invStack.clone();
					clone.setAmount(1);
					for(CombatConsumable co : cc)
					{
						ItemStack co_clone = co.getStack().clone();
						co_clone.setAmount(1);
						if(co_clone.isSimilar(clone))
						{
							values.put(i, co.getAmount());
							break;
						}
					}
					
					i++;
				}
				
				for(Entry<Integer,Integer> entry : values.entrySet())
				{
					content[entry.getKey()].setAmount(entry.getValue());
				}
			}
		}
		return content;
	}
	
	public void setupKits(Material[] blacklist_mats)
	{
		_pConsumables.clear();
		CombatDataCard _dataCard = (CombatDataCard) getDataCard();
		for(UUID uuid : get_players_accept().keySet())
		{
			Player p = Bukkit.getPlayer(uuid);
			if(_dataCard.isOwnGearKit())
			{
				ItemStack[] gear = new ItemStack[p.getInventory().getContents().length];
				int count = 0;
				for(ItemStack s : p.getInventory().getContents())
				{
					boolean found = false;
					if(s != null)
					{		
						Material mat  = s.getType();
												
						for(Material bl : blacklist_mats)
						{
							if(bl.equals(mat))
							{
								gear[count] = null;
								found = true;
								count++;
								break;
							}
						}
						
						if(!found && (mat.isEdible() || mat == Material.POTION || mat == Material.SPLASH_POTION || mat == Material.LINGERING_POTION || mat == Material.TIPPED_ARROW))
						{
							if(_pConsumables.containsKey(uuid))
							{
								_pConsumables.get(uuid).add(new CombatConsumable(s));
							}
							else
							{
								ArrayList<CombatConsumable> cc = new ArrayList<>();
								cc.add(new CombatConsumable(s));
								_pConsumables.put(uuid, cc);
							}
							
						}
					}
					if(found)
						continue;				
					
					gear[count]= s;
					count++;
					
					
				}
				_dataCard.setPlayerGear(uuid, gear);
				
			}else
			{
				_dataCard.setPlayerGear(uuid, _dataCard.get_kit().get_kitInv());
			}
		}
		
	}
	
	
	
	
	
	

	
	
	
	
	
}
