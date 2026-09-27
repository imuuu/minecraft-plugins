package imu.iCards.Managers;

import org.bukkit.entity.Player;

import imu.iCards.Invs.Inv_CreateCard;
import imu.iCards.Other.Card;
import imu.iCards.Other.Category;

public class CardManager
{
	public static CardManager Instance;
	
	public CardManager()
	{
		Instance = this;
	}
	
	public void OpenCreateCardInv(Player player)
	{
		new Inv_CreateCard(player, new Card("No name", 3, 1, new Category("CAT"))).openThis();
	}
}
